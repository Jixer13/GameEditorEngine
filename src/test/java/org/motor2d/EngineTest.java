package org.motor2d;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.motor2d.core.InputManager;
import org.motor2d.model.Project;
import org.motor2d.utilities.Vector2;
import java.awt.event.KeyEvent;
import javax.swing.JButton;

/**
 * EngineTest - Suite de pruebas unitarias para validar el núcleo del motor usando JUnit 5.
 */
public class EngineTest {

    @Test
    void testInputManagerRegistration() {
        InputManager input = new InputManager();
        JButton dummy = new JButton();
        
        // Simulamos pulsar la tecla 'A'
        KeyEvent press = new KeyEvent(dummy, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'A');
        input.keyPressed(press);
        
        assertTrue(InputManager.isKeyDown(KeyEvent.VK_A), "La tecla A debería detectarse como pulsada");
        
        // Simulamos soltar la tecla 'A'
        KeyEvent release = new KeyEvent(dummy, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'A');
        input.keyReleased(release);
        
        assertFalse(InputManager.isKeyDown(KeyEvent.VK_A), "La tecla A ya no debería estar pulsada tras soltarla");
    }

    @Test
    void testProjectDataPath() {
        Project project = new Project();
        String testPath = "C:/Proyectos/MiMotor2D";
        
        project.setPath(testPath);
        
        assertEquals(testPath, project.getPath(), "El path almacenado en el proyecto debe coincidir con el asignado para el correcto funcionamiento del Renderer");
    }

    @Test
    void testVectorProperties() {
        Vector2 zero = Vector2.zero();
        
        assertEquals(0.0f, zero.x, "La componente X de un vector zero debe ser 0.0");
        assertEquals(0.0f, zero.y, "La componente Y de un vector zero debe ser 0.0");
        
        Vector2 pos = new Vector2(15.5f, -10.2f);
        assertEquals(15.5f, pos.x);
        assertEquals(-10.2f, pos.y);
    }
}