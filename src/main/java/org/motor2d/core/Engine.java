package org.motor2d.core;

import org.motor2d.model.Project;
import org.motor2d.model.Scene;
import org.motor2d.graphics.Renderer;
import org.motor2d.graphics.Camara;
import javax.swing.JPanel;
import java.awt.Graphics2D;

/**
 * Engine - Punto de entrada principal y gestor del estado del motor.
 */
public class Engine {

    // ==================== ATRIBUTOS ESTÁTICOS ====================
    private static GameLoop gameLoop;
    private static Renderer renderer;
    private static Camara camara;
    private static org.motor2d.manager.AudioManager audioManager;
    private static org.motor2d.manager.PrefabManager prefabManager;
    private static Scene currentScene;

    // ==================== MÉTODOS DE INICIALIZACIÓN ====================

    /**
     * Inicializa el motor vinculando el proyecto, la escena y el lienzo.
     */
    public static void init(Project project, Scene scene, JPanel canvas) {
        currentScene = scene;

        // Inicializamos el sistema de entrada
        InputManager input = new InputManager();
        canvas.addKeyListener(input);
        canvas.addMouseListener(input);
        canvas.addMouseMotionListener(input);
        canvas.setFocusable(true);
        canvas.requestFocusInWindow();

        // Inicializamos el sistema de audio
        audioManager = new org.motor2d.manager.AudioManager(project.getPath());

        // Inicializamos el gestor de prefabs
        prefabManager = new org.motor2d.manager.PrefabManager(project.getPath());

        // Creamos la cámara ajustada al tamaño del panel de dibujo
        camara = new Camara(canvas.getWidth(), canvas.getHeight());
        
        // Creamos el renderer pasándole la cámara y la ruta raíz del proyecto
        renderer = new Renderer(camara, project.getPath());
        
        // Registramos todos los Transforms en el sistema DOD para máximo rendimiento
        if (scene != null && scene.getTransformSystem() != null) {
            for (org.motor2d.model.Entity entity : scene.getEntities()) {
                org.motor2d.model.components.Transform transform = 
                        entity.getComponent(org.motor2d.model.components.Transform.class);
                if (transform != null) {
                    transform.registerInSystem(scene.getTransformSystem());
                }
            }
        }
        
        // Configuramos el bucle de juego (GameLoop)
        gameLoop = new GameLoop(project, scene, renderer, canvas);
        
        System.out.println("Motor inicializado para el proyecto: " + project.getName());
    }

    // ==================== CONTROL DEL CICLO DE VIDA ====================

    public static void start() {
        if (gameLoop != null) {
            gameLoop.start();
        }
    }

    public static void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (audioManager != null) {
            audioManager.stopMusic();
        }
    }

    // ==================== RENDERIZADO ====================

    public static void render(Graphics2D g2) {
        if (gameLoop != null) {
            gameLoop.render(g2);
        }
    }

    // ==================== GETTERS ====================
    
    public static Camara getCamara() { return camara; }
    public static org.motor2d.manager.AudioManager getAudioManager() { return audioManager; }
    public static org.motor2d.manager.PrefabManager getPrefabManager() { return prefabManager; }
    public static Scene getCurrentScene() { return currentScene; }
}