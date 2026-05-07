package org.motor2d.physics;

import org.motor2d.model.Entity;

/**
 * CollisionResult - Almacena la información de un encuentro entre dos entidades.
 */
public class CollisionResult {
    private final Entity entityA;
    private final Entity entityB;
    private final boolean colliding;
    private float overlapX;
    private float overlapY;

    public CollisionResult(Entity a, Entity b, boolean colliding, float overlapX, float overlapY) {
        this.entityA = a;
        this.entityB = b;
        this.colliding = colliding;
        this.overlapX = overlapX;
        this.overlapY = overlapY;
    }

    public Entity getEntityA() { return entityA; }
    public Entity getEntityB() { return entityB; }
    public boolean isColliding() { return colliding; }
    public float getOverlapX() { return overlapX; }
    public float getOverlapY() { return overlapY; }
}