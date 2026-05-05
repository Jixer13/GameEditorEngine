package org.motor2d.core;

import java.awt.event.*;

/**
 * InputManager - Gestor centralizado de entrada (Teclado y Ratón).
 * 
 * Permite consultar el estado de las teclas en cualquier momento sin depender
 * de los eventos asíncronos de Swing. Esto evita el retraso de repetición del SO.
 */
public class InputManager implements KeyListener, MouseListener, MouseMotionListener {

    // ==================== ATRIBUTOS ESTÁTICOS ====================
    private static final boolean[] keys = new boolean[1024];
    private static final boolean[] keysLast = new boolean[1024];

    private static final boolean[] mouseButtons = new boolean[10];
    private static final boolean[] mouseButtonsLast = new boolean[10];
    private static float mouseX, mouseY;

    // ==================== MÉTODOS DE CONSULTA ====================

    /**
     * Comprueba si una tecla está siendo pulsada actualmente.
     */
    public static boolean isKeyDown(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length) return false;
        return keys[keyCode];
    }

    /**
     * Comprueba si una tecla ha sido pulsada justo en este frame.
     * Útil para acciones que no deben repetirse (ej: abrir un menú o saltar).
     */
    public static boolean isKeyPressed(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length) return false;
        return keys[keyCode] && !keysLast[keyCode];
    }

    /**
     * Comprueba si un botón del ratón está pulsado.
     */
    public static boolean isMouseButtonDown(int button) {
        if (button < 0 || button >= mouseButtons.length) return false;
        return mouseButtons[button];
    }

    public static float getMouseX() { return mouseX; }
    public static float getMouseY() { return mouseY; }

    /**
     * Sincroniza los estados para detectar pulsaciones nuevas en el próximo frame.
     * Se llama al final del ciclo en el GameLoop.
     */
    public static void update() {
        System.arraycopy(keys, 0, keysLast, 0, keys.length);
        System.arraycopy(mouseButtons, 0, mouseButtonsLast, 0, mouseButtons.length);
    }

    // ==================== IMPLEMENTACIÓN DE EVENTOS ====================

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) keys[code] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) keys[code] = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        int button = e.getButton();
        if (button >= 0 && button < mouseButtons.length) mouseButtons[button] = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int button = e.getButton();
        if (button >= 0 && button < mouseButtons.length) mouseButtons[button] = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}