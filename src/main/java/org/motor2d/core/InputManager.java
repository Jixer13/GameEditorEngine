package org.motor2d.core;

import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

/**
 * InputManager - Gestiona la entrada de teclado y ratón mediante polling.
 */
public class InputManager implements KeyListener, MouseListener, MouseMotionListener, FocusListener {

    // Estado de teclas y botones
    private static final boolean[] keys = new boolean[65536];
    private static final boolean[] keysLast = new boolean[65536];
    private static final boolean[] buttons = new boolean[10];
    private static final boolean[] buttonsLast = new boolean[10];

    // Posición del ratón
    private static float mouseX = 0;
    private static float mouseY = 0;

    /**
     * Resetea completamente el estado de todas las teclas y botones.
     */
    public static void clearState() {
        java.util.Arrays.fill(keys, false);
        java.util.Arrays.fill(keysLast, false);
        java.util.Arrays.fill(buttons, false);
        java.util.Arrays.fill(buttonsLast, false);
    }

    /**
     * Sincroniza el estado actual con el anterior. 
     * Debe llamarse al final de cada frame del GameLoop.
     */
    public static void update() {
        System.arraycopy(keys, 0, keysLast, 0, keys.length);
        System.arraycopy(buttons, 0, buttonsLast, 0, buttons.length);
    }

    // ==================== KEYBOARD API ====================

    public static boolean isKeyDown(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length) return false;
        return keys[keyCode];
    }

    public static boolean isKeyPressed(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length) return false;
        return keys[keyCode] && !keysLast[keyCode];
    }

    public static boolean isKeyUp(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length) return false;
        return !keys[keyCode] && keysLast[keyCode];
    }

    // ==================== MOUSE API ====================

    public static float getMouseX() { return mouseX; }
    public static float getMouseY() { return mouseY; }

    public static boolean isButtonDown(int button) {
        if (button < 0 || button >= buttons.length) return false;
        return buttons[button];
    }

    public static boolean isButtonPressed(int button) {
        if (button < 0 || button >= buttons.length) return false;
        return buttons[button] && !buttonsLast[button];
    }

    // ==================== LISTENERS (Swing) ====================

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() >= 0 && e.getKeyCode() < keys.length) {
            keys[e.getKeyCode()] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() >= 0 && e.getKeyCode() < keys.length) {
            keys[e.getKeyCode()] = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() >= 0 && e.getButton() < buttons.length) {
            buttons[e.getButton()] = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() >= 0 && e.getButton() < buttons.length) {
            buttons[e.getButton()] = false;
        }
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

    // ==================== FOCUS LISTENER ====================

    @Override
    public void focusGained(FocusEvent e) {
        clearState();
    }

    @Override
    public void focusLost(FocusEvent e) {
        // No hacer nada para evitar que el input se bloquee
    }
}
