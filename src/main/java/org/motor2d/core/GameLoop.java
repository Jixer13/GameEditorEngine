package org.motor2d.core;

import org.motor2d.graphics.Renderer;
import org.motor2d.model.Entity;
import org.motor2d.model.components.Animation;
import org.motor2d.model.components.Behavior;
import org.motor2d.model.components.Component;
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
    private volatile boolean running = false;
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
        // Inicializamos los valores de tiempo al arrancar el hilo
        Time.start();
        
        // Configuración de Timestep
        double targetUPS = project.getConfiguration().getFps(); // Updates per second (Fixed)
        double nsPerUpdate = 1_000_000_000.0 / targetUPS;
        
        long lastTime = System.nanoTime();
        double accumulator = 0;

        while (running) {
            long now = System.nanoTime();
            // Calculamos el tiempo transcurrido desde el último frame
            double elapsed = now - lastTime;
            lastTime = now;
            
            // Evitar el "espiral de la muerte" (max 0.25s por frame)
            if (elapsed > 250_000_000) elapsed = 250_000_000;
            
            accumulator += elapsed;

            // Bloque de actualización lógica (Fixed Timestep)
            while (accumulator >= nsPerUpdate) {
                float fixedDelta = (float)(nsPerUpdate / 1_000_000_000.0);
                // Actualizamos el tiempo global con el paso fijo
                Time.setFixedDeltaTime(fixedDelta);
                
                // Actualizamos estados previos para interpolación (DOD)
                if (scene != null && scene.getTransformSystem() != null) {
                    scene.getTransformSystem().updatePrevious();
                }
                
                // --- ACTUALIZACIÓN LÓGICA (SOLO SI ESTÁ EN MODO PLAY) ---
                if (Engine.isPlaying()) {
                    // 1. Lógica de Usuario (Behaviors)
                    if (scene != null) {
                        for (Entity entity : scene.getEntities()) {
                            if (!entity.isActive()) continue;
                            
                            // Aseguramos inicialización si no se ha hecho
                            for (Component comp : entity.getComponents()) {
                                if (comp.getOwner() == null) {
                                    comp.setOwner(entity);
                                    comp.initialize();
                                }
                            }

                            // Procesar Scripts/Comportamientos
                            for (Component comp : entity.getComponents()) {
                                if (comp instanceof Behavior behavior && comp.isEnabled()) {
                                    if (!behavior.isStarted()) {
                                        behavior.start();
                                        behavior.setStarted(true);
                                    }
                                    behavior.update(fixedDelta);
                                }
                                
                                // 2. Actualizar Animaciones
                                if (comp instanceof Animation animation && comp.isEnabled()) {
                                    animation.update(fixedDelta);
                                }
                            }
                        }
                    }
                    
                    physics.update(scene);
                    
                    // 3. Actualizar Cámara
                    if (renderer != null && renderer.getCamara() != null) {
                        renderer.getCamara().update(fixedDelta);
                        // Opcional: Clampear a los límites de la escena
                        if (scene != null) {
                            renderer.getCamara().clampToBounds(scene.getWidth(), scene.getHeight());
                        }
                    }
                }
                
                accumulator -= nsPerUpdate;
            }

            // Cálculo de interpolación (Alpha) para el renderizado
            float alpha = (float) (accumulator / nsPerUpdate);

            // Bloque de renderizado
            if (canvas != null && Engine.isPlaying()) {
                Time.setInterpolation(alpha);
                canvas.repaint();
            }

            // Sincronizamos el estado de la entrada para el próximo frame
            InputManager.update();

            // Control de carga de CPU
            try {
                // Intentamos dejar un poco de aire al sistema
                long sleepTime = (long)((nsPerUpdate - (System.nanoTime() - now)) / 1_000_000);
                if (sleepTime > 0) Thread.sleep(sleepTime);
                else Thread.yield();
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