package org.motor2d.editor;

import org.motor2d.utilities.Color;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Panel inferior del editor — Explorador de Assets.
 * <p>
 * Muestra el árbol de carpetas/archivos del proyecto.
 * Al hacer clic en una imagen (.png / .jpg / .jpeg / .gif / .bmp / .webp)
 * la envía al {@link PanelCanvas} para previsualizarla.
 */
public class PanelAssets extends JPanel {

    // ==================== ATRIBUTOS ====================
    private JTree   arbolCarpetas;
    private JLabel  labelInfo;
    private JPopupMenu popupMenu;

    private DefaultMutableTreeNode nodoSeleccionado;
    private File carpetaActual;

    private File   carpetaProyectos;
    private File   rutaProyecto;
    private final PanelCanvas canvas;         // referencia al visor central

    private EditorController controller;
    private PanelProperties panelProperties;

    // ==================== CONSTRUCTOR ====================
    public PanelAssets(File rutaProyecto, File carpetaProyectos,
                       PanelCanvas canvas) {
        this.rutaProyecto     = rutaProyecto;
        this.carpetaProyectos = carpetaProyectos;
        this.canvas           = canvas;
        setOpaque(false);
        setLayout(new BorderLayout());
        construirUI();
    }

    /** Llamar desde Editor tras construir todos los paneles */
    public void init(EditorController controller, PanelProperties panelProperties) {
        this.controller      = controller;
        this.panelProperties = panelProperties;
    }

    /**
     * Cambia la carpeta raíz que muestra el explorador.
     * Útil cuando se abre o crea un nuevo proyecto.
     */
    public void setRootDirectory(File newRoot) {
        this.carpetaProyectos = newRoot;
        refrescarArbol();
        labelInfo.setText("Proyecto: " + newRoot.getName());
    }

    // ==================== CONSTRUCCIÓN UI ====================
    private void construirUI() {
        // Cabecera
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Color.PANEL_BACKGROUND);
        cabecera.setPreferredSize(new Dimension(0, 28));
        cabecera.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                Color.BORDER_COLOR));

        JLabel titulo = new JLabel("  Assets");
        titulo.setForeground(Color.TEXT_SECONDARY);
        titulo.setFont(new Font("Arial", Font.BOLD, 11));
        cabecera.add(titulo, BorderLayout.CENTER);
        add(cabecera, BorderLayout.NORTH);

        // Árbol
        DefaultMutableTreeNode raizArbol = crearArbolCarpetas();
        arbolCarpetas = new JTree(raizArbol);
        arbolCarpetas.setBackground(Color.BACKGROUND);
        arbolCarpetas.setForeground(Color.TEXT_PRIMARY);
        arbolCarpetas.setFont(new Font("Arial", Font.PLAIN, 12));
        arbolCarpetas.setCellRenderer(new RendererCarpetas());
        arbolCarpetas.setRowHeight(24);
        arbolCarpetas.setToggleClickCount(1);
        arbolCarpetas.setRootVisible(true);
        arbolCarpetas.setShowsRootHandles(true);
        arbolCarpetas.setBorder(BorderFactory.createMatteBorder(
                1, 1, 0, 0, Color.BORDER_COLOR));

        crearPopupMenu();

        arbolCarpetas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int seleccion = arbolCarpetas.getRowForLocation(
                        e.getX(), e.getY());
                if (seleccion == -1) return;

                nodoSeleccionado = (DefaultMutableTreeNode)
                        arbolCarpetas.getPathForRow(seleccion)
                                     .getLastPathComponent();

                if (e.getButton() == MouseEvent.BUTTON1) {
                    procesarNodoSeleccionado(nodoSeleccionado);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    arbolCarpetas.setSelectionRow(seleccion);
                    popupMenu.show(arbolCarpetas, e.getX(), e.getY());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(arbolCarpetas);
        scrollPane.setBackground(Color.BACKGROUND);
        scrollPane.getViewport().setBackground(Color.BACKGROUND);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                Color.BORDER_COLOR, 1));
        scrollPane.getVerticalScrollBar().setUI(new ScrollBarPersonalizado());
        scrollPane.getHorizontalScrollBar().setUI(new ScrollBarPersonalizado());
        add(scrollPane, BorderLayout.CENTER);

        // Barra de info inferior
        JPanel panelInfo = new JPanel(new BorderLayout());
        panelInfo.setBackground(Color.PANEL_BACKGROUND);
        panelInfo.setPreferredSize(new Dimension(0, 30));
        panelInfo.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, Color.BORDER_COLOR));

        labelInfo = new JLabel("Proyecto: " + rutaProyecto.getName()
                + " | Click derecho para opciones");
        labelInfo.setForeground(Color.TEXT_SECONDARY);
        labelInfo.setFont(new Font("Arial", Font.PLAIN, 10));
        labelInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelInfo.add(labelInfo, BorderLayout.CENTER);
        add(panelInfo, BorderLayout.SOUTH);
    }

    // ==================== ÁRBOL ====================
    private DefaultMutableTreeNode crearArbolCarpetas() {
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("📁 Proyectos");
        if (carpetaProyectos.exists() && carpetaProyectos.isDirectory()) {
            agregarArchivosRecursivos(raiz, carpetaProyectos);
        }
        return raiz;
    }

    private void agregarArchivosRecursivos(DefaultMutableTreeNode nodo,
                                            File carpeta) {
        File[] archivos = carpeta.listFiles();
        if (archivos == null) return;

        for (File archivo : archivos) {
            if (archivo.isDirectory()
                    && !archivo.getName().startsWith(".")
                    && !archivo.getName().equals("target")) {
                DefaultMutableTreeNode subNodo = new DefaultMutableTreeNode(
                        "📁 " + archivo.getName());
                agregarArchivosRecursivos(subNodo, archivo);
                if (subNodo.getChildCount() == 0)
                    subNodo.add(new DefaultMutableTreeNode(""));
                nodo.add(subNodo);
            } else if (!archivo.isDirectory()) {
                String icono = obtenerIconoArchivo(archivo.getName());
                nodo.add(new DefaultMutableTreeNode(
                        icono + " " + archivo.getName()));
            }
        }
    }

    private String obtenerIconoArchivo(String nombre) {
        String n = nombre.toLowerCase();
        if (n.endsWith(".java"))                              return "☕";
        if (n.endsWith(".png")  || n.endsWith(".jpg")
         || n.endsWith(".jpeg") || n.endsWith(".gif")
         || n.endsWith(".bmp")  || n.endsWith(".webp"))      return "🖼";
        if (n.endsWith(".txt"))                              return "📄";
        if (n.endsWith(".json"))                             return "⚙";
        if (n.endsWith(".xml"))                              return "🏷";
        if (n.endsWith(".wav")  || n.endsWith(".mp3")
         || n.endsWith(".ogg")  || n.endsWith(".mp4"))       return "🔊";
        return "📋";
    }

    // ==================== SELECCIÓN DE NODO ====================
    private void procesarNodoSeleccionado(DefaultMutableTreeNode nodo) {
        String nombreNodo = nodo.getUserObject().toString();
        String nombre     = nombreNodo.replaceAll("[^\\w\\-. ]", "").trim();
        File archivo      = obtenerRutaArchivo(nodo);

        if (nombreNodo.contains("📁") || nombreNodo.contains("🎮")) {
            carpetaActual = archivo;
            labelInfo.setText("📁 Carpeta: " + nombre);
            if (panelProperties != null) panelProperties.limpiar();

        } else {
            labelInfo.setText((nombreNodo.contains("🖼") ? "🖼 Imagen: " : "📄 Archivo: ") + nombre);
            
            // Notificar a propiedades
            if (panelProperties != null && archivo.exists()) {
                panelProperties.mostrarAsset(archivo);
            }

            // Si es imagen, previsualizar en canvas
            if (nombreNodo.contains("🖼") && canvas != null && archivo.exists()) {
                canvas.mostrarImagen(archivo);
            }
        }
    }

    private File obtenerRutaArchivo(DefaultMutableTreeNode nodo) {
        StringBuilder ruta = new StringBuilder();
        Object[] rutaNodos = nodo.getPath();
        for (int i = 1; i < rutaNodos.length; i++) {
            String parte = rutaNodos[i].toString()
                    .replaceAll("[^\\w\\-. ]", "").trim();
            ruta.append(parte);
            if (i < rutaNodos.length - 1) ruta.append(File.separator);
        }
        return new File(carpetaProyectos, ruta.toString());
    }

    // ==================== POPUP MENU ====================
    private void crearPopupMenu() {
        popupMenu = new JPopupMenu();
        popupMenu.setBackground(Color.POPUP_BACKGROUND);
        popupMenu.setBorder(BorderFactory.createLineBorder(Color.BORDER_COLOR, 1));

        popupMenu.add(crearItemMenu("+ Nueva Carpeta",
                e -> crearNuevaCarpetaDesdePopup()));
        popupMenu.addSeparator();
        popupMenu.add(crearItemMenu("🔄 Refrescar",
                e -> refrescarArbol()));
        popupMenu.add(crearItemMenu("📂 Abrir en Explorador",
                e -> abrirRutaDesdePopup()));
        popupMenu.addSeparator();
        popupMenu.add(crearItemMenuDanger("🗑 Eliminar Carpeta",
                e -> eliminarCarpetaDesdePopup()));
    }

    private JMenuItem crearItemMenu(String texto, ActionListener accion) {
        JMenuItem item = new JMenuItem(texto);
        item.setBackground(Color.POPUP_BACKGROUND);
        item.setForeground(Color.TEXT_PRIMARY);
        item.setFont(new Font("Arial", Font.PLAIN, 11));
        item.addActionListener(accion);
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e)
                { item.setBackground(Color.POPUP_HOVER); }
            @Override public void mouseExited(MouseEvent e)
                { item.setBackground(Color.POPUP_BACKGROUND); }
        });
        return item;
    }

    private JMenuItem crearItemMenuDanger(String texto, ActionListener accion) {
        JMenuItem item = new JMenuItem(texto);
        item.setBackground(Color.POPUP_BACKGROUND);
        item.setForeground(Color.TEXT_DANGER);
        item.setFont(new Font("Arial", Font.PLAIN, 11));
        item.addActionListener(accion);
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e)
                { item.setBackground(Color.DANGER_HOVER); }
            @Override public void mouseExited(MouseEvent e)
                { item.setBackground(Color.POPUP_BACKGROUND); }
        });
        return item;
    }

    // ==================== ACCIONES POPUP ====================
    private void crearNuevaCarpetaDesdePopup() {
        if (nodoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una carpeta primero",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nombre = JOptionPane.showInputDialog(this,
                "Nombre de la nueva carpeta:");
        if (nombre != null && !nombre.isEmpty()) {
            File destino = obtenerRutaArchivo(nodoSeleccionado);
            if (destino.isDirectory()) {
                File nueva = new File(destino, nombre);
                if (nueva.mkdir()) {
                    JOptionPane.showMessageDialog(this, "Carpeta creada correctamente");
                    refrescarArbol();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al crear la carpeta",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public void refrescarArbol() {
        DefaultMutableTreeNode raiz = crearArbolCarpetas();
        arbolCarpetas.setModel(new DefaultTreeModel(raiz));
        labelInfo.setText("Árbol refrescado");
    }

    private void abrirRutaDesdePopup() {
        if (nodoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una carpeta primero",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            File carpeta = obtenerRutaArchivo(nodoSeleccionado);
            if (carpeta.exists()) Desktop.getDesktop().open(carpeta);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir explorador: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarCarpetaDesdePopup() {
        if (nodoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una carpeta primero",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File carpeta = obtenerRutaArchivo(nodoSeleccionado);
        if (carpeta.exists() && carpeta.isDirectory()) {
            int conf = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar \"" + carpeta.getName() + "\"?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                if (eliminarDirectorio(carpeta)) {
                    JOptionPane.showMessageDialog(this, "Carpeta eliminada");
                    refrescarArbol();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al eliminar",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private boolean eliminarDirectorio(File dir) {
        if (dir.isDirectory()) {
            File[] hijos = dir.listFiles();
            if (hijos != null)
                for (File h : hijos) eliminarDirectorio(h);
        }
        return dir.delete();
    }

    // ==================== RENDERERS Y SCROLLBAR ====================
    private static class RendererCarpetas extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected,
                    expanded, leaf, row, hasFocus);
            setBackground(selected ? Color.TREE_SELECTION : Color.BACKGROUND);
            setForeground(Color.TEXT_PRIMARY);
            setFont(new Font("Arial", Font.PLAIN, 12));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 0));
            return this;
        }
    }

    private static class ScrollBarPersonalizado
            extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = Color.SCROLLBAR_THUMB;
            this.trackColor = Color.BACKGROUND;
        }
    }
}
