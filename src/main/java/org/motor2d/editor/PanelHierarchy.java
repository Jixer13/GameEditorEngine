package org.motor2d.editor;

import org.motor2d.editor.helpers.EditorController;
import org.motor2d.editor.helpers.NodoJerarquia;
import org.motor2d.editor.helpers.UIFactory;
import org.motor2d.model.Entity;
import org.motor2d.model.Tileset;
import org.motor2d.utilities.Color;
import org.motor2d.model.ui.UIElement;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

/**
 * PanelHierarchy - Panel que muestra la jerarquía de escenas, entidades y elementos de UI.
 */
public class PanelHierarchy extends JPanel {

    private JTree arbolEntidades;
    private DefaultMutableTreeNode raizArbol;
    private DefaultTreeModel modeloArbol;

    private EditorController controller;
    private PanelProperties  panelProperties;
    private PanelCanvas      canvas;
    private Editor           editor;

    public PanelHierarchy() {
        setOpaque(false);
        setLayout(new BorderLayout());
        construirUI();
    }

    public void init(EditorController controller, PanelProperties panelProperties, PanelCanvas canvas, Editor editor) {
        this.controller      = controller;
        this.panelProperties = panelProperties;
        this.canvas          = canvas;
        this.editor          = editor;
    }

    public void refrescar() {
        raizArbol.removeAllChildren();

        if (controller != null && controller.isProjectOpen()) {
            // Grupo Escenas
            DefaultMutableTreeNode nodoScenes = new DefaultMutableTreeNode(
                new NodoJerarquia("Scenes", NodoJerarquia.Tipo.ESCENA, (Entity)null));
            try {
                List<String> escenas = controller.listScenes();
                for (String s : escenas) {
                    nodoScenes.add(new DefaultMutableTreeNode(
                            new NodoJerarquia(s, NodoJerarquia.Tipo.ESCENA, (Entity)null)));
                }
            } catch (Exception e) {}
            raizArbol.add(nodoScenes);

            if (controller.isSceneOpen()) {
                String nombreEscena = controller.getCurrentSceneName();
                raizArbol.setUserObject(new NodoJerarquia("Project: " + controller.getProjectManager().getCurrentProject().getName(), NodoJerarquia.Tipo.ESCENA, (Entity)null));

                // Grupo Entidades
                DefaultMutableTreeNode nodoEntities = new DefaultMutableTreeNode(
                    new NodoJerarquia("Entities (" + nombreEscena + ")", NodoJerarquia.Tipo.ESCENA, (Entity)null));
                List<Entity> entidades = controller.getAllEntities();
                for (Entity e : entidades) {
                    nodoEntities.add(new DefaultMutableTreeNode(
                            new NodoJerarquia(e.getName(), NodoJerarquia.Tipo.ENTIDAD, e)));
                }
                raizArbol.add(nodoEntities);

                // Grupo UI Elements
                DefaultMutableTreeNode nodoUI = new DefaultMutableTreeNode(
                    new NodoJerarquia("UI Elements", NodoJerarquia.Tipo.ESCENA, (Entity)null));
                List<UIElement> uiElements = controller.getAllUIElements();
                for (UIElement ui : uiElements) {
                    nodoUI.add(new DefaultMutableTreeNode(
                            new NodoJerarquia(ui.getName(), NodoJerarquia.Tipo.UI_ELEMENT, ui)));
                }
                raizArbol.add(nodoUI);
                
                // Grupo Tilesets
                DefaultMutableTreeNode nodoTiles = new DefaultMutableTreeNode(
                    new NodoJerarquia("Tilesets", NodoJerarquia.Tipo.ESCENA, (Entity)null));
                try {
                    for (Tileset ts : controller.getProjectManager().getCurrentProject().getTilesets()) {
                        nodoTiles.add(new DefaultMutableTreeNode(
                                new NodoJerarquia(ts.getName(), NodoJerarquia.Tipo.TILESET, ts)));
                    }
                } catch (Exception e) {}
                raizArbol.add(nodoTiles);
            }
        } else {
            raizArbol.setUserObject(new NodoJerarquia("No Project", NodoJerarquia.Tipo.ESCENA, (Entity)null));
        }

        modeloArbol.reload();
        for (int i = 0; i < arbolEntidades.getRowCount(); i++) {
            arbolEntidades.expandRow(i);
        }
    }

    private void construirUI() {
        // ── Cabecera ──
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Color.PANEL_BACKGROUND);
        cabecera.setPreferredSize(new Dimension(0, 28));
        cabecera.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BORDER_COLOR));

        JLabel titulo = new JLabel("  Hierarchy");
        titulo.setForeground(Color.TEXT_SECONDARY);
        titulo.setFont(new Font("Arial", Font.BOLD, 11));
        cabecera.add(titulo, BorderLayout.CENTER);

        JButton btnAdd = UIFactory.createIconButton("+ ");
        btnAdd.addActionListener(e -> mostrarMenuAnadirRapido(btnAdd));
        cabecera.add(btnAdd, BorderLayout.EAST);

        add(cabecera, BorderLayout.NORTH);

        // ── Árbol ──
        raizArbol   = new DefaultMutableTreeNode(new NodoJerarquia("Project", NodoJerarquia.Tipo.ESCENA, (Entity)null));
        modeloArbol = new DefaultTreeModel(raizArbol);
        arbolEntidades = new JTree(modeloArbol);
        arbolEntidades.setBackground(Color.BACKGROUND);
        arbolEntidades.setCellRenderer(new RendererEntidades());
        arbolEntidades.setRowHeight(24);
        arbolEntidades.setToggleClickCount(1);
        arbolEntidades.setRootVisible(true);
        arbolEntidades.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        // Selección
        arbolEntidades.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode)
                    arbolEntidades.getLastSelectedPathComponent();
            if (nodo == null) return;
            Object obj = nodo.getUserObject();
            if (obj instanceof NodoJerarquia nj) {
                if (canvas != null) canvas.limpiar();
                
                if (nj.tipo == NodoJerarquia.Tipo.ENTIDAD) {
                    controller.setSelectedEntity(nj.entidad);
                    if (panelProperties != null) panelProperties.mostrarEntidad(nj.entidad, controller);
                } else if (nj.tipo == NodoJerarquia.Tipo.UI_ELEMENT) {
                    controller.setSelectedEntity(null);
                    if (panelProperties != null) panelProperties.mostrarUIElement(nj.uiElement, controller);
                } else if (nj.tipo == NodoJerarquia.Tipo.TILESET) {
                    if (editor != null) editor.getPanelAssets().mostrarPaleta(nj.tileset);
                } else if (nj.tipo == NodoJerarquia.Tipo.ESCENA && nj.entidad == null) {
                    // Si es un nodo de escena (y no es una carpeta raíz)
                    if (nodo.getParent() != null && ((DefaultMutableTreeNode)nodo.getParent()).getUserObject() instanceof NodoJerarquia parentNj) {
                        if (parentNj.nombre.equals("Scenes")) {
                            accionCargarEscena(nj.nombre);
                        }
                    }
                    controller.setSelectedEntity(null);
                    if (panelProperties != null) panelProperties.limpiar();
                } else {
                    controller.setSelectedEntity(null);
                    if (panelProperties != null) panelProperties.limpiar();
                }
            }
        });

        // Clic derecho
        arbolEntidades.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int fila = arbolEntidades.getRowForLocation(e.getX(), e.getY());
                    if (fila != -1) arbolEntidades.setSelectionRow(fila);
                    mostrarMenuContextual(e.getX(), e.getY());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(arbolEntidades);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUI(new ScrollBarPersonalizado());
        add(scroll, BorderLayout.CENTER);
    }

    private void mostrarMenuAnadirRapido(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(crearItem("Nueva Escena", e -> accionCrearEscena()));
        menu.addSeparator();
        menu.add(crearItem("Entidad vacía", e -> accionCrearEntidad()));
        menu.add(crearItem("Sprite Entity", e -> accionCrearSpriteEntity()));
        menu.addSeparator();
        menu.add(crearItem("UI Label", e -> accionCrearUILabel()));
        menu.add(crearItem("UI Button", e -> accionCrearUIButton()));
        menu.add(crearItem("UI Image", e -> accionCrearUIImage()));
        menu.addSeparator();
        menu.add(crearItem("Nuevo Tileset", e -> accionCrearTileset()));
        menu.show(invoker, 0, invoker.getHeight());
    }

    private void mostrarMenuContextual(int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        DefaultMutableTreeNode nodoSel = (DefaultMutableTreeNode) arbolEntidades.getLastSelectedPathComponent();
        boolean enRaiz = (nodoSel == null || nodoSel == raizArbol);

        JMenu menuAdd = new JMenu("Añadir...");
        menuAdd.add(crearItem("Nueva Escena", e -> accionCrearEscena()));
        menuAdd.addSeparator();
        menuAdd.add(crearItem("Entidad vacía", e -> accionCrearEntidad()));
        menuAdd.add(crearItem("Sprite Entity", e -> accionCrearSpriteEntity()));
        menuAdd.addSeparator();
        menuAdd.add(crearItem("UI Label", e -> accionCrearUILabel()));
        menuAdd.add(crearItem("UI Button", e -> accionCrearUIButton()));
        menuAdd.add(crearItem("UI Image", e -> accionCrearUIImage()));
        menuAdd.addSeparator();
        menuAdd.add(crearItem("Nuevo Tileset", e -> accionCrearTileset()));
        popup.add(menuAdd);
        
        if (!enRaiz && nodoSel.getUserObject() instanceof NodoJerarquia nj) {
            popup.addSeparator();
            if (nj.tipo == NodoJerarquia.Tipo.ENTIDAD) {
                popup.add(crearItem("Eliminar Entidad", e -> accionEliminarEntidad(nj.entidad)));
            } else if (nj.tipo == NodoJerarquia.Tipo.UI_ELEMENT) {
                popup.add(crearItem("Eliminar UI Element", e -> accionEliminarUIElement(nj.uiElement)));
            } else if (nj.tipo == NodoJerarquia.Tipo.TILESET) {
                popup.add(crearItem("Eliminar Tileset", e -> accionEliminarTileset(nj.tileset)));
            } else if (nj.tipo == NodoJerarquia.Tipo.ESCENA && nj.entidad == null) {
                if (nodoSel.getParent() != null && ((DefaultMutableTreeNode)nodoSel.getParent()).getUserObject() instanceof NodoJerarquia parentNj) {
                    if (parentNj.nombre.equals("Scenes")) {
                        popup.add(crearItem("Cargar Escena", e -> accionCargarEscena(nj.nombre)));
                    }
                }
            }
        }
        popup.show(arbolEntidades, x, y);
    }

    private void accionCrearEscena() {
        if (controller == null || !controller.isProjectOpen()) {
            JOptionPane.showMessageDialog(this, "Abre un proyecto primero.");
            return;
        }
        String n = JOptionPane.showInputDialog("Nombre de la nueva escena:");
        if (n != null && !n.isBlank()) {
            if (controller.createScene(n.trim())) {
                refrescar();
                if (editor != null) editor.mostrarMensajeEstado("Escena creada: " + n);
            }
        }
    }

    private void accionCargarEscena(String nombre) {
        if (controller.loadScene(nombre)) {
            refrescar();
            if (editor != null) {
                editor.mostrarMensajeEstado("Escena cargada: " + nombre);
                editor.getPanelCanvas().resetVista();
            }
        }
    }

    private void accionCrearTileset() {
        if (!comprobarEscenaAbierta()) return;
        
        JTextField nombre = new JTextField("Tileset1");
        JTextField ancho = new JTextField("32");
        JTextField alto = new JTextField("32");
        
        Object[] message = {
            "Nombre:", nombre,
            "Ancho Tile:", ancho,
            "Alto Tile:", alto
        };

        if (JOptionPane.showConfirmDialog(null, message, "Crear nuevo Tileset", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            JFileChooser fc = new JFileChooser(controller.getProjectPath());
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    String relPath = fc.getSelectedFile().getAbsolutePath().substring(new File(controller.getProjectPath()).getAbsolutePath().length() + 1);
                    controller.getTilesetManager().createTileset(nombre.getText(), relPath.replace("\\", "/"), 
                                                               Integer.parseInt(ancho.getText()), 
                                                               Integer.parseInt(alto.getText()));
                    refrescar();
                } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
            }
        }
    }

    private void accionEliminarTileset(Tileset ts) {
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar \"" + ts.getName() + "\"?") == JOptionPane.YES_OPTION) {
            try {
                controller.getTilesetManager().deleteTileset(ts.getName());
                refrescar();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
        }
    }

    private JMenuItem crearItem(String texto, ActionListener al) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(al);
        return item;
    }

    private void accionCrearEntidad() {
        if (!comprobarEscenaAbierta()) return;
        String n = JOptionPane.showInputDialog("Nombre:");
        if (n != null && !n.isBlank() && controller.createEntity(n.trim()) != null) refrescar();
    }

    private void accionCrearSpriteEntity() {
        if (!comprobarEscenaAbierta()) return;
        String n = JOptionPane.showInputDialog("Nombre:");
        if (n == null || n.isBlank()) return;
        
        List<String> sprites = controller.listSprites();
        String sel = (String) JOptionPane.showInputDialog(this, "Sprite:", "Selección", 
                                                        JOptionPane.PLAIN_MESSAGE, null, 
                                                        sprites.toArray(), sprites.isEmpty() ? null : sprites.get(0));
        if (controller.createSpriteEntity(n.trim(), sel != null ? sel : "") != null) refrescar();
    }

    private void accionCrearUILabel() {
        if (!comprobarEscenaAbierta()) return;
        String n = JOptionPane.showInputDialog("Nombre Label:");
        if (n != null && !n.isBlank() && controller.createUILabel(n.trim(), "Texto") != null) refrescar();
    }

    private void accionCrearUIButton() {
        if (!comprobarEscenaAbierta()) return;
        String n = JOptionPane.showInputDialog("Nombre Botón:");
        if (n != null && !n.isBlank() && controller.createUIButton(n.trim(), "Click") != null) refrescar();
    }

    private void accionCrearUIImage() {
        if (!comprobarEscenaAbierta()) return;
        String n = JOptionPane.showInputDialog("Nombre Imagen UI:");
        if (n == null || n.isBlank()) return;
        
        List<String> sprites = controller.listSprites();
        String sel = (String) JOptionPane.showInputDialog(this, "Sprite UI:", "Selección", 
                                                        JOptionPane.PLAIN_MESSAGE, null, 
                                                        sprites.toArray(), sprites.isEmpty() ? null : sprites.get(0));
        if (controller.createUIImage(n.trim(), sel != null ? sel : "") != null) refrescar();
    }

    private void accionEliminarEntidad(Entity e) {
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar " + e.getName() + "?") == JOptionPane.YES_OPTION) {
            if (controller.removeEntity(e)) {
                refrescar();
                panelProperties.limpiar();
            }
        }
    }

    private void accionEliminarUIElement(UIElement ui) {
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar " + ui.getName() + "?") == JOptionPane.YES_OPTION) {
            if (controller.removeUIElement(ui)) {
                refrescar();
                panelProperties.limpiar();
            }
        }
    }

    private boolean comprobarEscenaAbierta() {
        if (controller == null || !controller.isSceneOpen()) {
            JOptionPane.showMessageDialog(this, "No hay escena abierta.");
            return false;
        }
        return true;
    }

    // ==================== SELECCIÓN DESDE FUERA ====================
    public void seleccionarEntidad(Entity entity) {
        if (entity == null) {
            arbolEntidades.clearSelection();
            return;
        }
        DefaultMutableTreeNode nodo = buscarNodoEntidad(raizArbol, entity);
        if (nodo != null) {
            TreePath path = new TreePath(nodo.getPath());
            arbolEntidades.setSelectionPath(path);
            arbolEntidades.scrollPathToVisible(path);
        }
    }

    private DefaultMutableTreeNode buscarNodoEntidad(DefaultMutableTreeNode raiz, Entity entity) {
        for (int i = 0; i < raiz.getChildCount(); i++) {
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) raiz.getChildAt(i);
            Object obj = nodo.getUserObject();
            if (obj instanceof NodoJerarquia nj && nj.entidad == entity) {
                return nodo;
            }
            DefaultMutableTreeNode encontrado = buscarNodoEntidad(nodo, entity);
            if (encontrado != null) return encontrado;
        }
        return null;
    }

    private class RendererEntidades extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            setBackground(selected ? Color.TREE_SELECTION : Color.BACKGROUND);
            setForeground(Color.TEXT_PRIMARY);
            setOpaque(true);
            return this;
        }
    }

    private static class ScrollBarPersonalizado extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            this.thumbColor = Color.SCROLLBAR_THUMB;
            this.trackColor = Color.BACKGROUND;
        }
    }
}
