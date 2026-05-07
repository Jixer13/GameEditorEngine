package org.motor2d.manager;

import org.motor2d.model.Entity;
import org.motor2d.model.components.Component;
import org.motor2d.model.components.Transform;
import org.motor2d.serialization.Serializer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PrefabManager - Gestiona la carga y clonación de plantillas de entidades (Prefabs).
 */
public class PrefabManager {

    private final String projectPath;
    private final Serializer serializer;
    private final Map<String, Entity> prefabs;

    public PrefabManager(String projectPath) {
        this.projectPath = projectPath;
        this.serializer = new Serializer();
        this.prefabs = new HashMap<>();
    }

    /**
     * Carga un prefab desde un archivo JSON.
     * @param relativePath Ruta relativa al proyecto (ej: "assets/prefabs/bullet.json")
     */
    public void loadPrefab(String relativePath) throws IOException {
        File file = new File(projectPath, relativePath);
        if (!file.exists()) throw new IOException("Prefab no encontrado: " + relativePath);
        
        Entity prefab = serializer.getMapper().readValue(file, Entity.class);
        prefabs.put(relativePath, prefab);
    }

    /**
     * Crea una instancia de un prefab cargado.
     */
    public Entity instantiate(String relativePath, org.motor2d.model.Scene scene) {
        Entity template = prefabs.get(relativePath);
        if (template == null) {
            try {
                loadPrefab(relativePath);
                template = prefabs.get(relativePath);
            } catch (IOException e) {
                System.err.println("Error al instanciar prefab: " + e.getMessage());
                return null;
            }
        }

        // Clonación profunda de la entidad
        Entity instance = new Entity();
        instance.setName(template.getName() + "_clone");
        instance.setTag(template.getTag());

        for (Component comp : template.getComponents()) {
            try {
                // Usamos el serializador para clonar el componente de forma profunda
                String json = serializer.getMapper().writeValueAsString(comp);
                Component clone = serializer.getMapper().readValue(json, comp.getClass());
                instance.addComponent(clone);
                
                // Si es un Transform, lo registramos en el sistema de la escena
                if (clone instanceof Transform t) {
                    t.registerInSystem(scene.getTransformSystem());
                }
            } catch (Exception e) {
                System.err.println("Error al clonar componente: " + e.getMessage());
            }
        }

        scene.addEntity(instance);
        return instance;
    }
}