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
    private static InputManager inputManager;
    private static org.motor2d.manager.AudioManager audioManager;
    private static Scene currentScene;
    private static Project project;
    private static boolean playing = false;

    // ==================== MÉTODOS DE INICIALIZACIÓN ====================

    /**
     * Inicializa el motor vinculando el proyecto, la escena y el lienzo.
     */
    public static void init(Project project, Scene scene, JPanel canvas) {
        Engine.project = project;
        currentScene = scene;

        // Limpiar estado de teclas para evitar que se queden pulsadas al reiniciar
        InputManager.clearState();
        
        // Inicializamos el sistema de audio
        audioManager = new org.motor2d.manager.AudioManager(project.getPath());

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

    public static void setPlaying(boolean p) {
        playing = p;
    }

    public static boolean isPlaying() {
        return playing;
    }

    public static void start() {
        if (gameLoop != null) {
            gameLoop.start();
        }
    }

    public static void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
            // No hacemos join() aquí para no bloquear el hilo de Swing, 
            // pero el flag volatile 'running' detendrá el bucle en el próximo frame.
            gameLoop = null; 
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
    public static Project getProject() { return project; }
    public static Scene getCurrentScene() { return currentScene; }
}