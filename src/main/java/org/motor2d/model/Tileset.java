package org.motor2d.model;

import java.util.ArrayList;
import java.util.List;

public class Tileset {

    private String name;
    private String imagePath;   // la imagen con todos los tiles juntos
    private int tileWidth;
    private int tileHeight;
    private List<Tile> tiles;

    public Tileset() {
        this.name = "Tileset";
        this.imagePath = "";
        this.tileWidth = 32;
        this.tileHeight = 32;
        this.tiles = new ArrayList<>();
    }

    // Buscar definición de tile por id
    public Tile getTileById(int id) {
        for (Tile tile : tiles) {
            if (tile.getId() == id) return tile;
        }
        return null;
    }

    public void addTile(Tile tile) { this.tiles.add(tile); }
    public void removeTile(Tile tile) { this.tiles.remove(tile); }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getTileWidth() { return tileWidth; }
    public void setTileWidth(int tileWidth) { this.tileWidth = tileWidth; }

    public int getTileHeight() { return tileHeight; }
    public void setTileHeight(int tileHeight) { this.tileHeight = tileHeight; }

    public List<Tile> getTiles() { return tiles; }
    public void setTiles(List<Tile> tiles) { this.tiles = tiles; }
}