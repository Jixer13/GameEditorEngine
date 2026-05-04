package org.motor2d.physics;

import org.motor2d.model.Entity;

/**
 * CollisionResult - Almacena la información de un encuentro entre dos entidades.
 */
public class CollisionResult {
    private final Entity entityA;
    private final Entity entityB;
    private final boolean colliding;

    public CollisionResult(Entity a, Entity b, boolean colliding) {
        this.entityA = a;
        this.entityB = b;
        this.colliding = colliding;
    }

    public Entity getEntityA() { return entityA; }
    public Entity getEntityB() { return entityB; }
    public boolean isColliding() { return colliding; }
}