package org.motor2d.model;

import org.motor2d.model.components.TilemapLayer;

import java.util.LinkedHashMap;
import java.util.Map;

public class Tilemap {

    private int cols;
    private int rows;
    private int tileWidth;
    private int tileHeight;

    // Las 3 capas siempre existen, LinkedHashMap mantiene el orden
    private Map<TilemapLayer.LayerType, TilemapLayer> layers;

    public Tilemap() {
        this.cols = 20;
        this.rows = 10;
        this.tileWidth = 32;
        this.tileHeight = 32;
        initLayers();
    }

    public Tilemap(int cols, int rows, int tileWidth, int tileHeight) {
        this.cols = cols;
        this.rows = rows;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        initLayers();
    }

    // Crea las 3 capas siempre, vacías por defecto
    private void initLayers() {
        this.layers = new LinkedHashMap<>();
        layers.put(TilemapLayer.LayerType.BACKGROUND,
                new TilemapLayer(TilemapLayer.LayerType.BACKGROUND));
        layers.put(TilemapLayer.LayerType.MIDGROUND,
                new TilemapLayer(TilemapLayer.LayerType.MIDGROUND));
        layers.put(TilemapLayer.LayerType.FOREGROUND,
                new TilemapLayer(TilemapLayer.LayerType.FOREGROUND));
    }

    // Operaciones sobre capas
    public void placeTile(int col, int row, int tileId,
                          TilemapLayer.LayerType layer) {
        if (!isValidPosition(col, row)) return;
        layers.get(layer).placeTile(col, row, tileId);
    }

    // Si no se especifica capa, usa MIDGROUND por defecto
    // es la más común (suelo, plataformas)
    public void placeTile(int col, int row, int tileId) {
        placeTile(col, row, tileId, TilemapLayer.LayerType.MIDGROUND);
    }

    public void eraseTile(int col, int row, TilemapLayer.LayerType layer) {
        layers.get(layer).eraseTile(col, row);
    }

    public void eraseTile(int col, int row) {
        eraseTile(col, row, TilemapLayer.LayerType.MIDGROUND);
    }

    public Integer getTileIdAt(int col, int row,
                               TilemapLayer.LayerType layer) {
        return layers.get(layer).getTileIdAt(col, row);
    }

    public Integer getTileIdAt(int col, int row) {
        return getTileIdAt(col, row, TilemapLayer.LayerType.MIDGROUND);
    }

    public void clearLayer(TilemapLayer.LayerType layer) {
        layers.get(layer).clearAll();
    }

    public void clearAll() {
        for (TilemapLayer layer : layers.values()) layer.clearAll();
    }

    //Conversión pixel → celda
    public int pixelToCol(float pixelX) { return (int)(pixelX / tileWidth); }
    public int pixelToRow(float pixelY) { return (int)(pixelY / tileHeight); }

    public int getPixelWidth() { return cols * tileWidth; }
    public int getPixelHeight() { return rows * tileHeight; }

    // Getters y Setters
    public int getCols() { return cols; }
    public void setCols(int cols) { this.cols = cols; }

    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public int getTileWidth() { return tileWidth; }
    public void setTileWidth(int tileWidth) { this.tileWidth = tileWidth; }

    public int getTileHeight() { return tileHeight; }
    public void setTileHeight(int tileHeight) { this.tileHeight = tileHeight; }

    public Map<TilemapLayer.LayerType, TilemapLayer> getLayers() { return layers; }
    public void setLayers(Map<TilemapLayer.LayerType, TilemapLayer> layers) {
        this.layers = layers;
    }

    public TilemapLayer getLayer(TilemapLayer.LayerType type) {
        return layers.get(type);
    }

    // Privados
    private boolean isValidPosition(int col, int row) {
        return col >= 0 && col < cols && row >= 0 && row < rows;
    }
}