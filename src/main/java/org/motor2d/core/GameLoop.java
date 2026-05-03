package org.motor2d.core;

import org.motor2d.graphics.Renderer;
import org.motor2d.physics.PhysicsSystem;
import org.motor2d.model.Scene;
import org.motor2d.model.Project;
import javax.swing.JPanel;
import java.awt.Graphics2D;

/**
 * GameLoop - Controla el tiempo y el ciclo de vida del juego (Update y Render).
 */
public class GameLoop implements Runnable {

    // ==================== ESTADO DEL BUCLE ====================
    private boolean running = false;
    private final Scene scene;
    private final Renderer renderer;
    private final PhysicsSystem physics;
    private final Project project;
    private final JPanel canvas;

    // ==================== CONSTRUCTOR ====================
    public GameLoop(Project project, Scene scene, Renderer renderer, JPanel canvas) {
        this.project  = project;
        this.scene    = scene;
        this.renderer = renderer;
        this.canvas   = canvas;
        this.physics  = new PhysicsSystem();
    }

    // ==================== CONTROL DEL HILO ====================

    /**
     * Arranca el bucle en un nuevo hilo.
     */
    public void start() {
        if (running) return;
        running = true;
        Thread thread = new Thread(this, "GameLoop");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    /**
     * Detiene la ejecución del bucle.
     */
    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        Time.start();
        
        // Configuración de FPS
        double targetFPS = project.getConfiguration().getFps();
        double nsPerFrame = 1_000_000_000.0 / targetFPS;
        long lastTime = System.nanoTime();
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerFrame;
            lastTime = now;

            // Bloque de actualización lógica (Físicas)
            while (delta >= 1) {
                Time.update();
                physics.update(scene);
                delta--;
            }

            // Bloque de renderizado: Solicitamos a Swing que repinte el panel
            if (canvas != null) {
                canvas.repaint();
            }

            // Pausa mínima para no saturar la CPU
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ==================== RENDERIZADO ====================

    /**
     * Ejecuta el dibujo real de la escena. 
     * Este método es llamado por el PanelCanvas desde el hilo de Swing.
     */
    public void render(Graphics2D g2) {
        if (renderer != null && scene != null) {
            renderer.render(g2, scene, project.getTilesets());
        }
    }
}