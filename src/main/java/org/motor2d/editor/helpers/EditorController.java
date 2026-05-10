package org.motor2d.editor.helpers;

import org.motor2d.core.Engine;
import org.motor2d.editor.Editor;
import org.motor2d.editor.PanelCanvas;
import org.motor2d.graphics.Camara;
import org.motor2d.graphics.Sprite;
import org.motor2d.manager.*;
import org.motor2d.model.Entity;
import org.motor2d.model.Scene;
import org.motor2d.model.Tile;
import org.motor2d.model.Tileset;
import org.motor2d.model.components.SpriteRenderer;
import org.motor2d.model.components.TilemapLayer;
import org.motor2d.model.components.Transform;
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
    private final HistoryManager historyManager;

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
        this.historyManager  = new HistoryManager();
    }

    public void registrarEstado() {
        if (isSceneOpen()) {
            historyManager.pushState(sceneManager.getCurrentScene());
        }
    }

    public void undo() {
        if (!isSceneOpen()) return;
        Scene anterior = historyManager.undo(sceneManager.getCurrentScene());
        if (anterior != null) {
            aplicarEstadoHistorial(anterior);
            if (editor != null) editor.mostrarMensajeEstado("Deshacer realizado");
        }
    }

    public void redo() {
        if (!isSceneOpen()) return;
        Scene siguiente = historyManager.redo();
        if (siguiente != null) {
            aplicarEstadoHistorial(siguiente);
            if (editor != null) editor.mostrarMensajeEstado("Rehacer realizado");
        }
    }

    private void aplicarEstadoHistorial(Scene nueva) {
        // Al restaurar una escena, los componentes Transform pierden su conexión con el TransformSystem 
        // o el sistema viejo es invalidado. Necesitamos volver a registrar los Transform.
        nueva.getTransformSystem().updatePrevious(); // Asegurar estado limpio
        for (Entity e : nueva.getEntities()) {
            Transform t = e.getComponent(Transform.class);
            if (t != null) {
                t.registerInSystem(nueva.getTransformSystem());
            }
        }

        sceneManager.setCurrentScene(nueva);
        inicializarMotor();
        if (editor != null) {
            editor.refrescarHierarchy();
            editor.getPanelCanvas().repaint();
        }
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

                Transform t = e.getComponent(Transform.class);
                SpriteRenderer s = e.getComponent(SpriteRenderer.class);

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

    public Editor getEditor() {
        return editor;
    }

    // ==================== GESTIÓN DE PROYECTO ====================

    public boolean createProject(String name, String path) {
        try {
            projectManager.createProject(name, path);
            sceneManager.loadScene("main");
            inicializarMotor();
            registrarEstado();
            if (editor != null) {
                editor.actualizarTitulo();
                editor.actualizarStatusBar();
                editor.refrescarHierarchy();
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

            try {
                sceneManager.loadScene(initialScene);
            } catch (Exception e) {
                // Fallback: si la escena inicial no existe, cargar la más reciente
                List<String> scenes = listScenes();
                if (!scenes.isEmpty()) {
                    sceneManager.loadScene(scenes.get(0));
                } else {
                    // Si no hay ninguna escena en el proyecto, crear una por defecto
                    sceneManager.createScene("main");
                }
            }

            inicializarMotor();
            registrarEstado();
            if (editor != null) {
                editor.actualizarTitulo();
                editor.actualizarStatusBar();
                editor.refrescarHierarchy();
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
            Tile.resetIdCounter(0);
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
        
        // Detenemos cualquier hilo previo antes de crear uno nuevo
        Engine.stop();
        
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
        try {
            Entity e = entityManager.createEntity(name);
            registrarEstado();
            return e;
        } catch (Exception e) { return null; }
    }

    public boolean paintTileAtPixel(float pixelX, float pixelY, int tileId, TilemapLayer.LayerType layer) {
        try {
            if (!isSceneOpen()) return false;
            
            // Auto-crear Tilemap si no existe
            if (sceneManager.getCurrentScene().getTilemap() == null) {
                tilesetManager.createTilemap(100, 100, 32, 32);
            }
            
            float worldX = screenToWorldX(pixelX);
            float worldY = screenToWorldY(pixelY);
            tilesetManager.paintTileAtPixel(worldX, worldY, tileId, layer);
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean eraseTileAtPixel(float pixelX, float pixelY, TilemapLayer.LayerType layer) {
        try {
            if (!isSceneOpen()) return false;
            registrarEstado(); // Guardar estado antes de borrar
            tilesetManager.eraseTileAtPixel(screenToWorldX(pixelX), screenToWorldY(pixelY), layer);
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean resizeTilemap(int cols, int rows) {
        try {
            if (!isSceneOpen()) return false;
            registrarEstado();
            tilesetManager.resizeTilemap(cols, rows);
            return true;
        } catch (Exception e) {
            mostrarError("Error al redimensionar tilemap", e.getMessage());
            return false;
        }
    }

    public void addTileToCurrentTileset(String name, String relativePath) {
        try {
            Tileset current = null;
            if (editor != null && editor.getPanelAssets() != null) {
                String tsName = editor.getPanelAssets().getSelectedTilesetName();
                if (tsName != null) {
                    current = projectManager.getCurrentProject().getTilesetByName(tsName);
                }
            }
            
            if (current == null) {
                List<Tileset> all = projectManager.getCurrentProject().getTilesets();
                if (!all.isEmpty()) current = all.get(0);
            }
            
            if (current == null) {
                current = tilesetManager.createTileset("Default Tileset", relativePath, 32, 32);
            }
            
            tilesetManager.addTile(current, name, relativePath, false, false);
            if (editor != null) {
                editor.getPanelAssets().refrescarComboTilesets();
                editor.getPanelAssets().mostrarPaleta(current);
            }
        } catch (Exception e) {
            mostrarError("Error al añadir tile", e.getMessage());
        }
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
    public TilesetManager getTilesetManager() { return tilesetManager; }

    // ==================== HELPERS PARA PANELES ====================

    public List<Entity> getAllEntities() {
        try { return entityManager.getAllEntities(); }
        catch (Exception e) { return List.of(); }
    }

    public Entity createSpriteEntity(String name, String spritePath) {
        try {
            Entity e = entityManager.createSpriteEntity(name, spritePath);
            registrarEstado();
            return e;
        } catch (Exception e) { mostrarError("Error al crear entidad", e.getMessage()); return null; }
    }

    public void setEntitySprite(Entity entity, String relativePath) {
        if (entity == null || relativePath == null) return;
        
        try {
            SpriteRenderer sr = entity.getComponent(SpriteRenderer.class);
            if (sr == null) {
                sr = new SpriteRenderer();
                entity.addComponent(sr);
            }
            
            sr.setSpritePath(relativePath);
            
            // Detectar tamaño
            File f = new File(getProjectPath(), relativePath);
            if (f.exists()) {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
                if (img != null) {
                    sr.setFrameWidth(img.getWidth());
                    sr.setFrameHeight(img.getHeight());
                }
            }
            
            registrarEstado();
            saveProject();
            if (editor != null) editor.getPanelCanvas().repaint();
            
        } catch (Exception e) {
            mostrarError("Error al asignar sprite", e.getMessage());
        }
    }

    public boolean removeEntity(Entity entity) {
        try {
            entityManager.removeEntity(entity);
            registrarEstado();
            return true;
        } catch (Exception e) { mostrarError("Error al eliminar entidad", e.getMessage()); return false; }
    }

    public Entity duplicateEntity(Entity entity) {
        try {
            Entity e = entityManager.duplicateEntity(entity);
            registrarEstado();
            return e;
        } catch (Exception e) { mostrarError("Error al duplicar entidad", e.getMessage()); return null; }
    }

    // ==================== UI ELEMENTS ====================

    public UIElement createUILabel(String name, String text) {
        try {
            UIElement e = entityManager.createUILabel(name, text);
            registrarEstado();
            return e;
        } catch (Exception e) { mostrarError("Error al crear Label", e.getMessage()); return null; }
    }

    public UIElement createUIButton(String name, String text) {
        try {
            UIElement e = entityManager.createUIButton(name, text);
            registrarEstado();
            return e;
        } catch (Exception e) { mostrarError("Error al crear Botón", e.getMessage()); return null; }
    }

    public UIElement createUIImage(String name, String path) {
        try {
            UIElement e = entityManager.createUIImage(name, path);
            registrarEstado();
            return e;
        } catch (Exception e) { mostrarError("Error al crear Imagen", e.getMessage()); return null; }
    }

    public List<UIElement> getAllUIElements() {
        try { return entityManager.getAllUIElements(); }
        catch (Exception e) { return List.of(); }
    }

    public boolean removeUIElement(UIElement element) {
        try {
            entityManager.removeUIElement(element);
            registrarEstado();
            return true;
        } catch (Exception e) { mostrarError("Error al eliminar UI", e.getMessage()); return false; }
    }

    public boolean createScene(String name) {
        try {
            sceneManager.createScene(name);
            registrarEstado();
            return true;
        } catch (Exception e) { mostrarError("Error al crear escena", e.getMessage()); return false; }
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
            return resourceManager.listSprites();
        } catch (Exception e) {
            return List.of();
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
                // Guardar estado actual antes de jugar para poder restaurar luego
                sceneManager.saveScene();
                Engine.setPlaying(true);
                
                if (canvas != null) {
                    canvas.requestFocusInWindow();
                    canvas.repaint(); // Forzar redibujado al entrar
                }

                if (editor != null) {
                    editor.mostrarMensajeEstado("Modo JUEGO activado");
                    editor.bloquearUI(true); // Bloquear edición
                }
            } catch (Exception e) {
                mostrarError("Error al iniciar modo juego", e.getMessage());
            }
        }
    }

    public void stopPlay() {
        if (!isSceneOpen()) return;
        
        Engine.setPlaying(false);
        try {
            // Recargar la escena original (antes de empezar a jugar)
            String sceneName = sceneManager.getCurrentScene().getName();
            sceneManager.loadScene(sceneName);
            inicializarMotor(); 
            
            if (canvas != null) {
                canvas.repaint(); // Forzar redibujado al salir
            }
            
            if (editor != null) {
                editor.refrescarHierarchy();
                editor.mostrarMensajeEstado("Modo EDITOR activado (Escena reseteada)");
            }
        } catch (Exception e) {
            mostrarError("Error al detener modo juego", e.getMessage());
        } finally {
            if (editor != null) {
                editor.bloquearUI(false); // Desbloquear edición SIEMPRE
            }
        }
    }
}