package org.motor2d.manager;

import org.motor2d.serialization.Serializer;
import org.motor2d.model.Scene;
import java.util.Stack;

/**
 * HistoryManager - Gestiona el historial de estados de la escena para Deshacer/Rehacer.
 */
public class HistoryManager {
    private final Stack<String> undoStack = new Stack<>();
    private final Stack<String> redoStack = new Stack<>();
    private final Serializer serializer;
    private static final int MAX_HISTORY = 50;

    public HistoryManager() {
        this.serializer = new Serializer();
    }

    /**
     * Guarda el estado actual de la escena en el historial.
     */
    public void pushState(Scene scene) {
        if (scene == null) return;
        try {
            String json = serializer.serializeScene(scene);
            
            // Si el nuevo estado es igual al último, no lo guardamos
            if (!undoStack.isEmpty() && undoStack.peek().equals(json)) {
                return;
            }

            undoStack.push(json);
            redoStack.clear(); // Al hacer una acción nueva, perdemos el futuro

            if (undoStack.size() > MAX_HISTORY) {
                undoStack.remove(0);
            }
        } catch (Exception e) {
            System.err.println("Error al guardar historial: " + e.getMessage());
        }
    }

    public Scene undo(Scene currentScene) {
        if (undoStack.size() <= 1) return null; // Necesitamos al menos 2 estados para volver a uno anterior

        try {
            // El estado actual está en el tope, lo pasamos a redo
            redoStack.push(undoStack.pop());
            
            // El nuevo estado actual es el que queda en el tope de undo
            String json = undoStack.peek();
            return serializer.deserializeScene(json);
        } catch (Exception e) {
            System.err.println("Error en Undo: " + e.getMessage());
            return null;
        }
    }

    public Scene redo() {
        if (redoStack.isEmpty()) return null;

        try {
            String json = redoStack.pop();
            undoStack.push(json);
            return serializer.deserializeScene(json);
        } catch (Exception e) {
            System.err.println("Error en Redo: " + e.getMessage());
            return null;
        }
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
