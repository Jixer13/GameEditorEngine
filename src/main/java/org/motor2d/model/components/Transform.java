package org.motor2d.model.components;

public class Transform {

    private float x;
    private float y;
    private float scaleX;
    private float scaleY;
    private float rotation;

    public Transform() {
        this.x = 0;
        this.y = 0;
        this.rotation = 0;
        this.scaleX = 1;
        this.scaleY = 1;

    }

    public Transform(float x, float y) {
        this();  // Llama al constructor vacío primero (pone los valores por defecto)
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    // Métodos útiles
    public void move(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }

    public void moveTo(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
