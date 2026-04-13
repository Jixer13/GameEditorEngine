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
 * Editor del Motor 2D - Versión monolítica completa.
 */
public class Editor extends JFrame {

    // ==================== CONSTANTES ====================
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final int DIVIDER = 1;
    private static final int MENUBAR_ALTO = 35;
    private static final int BTN_ANCHO = 40;
    private static final int BTN_ALTO = 22;
    private static final int PANEL_IZQ_W = 250;
    private static final int PANEL_DER_W = 300;
    private static final int PANEL_INF_H = 250;
    private static final String CARPETA_PROYECTOS = "Proyectos";

    // ==================== ATRIBUTOS ====================
    private int mouseX, mouseY;
    private boolean btnCerrarHover = false;
    private boolean btnMaximizarHover = false;
    private boolean btnMinimizarHover = false;
    private boolean maximizado = false;
    private Rectangle estadoAnterior;

    // Paneles
    private JPanel panelMenuBar, panelIzquierdo, panelCentral, panelDerecho, panelInferior;
    private JPanel raizPanel;

    // Gestión de proyectos
    private File rutaProyecto;
    private File carpetaProyectos;

    // Árbol de carpetas
    private JTree arbolCarpetas;
    private JLabel labelInfo;
    private JPopupMenu popupMenu;
    private DefaultMutableTreeNode nodoSeleccionado;
    private File carpetaActual;

    // ==================== CONSTRUCTOR ====================
    public Editor() {
        rutaProyecto = obtenerRutaProyecto();
        crearCarpetaProyectos();

        setUndecorated(true);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        crearEstructura();
        setVisible(true);
    }

    // ==================== DETECCIÓN DE RUTA ====================
    private File obtenerRutaProyecto() {
        File carpetaActual = new File(System.getProperty("user.dir"));
        System.out.println("Directorio actual: " + carpetaActual.getAbsolutePath());

        while (carpetaActual != null && !carpetaActual.getAbsolutePath().endsWith("src")) {
            carpetaActual = carpetaActual.getParentFile();
        }

        if (carpetaActual != null && carpetaActual.getName().equals("src")) {
            carpetaActual = carpetaActual.getParentFile();
        }

        if (carpetaActual != null && new File(carpetaActual, "pom.xml").exists()) {
            System.out.println("Raíz del proyecto detectada: " + carpetaActual.getAbsolutePath());
            return carpetaActual;
        }

        carpetaActual = new File(System.getProperty("user.dir"));
        System.out.println("Usando directorio actual como raíz: " + carpetaActual.getAbsolutePath());
        return carpetaActual;
    }

    // ==================== CREAR CARPETA DE PROYECTOS ====================
    private void crearCarpetaProyectos() {
        carpetaProyectos = new File(rutaProyecto, CARPETA_PROYECTOS);

        if (!carpetaProyectos.exists()) {
            if (carpetaProyectos.mkdir()) {
                System.out.println("Carpeta 'Proyectos' creada en: " + carpetaProyectos.getAbsolutePath());
            } else {
                System.err.println("Error al crear la carpeta 'Proyectos'");
            }
        } else {
            System.out.println("Carpeta 'Proyectos' ya existe en: " + carpetaProyectos.getAbsolutePath());
        }
    }

    // ==================== ESTRUCTURA PRINCIPAL ====================
    private void crearEstructura() {
        raizPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.BACKGROUND);
                g2.fillRect(0, 0, getWidth(), getHeight());
                pintarPanel(g2, panelMenuBar, Color.MENUBAR_BACKGROUND);
                pintarPanel(g2, panelIzquierdo, Color.BACKGROUND);
                pintarPanel(g2, panelCentral, Color.CANVAS_COLOR);
                pintarPanel(g2, panelDerecho, Color.BACKGROUND);
                pintarPanel(g2, panelInferior, Color.BACKGROUND);
                pintarBotones(g2);
                g2.dispose();
            }
        };
        raizPanel.setBackground(Color.BACKGROUND);
        agregarArrastre(raizPanel);

        panelMenuBar = crearMenuBar();
        panelIzquierdo = panelTransparente();
        panelCentral = panelTransparente();
        panelDerecho = panelTransparente();
        panelInferior = crearPanelCarpetas();

        for (JPanel p : new JPanel[]{panelMenuBar, panelIzquierdo, panelCentral, panelDerecho, panelInferior})
            raizPanel.add(p);

        raizPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                distribuir(raizPanel.getWidth(), raizPanel.getHeight());
                raizPanel.repaint();
            }
        });

        distribuir(WINDOW_WIDTH, WINDOW_HEIGHT);
        add(raizPanel);
    }

    // ==================== PANEL DE CARPETAS ====================
    private JPanel crearPanelCarpetas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

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
        arbolCarpetas.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BORDER_COLOR));

        crearPopupMenu();

        arbolCarpetas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int seleccion = arbolCarpetas.getRowForLocation(e.getX(), e.getY());
                if (seleccion != -1) {
                    nodoSeleccionado = (DefaultMutableTreeNode) arbolCarpetas.getPathForRow(seleccion).getLastPathComponent();

                    if (e.getButton() == MouseEvent.BUTTON1) {
                        procesarNodoSeleccionado(nodoSeleccionado);
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        arbolCarpetas.setSelectionRow(seleccion);
                        popupMenu.show(arbolCarpetas, e.getX(), e.getY());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(arbolCarpetas);
        scrollPane.setBackground(Color.BACKGROUND);
        scrollPane.getViewport().setBackground(Color.BACKGROUND);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BORDER_COLOR, 1));
        scrollPane.getVerticalScrollBar().setUI(new ScrollBarPersonalizado());
        scrollPane.getHorizontalScrollBar().setUI(new ScrollBarPersonalizado());

        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel panelInfo = new JPanel(new BorderLayout());
        panelInfo.setBackground(Color.PANEL_BACKGROUND);
        panelInfo.setPreferredSize(new Dimension(0, 30));
        panelInfo.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BORDER_COLOR));
        labelInfo = new JLabel("Proyecto: " + rutaProyecto.getName() + " | Click derecho para opciones");
        labelInfo.setForeground(Color.TEXT_SECONDARY);
        labelInfo.setFont(new Font("Arial", Font.PLAIN, 10));
        labelInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelInfo.add(labelInfo, BorderLayout.CENTER);

        panel.add(panelInfo, BorderLayout.SOUTH);
        return panel;
    }

    // ==================== MENÚ CONTEXTUAL ====================
    private void crearPopupMenu() {
        popupMenu = new JPopupMenu();
        popupMenu.setBackground(Color.POPUP_BACKGROUND);
        popupMenu.setBorder(BorderFactory.createLineBorder(Color.BORDER_COLOR, 1));

        JMenuItem itemNuevaCarPeta = crearItemMenu("+ Nueva Carpeta", e -> crearNuevaCarpetaDesdePopup());
        popupMenu.add(itemNuevaCarPeta);
        popupMenu.addSeparator();

        JMenuItem itemRefrescar = crearItemMenu("🔄 Refrescar", e -> refrescarArbolDesdePopup());
        popupMenu.add(itemRefrescar);

        JMenuItem itemAbrirRuta = crearItemMenu("📂 Abrir Ruta en Explorador", e -> abrirRutaDesdePopup());
        popupMenu.add(itemAbrirRuta);
        popupMenu.addSeparator();

        JMenuItem itemEliminar = crearItemMenuDanger("🗑 Eliminar Carpeta", e -> eliminarCarpetaDesdePopup());
        popupMenu.add(itemEliminar);
    }

    private JMenuItem crearItemMenu(String texto, ActionListener accion) {
        JMenuItem item = new JMenuItem(texto);
        item.setBackground(Color.POPUP_BACKGROUND);
        item.setForeground(Color.TEXT_PRIMARY);
        item.setFont(new Font("Arial", Font.PLAIN, 11));
        item.addActionListener(accion);
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { item.setBackground(Color.POPUP_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { item.setBackground(Color.POPUP_BACKGROUND); }
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
            @Override public void mouseEntered(MouseEvent e) { item.setBackground(Color.DANGER_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { item.setBackground(Color.POPUP_BACKGROUND); }
        });
        return item;
    }

    // ==================== ACCIONES DEL POPUP ====================
    private void crearNuevaCarpetaDesdePopup() {
        if (nodoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una carpeta primero", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nombre = JOptionPane.showInputDialog(this, "Nombre de la nueva carpeta:");
        if (nombre != null && !nombre.isEmpty()) {
            File carpetaDestino = obtenerRutaCarpeta(nodoSeleccionado);
            if (carpetaDestino.isDirectory()) {
                File nuevaCarpeta = new File(carpetaDestino, nombre);
                if (nuevaCarpeta.mkdir()) {
                    JOptionPane.showMessageDialog(this, "Carpeta creada correctamente");
                    refrescarArbolDesdePopup();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al crear la carpeta", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void refrescarArbolDesdePopup() {
        DefaultMutableTreeNode raizArbol = crearArbolCarpetas();
        DefaultTreeModel modelo = new DefaultTreeModel(raizArbol);
        arbolCarpetas.setModel(modelo);
        labelInfo.setText("Árbol refrescado");
    }

    private void abrirRutaDesdePopup() {
        if (nodoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una carpeta primero", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            File carpeta = obtenerRutaCarpeta(nodoSeleccionado);
            if (carpeta.exists()) Desktop.getDesktop().open(carpeta);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al abrir explorador: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarCarpetaDesdePopup() {
        if (nodoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una carpeta primero", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File carpeta = obtenerRutaCarpeta(nodoSeleccionado);
        if (carpeta.exists() && carpeta.isDirectory()) {
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de que quieres eliminar: " + carpeta.getName() + "?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                if (eliminarDirectorio(carpeta)) {
                    JOptionPane.showMessageDialog(this, "Carpeta eliminada correctamente");
                    refrescarArbolDesdePopup();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al eliminar la carpeta", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private boolean eliminarDirectorio(File directorio) {
        if (directorio.isDirectory()) {
            File[] archivos = directorio.listFiles();
            if (archivos != null) {
                for (File archivo : archivos) eliminarDirectorio(archivo);
            }
        }
        return directorio.delete();
    }

    // ==================== MÉTODOS AUXILIARES DEL ÁRBOL ====================
    private void procesarNodoSeleccionado(DefaultMutableTreeNode nodo) {
        String nombreNodo = nodo.getUserObject().toString();
        String nombre = nombreNodo.replaceAll("[^\\w\\-. ]", "").trim();

        if (nombreNodo.contains("📁") || nombreNodo.contains("🎮")) {
            carpetaActual = obtenerRutaCarpeta(nodo);
            labelInfo.setText("📁 Carpeta: " + nombre);
        } else {
            labelInfo.setText("📄 Archivo: " + nombre);
        }
    }

    private File obtenerRutaCarpeta(DefaultMutableTreeNode nodo) {
        StringBuilder ruta = new StringBuilder();
        Object[] ruta_nodos = nodo.getPath();
        for (int i = 1; i < ruta_nodos.length; i++) {
            String parte = ruta_nodos[i].toString().replaceAll("[^\\w\\-. ]", "").trim();
            ruta.append(parte);
            if (i < ruta_nodos.length - 1) ruta.append(File.separator);
        }
        return new File(carpetaProyectos, ruta.toString());
    }

    private DefaultMutableTreeNode crearArbolCarpetas() {
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("📁 Proyectos");
        if (carpetaProyectos.exists() && carpetaProyectos.isDirectory()) {
            agregarArchivosRecursivos(raiz, carpetaProyectos);
        }
        return raiz;
    }

    private void agregarArchivosRecursivos(DefaultMutableTreeNode nodo, File carpeta) {
        File[] archivos = carpeta.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isDirectory() && !archivo.getName().startsWith(".") && !archivo.getName().equals("target")) {
                    DefaultMutableTreeNode nodoSubcarpeta = new DefaultMutableTreeNode("📁 " + archivo.getName());
                    agregarArchivosRecursivos(nodoSubcarpeta, archivo);
                    if (nodoSubcarpeta.getChildCount() == 0) {
                        nodoSubcarpeta.add(new DefaultMutableTreeNode(""));
                    }
                    nodo.add(nodoSubcarpeta);
                } else if (!archivo.isDirectory()) {
                    String icono = obtenerIconoArchivo(archivo.getName());
                    nodo.add(new DefaultMutableTreeNode(icono + " " + archivo.getName()));
                }
            }
        }
    }

    private String obtenerIconoArchivo(String nombre) {
        if (nombre.endsWith(".java")) return "☕";
        if (nombre.endsWith(".png") || nombre.endsWith(".jpg")) return "🖼";
        if (nombre.endsWith(".txt")) return "📄";
        if (nombre.endsWith(".json")) return "⚙";
        if (nombre.endsWith(".xml")) return "🏷";
        return "📋";
    }

    // ==================== RENDERERS ====================
    private static class RendererCarpetas extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            setBackground(selected ? Color.TREE_SELECTION : Color.BACKGROUND);
            setForeground(Color.TEXT_PRIMARY);
            setFont(new Font("Arial", Font.PLAIN, 12));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 0));
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

    // ==================== BARRA DE TÍTULO ====================
    private JPanel crearMenuBar() {
        JPanel bar = new JPanel(null);
        bar.setOpaque(false);

        raizPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (btnMinBounds().contains(e.getPoint())) {
                    setState(JFrame.ICONIFIED);
                } else if (btnMaxBounds().contains(e.getPoint())) {
                    alternarMaximizar();
                } else if (btnCerrarBounds().contains(e.getPoint())) {
                    System.exit(0);
                }
            }
        });

        raizPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean dentroCerrar = btnCerrarBounds().contains(e.getPoint());
                boolean dentroMax    = btnMaxBounds().contains(e.getPoint());
                boolean dentroMin    = btnMinBounds().contains(e.getPoint());

                if (dentroCerrar != btnCerrarHover || dentroMax != btnMaximizarHover || dentroMin != btnMinimizarHover) {
                    btnCerrarHover    = dentroCerrar;
                    btnMaximizarHover = dentroMax;
                    btnMinimizarHover = dentroMin;
                    raizPanel.repaint();
                }
            }
        });

        return bar;
    }

    private Rectangle btnMinBounds() {
        int x = panelMenuBar.getX() + panelMenuBar.getWidth() - BTN_ANCHO * 3 - 6;
        int y = panelMenuBar.getY() + (MENUBAR_ALTO - BTN_ALTO) / 2;
        return new Rectangle(x, y, BTN_ANCHO, BTN_ALTO);
    }

    private Rectangle btnMaxBounds() {
        int x = panelMenuBar.getX() + panelMenuBar.getWidth() - BTN_ANCHO * 2 - 6;
        int y = panelMenuBar.getY() + (MENUBAR_ALTO - BTN_ALTO) / 2;
        return new Rectangle(x, y, BTN_ANCHO, BTN_ALTO);
    }

    private Rectangle btnCerrarBounds() {
        int x = panelMenuBar.getX() + panelMenuBar.getWidth() - BTN_ANCHO - 6;
        int y = panelMenuBar.getY() + (MENUBAR_ALTO - BTN_ALTO) / 2;
        return new Rectangle(x, y, BTN_ANCHO, BTN_ALTO);
    }

    // ==================== PINTADO DE BOTONES (CORREGIDO) ====================
    private void pintarBotones(Graphics2D g2) {
        pintarBotonMinimizar(g2);
        pintarBotonMaximizar(g2);
        pintarBotonCerrar(g2);
    }

    private void pintarBotonMinimizar(Graphics2D g2) {
        Rectangle r = btnMinBounds();
        if (btnMinimizarHover) {
            g2.setColor(Color.BTN_HOVER);
            g2.fillRect(r.x, r.y, r.width, r.height);
        }
        pintarTextoBoton(g2, r, "−");
    }

    private void pintarBotonMaximizar(Graphics2D g2) {
        Rectangle r = btnMaxBounds();
        if (btnMaximizarHover) {
            g2.setColor(Color.BTN_HOVER);
            g2.fillRect(r.x, r.y, r.width, r.height);
        }
        pintarTextoBoton(g2, r, maximizado ? "❐" : "□");
    }

    private void pintarBotonCerrar(Graphics2D g2) {
        Rectangle r = btnCerrarBounds();
        if (btnCerrarHover) {
            g2.setColor(Color.CLOSE_HOVER);
            g2.fillRect(r.x, r.y, r.width, r.height);
        }
        pintarTextoBoton(g2, r, "✕");
    }

    private void pintarTextoBoton(Graphics2D g2, Rectangle r, String txt) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.TEXT_PRIMARY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(txt)) / 2;
        int ty = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(txt, tx, ty);
    }

    private void alternarMaximizar() {
        if (!maximizado) {
            estadoAnterior = new Rectangle(getX(), getY(), getWidth(), getHeight());
            Rectangle pantalla = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration().getBounds();
            setBounds(pantalla);
            maximizado = true;
        } else {
            setBounds(estadoAnterior);
            maximizado = false;
        }
        repaint();
    }

    // ==================== LAYOUT ====================
    private JPanel panelTransparente() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    private void pintarPanel(Graphics2D g2, JPanel panel, java.awt.Color bg) {
        if (panel == null) return;
        int x = panel.getX(), y = panel.getY(), w = panel.getWidth(), h = panel.getHeight();
        g2.setColor(bg);
        g2.fillRect(x, y, w, h);
        g2.setColor(Color.BORDER_COLOR);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(x, y, w - 1, h - 1);
    }

    private void distribuir(int W, int H) {
        int panelsY = MENUBAR_ALTO + DIVIDER;
        int alturaTotal = H - panelsY;
        int cenX = PANEL_IZQ_W + DIVIDER;
        int cenW = W - PANEL_IZQ_W - PANEL_DER_W - DIVIDER * 2;
        int areaH = alturaTotal - PANEL_INF_H - DIVIDER;

        panelMenuBar.setBounds(0, 0, W, MENUBAR_ALTO);
        panelIzquierdo.setBounds(0, panelsY, PANEL_IZQ_W, areaH);
        panelCentral.setBounds(cenX, panelsY, cenW, areaH);
        panelDerecho.setBounds(W - PANEL_DER_W, panelsY, PANEL_DER_W, alturaTotal);
        panelInferior.setBounds(0, panelsY + areaH + DIVIDER, cenX + cenW, PANEL_INF_H);
    }

    private void agregarArrastre(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!maximizado && !btnCerrarBounds().contains(e.getPoint()) &&
                        !btnMaxBounds().contains(e.getPoint()) && !btnMinBounds().contains(e.getPoint())) {
                    setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
                }
            }
        });
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        SwingUtilities.invokeLater(() -> new Editor());
    }
}