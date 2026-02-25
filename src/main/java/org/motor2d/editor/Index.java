package org.motor2d.editor;

import org.motor2d.utilidades.Color;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public class Index extends JFrame {

    // ==================== CONSTANTES ====================
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 500;
    private static final int WINDOW_RADIUS = 40;
    private static final int BUTTON_SIZE = 80;
    private static final int BUTTON_ICON_SIZE = 50;
    private static final int BUTTON_RADIUS = 25;
    private static final int BUTTON_SPACING = 30;
    private static final int BUTTON_CONTAINER_Y = 210;

    // Rutas de iconos
    private static final String ICON_NEW_PROJECT = "/iconos/logoNewProject.png";
    private static final String ICON_CONFIG = "/iconos/logoCarpeta.png";
    private static final String ICON_EXIT = "/iconos/logoSalir2.png";

    // ==================== ATRIBUTOS ====================
    private int mouseX, mouseY;

    // ==================== CONSTRUCTOR ====================
    public Index() {
        configurarVentana();
        JPanel panelPrincipal = crearPanelPrincipal();
        agregarBotonesCentro(panelPrincipal);
        aplicarRedondeoVentana();
        add(panelPrincipal);
    }

    // ==================== MÉTODOS DE CONFIGURACIÓN ====================
    private void configurarVentana() {
        setUndecorated(true);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void aplicarRedondeoVentana() {
        setShape(new RoundRectangle2D.Double(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_RADIUS, WINDOW_RADIUS));

        // APLICAR REDONDEO AL ACTUALIZAR EL REDIMENSIONAMIENTO //
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), WINDOW_RADIUS, WINDOW_RADIUS));
            }
        });
    }

    // ==================== MÉTODOS DE INTERFAZ ====================
    private JPanel crearPanelPrincipal() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.BACKGROUND);
        panel.setLayout(null);
        agregarArrastrePanelPrincipal(panel);
        return panel;
    }

    private void agregarBotonesCentro(JPanel padre) {
        JPanel contenedor = new JPanel(new FlowLayout(FlowLayout.CENTER, BUTTON_SPACING, 0));
        contenedor.setOpaque(false);

        JButton btnProyecto = crearBoton(ICON_NEW_PROJECT);
        JButton btnConfig = crearBoton(ICON_CONFIG);
        JButton btnSalir = crearBoton(ICON_EXIT);

        btnSalir.addActionListener(e -> System.exit(0));

        contenedor.add(btnProyecto);
        contenedor.add(btnConfig);
        contenedor.add(btnSalir);

        contenedor.setBounds(0, BUTTON_CONTAINER_Y, WINDOW_WIDTH, 100);
        padre.add(contenedor);
    }

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
        if (rutaIcono == null || rutaIcono.isEmpty()) {
            return;
        }

        try {
            URL recursoURL = getClass().getResource(rutaIcono);
            if (recursoURL != null) {
                ImageIcon icon = new ImageIcon(recursoURL);
                Image img = icon.getImage().getScaledInstance(BUTTON_ICON_SIZE, BUTTON_ICON_SIZE, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(img));
            } else {
                System.err.println("Recurso no encontrado: " + rutaIcono);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar icono: " + rutaIcono);
            e.printStackTrace();
        }
    }

    // ==================== MÉTODOS DE EVENTO ====================
    private void agregarArrastrePanelPrincipal(JPanel panel) {
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
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();
                setLocation(x - mouseX, y - mouseY);
            }
        });
    }

    // ==================== CLASE INTERNA ====================
    /**
     * Botón personalizado con apariencia redonda y efecto hover
     */
    private class BotonRedondo extends JButton {
        private java.awt.Color colorActual = Color.BUTTON_DEFAULT;

        public BotonRedondo() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    colorActual = Color.BUTTON_HOVER;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    colorActual = Color.BUTTON_DEFAULT;
                    repaint();
                }
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
