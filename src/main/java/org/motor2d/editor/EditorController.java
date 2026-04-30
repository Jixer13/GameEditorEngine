package org.motor2d.editor;

import org.motor2d.graphics.Camara;
//import org.motor2d.graphics.Renderer;
import org.motor2d.graphics.Sprite;
import org.motor2d.manager.*;
import org.motor2d.model.Entity;
import org.motor2d.model.Scene;
import org.motor2d.model.Tile;
import org.motor2d.model.Tilemap;
import org.motor2d.model.components.TilemapLayer;
import org.motor2d.model.Tileset;
import org.motor2d.model.components.Component;
import org.motor2d.model.ui.UIElement;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class EditorController {

    private final ProjectManager projectManager;
    private final SceneManager sceneManager;
    private final EntityManager entityManager;
    private final TilesetManager tilesetManager;
    private final ResourceManager resourceManager;

   // private Renderer renderer;
    private Camara camara;

    // Para previsualización de audio en el editor
    private Clip audioClip;

    public EditorController() {
        this.projectManager  = new ProjectManager();
        this.sceneManager    = new SceneManager(projectManager);
        this.entityManager   = new EntityManager(sceneManager);
        this.tilesetManager  = new TilesetManager(projectManager, sceneManager);
        this.resourceManager = new ResourceManager(projectManager);
    }

    // PROYECTO

    public boolean createProject(String name, String path) {
        try {
            projectManager.createProject(name, path);
            sceneManager.loadScene("main");
            initGraphics(path);
            return true;
        } catch (Exception e) {
            mostrarError("Error al crear el proyecto", e.getMessage());
            return false;
        }
    }

    public boolean openProject(String path) {
        try {
            projectManager.openProject(path);
            String initialScene = projectManager.getCurrentProject()
                    .getInitialScene()
                    .replace("scenes/", "")
                    .replace(".json", "");
            sceneManager.loadScene(initialScene);
            initGraphics(path);
            return true;
        } catch (Exception e) {
            mostrarError("Error al abrir el proyecto", e.getMessage());
            return false;
        }
    }

    public boolean saveProject() {
        try {
            projectManager.saveProject();
            sceneManager.saveScene();
            return true;
        } catch (Exception e) {
            mostrarError("Error al guardar", e.getMessage());
            return false;
        }
    }

    public boolean closeProject() {
        try {
            stopAudioPreview();
            sceneManager.closeScene();
            projectManager.closeProject();
            Sprite.clearCache();
            //renderer = null;
            camara   = null;
            return true;
        } catch (Exception e) {
            mostrarError("Error al cerrar el proyecto", e.getMessage());
            return false;
        }
    }

    public boolean deleteProject(String path) {
        try {
            projectManager.deleteProject(path);
            return true;
        } catch (Exception e) {
            mostrarError("Error al eliminar el proyecto", e.getMessage());
            return false;
        }
    }

    // CONFIGURACIÓN DEL PROYECTO

    public boolean setProjectName(String name) {
        try {
            projectManager.setProjectName(name);
            return true;
        } catch (Exception e) {
            mostrarError("Error al cambiar el nombre", e.getMessage());
            return false;
        }
    }

    public boolean setWindowSize(int width, int height) {
        try {
            projectManager.setWindowSize(width, height);
            // Actualizar el viewport del renderer
            updateViewportSize(width, height);
            return true;
        } catch (Exception e) {
            mostrarError("Error al cambiar la resolución", e.getMessage());
            return false;
        }
    }

    public boolean setFps(int fps) {
        try {
            projectManager.setFps(fps);
            return true;
        } catch (Exception e) {
            mostrarError("Error al cambiar los FPS", e.getMessage());
            return false;
        }
    }

    public boolean setFullScreen(boolean fullScreen) {
        try {
            projectManager.setFullScreen(fullScreen);
            return true;
        } catch (Exception e) {
            mostrarError("Error al cambiar pantalla completa", e.getMessage());
            return false;
        }
    }

    public boolean setInitialScene(String sceneName) {
        try {
            projectManager.setInitialScene(sceneName);
            return true;
        } catch (Exception e) {
            mostrarError("Error al cambiar la escena inicial", e.getMessage());
            return false;
        }
    }

    // ESCENAS

    public boolean createScene(String name) {
        try {
            sceneManager.createScene(name);
            return true;
        } catch (Exception e) {
            mostrarError("Error al crear la escena", e.getMessage());
            return false;
        }
    }

    public boolean loadScene(String name) {
        try {
            sceneManager.loadScene(name);
            return true;
        } catch (Exception e) {
            mostrarError("Error al cargar la escena", e.getMessage());
            return false;
        }
    }

    public boolean deleteScene(String name) {
        try {
            sceneManager.deleteScene(name);
            return true;
        } catch (Exception e) {
            mostrarError("Error al eliminar la escena", e.getMessage());
            return false;
        }
    }

    public List<String> listScenes() {
        try {
            return sceneManager.listScenes();
        } catch (Exception e) {
            mostrarError("Error al listar escenas", e.getMessage());
            return List.of();
        }
    }

    // CONFIGURACIÓN DE LA ESCENA

    public boolean setSceneSize(int width, int height) {
        try {
            sceneManager.setSceneSize(width, height);
            return true;
        } catch (Exception e) {
            mostrarError("Error al cambiar el tamaño", e.getMessage());
            return false;
        }
    }

    public boolean setBackgroundColor(String color) {
        try {
            sceneManager.setBackgroundColor(color);
            return true;
        } catch (Exception e) {
            mostrarError("Error al cambiar el color de fondo", e.getMessage());
            return false;
        }
    }

    public boolean setSceneName(String name) {
        try {
            sceneManager.setSceneName(name);
            return true;
        } catch (Exception e) {
            mostrarError("Error al renombrar la escena", e.getMessage());
            return false;
        }
    }

    // ENTIDADES

    public Entity createEntity(String name) {
        try {
            return entityManager.createEntity(name);
        } catch (Exception e) {
            mostrarError("Error al crear entidad", e.getMessage());
            return null;
        }
    }

    public Entity createSpriteEntity(String name, String spritePath) {
        try {
            return entityManager.createSpriteEntity(name, spritePath);
        } catch (Exception e) {
            mostrarError("Error al crear entidad", e.getMessage());
            return null;
        }
    }

    public boolean removeEntity(Entity entity) {
        try {
            entityManager.removeEntity(entity);
            return true;
        } catch (Exception e) {
            mostrarError("Error al eliminar entidad", e.getMessage());
            return false;
        }
    }

    public Entity duplicateEntity(Entity entity) {
        try {
            return entityManager.duplicateEntity(entity);
        } catch (Exception e) {
            mostrarError("Error al duplicar entidad", e.getMessage());
            return null;
        }
    }

    public List<Entity> getAllEntities() {
        try {
            return entityManager.getAllEntities();
        } catch (Exception e) {
            return List.of();
        }
    }

    public Entity getEntityByName(String name) {
        try {
            return entityManager.getEntityByName(name);
        } catch (Exception e) {
            return null;
        }
    }

    // COMPONENTES

    public boolean addComponent(Entity entity, Component component) {
        try {
            entityManager.addComponent(entity, component);
            return true;
        } catch (Exception e) {
            mostrarError("Error al añadir componente", e.getMessage());
            return false;
        }
    }

    public <T extends Component> boolean removeComponent(Entity entity,
                                                         Class<T> type) {
        try {
            entityManager.removeComponent(entity, type);
            return true;
        } catch (Exception e) {
            mostrarError("Error al eliminar componente", e.getMessage());
            return false;
        }
    }

    // UI ELEMENTS

    public boolean addUIElement(UIElement element) {
        try {
            entityManager.addUIElement(element);
            return true;
        } catch (Exception e) {
            mostrarError("Error al añadir elemento UI", e.getMessage());
            return false;
        }
    }

    public boolean removeUIElement(UIElement element) {
        try {
            entityManager.removeUIElement(element);
            return true;
        } catch (Exception e) {
            mostrarError("Error al eliminar elemento UI", e.getMessage());
            return false;
        }
    }

    public List<UIElement> getAllUIElements() {
        try {
            return entityManager.getAllUIElements();
        } catch (Exception e) {
            return List.of();
        }
    }

    // TILESETS

    public Tileset createTileset(String name, String imagePath,
                                 int tileWidth, int tileHeight) {
        try {
            return tilesetManager.createTileset(name, imagePath,
                    tileWidth, tileHeight);
        } catch (Exception e) {
            mostrarError("Error al crear tileset", e.getMessage());
            return null;
        }
    }

    public boolean deleteTileset(String name) {
        try {
            tilesetManager.deleteTileset(name);
            return true;
        } catch (Exception e) {
            mostrarError("Error al eliminar tileset", e.getMessage());
            return false;
        }
    }

    public List<Tileset> getAllTilesets() {
        try {
            return tilesetManager.getAllTilesets();
        } catch (Exception e) {
            return List.of();
        }
    }

    public Tile addTile(Tileset tileset, String tileName,
                        String spritePath, boolean solid) {
        try {
            return tilesetManager.addTile(tileset, tileName, spritePath, solid);
        } catch (Exception e) {
            mostrarError("Error al añadir tile", e.getMessage());
            return null;
        }
    }

    public boolean removeTile(Tileset tileset, Tile tile) {
        try {
            tilesetManager.removeTile(tileset, tile);
            return true;
        } catch (Exception e) {
            mostrarError("Error al eliminar tile", e.getMessage());
            return false;
        }
    }

    // TILEMAP

    public Tilemap createTilemap(int cols, int rows,
                                 int tileWidth, int tileHeight) {
        try {
            return tilesetManager.createTilemap(cols, rows, tileWidth, tileHeight);
        } catch (Exception e) {
            mostrarError("Error al crear tilemap", e.getMessage());
            return null;
        }
    }

    public boolean removeTilemap() {
        try {
            tilesetManager.removeTilemap();
            return true;
        } catch (Exception e) {
            mostrarError("Error al eliminar tilemap", e.getMessage());
            return false;
        }
    }

    // Pintar tile desde click en el canvas
    // el editor envía coordenadas de píxel
    public boolean paintTileAtPixel(float pixelX, float pixelY, int tileId,
                                    TilemapLayer.LayerType layer) {
        try {
            // Convertir coordenadas de pantalla a coordenadas del mundo
            float worldX = screenToWorldX(pixelX);
            float worldY = screenToWorldY(pixelY);
            tilesetManager.paintTileAtPixel(worldX, worldY, tileId, layer);
            return true;
        } catch (Exception e) {
            mostrarError("Error al pintar tile", e.getMessage());
            return false;
        }
    }

    public boolean paintTileAtPixel(float pixelX, float pixelY, int tileId) {
        return paintTileAtPixel(pixelX, pixelY, tileId,
                TilemapLayer.LayerType.MIDGROUND);
    }

    public boolean eraseTileAtPixel(float pixelX, float pixelY,
                                    TilemapLayer.LayerType layer) {
        try {
            float worldX = screenToWorldX(pixelX);
            float worldY = screenToWorldY(pixelY);
            tilesetManager.eraseTileAtPixel(worldX, worldY, layer);
            return true;
        } catch (Exception e) {
            mostrarError("Error al borrar tile", e.getMessage());
            return false;
        }
    }

    public boolean eraseTileAtPixel(float pixelX, float pixelY) {
        return eraseTileAtPixel(pixelX, pixelY,
                TilemapLayer.LayerType.MIDGROUND);
    }

    public boolean clearTilemap() {
        try {
            tilesetManager.clearTilemap();
            return true;
        } catch (Exception e) {
            mostrarError("Error al limpiar tilemap", e.getMessage());
            return false;
        }
    }

    // RECURSOS

    public String importResource(String absolutePath,
                                 ResourceManager.ResourceType type) {
        try {
            return resourceManager.importResource(absolutePath, type);
        } catch (Exception e) {
            mostrarError("Error al importar recurso", e.getMessage());
            return null;
        }
    }

    public boolean deleteResource(String relativePath) {
        try {
            resourceManager.deleteResource(relativePath);
            return true;
        } catch (Exception e) {
            mostrarError("Error al eliminar recurso", e.getMessage());
            return false;
        }
    }

    public List<String> listSprites()  {
        try { return resourceManager.listSprites();
        } catch (Exception e) { return List.of(); }
    }

    public List<String> listAudio()    {
        try { return resourceManager.listAudio();
        } catch (Exception e) { return List.of(); }
    }

    public List<String> listFonts()    {
        try { return resourceManager.listFonts();
        } catch (Exception e) { return List.of(); }
    }

    public List<String> listTilesets() {
        try { return resourceManager.listTilesets();
        } catch (Exception e) { return List.of(); }
    }

    // AUDIO PREVIEW
    // Solo para previsualizar audios en el editor
    // La reproducción real en el juego va en el core

    public void playAudioPreview(String relativePath) {
        try {
            stopAudioPreview();

            String fullPath = getProjectPath()
                    + File.separator + relativePath;
            File audioFile = new File(fullPath);
            if (!audioFile.exists()) {
                mostrarError("Audio no encontrado", fullPath);
                return;
            }

            AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);
            audioClip = AudioSystem.getClip();
            audioClip.open(stream);
            audioClip.start();

        } catch (Exception e) {
            mostrarError("Error al reproducir audio", e.getMessage());
        }
    }

    public void stopAudioPreview() {
        if (audioClip != null && audioClip.isRunning()) {
            audioClip.stop();
            audioClip.close();
            audioClip = null;
        }
    }

    public boolean isAudioPlaying() {
        return audioClip != null && audioClip.isRunning();
    }

    // CAMARA

    public void moverCamara(float dx, float dy) {
        if (camara == null) return;
        camara.move(dx, dy);
        if (isProjectOpen() && isSceneOpen()) {
            Scene scene = sceneManager.getCurrentScene();
            camara.clampToBounds(scene.getWidth(), scene.getHeight());
        }
    }

    public void zoomIn()  { if (camara != null) camara.zoomIn(0.1f);  }
    public void zoomOut() { if (camara != null) camara.zoomOut(0.1f); }

    public void resetCamara() {
        if (camara != null) {
            camara.moveTo(0, 0);
            camara.setZoom(1.0f);
        }
    }

    public float screenToWorldX(float screenX) {
        return camara != null ? camara.screenToWorldX(screenX) : screenX;
    }

    public float screenToWorldY(float screenY) {
        return camara != null ? camara.screenToWorldY(screenY) : screenY;
    }

    // RENDER

//    public void render(java.awt.Graphics2D g2) {
//        if (renderer == null || !isSceneOpen()) return;
//        List<Tileset> tilesets = projectManager.getCurrentProject().getTilesets();
//        renderer.render(g2, sceneManager.getCurrentScene(), tilesets);
//    }

    public void updateViewportSize(int width, int height) {
        if (camara != null) {
            camara.setViewWidth(width);
            camara.setViewHeight(height);
        }
    }

    // ESTADO

    public boolean isProjectOpen() { return projectManager.isProjectOpen();  }
    public boolean isSceneOpen()   { return sceneManager.isSceneOpen();      }

    public String getProjectName() {
        return isProjectOpen()
                ? projectManager.getCurrentProject().getName() : "";
    }

    public String getProjectPath() {
        return isProjectOpen()
                ? projectManager.getCurrentProjectPath() : "";
    }

    public String getCurrentSceneName() {
        return isSceneOpen()
                ? sceneManager.getCurrentScene().getName() : "";
    }

    // GETTERS directos por si algún panel los necesita

    public ProjectManager  getProjectManager()  { return projectManager;  }
    public SceneManager    getSceneManager()    { return sceneManager;    }
    public EntityManager   getEntityManager()   { return entityManager;   }
    public TilesetManager  getTilesetManager()  { return tilesetManager;  }
    public ResourceManager getResourceManager() { return resourceManager; }
    //public Renderer        getRenderer()        { return renderer;        }
    public Camara          getCamara()          { return camara;          }

    // PRIVADOS

    private void initGraphics(String path) {
        int w = projectManager.getCurrentProject()
                .getConfiguration().getWindowWidth();
        int h = projectManager.getCurrentProject()
                .getConfiguration().getWindowHeight();
        camara   = new Camara(w, h);
        //renderer = new Renderer(camara, path);
    }

    private void mostrarError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(null,
                mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }
}