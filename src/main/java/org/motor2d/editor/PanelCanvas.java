package org.motor2d.editor;

import org.motor2d.core.Engine;
import org.motor2d.graphics.Camara;
import org.motor2d.model.Entity;
import org.motor2d.model.components.SpriteRenderer;
import org.motor2d.model.components.Transform;
import org.motor2d.utilities.Color;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
    private static final double ZOOM_MIN = 0.05;
    private static final double ZOOM_MAX = 32.0;
    private static final double ZOOM_STEP = 0.12;
    private static final int CHECKER_SIZE = 12;

    // ==================== ESTADO DE VISTA ====================
    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    
    // Para previsualización de imágenes individuales
    private BufferedImage imagen = null;
    private String rutaImagen = null;

    // Variables auxiliares para el arrastre (pan)
    private int dragStartX, dragStartY;
    private double offsetXAlIniciar, offsetYAlIniciar;
    private boolean arrastrandoCamara = false;

    // Variables para arrastre de entidades
    private EditorController controller;
    private org.motor2d.model.Entity entidadSiendoArrastrada = null;
    private float lastMouseWorldX, lastMouseWorldY;
    private boolean arrastrandoEntidad = false;

    // ==================== CONSTRUCTOR ====================
    public PanelCanvas() {
        setOpaque(true);
        setBackground(java.awt.Color.BLACK);
        setFocusable(true);
        
        registrarEventos();
    }

    public void init(EditorController controller) {
        this.controller = controller;
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
            
            actualizarCamaraMotor();
            repaint();
        });

        // Control de inicio de arrastre y clics
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (imagen == null && controller != null) {
                        float worldX = controller.screenToWorldX(e.getX());
                        float worldY = controller.screenToWorldY(e.getY());
                        
                        org.motor2d.model.Entity picked = controller.pickEntity(worldX, worldY);
                        if (picked != null) {
                            controller.setSelectedEntity(picked);
                            entidadSiendoArrastrada = picked;
                            lastMouseWorldX = worldX;
                            lastMouseWorldY = worldY;
                            arrastrandoEntidad = true;
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        } else {
                            // Si hacemos clic en el vacío, deseleccionamos
                            controller.setSelectedEntity(null);
                        }
                    }
                } else if (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
                    dragStartX         = e.getX();
                    dragStartY         = e.getY();
                    offsetXAlIniciar   = offsetX;
                    offsetYAlIniciar   = offsetY;
                    arrastrandoCamara  = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (arrastrandoEntidad) {
                    if (controller != null) controller.saveProject(); // Guardar tras mover
                }
                arrastrandoCamara  = false;
                arrastrandoEntidad = false;
                entidadSiendoArrastrada = null;
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Doble clic para restablecer la vista
                if (e.getClickCount() == 2) {
                    resetVista();
                    actualizarCamaraMotor();
                    repaint();
                }
            }
        });

        // Movimiento (pan o drag entidad)
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (arrastrandoCamara) {
                    offsetX = offsetXAlIniciar + (e.getX() - dragStartX);
                    offsetY = offsetYAlIniciar + (e.getY() - dragStartY);
                    actualizarCamaraMotor();
                    repaint();
                } else if (arrastrandoEntidad && entidadSiendoArrastrada != null && controller != null) {
                    float worldX = controller.screenToWorldX(e.getX());
                    float worldY = controller.screenToWorldY(e.getY());
                    
                    float dx = worldX - lastMouseWorldX;
                    float dy = worldY - lastMouseWorldY;
                    
                    org.motor2d.model.components.Transform t = entidadSiendoArrastrada.getComponent(org.motor2d.model.components.Transform.class);
                    if (t != null) {
                        t.setX(t.getX() + dx);
                        t.setY(t.getY() + dy);
                    }
                    
                    lastMouseWorldX = worldX;
                    lastMouseWorldY = worldY;
                    repaint();
                }
            }
        });
    }

    private void actualizarCamaraMotor() {
        if (controller == null) return;
        Camara cam = Engine.getCamara();
        if (cam != null) {
            cam.setZoom((float) zoom);
            cam.getPosition().x = (float) (-offsetX / zoom);
            cam.getPosition().y = (float) (-offsetY / zoom);
            cam.setViewWidth(getWidth());
            cam.setViewHeight(getHeight());
        }
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
            actualizarCamaraMotor();
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

        if (imagen != null) {
            // MODO VISOR: Dibujamos la imagen cargada con transformación local
            g2.translate(offsetX, offsetY);
            g2.scale(zoom, zoom);
            pintarTablero(g2);
            g2.drawImage(imagen, 0, 0, null);
        } else {
            // MODO MOTOR: Delegamos en el Engine
            // NO aplicamos translate/scale aquí porque el Renderer ya usa la cámara
            actualizarCamaraMotor(); // Asegurar sincronización
            Engine.render(g2);
            
            // Dibujar feedback de selección si hay una entidad seleccionada
            dibujarSeleccion(g2);
        }

        g2.dispose();
    }

    private void dibujarSeleccion(Graphics2D g2) {
        if (controller == null) return;
        Entity selected = controller.getSelectedEntity();
        if (selected == null || !selected.isActive()) return;
        
        Transform t = selected.getComponent(Transform.class);
        SpriteRenderer s = selected.getComponent(SpriteRenderer.class);
        
        if (t != null && s != null) {
            Camara cam = Engine.getCamara();
            float x = cam.worldToScreenX(t.getX());
            float y = cam.worldToScreenY(t.getY());
            float w = s.getFrameWidth() * t.getScaleX() * cam.getZoom();
            float h = s.getFrameHeight() * t.getScaleY() * cam.getZoom();
            
            g2.setColor(java.awt.Color.WHITE);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRect((int)x - 2, (int)y - 2, (int)w + 4, (int)h + 4);
            
            // Pequeños manejadores en las esquinas
            g2.fillRect((int)x - 4, (int)y - 4, 4, 4);
            g2.fillRect((int)(x + w), (int)y - 4, 4, 4);
            g2.fillRect((int)x - 4, (int)(y + h), 4, 4);
            g2.fillRect((int)(x + w), (int)(y + h), 4, 4);
        }
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