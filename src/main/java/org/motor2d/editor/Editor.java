package org.motor2d.editor;

import org.motor2d.utilities.Color;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import static org.motor2d.utilities.Color.*;

/**
 * Interfaz principal del Editor del Motor 2D
 * Contiene la estructura de divisiones para:
 * - ListaObjetos (izquierda)
 * - Visor (centro)
 * - CaracterísticasObjetos (derecha)
 * - Sistema de carpetas (inferior)
 */
public class Editor extends JFrame {

    // ==================== CONSTANTES ====================

    private static final int WINDOW_WIDTH  = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final int DIVIDER       = 1;
    private static final int MENUBAR_ALTO  = 35;
    private static final int BTN_ANCHO     = 40;
    private static final int BTN_ALTO      = 22;
    private static final int PANEL_IZQ_W   = 250;
    private static final int PANEL_DER_W   = 300;
    private static final int PANEL_INF_H   = 250;


    // ==================== ATRIBUTOS ====================
    private int mouseX, mouseY;
    private boolean btnHover = false;
    private JPanel panelMenuBar, panelIzquierdo, panelCentral, panelDerecho, panelInferior;

    // ==================== CONSTRUCTOR ====================
    public Editor() {
        setUndecorated(true);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        crearEstructura();
        setVisible(true);
    }

    // ==================== ESTRUCTURA ====================
    private void crearEstructura() {
        JPanel raiz = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.BACKGROUND);
                g2.fillRect(0, 0, getWidth(), getHeight());
                pintarPanel(g2, panelMenuBar,   Color.BACKGROUND);
                pintarPanel(g2, panelIzquierdo, Color.BACKGROUND);
                pintarPanel(g2, panelCentral,   CANVAS_COLOR);
                pintarPanel(g2, panelDerecho,   Color.BACKGROUND);
                pintarPanel(g2, panelInferior,  Color.BACKGROUND);
                if (panelMenuBar != null) pintarBotonCerrar(g2);
                g2.dispose();
            }
        };
        raiz.setBackground(Color.BACKGROUND);
        agregarArrastre(raiz);

        panelMenuBar   = crearMenuBar(raiz);
        panelIzquierdo = panelTransparente();
        panelCentral   = panelTransparente();
        panelDerecho   = panelTransparente();
        panelInferior  = panelTransparente();

        for (JPanel p : new JPanel[]{panelMenuBar, panelIzquierdo, panelCentral, panelDerecho, panelInferior})
            raiz.add(p);

        raiz.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                distribuir(raiz.getWidth(), raiz.getHeight());
                raiz.repaint();
            }
        });

        distribuir(WINDOW_WIDTH, WINDOW_HEIGHT);
        add(raiz);
    }

    // Devuelve los bounds del botón cerrar en coordenadas del raiz
    private Rectangle btnBounds() {
        int x = panelMenuBar.getX() + panelMenuBar.getWidth() - BTN_ANCHO - 6;
        int y = panelMenuBar.getY() + (MENUBAR_ALTO - BTN_ALTO) / 2;
        return new Rectangle(x, y, BTN_ANCHO, BTN_ALTO);
    }

    private void pintarBotonCerrar(Graphics2D g2) {
        Rectangle r = btnBounds();
        if (btnHover) {
            g2.setColor(CLOSE_HOVER);
            g2.fillRect(r.x, r.y, r.width, r.height);
        }
        // Texto "✕" centrado
        g2.setColor(new java.awt.Color(180, 180, 180));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        String txt = "✕";
        int tx = r.x + (r.width  - fm.stringWidth(txt)) / 2;
        int ty = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(txt, tx, ty);
    }

    private JPanel crearMenuBar(JPanel raiz) {
        JPanel bar = new JPanel(null);
        bar.setOpaque(false);

        // Gestionar hover y click del botón desde el raiz
        raiz.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (btnBounds().contains(e.getPoint())) System.exit(0);
            }
            @Override public void mouseMoved(MouseEvent e) { }
        });
        raiz.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                boolean dentro = btnBounds().contains(e.getPoint());
                if (dentro != btnHover) {
                    btnHover = dentro;
                    raiz.repaint();
                }
            }
        });

        return bar;
    }

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
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(x, y, w - 1, h - 1);
    }

    private void distribuir(int W, int H) {
        int panelsY = MENUBAR_ALTO + DIVIDER;
        int areaH   = H - PANEL_INF_H - DIVIDER - panelsY;
        int cenX    = PANEL_IZQ_W + DIVIDER;
        int cenW    = W - PANEL_IZQ_W - PANEL_DER_W - DIVIDER * 2;

        panelMenuBar  .setBounds(0,               0,       W,           MENUBAR_ALTO);
        panelIzquierdo.setBounds(0,               panelsY, PANEL_IZQ_W, areaH);
        panelCentral  .setBounds(cenX,            panelsY, cenW,        areaH);
        panelDerecho  .setBounds(W - PANEL_DER_W, panelsY, PANEL_DER_W, areaH);
        panelInferior .setBounds(0,               panelsY + areaH + DIVIDER, W, PANEL_INF_H);
    }

    private void agregarArrastre(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
        });
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) { setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY); }
        });
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        SwingUtilities.invokeLater(() -> new Editor());
    }
}
