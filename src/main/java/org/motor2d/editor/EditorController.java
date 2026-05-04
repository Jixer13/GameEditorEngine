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
    private Clip audioClip;

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

    // ==================== GESTIÓN DE PROYECTO ====================

    public boolean createProject(String name, String path) {
        try {
            projectManager.createProject(name, path);
            sceneManager.loadScene("main");
            inicializarMotor();
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
            inicializarMotor();
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
            projectManager.saveProject();
            sceneManager.saveScene();
            return true;
        } catch (Exception e) {
            mostrarError("Error al guardar", e.getMessage());
            return false;
        }
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
    
    public ProjectManager getProjectManager() { return projectManager; }
    public SceneManager getSceneManager() { return sceneManager; }

    private void mostrarError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }
}