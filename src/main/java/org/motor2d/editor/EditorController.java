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
    private Editor editor;
    private Clip audioClip;
    private Entity selectedEntity;

    public EditorController() {
        this.projectManager  = new ProjectManager();
        this.sceneManager    = new SceneManager(projectManager);
        this.entityManager   = new EntityManager(sceneManager);
        this.tilesetManager  = new TilesetManager(projectManager, sceneManager);
        this.resourceManager = new ResourceManager(projectManager);
    }

    public Entity getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(Entity entity) {
        this.selectedEntity = entity;
        if (editor != null) {
            editor.seleccionarEntidadEnUI(entity);
        }
    }

    /**
     * Busca una entidad en la posición del mundo indicada.
     * Útil para seleccionar entidades haciendo clic en el canvas.
     */
    public Entity pickEntity(float worldX, float worldY) {
        if (!isSceneOpen()) return null;
        try {
            List<Entity> entities = getAllEntities();
            // Recorremos de la más reciente a la más antigua (orden de renderizado inverso)
            for (int i = entities.size() - 1; i >= 0; i--) {
                Entity e = entities.get(i);
                if (!e.isActive()) continue;

                org.motor2d.model.components.Transform t = e.getComponent(org.motor2d.model.components.Transform.class);
                org.motor2d.model.components.SpriteRenderer s = e.getComponent(org.motor2d.model.components.SpriteRenderer.class);

                if (t != null && s != null) {
                    float x = t.getX();
                    float y = t.getY();
                    float w = s.getFrameWidth() * t.getScaleX();
                    float h = s.getFrameHeight() * t.getScaleY();

                    if (worldX >= x && worldX <= x + w && worldY >= y && worldY <= y + h) {
                        return e;
                    }
                }
            }
        } catch (Exception e) {
            // Ignorar errores en el picking
        }
        return null;
    }

    /**
     * Vincula el canvas del editor para que el motor pueda dibujar en él.
     */
    public void setCanvas(PanelCanvas canvas) {
        this.canvas = canvas;
    }

    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    // ==================== GESTIÓN DE PROYECTO ====================

    public boolean createProject(String name, String path) {
        try {
            projectManager.createProject(name, path);
            sceneManager.loadScene("main");
            inicializarMotor();
            if (editor != null) {
                editor.actualizarTitulo();
                editor.actualizarStatusBar();
                editor.mostrarMensajeEstado("Proyecto creado: " + name);
            }
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
            if (editor != null) {
                editor.actualizarTitulo();
                editor.actualizarStatusBar();
                editor.mostrarMensajeEstado("Proyecto abierto correctamente");
            }
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
            if (editor != null) {
                editor.actualizarTitulo();
                editor.actualizarStatusBar();
            }
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
            if (editor != null) editor.mostrarMensajeEstado("Proyecto guardado");
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
            if (projectPath == null || projectPath.isEmpty()) return List.of();
            File assetsDir = new File(projectPath, "assets");
            if (!assetsDir.exists()) return List.of();
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

    // ==================== PLAY MODE ====================

    public boolean isPlaying() {
        return Engine.isPlaying();
    }

    public void togglePlay() {
        if (!isSceneOpen()) return;
        
        if (Engine.isPlaying()) {
            stopPlay();
        } else {
            try {
                // Guardar escena antes de jugar para poder restaurar luego
                sceneManager.saveScene();
                Engine.setPlaying(true);
                if (editor != null) editor.mostrarMensajeEstado("Modo JUEGO activado");
            } catch (Exception e) {
                mostrarError("Error al iniciar modo juego", e.getMessage());
            }
        }
    }

    public void stopPlay() {
        if (!isSceneOpen()) return;
        
        Engine.setPlaying(false);
        try {
            // Recargar la escena para resetear posiciones y estados
            String sceneName = sceneManager.getCurrentScene().getName();
            sceneManager.loadScene(sceneName);
            inicializarMotor(); // Re-inicializar para vincular la nueva instancia de la escena
            
            if (editor != null) {
                editor.refrescarHierarchy();
                editor.mostrarMensajeEstado("Modo EDITOR activado (Escena reseteada)");
            }
        } catch (Exception e) {
            mostrarError("Error al detener modo juego", e.getMessage());
        }
    }
}