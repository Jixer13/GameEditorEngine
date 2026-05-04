package org.motor2d.core;

/**
 * Time - Gestiona el tiempo del motor y el cálculo del deltaTime.
 * Permite que el movimiento sea independiente de los FPS.
 */
public class Time {

    private static float deltaTime = 0f;
    private static float totalTime = 0f;
    private static int   fps = 0;
    private static int   frameCount = 0;
    private static float fpsTimer = 0f;

    private static long lastFrameTime = 0L;

    /**
     * Inicializa el cronómetro del motor.
     */
    public static void start() {
        lastFrameTime = System.nanoTime();
        deltaTime = 0f;
        totalTime = 0f;
        fps = 0;
        frameCount = 0;
        fpsTimer = 0f;
    }

    /**
     * Actualiza los contadores de tiempo. Debe llamarse en cada frame.
     */
    public static void update() {
        long now = System.nanoTime();
        // Conversión de nanosegundos a segundos
        deltaTime = (now - lastFrameTime) / 1_000_000_000f;
        lastFrameTime = now;

        // Cap para evitar saltos enormes si el motor se congela (mínimo 20 FPS)
        deltaTime = Math.min(deltaTime, 0.05f);

        totalTime += deltaTime;
        fpsTimer += deltaTime;
        frameCount++;

        // Cálculo de FPS cada segundo
        if (fpsTimer >= 1f) {
            fps = frameCount;
            frameCount = 0;
            fpsTimer -= 1f;
        }
    }

    // ==================== GETTERS ====================
    public static float getDeltaTime() { return deltaTime; }
    public static float getTotalTime() { return totalTime; }
    public static int getFps() { return fps; }
}