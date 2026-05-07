package org.motor2d.editor;

import org.motor2d.model.Entity;
import org.motor2d.utilities.Color;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Editor - Ventana principal del Motor 2D.
 *
 * Layout con JSplitPane para permitir redimensionado arrastrando divisores:
 *
 *  ┌──────────────────────────────────────────────────┐
 *  │                   Toolbar                        │
 *  ├──────────┬───────────────────────┬───────────────┤
 *  │          │                       │               │
 *  │Hierarchy │       Canvas          │  Properties   │
 *  │          │                       │               │
 *  ├──────────┴───────────────────────┤               │
 *  │           Assets                 │               │
 *  └──────────────────────────────────┴───────────────┘
 */
public class Editor extends JFrame {

    // ==================== CONSTANTES ====================
    private static final int WINDOW_WIDTH  = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final int PANEL_IZQ_W   = 250;
    private static final int PANEL_DER_W   = 300;
    private static final int PANEL_INF_H   = 220;
    private static final String CARPETA_PROYECTOS = "Proyectos";

    // ==================== ESTADO ====================
    private int mouseX, mouseY;
    private boolean maximizado = false;
    private Rectangle estadoAnterior;

    // ==================== COMPONENTES ====================
    private JPanel          raizPanel;
    private Toolbar         toolbar;
    private PanelHierarchy  panelHierarchy;
    private PanelCanvas     panelCanvas;
    private PanelProperties panelProperties;
    private PanelAssets     panelAssets;
    private StatusBar       statusBar;

    // Split panes principales
    private JSplitPane splitCentroPropiedad;  // canvas|properties
    private JSplitPane splitIzqCentro;        // hierarchy | (canvas+props)
    private JSplitPane splitVertical;         // (hierarchy+canvas+props) | assets

    private EditorController controller;
    private File rutaProyecto;
    private File carpetaProyectos;
// ==================== ACTUALIZAR TITULO ====================
public void actualizarTitulo() {
    String nombreProyecto = (controller.isProjectOpen()) 
                            ? controller.getProjectManager().getCurrentProject().getName() 
                            : "Sin proyecto";
    setTitle("Motor 2D - " + nombreProyecto);
}

// ... en constructor ...
public Editor() {
    rutaProyecto     = obtenerRutaProyecto();
    carpetaProyectos = inicializarCarpetaProyectos();
    controller       = new EditorController();
    controller.setEditor(this);

    actualizarTitulo(); // Llamada inicial

    setUndecorated(true);
    // ... el resto sigue igual ...

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        construirUI();
        setVisible(true);
    }

    // ==================== INICIALIZACIÓN ====================
    private File obtenerRutaProyecto() {
        File dir = new File(System.getProperty("user.dir"));
        while (dir != null && !dir.getName().equals("src")) dir = dir.getParentFile();
        if (dir != null) dir = dir.getParentFile();
        return (dir != null && new File(dir, "pom.xml").exists())
                ? dir : new File(System.getProperty("user.dir"));
    }

    private File inicializarCarpetaProyectos() {
        File carpeta = new File(rutaProyecto, CARPETA_PROYECTOS);
        if (!carpeta.exists()) carpeta.mkdir();
        return carpeta;
    }

    // ==================== CONSTRUCCIÓN UI ====================
    private void construirUI() {
        panelHierarchy  = new PanelHierarchy();
        panelProperties = new PanelProperties();
        panelCanvas     = new PanelCanvas();
        
        // Inicializar con la ruta del proyecto si está abierto
        File assetsDir = controller.isProjectOpen() 
                         ? new File(controller.getProjectPath(), "assets") 
                         : carpetaProyectos;
        panelAssets     = new PanelAssets(rutaProyecto, assetsDir, panelCanvas);
        toolbar         = new Toolbar(this);
        statusBar       = new StatusBar();

        panelHierarchy.init(controller, panelProperties);
        panelAssets.init(controller, panelProperties, this);
        panelCanvas.init(controller);
        controller.setCanvas(panelCanvas);

        // ── 1. Canvas + Assets (vertical) ──
        splitVertical = crearSplit(
                JSplitPane.VERTICAL_SPLIT,
                panelCanvas,
                panelAssets,
                WINDOW_HEIGHT - PANEL_INF_H - Toolbar.getAlto());
        splitVertical.setResizeWeight(0.8); // Canvas se lleva el 80% al redimensionar

        // ── 2. Hierarchy | (canvas+assets) ──
        splitIzqCentro = crearSplit(
                JSplitPane.HORIZONTAL_SPLIT,
                panelHierarchy,
                splitVertical,
                PANEL_IZQ_W);
        splitIzqCentro.setResizeWeight(0.1);

        // ── 3. (hierarchy+canvas+assets) | Properties ──
        splitCentroPropiedad = crearSplit(
                JSplitPane.HORIZONTAL_SPLIT,
                splitIzqCentro,
                panelProperties,
                WINDOW_WIDTH - PANEL_DER_W);
        splitCentroPropiedad.setResizeWeight(0.9);

        // ── Panel raíz: Toolbar (North), SplitPane (Center), StatusBar (South) ──
        raizPanel = new JPanel(new BorderLayout());
        raizPanel.setBackground(Color.BACKGROUND);
        raizPanel.add(toolbar, BorderLayout.NORTH);
        raizPanel.add(splitCentroPropiedad, BorderLayout.CENTER);
        raizPanel.add(statusBar, BorderLayout.SOUTH);

        registrarEventosVentana();
        add(raizPanel);
        
        actualizarStatusBar();
    }

    public void actualizarStatusBar() {
        if (controller.isProjectOpen()) {
            statusBar.setInfoProyecto("Proyecto: " + controller.getProjectManager().getCurrentProject().getName() + 
                                     " | " + controller.getProjectPath());
            statusBar.mostrarMensajePermanente("Listo");
        } else {
            statusBar.setInfoProyecto("Ningún proyecto abierto");
            statusBar.mostrarMensajePermanente("Inicie o abra un proyecto para comenzar");
        }
    }

    public void mostrarMensajeEstado(String msg) {
        if (statusBar != null) statusBar.mostrarMensaje(msg, 3000);
    }

    /** Crea un JSplitPane con el estilo oscuro del editor */
    private JSplitPane crearSplit(int orientation, Component a, Component b,
                                   int dividerPos) {
        JSplitPane split = new JSplitPane(orientation, a, b);
        split.setDividerSize(6); // Divisor un poco más grueso para que sea más fácil de agarrar
        split.setDividerLocation(dividerPos);
        split.setBorder(null);
        split.setBackground(Color.BORDER_COLOR);
        split.setContinuousLayout(true);

        // Estilo del divisor
        split.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(Color.BORDER_COLOR);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        // Dibujar una pequeña marca en el centro
                        g.setColor(Color.TEXT_SECONDARY);
                        if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
                            int mid = getHeight() / 2;
                            g.fillRect(2, mid - 10, 2, 20);
                        } else {
                            int mid = getWidth() / 2;
                            g.fillRect(mid - 10, 2, 20, 2);
                        }
                    }

                    @Override
                    public Dimension getPreferredSize() {
                        return orientation == JSplitPane.HORIZONTAL_SPLIT
                                ? new Dimension(6, 0)
                                : new Dimension(0, 6);
                    }
                };
            }
        });

        return split;
    }

    // ==================== TOGGLE PANELES ====================

    /** Colapsa o expande el panel Hierarchy */
    public void toggleHierarchy() {
        boolean visible = splitIzqCentro.getDividerLocation() > 10;
        splitIzqCentro.setDividerLocation(visible ? 0 : PANEL_IZQ_W);
    }

    /** Colapsa o expande el panel Properties */
    public void toggleProperties() {
        int total = splitCentroPropiedad.getWidth();
        boolean visible = splitCentroPropiedad.getDividerLocation() < total - 10;
        splitCentroPropiedad.setDividerLocation(visible ? total : total - PANEL_DER_W);
    }

    /** Colapsa o expande el panel Assets */
    public void toggleAssets() {
        int total = splitVertical.getHeight();
        boolean visible = splitVertical.getDividerLocation() < total - 10;
        splitVertical.setDividerLocation(visible ? total : total - PANEL_INF_H);
    }

    // ==================== EVENTOS VENTANA ====================
    private void registrarEventosVentana() {
        raizPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX(); mouseY = e.getY();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (toolbar.btnMinBounds().contains(p))    setState(JFrame.ICONIFIED);
                else if (toolbar.btnMaxBounds().contains(p)) alternarMaximizar();
                else if (toolbar.btnCerrarBounds().contains(p)) confirmarCerrar();
            }
        });

        raizPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!maximizado
                        && !toolbar.btnCerrarBounds().contains(e.getPoint())
                        && !toolbar.btnMaxBounds().contains(e.getPoint())
                        && !toolbar.btnMinBounds().contains(e.getPoint())) {
                    // Solo arrastrar desde la toolbar
                    if (e.getY() <= Toolbar.getAlto()) {
                        setLocation(e.getXOnScreen() - mouseX,
                                    e.getYOnScreen() - mouseY);
                    }
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

    private void confirmarCerrar() {
        if (controller.isProjectOpen()) {
            int r = JOptionPane.showConfirmDialog(this,
                    "¿Guardar el proyecto antes de salir?",
                    "Salir", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (r == JOptionPane.CANCEL_OPTION) return;
            if (r == JOptionPane.YES_OPTION)    controller.saveProject();
        }
        System.exit(0);
    }

    // ==================== GETTERS ====================
    public boolean isMaximizado()        { return maximizado;      }
    public EditorController getController()      { return controller;      }
    public EditorController getEditorController(){ return controller;      }
    public PanelCanvas      getPanelCanvas()     { return panelCanvas;     }
    public PanelAssets      getPanelAssets()     { return panelAssets;     }
    public JPanel           getRaizPanel()       { return raizPanel;       }

    public void refrescarHierarchy() {
        SwingUtilities.invokeLater(() -> panelHierarchy.refrescar());
    }

    /**
     * Selecciona una entidad en los paneles de Hierarchy y Properties.
     * Invocado usualmente por EditorController cuando se selecciona algo en el Canvas.
     */
    public void seleccionarEntidadEnUI(Entity entidad) {
        SwingUtilities.invokeLater(() -> {
            panelHierarchy.seleccionarEntidad(entidad);
            if (entidad != null) {
                panelProperties.mostrarEntidad(entidad, controller);
            } else {
                panelProperties.limpiar();
            }
        });
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        SwingUtilities.invokeLater(Editor::new);
    }

    public void confirmarSalida() {
    }
}
