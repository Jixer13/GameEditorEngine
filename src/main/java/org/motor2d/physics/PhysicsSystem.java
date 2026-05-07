package org.motor2d.physics;

import org.motor2d.model.Entity;
import org.motor2d.model.Scene;
import org.motor2d.model.components.Collider;
import org.motor2d.model.components.Transform;
import org.motor2d.core.Time;

import java.util.List;

/**
 * PhysicsSystem - El motor de físicas encargado de resolver colisiones y movimiento.
 */
public class PhysicsSystem {

    /**
     * Ciclo de actualización de físicas.
     * Se encarga de procesar el movimiento y detectar colisiones entre entidades activas.
     */
    public void update(Scene scene) {
        if (scene == null) return;
        
        List<Entity> entities = scene.getEntities();

        // Resolución de colisiones mediante doble bucle (Optimizable en el futuro)
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Entity a = entities.get(i);
                Entity b = entities.get(j);

                // Solo procesamos si ambas están activas y tienen los componentes necesarios
                if (!a.isActive() || !b.isActive()) continue;

                CollisionResult result = checkCollision(a, b);
                if (result != null && result.isColliding()) {
                    resolveCollision(result);
                }
            }
        }
    }

    /**
     * Comprueba si dos entidades están colisionando usando sus cuadros delimitadores (AABB).
     * Calcula el vector de separación mínima (MTV).
     */
    public CollisionResult checkCollision(Entity a, Entity b) {
        Collider colA = a.getComponent(Collider.class);
        Collider colB = b.getComponent(Collider.class);
        Transform transA = a.getComponent(Transform.class);
        Transform transB = b.getComponent(Transform.class);

        // Si alguna no tiene física, no hay colisión
        if (colA == null || colB == null || transA == null || transB == null) return null;

        // Calculamos posiciones globales (entidad + offset del collider)
        float ax = transA.getX() + colA.getOffsetX();
        float ay = transA.getY() + colA.getOffsetY();
        float aw = colA.getWidth();
        float ah = colA.getHeight();

        float bx = transB.getX() + colB.getOffsetX();
        float by = transB.getY() + colB.getOffsetY();
        float bw = colB.getWidth();
        float bh = colB.getHeight();

        // Detección AABB estándar
        boolean colliding = ax < bx + bw &&
                            ax + aw > bx &&
                            ay < by + bh &&
                            ay + ah > by;

        if (!colliding) return null;

        // Calcular penetración en cada eje
        float overlapX = 0;
        float overlapY = 0;

        float distCenterX = (ax + aw / 2f) - (bx + bw / 2f);
        float distCenterY = (ay + ah / 2f) - (by + bh / 2f);
        float minDistanceX = (aw + bw) / 2f;
        float minDistanceY = (ah + bh) / 2f;

        overlapX = minDistanceX - Math.abs(distCenterX);
        overlapY = minDistanceY - Math.abs(distCenterY);

        // Solo nos interesa el eje de menor penetración para resolver
        if (overlapX < overlapY) {
            overlapY = 0;
            if (distCenterX < 0) overlapX *= -1;
        } else {
            overlapX = 0;
            if (distCenterY < 0) overlapY *= -1;
        }

        return new CollisionResult(a, b, true, overlapX, overlapY);
    }

    /**
     * Resuelve la colisión separando las entidades (lógica de empuje).
     */
    private void resolveCollision(CollisionResult result) {
        Entity a = result.getEntityA();
        Entity b = result.getEntityB();
        Collider colA = a.getComponent(Collider.class);
        Collider colB = b.getComponent(Collider.class);

        // Los triggers no se resuelven físicamente
        if (colA.isTrigger() || colB.isTrigger()) return;

        Transform transA = a.getComponent(Transform.class);
        Transform transB = b.getComponent(Transform.class);

        boolean staticA = colA.isStatic();
        boolean staticB = colB.isStatic();

        // Si ambas son estáticas, no hacemos nada
        if (staticA && staticB) return;

        float ox = result.getOverlapX();
        float oy = result.getOverlapY();

        if (staticA) {
            // Solo movemos B
            transB.setX(transB.getX() - ox);
            transB.setY(transB.getY() - oy);
        } else if (staticB) {
            // Solo movemos A
            transA.setX(transA.getX() + ox);
            transA.setY(transA.getY() + oy);
        } else {
            // Movemos ambas a la mitad
            transA.setX(transA.getX() + ox / 2f);
            transA.setY(transA.getY() + oy / 2f);
            transB.setX(transB.getX() - ox / 2f);
            transB.setY(transB.getY() - oy / 2f);
        }
    }
}