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

    // ==================== MÉTODOS DE INICIALIZACIÓN ====================

    /**
     * Inicializa el motor vinculando el proyecto, la escena y el lienzo.
     */
    public static void init(Project project, Scene scene, JPanel canvas) {
        // Creamos la cámara ajustada al tamaño del panel de dibujo
        camara = new Camara(canvas.getWidth(), canvas.getHeight());
        
        // Creamos el renderer pasándole la cámara y la ruta raíz del proyecto
        renderer = new Renderer(camara, project.getPath());
        
        // Configuramos el bucle de juego (GameLoop)
        gameLoop = new GameLoop(project, scene, renderer, canvas);
        
        System.out.println("Motor inicializado para el proyecto: " + project.getName());
    }

    // ==================== CONTROL DEL CICLO DE VIDA ====================

    /**
     * Inicia la ejecución del bucle de juego en un hilo separado.
     */
    public static void start() {
        if (gameLoop != null) {
            gameLoop.start();
        }
    }

    /**
     * Detiene el bucle de juego de forma segura.
     */
    public static void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    // ==================== RENDERIZADO ====================

    /**
     * Método puente utilizado por el PanelCanvas para solicitar el dibujado.
     */
    public static void render(Graphics2D g2) {
        if (gameLoop != null) {
            gameLoop.render(g2);
        }
    }

    // ==================== GETTERS ====================
    
    public static Camara getCamara() {
        return camara;
    }
}