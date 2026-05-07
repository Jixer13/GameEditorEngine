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

    private static float fixedDeltaTime = 0.016f;
    private static float interpolation = 0f;

    /**
     * Establece el delta de tiempo fijo para la física.
     */
    public static void setFixedDeltaTime(float dt) {
        fixedDeltaTime = dt;
        totalTime += dt;
        // Opcional: actualizar contadores de FPS aquí o en el render
    }

    /**
     * Establece el factor de interpolación para el renderizado.
     */
    public static void setInterpolation(float alpha) {
        interpolation = alpha;
    }

    public static float getFixedDeltaTime() { return fixedDeltaTime; }
    public static float getInterpolation() { return interpolation; }
    public static float getTotalTime() { return totalTime; }
    public static int getFps() { return fps; }
}