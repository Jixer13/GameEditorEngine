package org.motor2d.ecs;

import java.util.Arrays;

/**
 * TransformSystem - Gestiona los datos de transformación de forma contigua en memoria.
 * Sigue principios de Data-Oriented Design (DOD).
 */
public class TransformSystem {

    private static final int INITIAL_CAPACITY = 1024;
    
    // Almacenamos los datos en arrays de primitivos para localidad de datos
    private float[] x;
    private float[] y;
    private float[] prevX;
    private float[] prevY;
    private float[] rotation;
    private float[] scaleX;
    private float[] scaleY;
    
    private int size = 0;

    public TransformSystem() {
        x = new float[INITIAL_CAPACITY];
        y = new float[INITIAL_CAPACITY];
        prevX = new float[INITIAL_CAPACITY];
        prevY = new float[INITIAL_CAPACITY];
        rotation = new float[INITIAL_CAPACITY];
        scaleX = new float[INITIAL_CAPACITY];
        scaleY = new float[INITIAL_CAPACITY];
        Arrays.fill(scaleX, 1.0f);
        Arrays.fill(scaleY, 1.0f);
    }

    public int createTransform() {
        if (size >= x.length) grow();
        return size++;
    }

    private void grow() {
        int newCapacity = x.length * 2;
        x = Arrays.copyOf(x, newCapacity);
        y = Arrays.copyOf(y, newCapacity);
        prevX = Arrays.copyOf(prevX, newCapacity);
        prevY = Arrays.copyOf(prevY, newCapacity);
        rotation = Arrays.copyOf(rotation, newCapacity);
        scaleX = Arrays.copyOf(scaleX, newCapacity);
        scaleY = Arrays.copyOf(scaleY, newCapacity);
    }

    /**
     * Sincroniza todas las posiciones previas. Muy rápido por la localidad de datos.
     */
    public void updatePrevious() {
        System.arraycopy(x, 0, prevX, 0, size);
        System.arraycopy(y, 0, prevY, 0, size);
    }

    public void setPosition(int id, float nx, float ny) {
        x[id] = nx;
        y[id] = ny;
    }

    public void setRotation(int id, float r) {
        rotation[id] = r;
    }

    public void setScale(int id, float sx, float sy) {
        scaleX[id] = sx;
        scaleY[id] = sy;
    }

    public float getX(int id) { return x[id]; }
    public float getY(int id) { return y[id]; }
    
    public float getInterpolatedX(int id, float alpha) {
        return prevX[id] + (x[id] - prevX[id]) * alpha;
    }
    
    public float getInterpolatedY(int id, float alpha) {
        return prevY[id] + (y[id] - prevY[id]) * alpha;
    }

    public float getRotation(int id) { return rotation[id]; }
    public float getScaleX(int id) { return scaleX[id]; }
    public float getScaleY(int id) { return scaleY[id]; }
}