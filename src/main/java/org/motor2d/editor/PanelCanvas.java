package org.motor2d.editor;

import org.motor2d.core.Engine;
import org.motor2d.core.InputManager;
import org.motor2d.editor.helpers.EditorController;
import org.motor2d.graphics.Camara;
import org.motor2d.model.Entity;
import org.motor2d.model.Tilemap;
import org.motor2d.model.components.SpriteRenderer;
import org.motor2d.model.components.Transform;
import org.motor2d.model.components.TilemapLayer.LayerType;
import org.motor2d.utilities.Color;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * PanelCanvas - El lienzo central del editor.
 */
public class PanelCanvas extends JPanel {

    // ==================== CONSTANTES ====================
    private static final double ZOOM_MIN = 0.05;
    private static final double ZOOM_MAX = 32.0;
    private static final double ZOOM_STEP = 0.12;
    private static final int CHECKER_SIZE = 12;

    // ==================== ESTADO ====================
    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private BufferedImage imagen = null;
    private String rutaImagen = null;

    // Pintura
    private boolean modoPintura = false;
    private boolean modoBorrado = false;
    private int tileIdPincel = -1;

    // Arrastre y edición
    private int dragStartX, dragStartY;
    private double offsetXAlIniciar, offsetYAlIniciar;
    private boolean arrastrandoCamara = false;
    private EditorController controller;
    private Entity entidadSiendoArrastrada = null;
    private float lastMouseWorldX, lastMouseWorldY;
    private boolean arrastrandoEntidad = false;

    // ==================== CONSTRUCTOR ====================
    public PanelCanvas() {
        setOpaque(true);
        setBackground(java.awt.Color.BLACK);
        setFocusable(true);
        registrarEventos();
        
        // Registrar el InputManager del motor una sola vez
        InputManager input = new InputManager();
        addKeyListener(input);
        addMouseListener(input);
        addMouseMotionListener(input);
        addFocusListener(input);
    }

    public void init(EditorController controller) {
        this.controller = controller;
    }

    public void setModoSeleccion() {
        this.modoPintura = false;
        this.modoBorrado = false;
        this.tileIdPincel = -1;
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    public void setModoPintura(boolean activo, int tileId) {
        this.modoPintura = activo;
        this.modoBorrado = false;
        this.tileIdPincel = tileId;
        setCursor(activo ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getDefaultCursor());
    }

    public void setModoBorrado(boolean activo) {
        this.modoBorrado = activo;
        this.modoPintura = false;
        setCursor(activo ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

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

    public void limpiar() {
        imagen     = null;
        rutaImagen = null;
        repaint();
    }

    // ==================== GESTIÓN DE EVENTOS ====================
    private void registrarEventos() {
        addMouseWheelListener(e -> {
            if (Engine.isPlaying()) return;
            double factor = (e.getWheelRotation() < 0) ? (1.0 + ZOOM_STEP) : (1.0 - ZOOM_STEP);
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

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Solicitar foco al hacer clic para asegurar que el InputManager reciba eventos
                requestFocusInWindow();

                if (imagen != null) {
                    int w = 30, h = 30, bx = getWidth() - w - 10, by = 10;
                    if (e.getX() >= bx && e.getX() <= bx + w && e.getY() >= by && e.getY() <= by + h) {
                        limpiar();
                        return;
                    }
                }

                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (Engine.isPlaying()) return;
                    if (modoPintura && tileIdPincel != -1) {
                        LayerType capa = LayerType.MIDGROUND;
                        if (controller.getEditor() != null && controller.getEditor().getPanelAssets() != null) {
                            capa = controller.getEditor().getPanelAssets().getPanelPaleta().getCapaSeleccionada();
                        }
                        controller.registrarEstado(); // Guardar estado antes de pintar
                        controller.paintTileAtPixel(e.getX(), e.getY(), tileIdPincel, capa);
                        repaint();
                    } else if (modoBorrado) {
                        LayerType capa = LayerType.MIDGROUND;
                        if (controller.getEditor() != null && controller.getEditor().getPanelAssets() != null) {
                            capa = controller.getEditor().getPanelAssets().getPanelPaleta().getCapaSeleccionada();
                        }
                        controller.eraseTileAtPixel(e.getX(), e.getY(), capa);
                        repaint();
                    } else if (imagen == null && controller != null) {
                        float worldX = controller.screenToWorldX(e.getX());
                        float worldY = controller.screenToWorldY(e.getY());
                        Entity picked = controller.pickEntity(worldX, worldY);
                        if (picked != null) {
                            controller.registrarEstado(); // Guardar estado antes de mover
                            controller.setSelectedEntity(picked);
                            entidadSiendoArrastrada = picked;
                            lastMouseWorldX = worldX;
                            lastMouseWorldY = worldY;
                            arrastrandoEntidad = true;
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        } else {
                            controller.setSelectedEntity(null);
                        }
                    }
                } else if (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
                    dragStartX = e.getX(); dragStartY = e.getY();
                    offsetXAlIniciar = offsetX; offsetYAlIniciar = offsetY;
                    arrastrandoCamara = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if ((arrastrandoEntidad || modoPintura) && controller != null) {
                    controller.registrarEstado(); // Guardar estado final después de la acción
                    controller.saveProject();
                }
                arrastrandoCamara = false; arrastrandoEntidad = false;
                entidadSiendoArrastrada = null;
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (Engine.isPlaying()) return;
                if (e.getClickCount() == 2) { resetVista(); actualizarCamaraMotor(); repaint(); }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (imagen != null) limpiar();
                    else setModoPintura(false, -1);
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (modoPintura) {
                    repaint(); // Para actualizar el ghost tile
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (Engine.isPlaying()) return;
                if (arrastrandoCamara) {
                    offsetX = offsetXAlIniciar + (e.getX() - dragStartX);
                    offsetY = offsetYAlIniciar + (e.getY() - dragStartY);
                    actualizarCamaraMotor();
                    repaint();
                } else if (arrastrandoEntidad && entidadSiendoArrastrada != null) {
                    float worldX = controller.screenToWorldX(e.getX());
                    float worldY = controller.screenToWorldY(e.getY());
                    Transform t = entidadSiendoArrastrada.getComponent(Transform.class);
                    if (t != null) {
                        t.setX(t.getX() + (worldX - lastMouseWorldX));
                        t.setY(t.getY() + (worldY - lastMouseWorldY));
                    }
                    lastMouseWorldX = worldX; lastMouseWorldY = worldY;
                    repaint();
                } else if (modoBorrado && SwingUtilities.isLeftMouseButton(e)) {
                    LayerType capa = LayerType.MIDGROUND;
                    if (controller.getEditor() != null && controller.getEditor().getPanelAssets() != null) {
                        capa = controller.getEditor().getPanelAssets().getPanelPaleta().getCapaSeleccionada();
                    }
                    controller.eraseTileAtPixel(e.getX(), e.getY(), capa);
                    repaint();
                } else if (modoPintura && tileIdPincel != -1 && SwingUtilities.isLeftMouseButton(e)) {
                    LayerType capa = LayerType.MIDGROUND;
                    if (controller.getEditor() != null && controller.getEditor().getPanelAssets() != null) {
                        capa = controller.getEditor().getPanelAssets().getPanelPaleta().getCapaSeleccionada();
                    }
                    controller.paintTileAtPixel(e.getX(), e.getY(), tileIdPincel, capa);
                    repaint();
                }
            }
        });
    }

    private void actualizarCamaraMotor() {
        if (controller == null || Engine.isPlaying()) return;
        Camara cam = Engine.getCamara();
        if (cam != null) {
            cam.setZoom((float) zoom);
            cam.getPosition().x = (float) (-offsetX / zoom);
            cam.getPosition().y = (float) (-offsetY / zoom);
            cam.setViewWidth(getWidth());
            cam.setViewHeight(getHeight());
        }
    }

    public void resetVista() {
        if (imagen != null) {
            int margen = 20;
            double ratioW = (double)(getWidth() - margen * 2) / imagen.getWidth();
            double ratioH = (double)(getHeight() - margen * 2) / imagen.getHeight();
            zoom = Math.min(ratioW, ratioH);
            if (zoom <= 0) zoom = 1.0;
            offsetX = (getWidth() - imagen.getWidth() * zoom) / 2.0;
            offsetY = (getHeight() - imagen.getHeight() * zoom) / 2.0;
        } else {
            zoom = 1.0; offsetX = 0; offsetY = 0;
            actualizarCamaraMotor();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setColor(Color.CANVAS_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (imagen != null) {
            // Fondo gris neutro para el visor
            g2.setColor(new java.awt.Color(60, 60, 60));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.translate(offsetX, offsetY);
            g2.scale(zoom, zoom);
            g2.drawImage(imagen, 0, 0, null);

            g2.setTransform(new java.awt.geom.AffineTransform());
            pintarBotonCerrarVisor(g2);
        } else {
            actualizarCamaraMotor();
            Engine.render(g2);
            if (!Engine.isPlaying()) {
                dibujarRejilla(g2);
                dibujarSeleccion(g2);
                if (modoPintura) {
                    dibujarGhostTile(g2);
                }
            }
        }
        g2.dispose();
    }

    private void dibujarGhostTile(Graphics2D g2) {
        Point mousePos = getMousePosition();
        if (mousePos == null) return;

        try {
            Tilemap tilemap = controller.getSceneManager().getCurrentScene().getTilemap();
            if (tilemap == null) return;

            float worldX = controller.screenToWorldX(mousePos.x);
            float worldY = controller.screenToWorldY(mousePos.y);

            int col = tilemap.pixelToCol(worldX);
            int row = tilemap.pixelToRow(worldY);

            float snapWorldX = col * tilemap.getTileWidth();
            float snapWorldY = row * tilemap.getTileHeight();

            Camara cam = Engine.getCamara();
            float screenX = cam.worldToScreenX(snapWorldX);
            float screenY = cam.worldToScreenY(snapWorldY);
            float sw = tilemap.getTileWidth() * cam.getZoom();
            float sh = tilemap.getTileHeight() * cam.getZoom();

            // Dibujar rectángulo semi-transparente
            g2.setColor(new java.awt.Color(255, 255, 0, 100));
            g2.fillRect((int)screenX, (int)screenY, (int)sw, (int)sh);
            g2.setColor(java.awt.Color.YELLOW);
            g2.drawRect((int)screenX, (int)screenY, (int)sw, (int)sh);
            
        } catch (Exception e) {}
    }

    private void dibujarRejilla(Graphics2D g2) {
        Camara cam = Engine.getCamara();
        if (cam == null) return;

        float z = cam.getZoom();
        float gridSpacing = 32 * z;
        
        // Evitar dibujar rejilla si es demasiado densa
        if (gridSpacing < 4) return;

        int w = getWidth();
        int h = getHeight();

        float startX = (float) (cam.getPosition().x * z % gridSpacing);
        float startY = (float) (cam.getPosition().y * z % gridSpacing);

        // Líneas normales: Gris claro muy sutil
        g2.setColor(new java.awt.Color(200, 200, 200, 40));
        g2.setStroke(new BasicStroke(1.0f));

        for (float x = -startX; x < w; x += gridSpacing) {
            g2.drawLine((int)x, 0, (int)x, h);
        }
        for (float y = -startY; y < h; y += gridSpacing) {
            g2.drawLine(0, (int)y, w, (int)y);
        }
        
        // Ejes principales: Un poco más visibles
        g2.setColor(new java.awt.Color(200, 200, 200, 100));
        float axisX = cam.worldToScreenX(0);
        float axisY = cam.worldToScreenY(0);
        
        if (axisX >= 0 && axisX <= w) g2.drawLine((int)axisX, 0, (int)axisX, h);
        if (axisY >= 0 && axisY <= h) g2.drawLine(0, (int)axisY, w, (int)axisY);
    }

    private void pintarBotonCerrarVisor(Graphics2D g2) {
        int w = 30, h = 30, x = getWidth() - w - 10, y = 10;
        g2.setColor(new java.awt.Color(200, 50, 50, 180));
        g2.fillRoundRect(x, y, w, h, 8, 8);
        g2.setColor(java.awt.Color.WHITE);
        g2.setStroke(new BasicStroke(2.0f));
        int p = 8;
        g2.drawLine(x+p, y+p, x+w-p, y+h-p);
        g2.drawLine(x+w-p, y+p, x+p, y+h-p);
    }

    private void dibujarSeleccion(Graphics2D g2) {
        if (controller == null) return;
        Entity selected = controller.getSelectedEntity();
        if (selected == null || !selected.isActive()) return;
        Transform t = selected.getComponent(Transform.class);
        SpriteRenderer s = selected.getComponent(SpriteRenderer.class);
        if (t != null && s != null) {
            Camara cam = Engine.getCamara();
            float x = cam.worldToScreenX(t.getX()), y = cam.worldToScreenY(t.getY());
            float w = s.getFrameWidth() * t.getScaleX() * cam.getZoom();
            float h = s.getFrameHeight() * t.getScaleY() * cam.getZoom();
            g2.setColor(java.awt.Color.WHITE);
            g2.drawRect((int)x-2, (int)y-2, (int)w+4, (int)h+4);
        }
    }
}
