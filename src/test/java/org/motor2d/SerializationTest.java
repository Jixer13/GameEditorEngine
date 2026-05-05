package org.motor2d;

import org.junit.jupiter.api.Test;
import org.motor2d.model.Project;
import org.motor2d.serialization.Serializer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class SerializationTest {

    @Test
    void testProjectSerialization() throws IOException {
        Serializer serializer = new Serializer();
        Path tempDir = Files.createTempDirectory("motor2d-test-serialization");
        
        Project project = new Project();
        project.setName("Test Serialization");
        
        serializer.saveProject(project, tempDir.toString());
        
        Project loaded = serializer.loadProject(tempDir.toString());
        
        assertEquals(project.getName(), loaded.getName());
        // Clean up
        Files.deleteIfExists(tempDir.resolve("project.json"));
        Files.deleteIfExists(tempDir);
    }
}