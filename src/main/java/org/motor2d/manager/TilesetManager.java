package org.motor2d.manager;

import org.motor2d.model.Tile;
import org.motor2d.model.Tilemap;
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

    // nivel proyecto
    public Tileset createTileset(String name, String imagePath,
                                 int tileWidth, int tileHeight) throws IOException {
        checkProjectOpen();

        // No permitir nombres duplicados
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
            throw new IOException("No se encontró el tileset: " + name);
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

    // dentro de un tileset
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


    //nivel escena
    // Inicializa el tilemap de la escena actual
    public Tilemap createTilemap(int cols, int rows,
                                 int tileWidth, int tileHeight) throws IOException {
        checkProjectOpen();
        checkSceneOpen();

        Tilemap tilemap = new Tilemap();
        tilemap.setCols(cols);
        tilemap.setRows(rows);
        tilemap.setTileWidth(tileWidth);
        tilemap.setTileHeight(tileHeight);

        sceneManager.getCurrentScene().setTilemap(tilemap);
        sceneManager.saveScene();

        return tilemap;
    }

    // Elimina el tilemap de la escena actual
    public void removeTilemap() throws IOException {
        checkSceneOpen();
        sceneManager.getCurrentScene().setTilemap(null);
        sceneManager.saveScene();
    }

    // Pintar un tile en una posición de la cuadrícula
    public void paintTile(int col, int row, int tileId) throws IOException {
        checkSceneOpen();
        checkTilemapExists();
        sceneManager.getCurrentScene().getTilemap().placeTile(col, row, tileId);
        sceneManager.saveScene();
    }

    // Pintar usando coordenadas en píxeles (viene del click del editor)
    public void paintTileAtPixel(float pixelX, float pixelY,
                                 int tileId) throws IOException {
        checkSceneOpen();
        checkTilemapExists();

        Tilemap tilemap = sceneManager.getCurrentScene().getTilemap();
        int col = tilemap.pixelToCol(pixelX);
        int row = tilemap.pixelToRow(pixelY);

        tilemap.placeTile(col, row, tileId);
        sceneManager.saveScene();
    }

    // Borrar tile en una posición
    public void eraseTile(int col, int row) throws IOException {
        checkSceneOpen();
        checkTilemapExists();
        sceneManager.getCurrentScene().getTilemap().eraseTile(col, row);
        sceneManager.saveScene();
    }

    // Borrar tile usando coordenadas en píxeles
    public void eraseTileAtPixel(float pixelX, float pixelY) throws IOException {
        checkSceneOpen();
        checkTilemapExists();

        Tilemap tilemap = sceneManager.getCurrentScene().getTilemap();
        int col = tilemap.pixelToCol(pixelX);
        int row = tilemap.pixelToRow(pixelY);

        tilemap.eraseTile(col, row);
        sceneManager.saveScene();
    }

    // Obtener el id del tile en una posición
    public Integer getTileIdAt(int col, int row) throws IOException {
        checkSceneOpen();
        checkTilemapExists();
        return sceneManager.getCurrentScene().getTilemap().getTileIdAt(col, row);
    }

    // Limpiar todo el tilemap
    public void clearTilemap() throws IOException {
        checkSceneOpen();
        checkTilemapExists();
        sceneManager.getCurrentScene().getTilemap().clearAll();
        sceneManager.saveScene();
    }

    // Obtener el tilemap de la escena actual
    public Tilemap getCurrentTilemap() throws IOException {
        checkSceneOpen();
        return sceneManager.getCurrentScene().getTilemap();
    }

    // Métodos privados de apoyo
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