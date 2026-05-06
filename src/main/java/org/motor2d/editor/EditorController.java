package org.motor2d.editor;

import org.motor2d.core.Engine;
import org.motor2d.graphics.Camara;
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
import java.util.List;

/**
 * EditorController - El "cerebro" que coordina los managers y el motor.
 */
public class EditorController {

    private final ProjectManager projectManager;
    private final SceneManager sceneManager;
    private final EntityManager entityManager;
    private final TilesetManager tilesetManager;
    private final ResourceManager resourceManager;

    private PanelCanvas canvas;
    private PanelAssets panelAssets;
    private StatusBar statusBar;
    private Clip audioClip;

    private boolean cambiosSinGuardar = false;

    public EditorController() {
        this.projectManager  = new ProjectManager();
        this.sceneManager    = new SceneManager(projectManager);
        this.entityManager   = new EntityManager(sceneManager);
        this.tilesetManager  = new TilesetManager(projectManager, sceneManager);
        this.resourceManager = new ResourceManager(projectManager);
    }

    /**
     * Vincula el canvas del editor para que el motor pueda dibujar en él.
     */
    public void setCanvas(PanelCanvas canvas) {
        this.canvas = canvas;
    }

    public void setPanelAssets(PanelAssets panelAssets) {
        this.panelAssets = panelAssets;
    }

    public void setStatusBar(StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    // ==================== GESTIÓN DE PROYECTO ====================

    public boolean createProject(String name, String path) {
        try {
            if (statusBar != null) statusBar.mostrarMensajePermanente("Creando proyecto...");
            projectManager.createProject(name, path);
            sceneManager.loadScene("main");
            inicializarMotor();
            if (panelAssets != null) panelAssets.actualizarRuta(new File(projectManager.getCurrentProjectPath()));
            if (statusBar != null) {
                statusBar.setInfoProyecto(name);
                statusBar.mostrarMensaje("Proyecto creado con éxito", 3000);
            }
            cambiosSinGuardar = false;
            return true;
        } catch (Exception e) {
            mostrarError("Error al crear el proyecto", e.getMessage());
            return false;
        }
    }

    public boolean openProject(String path) {
        try {
            if (statusBar != null) statusBar.mostrarMensajePermanente("Abriendo proyecto...");
            projectManager.openProject(path);
            String initialScene = projectManager.getCurrentProject()
                    .getInitialScene()
                    .replace("scenes/", "")
                    .replace(".json", "");
            sceneManager.loadScene(initialScene);
            inicializarMotor();
            if (panelAssets != null) panelAssets.actualizarRuta(new File(path));
            if (statusBar != null) {
                statusBar.setInfoProyecto(projectManager.getCurrentProject().getName());
                statusBar.mostrarMensaje("Proyecto abierto con éxito", 3000);
            }
            cambiosSinGuardar = false;
            return true;
        } catch (Exception e) {
            mostrarError("Error al abrir el proyecto", e.getMessage());
            return false;
        }
    }

    public boolean closeProject() {
        try {
            Engine.stop();
            stopAudioPreview();
            sceneManager.closeScene();
            projectManager.closeProject();
            Sprite.clearCache();
            if (statusBar != null) {
                statusBar.setInfoProyecto("");
                statusBar.mostrarMensaje("Proyecto cerrado", 3000);
            }
            cambiosSinGuardar = false;
            return true;
        } catch (Exception e) {
            mostrarError("Error al cerrar el proyecto", e.getMessage());
            return false;
        }
    }

    // ==================== INTEGRACIÓN CON EL MOTOR ====================

    private void inicializarMotor() {
        if (canvas == null) return;
        
        // Inicializamos el motor estático con los datos actuales
        Engine.init(projectManager.getCurrentProject(), 
                    sceneManager.getCurrentScene(), 
                    canvas);
        
        // Arrancamos el bucle de juego
        Engine.start();
    }

    public float screenToWorldX(float screenX) {
        Camara cam = Engine.getCamara();
        return cam != null ? cam.screenToWorldX(screenX) : screenX;
    }

    public float screenToWorldY(float screenY) {
        Camara cam = Engine.getCamara();
        return cam != null ? cam.screenToWorldY(screenY) : screenY;
    }

    // ==================== MÉTODOS DELEGADOS (MANAGERS) ====================

    public boolean saveProject() {
        try {
            if (statusBar != null) statusBar.mostrarMensajePermanente("Guardando...");
            projectManager.saveProject();
            sceneManager.saveScene();
            if (statusBar != null) statusBar.mostrarMensaje("Proyecto guardado correctamente", 3000);
            cambiosSinGuardar = false;
            return true;
        } catch (Exception e) {
            mostrarError("Error al guardar", e.getMessage());
            return false;
        }
    }

    public void marcarCambio() {
        cambiosSinGuardar = true;
    }

    public boolean tieneCambiosSinGuardar() {
        return cambiosSinGuardar;
    }

    public Entity createEntity(String name) {
        try { return entityManager.createEntity(name);
        } catch (Exception e) { return null; }
    }

    public boolean paintTileAtPixel(float pixelX, float pixelY, int tileId, TilemapLayer.LayerType layer) {
        try {
            float worldX = screenToWorldX(pixelX);
            float worldY = screenToWorldY(pixelY);
            tilesetManager.paintTileAtPixel(worldX, worldY, tileId, layer);
            return true;
        } catch (Exception e) { return false; }
    }

    // ==================== AUDIO PREVIEW ====================

    public void playAudioPreview(String relativePath) {
        try {
            stopAudioPreview();
            File audioFile = new File(getProjectPath(), relativePath);
            if (!audioFile.exists()) return;

            AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);
            audioClip = AudioSystem.getClip();
            audioClip.open(stream);
            audioClip.start();
        } catch (Exception e) {
            mostrarError("Error de audio", e.getMessage());
        }
    }

    public void stopAudioPreview() {
        if (audioClip != null && audioClip.isRunning()) {
            audioClip.stop();
            audioClip.close();
            audioClip = null;
        }
    }

    // ==================== GETTERS Y HELPERS ====================

    public boolean isProjectOpen() { return projectManager.isProjectOpen(); }
    public boolean isSceneOpen()   { return sceneManager.isSceneOpen();     }
    public String getProjectPath() { return projectManager.getCurrentProjectPath(); }

    public String getCurrentSceneName() {
        return sceneManager.isSceneOpen() ? sceneManager.getCurrentScene().getName() : "";
    }

    public ProjectManager getProjectManager() { return projectManager; }
    public SceneManager getSceneManager() { return sceneManager; }

    // ==================== HELPERS PARA PANELES ====================

    public List<Entity> getAllEntities() {
        try { return entityManager.getAllEntities(); }
        catch (Exception e) { return List.of(); }
    }

    public Entity createSpriteEntity(String name, String spritePath) {
        try { return entityManager.createSpriteEntity(name, spritePath); }
        catch (Exception e) { mostrarError("Error al crear entidad", e.getMessage()); return null; }
    }

    public boolean removeEntity(Entity entity) {
        try { entityManager.removeEntity(entity); return true; }
        catch (Exception e) { mostrarError("Error al eliminar entidad", e.getMessage()); return false; }
    }

    public Entity duplicateEntity(Entity entity) {
        try { return entityManager.duplicateEntity(entity); }
        catch (Exception e) { mostrarError("Error al duplicar entidad", e.getMessage()); return null; }
    }

    public boolean createScene(String name) {
        try { sceneManager.createScene(name); return true; }
        catch (Exception e) { mostrarError("Error al crear escena", e.getMessage()); return false; }
    }

    public boolean loadScene(String name) {
        try {
            sceneManager.loadScene(name);
            inicializarMotor();
            return true;
        } catch (Exception e) { mostrarError("Error al cargar escena", e.getMessage()); return false; }
    }

    public List<String> listScenes() {
        try { return sceneManager.listScenes(); }
        catch (Exception e) { return List.of(); }
    }

    public List<String> listSprites() {
        try {
            String projectPath = getProjectPath();
            if (projectPath == null) return List.of();
            File assetsDir = new File(projectPath, "assets");
            if (!assetsDir.exists()) {
                // Intento alternativo por si acaso está en Mayúsculas
                assetsDir = new File(projectPath, "Assets");
                if (!assetsDir.exists()) return List.of();
            }
            java.util.List<String> result = new java.util.ArrayList<>();
            buscarImagenes(assetsDir, assetsDir, result);
            return result;
        } catch (Exception e) { return List.of(); }
    }

    private void buscarImagenes(File base, File dir, java.util.List<String> result) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) { buscarImagenes(base, f, result); }
            else {
                String name = f.getName().toLowerCase();
                if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                    result.add(base.toURI().relativize(f.toURI()).getPath());
                }
            }
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }
}