package org.motor2d.manager;

import org.motor2d.model.Tile;
import org.motor2d.model.Tilemap;
import org.motor2d.model.components.TilemapLayer;
import org.motor2d.model.Tileset;

import java.io.IOException;
import java.util.List;

public class TilesetManager {

    private final ProjectManager projectManager;
    private final SceneManager sceneManager;

    public TilesetManager(ProjectManager projectManager, SceneManager sceneManager) {
        this.projectManager = projectManager;
        this.sceneManager = sceneManager;
    }

    // TILESET — nivel proyecto

    public Tileset createTileset(String name, String imagePath,
                                 int tileWidth, int tileHeight) throws IOException {
        checkProjectOpen();

        if (projectManager.getCurrentProject().getTilesetByName(name) != null) {
            throw new IOException("Ya existe un tileset con el nombre: " + name);
        }

        Tileset tileset = new Tileset();
        tileset.setName(name);
        tileset.setImagePath(imagePath);
        tileset.setTileWidth(tileWidth);
        tileset.setTileHeight(tileHeight);

        projectManager.getCurrentProject().addTileset(tileset);
        projectManager.saveProject();

        return tileset;
    }

    public void deleteTileset(String name) throws IOException {
        checkProjectOpen();

        Tileset tileset = getTilesetByName(name);
        if (tileset == null) {
            throw new IOException("No se encontro el tileset: " + name);
        }

        projectManager.getCurrentProject().removeTileset(tileset);
        projectManager.saveProject();
    }

    public Tileset getTilesetByName(String name) throws IOException {
        checkProjectOpen();
        return projectManager.getCurrentProject().getTilesetByName(name);
    }

    public List<Tileset> getAllTilesets() throws IOException {
        checkProjectOpen();
        return projectManager.getCurrentProject().getTilesets();
    }

    // TILES — dentro de un tileset

    public Tile addTile(Tileset tileset, String tileName,
                        String spritePath, boolean solid) throws IOException {
        checkProjectOpen();

        Tile tile = new Tile();
        tile.setName(tileName);
        tile.setSpritePath(spritePath);
        tile.setSolid(solid);

        tileset.addTile(tile);
        projectManager.saveProject();

        return tile;
    }

    public void removeTile(Tileset tileset, Tile tile) throws IOException {
        checkProjectOpen();
        tileset.removeTile(tile);
        projectManager.saveProject();
    }

    public Tile getTileById(Tileset tileset, int id) {
        return tileset.getTileById(id);
    }

    // TILEMAP — nivel escena

    public Tilemap createTilemap(int cols, int rows,
                                 int tileWidth, int tileHeight) throws IOException {
        checkProjectOpen();
        checkSceneOpen();

        Tilemap tilemap = new Tilemap(cols, rows, tileWidth, tileHeight);

        sceneManager.getCurrentScene().setTilemap(tilemap);
        sceneManager.saveScene();

        return tilemap;
    }

    public void removeTilemap() throws IOException {
        checkSceneOpen();
        sceneManager.getCurrentScene().setTilemap(null);
        sceneManager.saveScene();
    }

    // PINTAR — con capa especifica

    public void paintTile(int col, int row, int tileId,
                          TilemapLayer.LayerType layer) throws IOException {
        checkSceneOpen();
        checkTilemapExists();
        sceneManager.getCurrentScene().getTilemap()
                .placeTile(col, row, tileId, layer);
        sceneManager.saveScene();
    }

    // PINTAR — en MIDGROUND por defecto

    public void paintTile(int col, int row, int tileId) throws IOException {
        paintTile(col, row, tileId, TilemapLayer.LayerType.MIDGROUND);
    }

    public void paintTileAtPixel(float pixelX, float pixelY, int tileId,
                                 TilemapLayer.LayerType layer) throws IOException {
        checkSceneOpen();
        checkTilemapExists();
        Tilemap tilemap = sceneManager.getCurrentScene().getTilemap();
        tilemap.placeTile(tilemap.pixelToCol(pixelX),
                tilemap.pixelToRow(pixelY), tileId, layer);
        sceneManager.saveScene();
    }

    public void paintTileAtPixel(float pixelX, float pixelY,
                                 int tileId) throws IOException {
        paintTileAtPixel(pixelX, pixelY, tileId, TilemapLayer.LayerType.MIDGROUND);
    }

    // BORRAR — con capa especifica

    public void eraseTile(int col, int row,
                          TilemapLayer.LayerType layer) throws IOException {
        checkSceneOpen();
        checkTilemapExists();
        sceneManager.getCurrentScene().getTilemap().eraseTile(col, row, layer);
        sceneManager.saveScene();
    }

    // BORRAR — en MIDGROUND por defecto

    public void eraseTile(int col, int row) throws IOException {
        eraseTile(col, row, TilemapLayer.LayerType.MIDGROUND);
    }

    public void eraseTileAtPixel(float pixelX, float pixelY,
                                 TilemapLayer.LayerType layer) throws IOException {
        checkSceneOpen();
        checkTilemapExists();
        Tilemap tilemap = sceneManager.getCurrentScene().getTilemap();
        tilemap.eraseTile(tilemap.pixelToCol(pixelX),
                tilemap.pixelToRow(pixelY), layer);
        sceneManager.saveScene();
    }

    public void eraseTileAtPixel(float pixelX, float pixelY) throws IOException {
        eraseTileAtPixel(pixelX, pixelY, TilemapLayer.LayerType.MIDGROUND);
    }

    public void resizeTilemap(int cols, int rows) throws IOException {
        checkSceneOpen();
        checkTilemapExists();
        sceneManager.getCurrentScene().getTilemap().resize(cols, rows);
        sceneManager.saveScene();
    }

    // CONSULTAR

    public Integer getTileIdAt(int col, int row,
                               TilemapLayer.LayerType layer) throws IOException {
        checkSceneOpen();
        checkTilemapExists();
        return sceneManager.getCurrentScene().getTilemap()
                .getTileIdAt(col, row, layer);
    }

    public Integer getTileIdAt(int col, int row) throws IOException {
        return getTileIdAt(col, row, TilemapLayer.LayerType.MIDGROUND);
    }

    public Tilemap getCurrentTilemap() throws IOException {
        checkSceneOpen();
        return sceneManager.getCurrentScene().getTilemap();
    }

    // LIMPIAR

    public void clearLayer(TilemapLayer.LayerType layer) throws IOException {
        checkSceneOpen();
        checkTilemapExists();
        sceneManager.getCurrentScene().getTilemap().clearLayer(layer);
        sceneManager.saveScene();
    }

    public void clearTilemap() throws IOException {
        checkSceneOpen();
        checkTilemapExists();
        sceneManager.getCurrentScene().getTilemap().clearAll();
        sceneManager.saveScene();
    }

    // Privados

    private void checkProjectOpen() throws IOException {
        if (!projectManager.isProjectOpen()) {
            throw new IOException("No hay ningun proyecto abierto");
        }
    }

    private void checkSceneOpen() throws IOException {
        if (!sceneManager.isSceneOpen()) {
            throw new IOException("No hay ninguna escena abierta");
        }
    }

    private void checkTilemapExists() throws IOException {
        if (sceneManager.getCurrentScene().getTilemap() == null) {
            throw new IOException("La escena no tiene tilemap, crealo primero");
        }
    }
}