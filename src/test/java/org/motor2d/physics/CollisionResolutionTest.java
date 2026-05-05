package org.motor2d.physics;

import org.junit.jupiter.api.Test;
import org.motor2d.model.Entity;
import org.motor2d.model.components.Collider;
import org.motor2d.model.components.Transform;
import org.motor2d.model.Scene;
import static org.junit.jupiter.api.Assertions.*;

public class CollisionResolutionTest {

    @Test
    void testCollisionResolutionSeparatesEntities() {
        PhysicsSystem physics = new PhysicsSystem();
        
        Entity a = new Entity();
        a.setName("A");
        Transform transA = new Transform();
        a.addComponent(transA);
        Collider colA = new Collider();
        colA.setWidth(32);
        colA.setHeight(32);
        a.addComponent(colA);
        transA.setX(10);
        transA.setY(0);

        Entity b = new Entity();
        b.setName("B");
        Transform transB = new Transform();
        b.addComponent(transB);
        Collider colB = new Collider();
        colB.setWidth(32);
        colB.setHeight(32);
        b.addComponent(colB);
        transB.setX(20); 
        transB.setY(0);

        // Creamos una escena y añadimos entidades
        Scene scene = new Scene();
        scene.addEntity(a);
        scene.addEntity(b);
        
        // Verificamos colisión inicial
        CollisionResult result = physics.checkCollision(a, b);
        assertNotNull(result);
        assertTrue(result.isColliding(), "Deberían estar colisionando inicialmente");

        // Ejecutamos la actualización de físicas
        physics.update(scene);
        
        // Comprobamos que ya no colisionan
        CollisionResult after = physics.checkCollision(a, b);
        assertNull(after, "Tras la resolución, ya no deberían colisionar");
        
        // Verificamos el movimiento de separación
        // A estaba en 10, B en 20. 
        // Centro A = 10 + 16 = 26
        // Centro B = 20 + 16 = 36
        // dx = 26 - 36 = -10
        // dy = 0 - 0 = 0
        // overlapX = 32 - |-10| = 22
        // overlapY = 32 - 0 = 32
        // Como overlapX < overlapY (22 < 32), separa en X.
        // dx < 0, por lo que transA.setX(transA.getX() - overlapX) -> 10 - 22 = -12
        assertEquals(-12.0f, transA.getX(), 0.01f);
    }
}