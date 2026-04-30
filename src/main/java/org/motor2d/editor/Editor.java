package org.motor2d.editor;

import org.motor2d.utilities.Color;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Ventana principal del Editor del Motor 2D.
 * <p>
 * Esta clase actúa únicamente como contenedor y orquestador.
 * Toda la lógica de cada zona está delegada en su panel correspondiente:
 * <ul>
 *   <li>{@link Toolbar}         — Barra de título y botones de ventana</li>
 *   <li>{@link PanelHierarchy}  — Jerarquía de entidades (izquierda)</li>
 *   <li>{@link PanelCanvas}     — Visor / Canvas central</li>
 *   <li>{@link PanelProperties} — Propiedades del elemento seleccionado (derecha)</li>
 *   <li>{@link PanelAssets}     — Explorador de assets (inferior)</li>
 * </ul>
 */
public class Editor extends JFrame {

    // ==================== CONSTANTES DE LAYOUT ====================
    private static final int WINDOW_WIDTH  = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final int DIVIDER       = 1;
    private static final int PANEL_IZQ_W   = 250;
    private static final int PANEL_DER_W   = 300;
    private static final int PANEL_INF_H   = 250;
    private static final String CARPETA_PROYECTOS = "Proyectos";

    // ==================== ESTADO DE VENTANA ====================
    private int mouseX, mouseY;
    private boolean maximizado = false;
    private Rectangle estadoAnterior;

    // ==================== PANELES ====================
    private JPanel          raizPanel;
    private Toolbar         toolbar;
    private PanelHierarchy  panelHierarchy;
    private PanelCanvas     panelCanvas;
    private PanelProperties panelProperties;
    private PanelAssets     panelAssets;

    // ==================== PROYECTO ====================
    private File rutaProyecto;
    private File carpetaProyectos;

    // ==================== CONSTRUCTOR ====================
    public Editor() {
        rutaProyecto      = obtenerRutaProyecto();
        carpetaProyectos  = inicializarCarpetaProyectos();

        setUndecorated(true);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        construirUI();
        setVisible(true);
    }

    // ==================== GETTER para Toolbar ====================
    public JPanel getRaizPanel() {
        return raizPanel;
    }

    // ==================== DETECCIÓN DE RUTA ====================
    private File obtenerRutaProyecto() {
        File dir = new File(System.getProperty("user.dir"));

        while (dir != null && !dir.getAbsolutePath().endsWith("src"))
            dir = dir.getParentFile();

        if (dir != null && dir.getName().equals("src"))
            dir = dir.getParentFile();

        if (dir != null && new File(dir, "pom.xml").exists()) {
            System.out.println("Raíz del proyecto: " + dir.getAbsolutePath());
            return dir;
        }

        dir = new File(System.getProperty("user.dir"));
        System.out.println("Usando directorio actual: " + dir.getAbsolutePath());
        return dir;
    }

    private File inicializarCarpetaProyectos() {
        File carpeta = new File(rutaProyecto, CARPETA_PROYECTOS);
        if (!carpeta.exists()) {
            if (carpeta.mkdir())
                System.out.println("Carpeta 'Proyectos' creada: " + carpeta.getAbsolutePath());
            else
                System.err.println("Error al crear carpeta 'Proyectos'");
        }
        return carpeta;
    }

    // ==================== CONSTRUCCIÓN DE UI ====================
    private void construirUI() {
        // ── Paneles hoja (sin dependencias entre sí) ──
        toolbar         = new Toolbar(this);
        panelHierarchy  = new PanelHierarchy();
        panelProperties = new PanelProperties();

        // Canvas primero — Assets lo necesita como referencia
        panelCanvas = new PanelCanvas();
        panelAssets = new PanelAssets(rutaProyecto, carpetaProyectos, panelCanvas);

        // ── Panel raíz con pintado personalizado ──
        raizPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.BACKGROUND);
                g2.fillRect(0, 0, getWidth(), getHeight());

                pintarZona(g2, toolbar,         Color.MENUBAR_BACKGROUND);
                pintarZona(g2, panelHierarchy,  Color.BACKGROUND);
                pintarZona(g2, panelCanvas,     Color.CANVAS_COLOR);
                pintarZona(g2, panelProperties, Color.BACKGROUND);
                pintarZona(g2, panelAssets,     Color.BACKGROUND);

                // Botones de ventana pintados encima
                toolbar.pintarBotones(g2, maximizado);
                g2.dispose();
            }
        };
        raizPanel.setBackground(Color.BACKGROUND);

        raizPanel.add(toolbar);
        raizPanel.add(panelHierarchy);
        raizPanel.add(panelCanvas);
        raizPanel.add(panelProperties);
        raizPanel.add(panelAssets);

        registrarEventosVentana();

        raizPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                distribuirPaneles(raizPanel.getWidth(), raizPanel.getHeight());
                raizPanel.repaint();
            }
        });

        distribuirPaneles(WINDOW_WIDTH, WINDOW_HEIGHT);
        add(raizPanel);
    }

    // ==================== LAYOUT ====================
    private void distribuirPaneles(int W, int H) {
        int barH   = Toolbar.getAlto();
        int panelY = barH + DIVIDER;
        int totalH = H - panelY;
        int cenX   = PANEL_IZQ_W + DIVIDER;
        int cenW   = W - PANEL_IZQ_W - PANEL_DER_W - DIVIDER * 2;
        int areaH  = totalH - PANEL_INF_H - DIVIDER;

        toolbar        .setBounds(0,              0,       W,            barH);
        panelHierarchy .setBounds(0,              panelY,  PANEL_IZQ_W, areaH);
        panelCanvas    .setBounds(cenX,           panelY,  cenW,        areaH);
        panelProperties.setBounds(W - PANEL_DER_W, panelY, PANEL_DER_W, totalH);
        panelAssets    .setBounds(0,    panelY + areaH + DIVIDER,
                                  cenX + cenW,  PANEL_INF_H);
    }

    private void pintarZona(Graphics2D g2, JPanel panel, java.awt.Color bg) {
        if (panel == null) return;
        int x = panel.getX(), y = panel.getY(),
            w = panel.getWidth(), h = panel.getHeight();
        g2.setColor(bg);
        g2.fillRect(x, y, w, h);
        g2.setColor(Color.BORDER_COLOR);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(x, y, w - 1, h - 1);
    }

    // ==================== EVENTOS DE VENTANA ====================
    private void registrarEventosVentana() {
        // Arrastre
        raizPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (toolbar.btnMinBounds().contains(p)) {
                    setState(JFrame.ICONIFIED);
                } else if (toolbar.btnMaxBounds().contains(p)) {
                    alternarMaximizar();
                } else if (toolbar.btnCerrarBounds().contains(p)) {
                    System.exit(0);
                }
            }
        });

        raizPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!maximizado
                        && !toolbar.btnCerrarBounds().contains(e.getPoint())
                        && !toolbar.btnMaxBounds().contains(e.getPoint())
                        && !toolbar.btnMinBounds().contains(e.getPoint())) {
                    setLocation(e.getXOnScreen() - mouseX,
                                e.getYOnScreen() - mouseY);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                toolbar.actualizarHover(e.getPoint());
            }
        });
    }

    private void alternarMaximizar() {
        if (!maximizado) {
            estadoAnterior = new Rectangle(getX(), getY(),
                                           getWidth(), getHeight());
            Rectangle pantalla = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration()
                    .getBounds();
            setBounds(pantalla);
            maximizado = true;
        } else {
            setBounds(estadoAnterior);
            maximizado = false;
        }
        repaint();
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        SwingUtilities.invokeLater(Editor::new);
    }
}
