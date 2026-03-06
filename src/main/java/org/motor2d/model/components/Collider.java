package org.motor2d.model.components;

public class Collider extends Component {

    // Enum para definir la forma del collider
    public enum ColliderShape {
        RECTANGLE,
        CIRCLE
    }

    private ColliderShape shape;
    private float offsetX;  // desplazamiento respecto al transform
    private float offsetY;
    private float width;    // para RECTANGLE
    private float height;   // para RECTANGLE
    private float radius;   // para CIRCLE
    private boolean isTrigger;

    public Collider() {
        super();
        this.shape = ColliderShape.RECTANGLE;
        this.offsetX = 0;
        this.offsetY = 0;
        this.width = 32;
        this.height = 32;
        this.radius = 16;
        this.isTrigger = false;
    }

    // Getters y Setters
    public ColliderShape getShape() { return shape; }
    public void setShape(ColliderShape shape) { this.shape = shape; }

    public float getOffsetX() { return offsetX; }
    public void setOffsetX(float offsetX) { this.offsetX = offsetX; }

    public float getOffsetY() { return offsetY; }
    public void setOffsetY(float offsetY) { this.offsetY = offsetY; }

    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public float getRadius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }

    public boolean isTrigger() { return isTrigger; }
    public void setTrigger(boolean trigger) { this.isTrigger = trigger; }
}