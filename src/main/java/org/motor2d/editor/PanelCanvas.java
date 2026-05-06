package org.motor2d.editor;

import org.motor2d.core.Engine;
import org.motor2d.utilities.Color;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * PanelCanvas - El lienzo central del editor.
 * 
 * Este componente es el encargado de mostrar el renderizado del motor o previsualizar imágenes.
 * Soporta zoom centrado en el ratón y desplazamiento (pan).
 */
public class PanelCanvas extends JPanel {

    // ==================== CONSTANTES ====================
    private static final double ZOOM_MIN       = 0.05;
    private static final double ZOOM_MAX       = 32.0;
    private static final double ZOOM_STEP      = 0.12;
    private static final int    CHECKER_SIZE   = 12;

    // ==================== ESTADO DE VISTA ====================
    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    
    // Para previsualización de imágenes individuales
    private BufferedImage imagen       = null;
    private String        rutaImagen   = null;

    // Variables auxiliares para el arrastre (pan)
    private int dragStartX, dragStartY;
    private double offsetXAlIniciar, offsetYAlIniciar;
    private boolean arrastrando = false;

    // ==================== CONSTRUCTOR ====================
    public PanelCanvas() {
        setOpaque(true);
        setBackground(java.awt.Color.BLACK);
        setFocusable(true);
        
        registrarEventos();
    }

    // ==================== API DE PREVISUALIZACIÓN ====================

    /**
     * Carga y muestra una imagen en el canvas (modo visor).
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

    /** Limpia la imagen de previsualización y vuelve al modo motor. */
    public void limpiar() {
        imagen     = null;
        rutaImagen = null;
        repaint();
    }

    // ==================== GESTIÓN DE EVENTOS ====================
    private void registrarEventos() {
        // Zoom centrado en la posición del cursor
        addMouseWheelListener(e -> {
            double factor = (e.getWheelRotation() < 0)
                    ? (1.0 + ZOOM_STEP)
                    : (1.0 - ZOOM_STEP);

            double mouseX = e.getX();
            double mouseY = e.getY();
            double nuevoZoom = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, zoom * factor));
            double ratio    = nuevoZoom / zoom;

            offsetX = mouseX - ratio * (mouseX - offsetX);
            offsetY = mouseY - ratio * (mouseY - offsetY);
            zoom    = nuevoZoom;
            repaint();
        });

        // Control de inicio de arrastre y clics
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
                // Doble clic para restablecer la vista
                if (e.getClickCount() == 2) {
                    resetVista();
                    repaint();
                }
            }
        });

        // Movimiento de cámara (pan)
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
    public void resetVista() {
        if (imagen != null) {
            int margen = 20;
            double ratioW = (double)(getWidth()  - margen * 2) / imagen.getWidth();
            double ratioH = (double)(getHeight() - margen * 2) / imagen.getHeight();
            zoom = Math.min(ratioW, ratioH);
            if (zoom <= 0) zoom = 1.0;
            offsetX = (getWidth()  - imagen.getWidth()  * zoom) / 2.0;
            offsetY = (getHeight() - imagen.getHeight() * zoom) / 2.0;
        } else {
            zoom = 1.0;
            offsetX = 0;
            offsetY = 0;
        }
        repaint();
    }

    // ==================== PINTADO ====================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Configuración de calidad
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo del canvas
        g2.setColor(Color.CANVAS_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Aplicamos la transformación de cámara
        g2.translate(offsetX, offsetY);
        g2.scale(zoom, zoom);

        if (imagen != null) {
            // MODO VISOR: Dibujamos la imagen cargada
            pintarTablero(g2);
            g2.drawImage(imagen, 0, 0, null);
        } else {
            // MODO MOTOR: Delegamos en el Engine
            Engine.render(g2);
        }

        g2.dispose();
    }

    /** Tablero de transparencia para el modo visor. */
    private void pintarTablero(Graphics2D g2) {
        if (imagen == null) return;
        int imgW = imagen.getWidth();
        int imgH = imagen.getHeight();

        for (int cy = 0; cy < imgH; cy += CHECKER_SIZE) {
            for (int cx = 0; cx < imgW; cx += CHECKER_SIZE) {
                boolean par = ((cx / CHECKER_SIZE + cy / CHECKER_SIZE) % 2 == 0);
                g2.setColor(par ? new java.awt.Color(200, 200, 200)
                                : new java.awt.Color(255, 255, 255));
                g2.fillRect(cx, cy,
                        Math.min(CHECKER_SIZE, imgW - cx),
                        Math.min(CHECKER_SIZE, imgH - cy));
            }
        }
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        // Si no hay imagen, no reseteamos para no molestar al usuario mientras edita
        if (imagen != null) resetVista();
    }
}