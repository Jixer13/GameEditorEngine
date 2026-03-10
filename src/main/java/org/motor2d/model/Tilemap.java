package org.motor2d.model;

import java.util.HashMap;
import java.util.Map;

public class Tilemap {

    private int cols;
    private int rows;
    private int tileWidth;
    private int tileHeight;
    // "col,row" → tileId  (solo guarda el id, la definición está en Tileset)
    private Map<String, Integer> tileGrid;

    public Tilemap() {
        this.cols = 20;
        this.rows = 10;
        this.tileWidth = 32;
        this.tileHeight = 32;
        this.tileGrid = new HashMap<>();
    }

    // --- Métodos para el editor ---

    public void placeTile(int col, int row, int tileId) {
        if (!isValidPosition(col, row)) return;
        tileGrid.put(buildKey(col, row), tileId);
    }

    public void eraseTile(int col, int row) {
        tileGrid.remove(buildKey(col, row));
    }

    public Integer getTileIdAt(int col, int row) {
        return tileGrid.get(buildKey(col, row)); // null si no hay tile
    }

    public boolean hasTileAt(int col, int row) {
        return tileGrid.containsKey(buildKey(col, row));
    }

    public void clearAll() { tileGrid.clear(); }

    // Píxel → celda (para cuando el editor envíe clicks)
    public int pixelToCol(float pixelX) { return (int)(pixelX / tileWidth); }
    public int pixelToRow(float pixelY) { return (int)(pixelY / tileHeight); }

    public int getPixelWidth() { return cols * tileWidth; }
    public int getPixelHeight() { return rows * tileHeight; }

    private String buildKey(int col, int row) { return col + "," + row; }
    private boolean isValidPosition(int col, int row) {
        return col >= 0 && col < cols && row >= 0 && row < rows;
    }

    public int getCols() { return cols; }
    public void setCols(int cols) { this.cols = cols; }

    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public int getTileWidth() { return tileWidth; }
    public void setTileWidth(int tileWidth) { this.tileWidth = tileWidth; }

    public int getTileHeight() { return tileHeight; }
    public void setTileHeight(int tileHeight) { this.tileHeight = tileHeight; }

    public Map<String, Integer> getTileGrid() { return tileGrid; }
    public void setTileGrid(Map<String, Integer> tileGrid) { this.tileGrid = tileGrid; }
}