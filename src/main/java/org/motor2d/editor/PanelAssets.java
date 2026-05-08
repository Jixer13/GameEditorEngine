package org.motor2d.editor;

import org.motor2d.editor.helpers.EditorController;
import org.motor2d.model.Tileset;
import org.motor2d.utilities.Color;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * Panel inferior del editor — Explorador de Assets y Paleta de Tiles.
 */
public class PanelAssets extends JPanel {

    private JPanel contenedorTarjetas;
    
    private JTree   arbolCarpetas;
    private JPopupMenu popupMenu;
    private DefaultMutableTreeNode nodoSeleccionado;
    
    private PanelPaleta panelPaleta;
    
    private File carpetaProyectos;
    private File rutaProyecto;
    private final PanelCanvas canvas;

    private EditorController controller;
    private PanelProperties panelProperties;
    private Editor editor;

    public PanelAssets(File rutaProyecto, File carpetaProyectos, PanelCanvas canvas) {
        this.rutaProyecto= rutaProyecto;
        this.carpetaProyectos= carpetaProyectos;
        this.canvas= canvas;
        setOpaque(false);
        setLayout(new BorderLayout());
        construirUI();
    }

    public void init(EditorController controller, PanelProperties panelProperties, Editor editor) {
        this.controller = controller;
        this.panelProperties = panelProperties;
        this.editor = editor;
        this.panelPaleta = new PanelPaleta(controller);
        panelPaleta.setCanvas(canvas);
        contenedorTarjetas.add(panelPaleta, "PALETA");
    }

    public void setRootDirectory(File newRoot) {
        this.carpetaProyectos = newRoot;
        refrescarArbol();
    }

    public void mostrarPaleta(Tileset ts) {
        panelPaleta.cargarTileset(ts);
        CardLayout cl = (CardLayout) contenedorTarjetas.getLayout();
        cl.show(contenedorTarjetas, "PALETA");
    }

    public void mostrarArchivos() {
        CardLayout cl = (CardLayout) contenedorTarjetas.getLayout();
        cl.show(contenedorTarjetas, "ARCHIVOS");
    }

    private void construirUI() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Color.PANEL_BACKGROUND);
        cabecera.setPreferredSize(new Dimension(0, 28));
        cabecera.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BORDER_COLOR));

        JLabel titulo = new JLabel("  Assets");
        titulo.setForeground(Color.TEXT_SECONDARY);
        titulo.setFont(new Font("Arial", Font.BOLD, 11));
        cabecera.add(titulo, BorderLayout.CENTER);
        
        JButton btnToggle = new JButton("Paleta / Assets");
        btnToggle.setBackground(Color.PANEL_BACKGROUND);
        btnToggle.setForeground(Color.TEXT_PRIMARY);
        btnToggle.setFont(new Font("Arial", Font.PLAIN, 10));
        btnToggle.setFocusPainted(false);
        btnToggle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        btnToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggle.addActionListener(e -> {
            CardLayout cl = (CardLayout) contenedorTarjetas.getLayout();
            cl.next(contenedorTarjetas);
        });
        
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        panelBoton.setOpaque(false);
        panelBoton.add(btnToggle);
        
        cabecera.add(panelBoton, BorderLayout.EAST);
        add(cabecera, BorderLayout.NORTH);

        contenedorTarjetas = new JPanel(new CardLayout());
        
        // ── Tarjeta: Archivos ──
        contenedorTarjetas.add(crearPanelArchivos(), "ARCHIVOS");
        
        add(contenedorTarjetas, BorderLayout.CENTER);
    }

    private JPanel crearPanelArchivos() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(Color.BACKGROUND);
        
        DefaultMutableTreeNode raizArbol = crearArbolCarpetas();
        arbolCarpetas = new JTree(raizArbol);
        arbolCarpetas.setBackground(Color.BACKGROUND);
        arbolCarpetas.setForeground(Color.TEXT_PRIMARY);
        arbolCarpetas.setFont(new Font("Arial", Font.PLAIN, 12));
        arbolCarpetas.setCellRenderer(new RendererCarpetas());
        arbolCarpetas.setRowHeight(24);
        arbolCarpetas.setToggleClickCount(1);
        arbolCarpetas.setRootVisible(true);
        arbolCarpetas.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BORDER_COLOR));

        crearPopupMenu();

        arbolCarpetas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int seleccion = arbolCarpetas.getRowForLocation(e.getX(), e.getY());
                if (seleccion == -1) return;

                nodoSeleccionado = (DefaultMutableTreeNode) arbolCarpetas.getPathForRow(seleccion).getLastPathComponent();

                if (e.getButton() == MouseEvent.BUTTON1) {
                    procesarNodoSeleccionado(nodoSeleccionado);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    arbolCarpetas.setSelectionRow(seleccion);
                    actualizarPopupMenu(nodoSeleccionado);
                    popupMenu.show(arbolCarpetas, e.getX(), e.getY());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(arbolCarpetas);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUI(new ScrollBarPersonalizado());
        
        contenedor.add(scroll, BorderLayout.CENTER);
        return contenedor;
    }

    private DefaultMutableTreeNode crearArbolCarpetas() {
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode(new NodoAsset(carpetaProyectos.getName(), carpetaProyectos));
        if (carpetaProyectos.exists() && carpetaProyectos.isDirectory()) {
            agregarArchivosRecursivos(raiz, carpetaProyectos);
        }
        return raiz;
    }

    private void agregarArchivosRecursivos(DefaultMutableTreeNode nodo, File carpeta) {
        File[] archivos = carpeta.listFiles();
        if (archivos == null) return;

        for (File archivo : archivos) {
            if (archivo.isDirectory() && !archivo.getName().startsWith(".") && !archivo.getName().equals("target")) {
                DefaultMutableTreeNode subNodo = new DefaultMutableTreeNode(new NodoAsset(archivo.getName(), archivo));
                agregarArchivosRecursivos(subNodo, archivo);
                if (subNodo.getChildCount() == 0) subNodo.add(new DefaultMutableTreeNode(""));
                nodo.add(subNodo);
            } else if (!archivo.isDirectory()) {
                nodo.add(new DefaultMutableTreeNode(new NodoAsset(archivo.getName(), archivo)));
            }
        }
    }

    private void procesarNodoSeleccionado(DefaultMutableTreeNode nodo) {
        Object userObj = nodo.getUserObject();
        if (!(userObj instanceof NodoAsset nodoAsset)) return;
        
        File archivo = nodoAsset.archivo;
        if (archivo.isDirectory()) {
            if (panelProperties != null) panelProperties.limpiar();
        } else {
            if (panelProperties != null && archivo.exists()) {
                panelProperties.mostrarAsset(archivo);
            }
            String nombre = nodoAsset.nombre.toLowerCase();
            if (canvas != null && archivo.exists() && (nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg"))) {
                canvas.mostrarImagen(archivo);
            }
        }
    }

    private File obtenerRutaArchivo(DefaultMutableTreeNode nodo) {
        Object userObj = nodo.getUserObject();
        if (userObj instanceof NodoAsset na) return na.archivo;
        return null;
    }

    private void crearPopupMenu() {
        popupMenu = new JPopupMenu();
        popupMenu.setBackground(Color.POPUP_BACKGROUND);
        popupMenu.setBorder(BorderFactory.createLineBorder(Color.BORDER_COLOR, 1));
    }

    private void actualizarPopupMenu(DefaultMutableTreeNode nodo) {
        popupMenu.removeAll();
        Object userObj = nodo.getUserObject();
        if (!(userObj instanceof NodoAsset na)) return;

        File f = na.archivo;
        if (f.isDirectory()) {
            popupMenu.add(crearItemMenu("+ Nueva Carpeta", e -> crearNuevaCarpetaDesdePopup()));
            popupMenu.add(crearItemMenu("Importar Archivo...", e -> importarArchivoDesdePopup(f)));
            popupMenu.addSeparator();
        } else {
            String nombre = f.getName().toLowerCase();
            if (nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) {
                popupMenu.add(crearItemMenu("Añadir a la Escena", e -> anadirImagenAEscena(f)));
                popupMenu.addSeparator();
            }
        }

        popupMenu.add(crearItemMenu("Refrescar", e -> refrescarArbol()));
        popupMenu.add(crearItemMenu("Abrir en Explorador", e -> abrirRutaDesdePopup()));
        popupMenu.addSeparator();
        popupMenu.add(crearItemMenuDanger("🗑 Eliminar", e -> eliminarCarpetaDesdePopup()));
    }

    private void anadirImagenAEscena(File archivo) {
        if (!controller.isProjectOpen() || !controller.isSceneOpen()) {
            JOptionPane.showMessageDialog(this, "Abre un proyecto y una escena primero.");
            return;
        }
        try {
            String projectPath = new File(controller.getProjectPath()).getAbsolutePath();
            String assetPath = archivo.getAbsolutePath();
            
            if (assetPath.startsWith(projectPath)) {
                String relativePath = assetPath.substring(projectPath.length());
                if (relativePath.startsWith(File.separator)) relativePath = relativePath.substring(1);
                
                String nombreEntidad = archivo.getName().split("\\.")[0];
                controller.createSpriteEntity(nombreEntidad, relativePath.replace("\\", "/"));
                
                if (editor != null) {
                    editor.refrescarHierarchy();
                    editor.mostrarMensajeEstado("Entidad añadida: " + nombreEntidad);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al añadir a la escena: " + e.getMessage());
        }
    }

    private JMenuItem crearItemMenu(String texto, ActionListener accion) {
        JMenuItem item = new JMenuItem(texto);
        item.setBackground(Color.POPUP_BACKGROUND);
        item.setForeground(Color.TEXT_PRIMARY);
        item.setFont(new Font("Arial", Font.PLAIN, 11));
        item.addActionListener(accion);
        return item;
    }

    private JMenuItem crearItemMenuDanger(String texto, ActionListener accion) {
        JMenuItem item = new JMenuItem(texto);
        item.setBackground(Color.POPUP_BACKGROUND);
        item.setForeground(Color.TEXT_DANGER);
        item.setFont(new Font("Arial", Font.PLAIN, 11));
        item.addActionListener(accion);
        return item;
    }

    private void importarArchivoDesdePopup(File destino) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
        chooser.setDialogTitle("Selecciona archivo para importar");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File origen = chooser.getSelectedFile();
            File nuevoArchivo = new File(destino, origen.getName());
            try {
                copiarArchivo(origen, nuevoArchivo);
                refrescarArbol();
                if (editor != null) editor.mostrarMensajeEstado("Archivo importado: " + origen.getName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al importar: " + e.getMessage());
            }
        }
    }

    private void copiarArchivo(File origen, File destino) throws java.io.IOException {
        try (java.io.InputStream in = new java.io.FileInputStream(origen);
             java.io.OutputStream out = new java.io.FileOutputStream(destino)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    private void crearNuevaCarpetaDesdePopup() {
        if (nodoSeleccionado == null) return;
        File destino = obtenerRutaArchivo(nodoSeleccionado);
        if (destino == null || !destino.isDirectory()) destino = destino.getParentFile();

        String nombre = JOptionPane.showInputDialog(this, "Nombre de la nueva carpeta:");
        if (nombre != null && !nombre.isEmpty()) {
            File nueva = new File(destino, nombre);
            if (nueva.mkdir()) refrescarArbol();
            else JOptionPane.showMessageDialog(this, "Error al crear la carpeta");
        }
    }

    public void refrescarArbol() {
        DefaultMutableTreeNode raiz = crearArbolCarpetas();
        arbolCarpetas.setModel(new DefaultTreeModel(raiz));
    }

    private void abrirRutaDesdePopup() {
        File f = obtenerRutaArchivo(nodoSeleccionado);
        if (f == null) return;
        try {
            if (f.isDirectory()) Desktop.getDesktop().open(f);
            else Desktop.getDesktop().open(f.getParentFile());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void eliminarCarpetaDesdePopup() {
        File f = obtenerRutaArchivo(nodoSeleccionado);
        if (f == null) return;
        
        int conf = JOptionPane.showConfirmDialog(this, "¿Eliminar \"" + f.getName() + "\"?");
        if (conf == JOptionPane.YES_OPTION) {
            if (eliminarDirectorio(f)) refrescarArbol();
        }
    }

    private boolean eliminarDirectorio(File dir) {
        if (dir.isDirectory()) {
            File[] hijos = dir.listFiles();
            if (hijos != null) for (File h : hijos) eliminarDirectorio(h);
        }
        return dir.delete();
    }

    private static class NodoAsset {
        String nombre;
        File archivo;
        NodoAsset(String nombre, File archivo) { this.nombre = nombre; this.archivo = archivo; }
        @Override public String toString() { return nombre; }
    }

    private class RendererCarpetas extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
            Object obj = nodo.getUserObject();
            if (obj instanceof NodoAsset na) setText(na.nombre);
            setBackground(selected ? Color.TREE_SELECTION : Color.BACKGROUND);
            setForeground(Color.TEXT_PRIMARY);
            setFont(new Font("Arial", Font.PLAIN, 12));
            setOpaque(true);
            return this;
        }
    }

    private static class ScrollBarPersonalizado extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = Color.SCROLLBAR_THUMB;
            this.trackColor = Color.BACKGROUND;
        }
    }
}
