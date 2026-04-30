package org.motor2d.editor;

import org.motor2d.utilities.Color;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Panel central del editor (Canvas / Visor).
 * <p>
 * Funcionalidades:
 * - Muestra la imagen seleccionada en el panel de assets.
 * - Zoom con la rueda del ratón (Ctrl + scroll o scroll directo).
 * - Pan (arrastrar) con clic izquierdo.
 * - Doble clic para resetear zoom y posición.
 * - Indicador de zoom en la esquina inferior derecha.
 * - Fondo de tablero de ajedrez (transparencia).
 */
public class PanelCanvas extends JPanel {

    // ==================== CONSTANTES ====================
    private static final double ZOOM_MIN       = 0.05;
    private static final double ZOOM_MAX       = 32.0;
    private static final double ZOOM_STEP      = 0.12;
    private static final int    CHECKER_SIZE   = 12;

    // ==================== ESTADO ====================
    private BufferedImage imagen       = null;
    private String        rutaImagen   = null;

    private double  zoom   = 1.0;
    private double  offsetX = 0;
    private double  offsetY = 0;

    private int  dragStartX, dragStartY;
    private double offsetXAlIniciar, offsetYAlIniciar;
    private boolean arrastrando = false;

    // ==================== CONSTRUCTOR ====================
    public PanelCanvas() {
        setOpaque(false);
        setLayout(null);
        registrarEventos();
    }

    // ==================== API PÚBLICA ====================

    /**
     * Carga y muestra una imagen en el canvas.
     * Llamado desde PanelAssets al seleccionar un archivo.
     */
    public void mostrarImagen(File archivo) {
        try {
            imagen     = ImageIO.read(archivo);
            rutaImagen = archivo.getName();
            resetVista();
            repaint();
        } catch (Exception e) {
            imagen     = null;
            rutaImagen = "Error: " + e.getMessage();
            repaint();
        }
    }

    /** Limpia el canvas. */
    public void limpiar() {
        imagen     = null;
        rutaImagen = null;
        repaint();
    }

    // ==================== EVENTOS ====================
    private void registrarEventos() {
        // Zoom con la rueda
        addMouseWheelListener(e -> {
            double factor = (e.getWheelRotation() < 0)
                    ? (1.0 + ZOOM_STEP)
                    : (1.0 - ZOOM_STEP);

            // Zoom centrado en el cursor
            double mouseX = e.getX();
            double mouseY = e.getY();
            double nuevoZoom = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, zoom * factor));
            double ratio    = nuevoZoom / zoom;

            offsetX = mouseX - ratio * (mouseX - offsetX);
            offsetY = mouseY - ratio * (mouseY - offsetY);
            zoom    = nuevoZoom;
            repaint();
        });

        // Inicio del arrastre
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragStartX         = e.getX();
                    dragStartY         = e.getY();
                    offsetXAlIniciar   = offsetX;
                    offsetYAlIniciar   = offsetY;
                    arrastrando        = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                arrastrando = false;
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Doble clic → reset
                if (e.getClickCount() == 2) {
                    resetVista();
                    repaint();
                }
            }
        });

        // Pan mientras se arrastra
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (arrastrando) {
                    offsetX = offsetXAlIniciar + (e.getX() - dragStartX);
                    offsetY = offsetYAlIniciar + (e.getY() - dragStartY);
                    repaint();
                }
            }
        });
    }

    // ==================== LÓGICA DE VISTA ====================
    private void resetVista() {
        if (imagen == null) {
            zoom = 1.0; offsetX = 0; offsetY = 0;
            return;
        }
        // Ajustar al panel con un pequeño margen
        int margen = 20;
        double ratioW = (double)(getWidth()  - margen * 2) / imagen.getWidth();
        double ratioH = (double)(getHeight() - margen * 2) / imagen.getHeight();
        zoom = Math.min(ratioW, ratioH);
        if (zoom <= 0) zoom = 1.0;

        offsetX = (getWidth()  - imagen.getWidth()  * zoom) / 2.0;
        offsetY = (getHeight() - imagen.getHeight() * zoom) / 2.0;
    }

    // ==================== PINTADO ====================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo propio del canvas
        g2.setColor(Color.CANVAS_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (imagen != null) {
            pintarTablero(g2);
            pintarImagen(g2);
            pintarZoomIndicador(g2);
        } else {
            pintarPlaceholder(g2);
        }

        g2.dispose();
    }

    /** Tablero de transparencia detrás de la imagen. */
    private void pintarTablero(Graphics2D g2) {
        int imgW = (int)(imagen.getWidth()  * zoom);
        int imgH = (int)(imagen.getHeight() * zoom);
        int x0   = (int) offsetX;
        int y0   = (int) offsetY;

        for (int cy = 0; cy < imgH; cy += CHECKER_SIZE) {
            for (int cx = 0; cx < imgW; cx += CHECKER_SIZE) {
                boolean par = ((cx / CHECKER_SIZE + cy / CHECKER_SIZE) % 2 == 0);
                g2.setColor(par ? new java.awt.Color(200, 200, 200)
                                : new java.awt.Color(255, 255, 255));
                g2.fillRect(x0 + cx, y0 + cy,
                        Math.min(CHECKER_SIZE, imgW - cx),
                        Math.min(CHECKER_SIZE, imgH - cy));
            }
        }
    }

    private void pintarImagen(Graphics2D g2) {
        AffineTransform at = new AffineTransform();
        at.translate(offsetX, offsetY);
        at.scale(zoom, zoom);
        g2.drawImage(imagen, at, null);

        // Borde alrededor de la imagen
        g2.setColor(Color.BORDER_COLOR);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect((int) offsetX, (int) offsetY,
                (int)(imagen.getWidth()  * zoom),
                (int)(imagen.getHeight() * zoom));
    }

    private void pintarZoomIndicador(Graphics2D g2) {
        String txt = String.format("%.0f%%  %s  %dx%d",
                zoom * 100, rutaImagen, imagen.getWidth(), imagen.getHeight());

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(txt) + 12;
        int th = fm.getHeight() + 6;
        int tx = getWidth()  - tw - 8;
        int ty = getHeight() - th - 8;

        g2.setColor(new java.awt.Color(0, 0, 0, 140));
        g2.fillRoundRect(tx, ty, tw, th, 6, 6);
        g2.setColor(java.awt.Color.WHITE);
        g2.drawString(txt, tx + 6, ty + fm.getAscent() + 3);
    }

    private void pintarPlaceholder(Graphics2D g2) {
        g2.setColor(new java.awt.Color(80, 80, 80, 120));
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        String msg = "Selecciona una imagen en Assets";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg,
                (getWidth()  - fm.stringWidth(msg)) / 2,
                (getHeight() + fm.getAscent()) / 2);
    }

    // ==================== OVERRIDE PARA RESET ON RESIZE ====================
    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        if (imagen != null) resetVista();
    }
}
