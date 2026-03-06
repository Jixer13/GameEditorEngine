package org.motor2d.model.components;

public class Transform extends Component {

    private float x;
    private float y;
    private float scaleX;
    private float scaleY;
    private float rotation;

    public Transform() {
        super();          // llama al constructor de Component
        this.x = 0;
        this.y = 0;
        this.scaleX = 1; // escala 1 = tamaño normal
        this.scaleY = 1;
        this.rotation = 0;
    }

    // Getters y Setters
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public float getScaleX() { return scaleX; }
    public void setScaleX(float scaleX) { this.scaleX = scaleX; }

    public float getScaleY() { return scaleY; }
    public void setScaleY(float scaleY) { this.scaleY = scaleY; }

    public float getRotation() { return rotation; }
    public void setRotation(float rotation) { this.rotation = rotation; }
}