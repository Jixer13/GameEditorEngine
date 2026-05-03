package org.motor2d.editor;

import org.motor2d.core.Engine;
import org.motor2d.utilities.Color;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * PanelCanvas - El lienzo central del editor.
 * 
 * Este componente es el encargado de mostrar el renderizado del motor.
 * Implementa funcionalidades de cámara básicas como zoom y desplazamiento (pan).
 */
public class PanelCanvas extends JPanel {

    // ==================== ATRIBUTOS DE VISTA ====================
    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    
    // Variables auxiliares para el arrastre (pan)
    private int dragStartX, dragStartY;
    private double offsetXAlIniciar, offsetYAlIniciar;
    private boolean arrastrando = false;

    // ==================== CONSTRUCTOR ====================
    public PanelCanvas() {
        setOpaque(true);
        setBackground(java.awt.Color.BLACK); // Fondo por defecto
        setFocusable(true);
        
        registrarEventos();
    }

    // ==================== GESTIÓN DE EVENTOS ====================
    /**
     * Registra los listeners para el control de la cámara mediante el ratón.
     */
    private void registrarEventos() {
        // Zoom: Controlado por la rueda del ratón
        addMouseWheelListener(e -> {
            double factor = (e.getWheelRotation() < 0) ? 1.1 : 0.9;
            zoom *= factor;
            
            // Limitación de zoom para mantener la estabilidad visual
            zoom = Math.max(0.1, Math.min(zoom, 10.0));
            repaint();
        });

        // Arrastre (Pan): Inicia al pulsar el botón izquierdo
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragStartX = e.getX();
                    dragStartY = e.getY();
                    offsetXAlIniciar = offsetX;
                    offsetYAlIniciar = offsetY;
                    arrastrando = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                arrastrando = false;
                setCursor(Cursor.getDefaultCursor());
            }
        });

        // Arrastre (Pan): Calcula el desplazamiento mientras se mueve el ratón
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

    // ==================== RENDERIZADO ====================
    /**
     * Sobrescribe el método de dibujado de Swing para integrar el motor.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        
        // Mejoramos la calidad del escalado para el zoom
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Aplicamos la transformación de cámara del editor
        // Esto permite que el usuario se mueva por el nivel libremente
        g2.translate(offsetX, offsetY);
        g2.scale(zoom, zoom);

        // Delegamos el renderizado de los elementos del juego al motor
        Engine.render(g2);
    }
    
    // ==================== MÉTODOS DE APOYO ====================
    /**
     * Restablece la cámara a la posición original.
     */
    public void resetVista() {
        zoom = 1.0;
        offsetX = 0;
        offsetY = 0;
        repaint();
    }
}
