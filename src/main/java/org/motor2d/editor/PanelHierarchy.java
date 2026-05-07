package org.motor2d.editor;

import org.motor2d.model.Entity;
import org.motor2d.model.components.SpriteRenderer;
import org.motor2d.utilities.Color;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

/**
 * Panel izquierdo del editor.
 * Muestra la jerarquía de escenas y entidades.
 * Clic derecho → menú contextual para añadir / eliminar.
 * Al seleccionar una entidad notifica a PanelProperties.
 */
public class PanelHierarchy extends JPanel {

    // ==================== ATRIBUTOS ====================
    private JTree arbolEntidades;
    private DefaultMutableTreeNode raizArbol;
    private DefaultTreeModel modeloArbol;

    private EditorController controller;
    private PanelProperties  panelProperties;

    // ==================== CONSTRUCTOR ====================
    public PanelHierarchy() {
        setOpaque(false);
        setLayout(new BorderLayout());
        construirUI();
    }

    // ==================== INYECCIÓN DE DEPENDENCIAS ====================
    /** Llamar desde Editor tras construir todos los paneles */
    public void init(EditorController controller, PanelProperties panelProperties) {
        this.controller      = controller;
        this.panelProperties = panelProperties;
    }

    // ==================== ACTUALIZAR ÁRBOL ====================
    /** Reconstruye el árbol con la escena/entidades actuales */
    public void refrescar() {
        raizArbol.removeAllChildren();

        if (controller != null && controller.isSceneOpen()) {
            String nombreEscena = controller.getCurrentSceneName();
            raizArbol.setUserObject(new NodoJerarquia(nombreEscena, NodoJerarquia.Tipo.ESCENA, null));

            List<Entity> entidades = controller.getAllEntities();
            for (Entity e : entidades) {
                raizArbol.add(new DefaultMutableTreeNode(
                        new NodoJerarquia(e.getName(), NodoJerarquia.Tipo.ENTIDAD, e)));
            }
        } else {
            raizArbol.setUserObject(new NodoJerarquia("Scene", NodoJerarquia.Tipo.ESCENA, null));
        }

        modeloArbol.reload();
        arbolEntidades.expandRow(0);
    }

    private boolean tieneSprite(Entity e) {
        return e.getComponents().stream().anyMatch(c -> c instanceof SpriteRenderer);
    }

    // ==================== CONSTRUCCIÓN UI ====================
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

        // Botón rápido de añadir
        JButton btnAdd = new JButton("+ ");
        btnAdd.setFocusPainted(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setContentAreaFilled(false);
        btnAdd.setForeground(Color.TEXT_PRIMARY);
        btnAdd.setFont(new Font("Arial", Font.BOLD, 16));
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.setToolTipText("Añadir Entidad");
        btnAdd.addActionListener(e -> mostrarMenuAñadirRapido(btnAdd));
        cabecera.add(btnAdd, BorderLayout.EAST);

        add(cabecera, BorderLayout.NORTH);

        // ── Árbol ──
        raizArbol   = new DefaultMutableTreeNode(new NodoJerarquia("Scene", NodoJerarquia.Tipo.ESCENA, null));
        modeloArbol = new DefaultTreeModel(raizArbol);
        arbolEntidades = new JTree(modeloArbol);
        arbolEntidades.setBackground(Color.BACKGROUND);
        arbolEntidades.setForeground(Color.TEXT_PRIMARY);
        arbolEntidades.setFont(new Font("Arial", Font.PLAIN, 12));
        arbolEntidades.setCellRenderer(new RendererEntidades());
        arbolEntidades.setRowHeight(24);
        arbolEntidades.setToggleClickCount(1);
        arbolEntidades.setRootVisible(true);
        arbolEntidades.setShowsRootHandles(true);
        arbolEntidades.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        // Selección → notificar propiedades y controlador
        arbolEntidades.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode)
                    arbolEntidades.getLastSelectedPathComponent();
            if (nodo == null) return;
            Object obj = nodo.getUserObject();
            if (obj instanceof NodoJerarquia nodoJer && nodoJer.tipo == NodoJerarquia.Tipo.ENTIDAD) {
                // Notificar al controlador si la selección viene del árbol
                if (controller.getSelectedEntity() != nodoJer.entidad) {
                    controller.setSelectedEntity(nodoJer.entidad);
                }
                if (panelProperties != null)
                    panelProperties.mostrarEntidad(nodoJer.entidad, controller);
            } else {
                if (controller.getSelectedEntity() != null) {
                    controller.setSelectedEntity(null);
                }
                if (panelProperties != null) panelProperties.limpiar();
            }
        });

        // Clic derecho → menú contextual
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
        scroll.setBackground(Color.BACKGROUND);
        scroll.getViewport().setBackground(Color.BACKGROUND);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUI(new ScrollBarPersonalizado());
        scroll.getHorizontalScrollBar().setUI(new ScrollBarPersonalizado());
        add(scroll, BorderLayout.CENTER);
    }

    private void mostrarMenuAñadirRapido(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Color.POPUP_BACKGROUND);
        menu.setBorder(BorderFactory.createLineBorder(Color.BORDER_COLOR, 1));
        menu.add(crearItem("Crear Entidad vacía", false, e -> accionCrearEntidad()));
        menu.add(crearItem("Crear Sprite Entity", false, e -> accionCrearSpriteEntity()));
        menu.show(invoker, 0, invoker.getHeight());
    }

    // ==================== MENÚ CONTEXTUAL ====================
    private void mostrarMenuContextual(int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Color.POPUP_BACKGROUND);
        popup.setBorder(BorderFactory.createLineBorder(Color.BORDER_COLOR, 1));

        DefaultMutableTreeNode nodoSel = (DefaultMutableTreeNode)
                arbolEntidades.getLastSelectedPathComponent();
        boolean enRaiz = (nodoSel == null || nodoSel == raizArbol);

        // ── PROYECTO ──
        popup.add(crearItem("Abrir Proyecto",  false, e -> accionAbrirProyecto()));
        popup.add(crearItem("Guardar Proyecto", false, e -> accionGuardarProyecto()));
        popup.addSeparator();

        // ── ESCENA ──
        popup.add(crearItem("Nueva Escena",  false, e -> accionNuevaEscena()));
        popup.add(crearItem("Cargar Escena", false, e -> accionCargarEscena()));
        popup.addSeparator();

        // ── ENTIDAD ──
        popup.add(crearItem("Crear Entidad vacía",  false, e -> accionCrearEntidad()));
        popup.add(crearItem("Crear Sprite Entity",  false, e -> accionCrearSpriteEntity()));

        if (!enRaiz && nodoSel.getUserObject() instanceof NodoJerarquia nodoJer && nodoJer.tipo == NodoJerarquia.Tipo.ENTIDAD) {
            popup.addSeparator();
            popup.add(crearItem("Renombrar", false, e -> accionRenombrar(nodoJer.entidad)));
            popup.add(crearItem("Duplicar", false, e -> accionDuplicar(nodoJer.entidad)));
            popup.addSeparator();
            popup.add(crearItem("Eliminar Entidad", true,
                    e -> accionEliminarEntidad(nodoJer.entidad)));
        }

        popup.show(arbolEntidades, x, y);
    }

    private JMenuItem crearItem(String texto, boolean danger, ActionListener al) {
        JMenuItem item = new JMenuItem(texto);
        item.setBackground(Color.POPUP_BACKGROUND);
        item.setForeground(danger ? Color.TEXT_DANGER : Color.TEXT_PRIMARY);
        item.setFont(new Font("Arial", Font.PLAIN, 11));
        item.addActionListener(al);
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                item.setBackground(danger ? Color.DANGER_HOVER : Color.POPUP_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                item.setBackground(Color.POPUP_BACKGROUND);
            }
        });
        return item;
    }

    // ==================== ACCIONES: PROYECTO ====================
    private void accionAbrirProyecto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecciona la carpeta del proyecto");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        File carpetaProyectos = new File(System.getProperty("user.dir"), "Proyectos");
        if (carpetaProyectos.exists()) chooser.setCurrentDirectory(carpetaProyectos);

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String ruta = chooser.getSelectedFile().getAbsolutePath();

        if (!new File(ruta, "project.json").exists()) {
            JOptionPane.showMessageDialog(this,
                    "La carpeta seleccionada no contiene un proyecto válido.\n" +
                    "Asegúrate de que exista un archivo project.json.",
                    "Proyecto inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (controller.openProject(ruta)) {
            refrescar();
            if (panelProperties != null) panelProperties.limpiar();
        }
    }

    private void accionGuardarProyecto() {
        if (controller == null || !controller.isProjectOpen()) {
            JOptionPane.showMessageDialog(this, "No hay proyecto abierto.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (controller.saveProject()) {
            JOptionPane.showMessageDialog(this, "Proyecto guardado correctamente.",
                    "Guardado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ==================== ACCIONES: ESCENA ====================
    private void accionNuevaEscena() {
        if (controller == null || !controller.isProjectOpen()) {
            JOptionPane.showMessageDialog(this,
                    "Primero debes tener un proyecto abierto.",
                    "Sin proyecto", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nombre = JOptionPane.showInputDialog(this,
                "Nombre de la nueva escena:", "Nueva Escena", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null || nombre.isBlank()) return;

        if (controller.createScene(nombre.trim())) refrescar();
    }

    private void accionCargarEscena() {
        if (controller == null || !controller.isProjectOpen()) {
            JOptionPane.showMessageDialog(this, "No hay proyecto abierto.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<String> escenas = controller.listScenes();
        if (escenas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay escenas disponibles.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String sel = (String) JOptionPane.showInputDialog(this,
                "Selecciona la escena:", "Cargar Escena",
                JOptionPane.PLAIN_MESSAGE, null,
                escenas.toArray(), escenas.get(0));
        if (sel == null) return;

        if (controller.loadScene(sel)) {
            refrescar();
            if (panelProperties != null) panelProperties.limpiar();
        }
    }

    // ==================== ACCIONES: ENTIDAD ====================
    private void accionCrearEntidad() {
        if (!comprobarEscenaAbierta()) return;
        String nombre = JOptionPane.showInputDialog(this,
                "Nombre de la entidad:", "Nueva Entidad", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null || nombre.isBlank()) return;

        Entity e = controller.createEntity(nombre.trim());
        if (e != null) refrescar();
    }

    private void accionCrearSpriteEntity() {
        if (!comprobarEscenaAbierta()) return;
        String nombre = JOptionPane.showInputDialog(this,
                "Nombre de la entidad:", "Nueva Sprite Entity", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null || nombre.isBlank()) return;

        String ruta = "";
        List<String> sprites = controller.listSprites();
        if (!sprites.isEmpty()) {
            String sel = (String) JOptionPane.showInputDialog(this,
                    "Selecciona un sprite (o cancela para dejar vacío):",
                    "Sprite", JOptionPane.PLAIN_MESSAGE,
                    null, sprites.toArray(), sprites.get(0));
            if (sel != null) ruta = sel;
        }

        Entity e = controller.createSpriteEntity(nombre.trim(), ruta);
        if (e != null) refrescar();
    }

    private void accionRenombrar(Entity entidad) {
        String nuevo = JOptionPane.showInputDialog(this,
                "Nuevo nombre:", entidad.getName(), JOptionPane.PLAIN_MESSAGE);
        if (nuevo == null || nuevo.isBlank()) return;
        entidad.setName(nuevo.trim());
        controller.saveProject();
        refrescar();
        if (panelProperties != null) panelProperties.mostrarEntidad(entidad, controller);
    }

    private void accionDuplicar(Entity entidad) {
        Entity copia = controller.duplicateEntity(entidad);
        if (copia != null) refrescar();
    }

    private void accionEliminarEntidad(Entity entidad) {
        int conf = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la entidad \"" + entidad.getName() + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (conf != JOptionPane.YES_OPTION) return;

        if (controller.removeEntity(entidad)) {
            refrescar();
            if (panelProperties != null) panelProperties.limpiar();
        }
    }

    private boolean comprobarEscenaAbierta() {
        if (controller == null || !controller.isSceneOpen()) {
            JOptionPane.showMessageDialog(this,
                    "Primero carga o crea una escena.",
                    "Sin escena", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

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
        }
        return null;
    }

    // ==================== NODO WRAPPER ====================
    private static class NodoJerarquia {
        enum Tipo { ESCENA, ENTIDAD }
        String nombre;
        Tipo tipo;
        Entity entidad;

        NodoJerarquia(String nombre, Tipo tipo, Entity entidad) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.entidad = entidad;
        }

        @Override public String toString() { return nombre; }
    }

    // ==================== RENDERER ====================
    private class RendererEntidades extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected,
                    expanded, leaf, row, hasFocus);
            
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
            Object obj = nodo.getUserObject();
            
            if (obj instanceof NodoJerarquia nj) {
                if (nj.tipo == NodoJerarquia.Tipo.ESCENA) {
                    setText(nj.nombre);
                } else {
                    setText(nj.nombre);
                }
            }

            setBackground(selected ? Color.TREE_SELECTION : Color.BACKGROUND);
            setForeground(Color.TEXT_PRIMARY);
            setFont(new Font("Arial", Font.PLAIN, 12));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 0));
            return this;
        }
    }

    // ==================== SCROLLBAR ====================
    private static class ScrollBarPersonalizado
            extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = Color.SCROLLBAR_THUMB;
            this.trackColor = Color.BACKGROUND;
        }
    }
}
