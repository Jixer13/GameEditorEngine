package org.motor2d.manager;

import org.motor2d.model.Scene;
import org.motor2d.model.Project;
import org.motor2d.serialization.Serializer;

import java.io.IOException;
import java.util.List;

public class SceneManager {

    private Scene currentScene;         // escena actualmente abierta en el editor
    private final Serializer serializer;
    private final ProjectManager projectManager;

    public SceneManager(ProjectManager projectManager) {
        this.currentScene = null;
        this.serializer = new Serializer();
        this.projectManager = projectManager;
    }

    // CREAR ESCENA
    public Scene createScene(String name) throws IOException {
        checkProjectOpen();
        checkSceneNameNotEmpty(name);

        // Comprobar que no existe ya una escena con ese nombre
        List<String> existing = listScenes();
        if (existing.contains(name)) {
            throw new IOException("Ya existe una escena con el nombre: " + name);
        }

        Scene scene = new Scene();
        scene.setName(name);

        // Guardar en disco inmediatamente
        serializer.saveScene(scene, projectManager.getCurrentProjectPath());

        // Establecer como escena actual
        this.currentScene = scene;

        return scene;
    }


    // CARGAR ESCENA
    public Scene loadScene(String name) throws IOException {
        checkProjectOpen();
        checkSceneNameNotEmpty(name);

        Scene scene = serializer.loadScene(
                projectManager.getCurrentProjectPath(), name);

        this.currentScene = scene;
        return scene;
    }

    // GUARDAR ESCENA

    public void saveScene() throws IOException {
        checkProjectOpen();
        checkSceneOpen();
        serializer.saveScene(currentScene, projectManager.getCurrentProjectPath());
    }

    // Guardar con nuevo nombre (como "Guardar como...")
    public void saveSceneAs(String newName) throws IOException {
        checkProjectOpen();
        checkSceneOpen();
        checkSceneNameNotEmpty(newName);

        currentScene.setName(newName);
        serializer.saveScene(currentScene, projectManager.getCurrentProjectPath());
    }

    // -------------------------------------------------------
    // ELIMINAR ESCENA
    // -------------------------------------------------------

    public void deleteScene(String name) throws IOException {
        checkProjectOpen();
        checkSceneNameNotEmpty(name);

        // No se puede borrar si es la escena inicial del proyecto
        String initialScene = projectManager.getCurrentProject().getInitialScene();
        if (initialScene.equals("scenes/" + name + ".json")) {
            throw new IOException(
                    "No se puede eliminar la escena inicial del proyecto: " + name);
        }

        // Si es la escena actual, cerrarla primero
        if (currentScene != null && currentScene.getName().equals(name)) {
            closeScene();
        }

        boolean deleted = serializer.deleteScene(
                projectManager.getCurrentProjectPath(), name);

        if (!deleted) {
            throw new IOException("No se pudo eliminar la escena: " + name);
        }
    }

    // -------------------------------------------------------
    // CERRAR ESCENA
    // -------------------------------------------------------

    public void closeScene() {
        this.currentScene = null;
    }

    // -------------------------------------------------------
    // LISTAR ESCENAS
    // -------------------------------------------------------

    public List<String> listScenes() throws IOException {
        checkProjectOpen();
        return serializer.listScenes(projectManager.getCurrentProjectPath());
    }

    // -------------------------------------------------------
    // CONFIGURACIÓN DE LA ESCENA
    // -------------------------------------------------------

    public void setSceneName(String name) throws IOException {
        checkSceneOpen();
        checkSceneNameNotEmpty(name);

        // Borrar el archivo antiguo
        serializer.deleteScene(
                projectManager.getCurrentProjectPath(),
                currentScene.getName());

        currentScene.setName(name);
        saveScene();
    }

    public void setSceneSize(int width, int height) throws IOException {
        checkSceneOpen();
        currentScene.setWidth(width);
        currentScene.setHeight(height);
        saveScene();
    }

    public void setBackgroundColor(String color) throws IOException {
        checkSceneOpen();
        currentScene.setBackgroundColor(color);
        saveScene();
    }

    // -------------------------------------------------------
    // Getters
    // -------------------------------------------------------

    public Scene getCurrentScene() { return currentScene; }
    public boolean isSceneOpen() { return currentScene != null; }

    // -------------------------------------------------------
    // Métodos privados de apoyo
    // -------------------------------------------------------

    private void checkProjectOpen() throws IOException {
        if (!projectManager.isProjectOpen()) {
            throw new IOException("No hay ningún proyecto abierto");
        }
    }

    private void checkSceneOpen() throws IOException {
        if (currentScene == null) {
            throw new IOException("No hay ninguna escena abierta");
        }
    }

    private void checkSceneNameNotEmpty(String name) throws IOException {
        if (name == null || name.trim().isEmpty()) {
            throw new IOException("El nombre de la escena no puede estar vacío");
        }
    }
}