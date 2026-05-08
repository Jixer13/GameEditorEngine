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

    public Tile getOrCreateTile(int col, int row) {
        int id = row * getCols() + col;
        Tile tile = getTileById(id);
        if (tile == null) {
            tile = new Tile();
            tile.setId(id);
            tile.setName("Tile_" + id);
            // Asumimos que el spritePath es la misma imagen del tileset
            // o se manejará mediante offsets en el futuro.
            tile.setSpritePath(this.imagePath); 
            addTile(tile);
        }
        return tile;
    }

    public Tile getTileById(int id) {
        for (Tile tile : tiles) {
            if (tile.getId() == id) return tile;
        }
        return null;
    }

    public int getCols() {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.File(imagePath));
            return img.getWidth() / tileWidth;
        } catch (Exception e) { return 0; }
    }

    public int getRows() {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.File(imagePath));
            return img.getHeight() / tileHeight;
        } catch (Exception e) { return 0; }
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