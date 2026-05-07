package org.motor2d.model.components;

public class Transform extends Component {

    @com.fasterxml.jackson.annotation.JsonIgnore
    private int transformId = -1;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private org.motor2d.ecs.TransformSystem system;

    private float x;
    private float y;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private float prevX;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private float prevY;
    private float scaleX;
    private float scaleY;
    private float rotation;

    public Transform() {
        super();
        this.x = 0;
        this.y = 0;
        this.prevX = 0;
        this.prevY = 0;
        this.scaleX = 1;
        this.scaleY = 1;
        this.rotation = 0;
    }

    /**
     * Registra este componente en el sistema de transformaciones para alto rendimiento.
     */
    public void registerInSystem(org.motor2d.ecs.TransformSystem system) {
        this.system = system;
        this.transformId = system.createTransform();
        syncToSystem();
    }

    private void syncToSystem() {
        if (system != null && transformId != -1) {
            system.setPosition(transformId, x, y);
            system.setRotation(transformId, rotation);
        }
    }

    // Getters y Setters actualizados para usar el sistema si existe
    public float getX() { 
        return (system != null) ? system.getX(transformId) : x; 
    }
    
    public void setX(float x) { 
        this.x = x; 
        if (system != null) system.setPosition(transformId, x, getY());
    }

    public float getY() { 
        return (system != null) ? system.getY(transformId) : y; 
    }
    
    public void setY(float y) { 
        this.y = y; 
        if (system != null) system.setPosition(transformId, getX(), y);
    }

    public void updatePrevious() {
        if (system != null) {
            // El sistema actualiza todos a la vez, pero permitimos llamada individual
        } else {
            this.prevX = this.x;
            this.prevY = this.y;
        }
    }

    public float getInterpolatedX(float alpha) {
        return (system != null) ? system.getInterpolatedX(transformId, alpha) : (prevX + (x - prevX) * alpha);
    }

    public float getInterpolatedY(float alpha) {
        return (system != null) ? system.getInterpolatedY(transformId, alpha) : (prevY + (y - prevY) * alpha);
    }

    public float getScaleX() { 
        return (system != null) ? system.getScaleX(transformId) : scaleX; 
    }
    public void setScaleX(float scaleX) { this.scaleX = scaleX; }

    public float getScaleY() { 
        return (system != null) ? system.getScaleY(transformId) : scaleY; 
    }
    public void setScaleY(float scaleY) { this.scaleY = scaleY; }

    public float getRotation() { 
        return (system != null) ? system.getRotation(transformId) : rotation; 
    }
    public void setRotation(float rotation) { 
        this.rotation = rotation; 
        if (system != null) system.setRotation(transformId, rotation);
    }
}