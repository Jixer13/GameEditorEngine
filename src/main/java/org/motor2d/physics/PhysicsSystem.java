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
        float bx = transB.getX() + colB.getOffsetX();
        float by = transB.getY() + colB.getOffsetY();

        // Detección AABB estándar
        boolean colliding = ax < bx + colB.getWidth() &&
                            ax + colA.getWidth() > bx &&
                            ay < by + colB.getHeight() &&
                            ay + colA.getHeight() > by;

        return colliding ? new CollisionResult(a, b, true) : null;
    }

    /**
     * Resuelve la colisión separando las entidades (lógica básica de empuje).
     */
    private void resolveCollision(CollisionResult result) {
        // En una implementación avanzada, aquí se calcularía el vector de separación
        // y se aplicaría a los Transforms para que no se solapen.
    }
}