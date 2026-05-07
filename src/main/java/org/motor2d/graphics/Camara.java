package org.motor2d.graphics;

import org.motor2d.utilities.Vector2;

public class Camara {

    private Vector2 position;
    private float zoom;
    private int viewWidth;
    private int viewHeight;
    
    // Seguimiento suave
    private org.motor2d.model.Entity target;
    private float lerpSpeed = 5.0f; // Velocidad de seguimiento (0 = no se mueve, mayor = más rápido)

    public Camara(int viewWidth, int viewHeight) {
        this.position   = Vector2.zero();
        this.zoom       = 1.0f;
        this.viewWidth  = viewWidth;
        this.viewHeight = viewHeight;
    }

    /**
     * Actualiza la posición de la cámara siguiendo al objetivo con suavizado.
     */
    public void update(float deltaTime) {
        if (target == null) return;

        org.motor2d.model.components.Transform t = target.getComponent(org.motor2d.model.components.Transform.class);
        if (t == null) return;

        // Calculamos la posición deseada (centro del objetivo)
        float desiredX = t.getX() - (viewWidth / 2f / zoom);
        float desiredY = t.getY() - (viewHeight / 2f / zoom);

        // Aplicamos Interpolación Lineal (Lerp)
        position.x += (desiredX - position.x) * lerpSpeed * deltaTime;
        position.y += (desiredY - position.y) * lerpSpeed * deltaTime;
    }

    public void setTarget(org.motor2d.model.Entity target) {
        this.target = target;
    }

    public void setLerpSpeed(float speed) {
        this.lerpSpeed = speed;
    }

    // Mover la cámara
    public void move(float dx, float dy) {
        position = new Vector2(position.x + dx, position.y + dy);
    }

    public void moveTo(float x, float y) {
        position = new Vector2(x, y);
    }

    // Seguir a una entidad
    public void follow(float targetX, float targetY) {
        position = new Vector2(
                targetX - (viewWidth  / 2f / zoom),
                targetY - (viewHeight / 2f / zoom)
        );
    }

    // Limitar la cámara a los límites del mundo
    // llamar después de follow() o move()
    public void clampToBounds(int worldWidth, int worldHeight) {
        float maxX = worldWidth  - (viewWidth  / zoom);
        float maxY = worldHeight - (viewHeight / zoom);
        position.x = Math.max(0, Math.min(position.x, maxX));
        position.y = Math.max(0, Math.min(position.y, maxY));
    }

    // Conversiones mundo ↔ pantalla
    public float worldToScreenX(float worldX) {
        return (worldX - position.x) * zoom;
    }

    public float worldToScreenY(float worldY) {
        return (worldY - position.y) * zoom;
    }

    public float screenToWorldX(float screenX) {
        return screenX / zoom + position.x;
    }

    public float screenToWorldY(float screenY) {
        return screenY / zoom + position.y;
    }

    // Zoom
    public void setZoom(float zoom) {
        this.zoom = Math.max(0.1f, Math.min(10.0f, zoom));
    }

    public void zoomIn(float amount)  { setZoom(zoom + amount); }
    public void zoomOut(float amount) { setZoom(zoom - amount); }

    // Culling — comprobar si algo es visible antes de dibujarlo
    public boolean isVisible(float worldX, float worldY,
                             float width, float height) {
        float screenX = worldToScreenX(worldX);
        float screenY = worldToScreenY(worldY);
        return screenX + width  > 0 && screenX < viewWidth &&
                screenY + height > 0 && screenY < viewHeight;
    }

    // Getters y Setters
    public Vector2 getPosition()       { return position;   }
    public void setPosition(Vector2 p) { this.position = p; }

    public float getZoom()             { return zoom;        }

    public int getViewWidth()          { return viewWidth;   }
    public void setViewWidth(int w)    { this.viewWidth = w; }

    public int getViewHeight()         { return viewHeight;     }
    public void setViewHeight(int h)   { this.viewHeight = h;   }
}