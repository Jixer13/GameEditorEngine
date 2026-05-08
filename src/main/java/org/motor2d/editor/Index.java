package org.motor2d.editor;

import org.motor2d.utilities.Color;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.net.URL;

/**
 * Pantalla de bienvenida del Motor 2D.
 * Ofrece acceso a: Nuevo Proyecto, Abrir Proyecto y Salir.
 */
public class Index extends JFrame {

    // ==================== CONSTANTES ====================
    private static final int WINDOW_WIDTH       = 600;
    private static final int WINDOW_HEIGHT      = 500;
    private static final int WINDOW_RADIUS      = 40;
    private static final int BUTTON_SIZE        = 80;
    private static final int BUTTON_ICON_SIZE   = 50;
    private static final int BUTTON_RADIUS      = 25;
    private static final int BUTTON_SPACING     = 30;
    private static final int BUTTON_CONTAINER_Y = 210;

    private static final String ICON_NEW_PROJECT = "/iconos/logoNewProject.png";
    private static final String ICON_OPEN        = "/iconos/logoCarpeta.png";
    private static final String ICON_EXIT        = "/iconos/logoSalir2.png";

    // ==================== ESTADO ====================
    private int mouseX, mouseY;

    // ==================== CONSTRUCTOR ====================
    public Index() {
        configurarVentana();
        JPanel panelPrincipal = crearPanelPrincipal();
        agregarTitulo(panelPrincipal);
        agregarBotonesCentro(panelPrincipal);
        agregarLabelsDebajo(panelPrincipal);
        aplicarRedondeoVentana();
        add(panelPrincipal);
    }

    // ==================== CONFIGURACIÓN DE VENTANA ====================
    private void configurarVentana() {
        setUndecorated(true);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void aplicarRedondeoVentana() {
        setShape(new RoundRectangle2D.Double(
                0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_RADIUS, WINDOW_RADIUS));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                setShape(new RoundRectangle2D.Double(
                        0, 0, getWidth(), getHeight(), WINDOW_RADIUS, WINDOW_RADIUS));
            }
        });
    }

    // ==================== INTERFAZ ====================
    private JPanel crearPanelPrincipal() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.BACKGROUND);
        agregarArrastePanelPrincipal(panel);
        return panel;
    }

    private void agregarTitulo(JPanel padre) {
        JLabel titulo = new JLabel("Motor 2D Editor", SwingConstants.CENTER);
        titulo.setForeground(Color.TEXT_PRIMARY);
        titulo.setFont(new Font("Arial", Font.BOLD, 26));
        titulo.setBounds(0, 100, WINDOW_WIDTH, 40);
        padre.add(titulo);

        JLabel subtitulo = new JLabel("Selecciona una opción para comenzar", SwingConstants.CENTER);
        subtitulo.setForeground(Color.TEXT_SECONDARY);
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitulo.setBounds(0, 148, WINDOW_WIDTH, 24);
        padre.add(subtitulo);
    }

    private void agregarBotonesCentro(JPanel padre) {
        JPanel contenedor = new JPanel(
                new FlowLayout(FlowLayout.CENTER, BUTTON_SPACING, 0));
        contenedor.setOpaque(false);

        JButton btnNuevo = crearBoton(ICON_NEW_PROJECT);
        JButton btnAbrir = crearBoton(ICON_OPEN);
        JButton btnSalir = crearBoton(ICON_EXIT);

        btnNuevo.addActionListener(e -> accionNuevoProyecto());
        btnAbrir.addActionListener(e -> accionAbrirProyecto());
        btnSalir.addActionListener(e -> System.exit(0));

        contenedor.add(btnNuevo);
        contenedor.add(btnAbrir);
        contenedor.add(btnSalir);

        contenedor.setBounds(0, BUTTON_CONTAINER_Y, WINDOW_WIDTH, 100);
        padre.add(contenedor);
    }

    private void agregarLabelsDebajo(JPanel padre) {
        String[] labels = {"Nuevo Proyecto", "Abrir Proyecto", "Salir"};
        int totalAncho = 3 * BUTTON_SIZE + 2 * BUTTON_SPACING;
        int startX     = (WINDOW_WIDTH - totalAncho) / 2;
        int labelY     = BUTTON_CONTAINER_Y + BUTTON_SIZE + 10;

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i], SwingConstants.CENTER);
            lbl.setForeground(Color.TEXT_SECONDARY);
            lbl.setFont(new Font("Arial", Font.PLAIN, 11));
            lbl.setBounds(startX + i * (BUTTON_SIZE + BUTTON_SPACING), labelY, BUTTON_SIZE, 20);
            padre.add(lbl);
        }
    }

    // ==================== ACCIONES ====================
    private void accionNuevoProyecto() {
        String nombre = JOptionPane.showInputDialog(this,
                "Nombre del nuevo proyecto:", "Nuevo Proyecto", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null || nombre.isBlank()) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecciona la carpeta donde crear el proyecto");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        File carpetaProyectos = new File(System.getProperty("user.dir"), "Proyectos");
        if (carpetaProyectos.exists()) chooser.setCurrentDirectory(carpetaProyectos);

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String ruta = chooser.getSelectedFile().getAbsolutePath();
        abrirEditorConProyecto(nombre.trim(), ruta, true);
    }

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
                    "Asegúrate de seleccionar una carpeta con un archivo project.json.",
                    "Proyecto inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        abrirEditorConProyecto(null, ruta, false);
    }

    private void abrirEditorConProyecto(String nombre, String ruta, boolean esNuevo) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            Editor editor = new Editor();
            EditorController ctrl = editor.getController();
            if (esNuevo) {
                ctrl.createProject(nombre, ruta);
            } else {
                ctrl.openProject(ruta);
            }
            
            // Actualizar el explorador de assets para que apunte a la carpeta del proyecto
            if (editor.getPanelAssets() != null) {
                File rootPath = esNuevo
                        ? new File(ruta, nombre)
                        : new File(ruta);

                editor.getPanelAssets().setRootDirectory(rootPath);
            }
            
            editor.refrescarHierarchy();
        });
    }

    // ==================== BOTONES ====================
    private JButton crearBoton(String rutaIcono) {
        BotonRedondo btn = new BotonRedondo();
        cargarIconoEnBoton(btn, rutaIcono);
        btn.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private void cargarIconoEnBoton(JButton btn, String rutaIcono) {
        if (rutaIcono == null || rutaIcono.isEmpty()) return;
        try {
            URL url = getClass().getResource(rutaIcono);
            if (url != null) {
                Image img = new ImageIcon(url).getImage()
                        .getScaledInstance(BUTTON_ICON_SIZE, BUTTON_ICON_SIZE, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(img));
            } else {
                System.err.println("Recurso no encontrado: " + rutaIcono);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar icono: " + rutaIcono);
        }
    }

    // ==================== EVENTOS ====================
    private void agregarArrastePanelPrincipal(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                mouseX = e.getX(); mouseY = e.getY();
            }
        });
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
            }
        });
    }

    // ==================== CLASE INTERNA ====================
    private class BotonRedondo extends JButton {
        private java.awt.Color colorActual = Color.BUTTON_DEFAULT;

        public BotonRedondo() {
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { colorActual = Color.BUTTON_HOVER;   repaint(); }
                @Override public void mouseExited (MouseEvent e) { colorActual = Color.BUTTON_DEFAULT; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(colorActual);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_RADIUS, BUTTON_RADIUS);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        SwingUtilities.invokeLater(() -> new Index().setVisible(true));
    }
}
