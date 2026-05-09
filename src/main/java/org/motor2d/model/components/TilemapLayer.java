package org.motor2d.model.components;

import java.util.HashMap;
import java.util.Map;

public class TilemapLayer {

    // Capas fijas con propósito claro
    public enum LayerType {
        BACKGROUND,  // fondo: cielo, agua, montañas
        MIDGROUND,   // suelo: plataformas, paredes
        FOREGROUND   // decoración encima del jugador: ramas, niebla
    }

    private LayerType type;
    private boolean visible;
    private float opacity;
    private Map<String, Integer> tileGrid;

    public TilemapLayer() {
        this.type = LayerType.BACKGROUND;
        this.visible = true;
        this.opacity = 1.0f;
        this.tileGrid = new HashMap<>();
    }

    public TilemapLayer(LayerType type) {
        this.type = type;
        this.visible = true;
        this.opacity = 1.0f;
        this.tileGrid = new HashMap<>();
    }

    public boolean isEmpty() {
        return tileGrid.isEmpty();
    }

    // Operaciones sobre la cuadrícula
    public void placeTile(int col, int row, int tileId) {
        // Al colocar un tile, limpiamos cualquier dato previo en esa posición
        // para garantizar que la celda solo contenga un tile a la vez.
        tileGrid.remove(buildKey(col, row));
        tileGrid.put(buildKey(col, row), tileId);
    }

    public void eraseTile(int col, int row) {
        tileGrid.remove(buildKey(col, row));
    }

    public Integer getTileIdAt(int col, int row) {
        return tileGrid.get(buildKey(col, row));
    }

    public boolean hasTileAt(int col, int row) {
        return tileGrid.containsKey(buildKey(col, row));
    }

    public void clearAll() {
        tileGrid.clear();
    }

    private String buildKey(int col, int row) {
        return col + "," + row;
    }

    // Getters y Setters
    public LayerType getType() { return type; }
    public void setType(LayerType type) { this.type = type; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public float getOpacity() { return opacity; }
    public void setOpacity(float opacity) { this.opacity = opacity; }

    public Map<String, Integer> getTileGrid() { return tileGrid; }
    public void setTileGrid(Map<String, Integer> tileGrid) { this.tileGrid = tileGrid; }
}