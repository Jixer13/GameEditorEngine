package org.motor2d.manager;

import org.motor2d.model.Project;
import org.motor2d.model.Scene;
import org.motor2d.serialization.Serializer;

import java.io.File;
import java.io.IOException;

public class ProjectManager {

    private Project currentProject;    // proyecto actualmente abierto
    private String currentProjectPath; // ruta en disco del proyecto actual
    private final Serializer serializer;

    public ProjectManager() {
        this.currentProject = null;
        this.currentProjectPath = null;
        this.serializer = new Serializer();
    }

    // CREAR PROYECTO
    public Project createProject(String name, String path) throws IOException {
        //Crear el objeto proyecto con valores por defecto
        Project project = new Project();
        project.setName(name);

        //Crear la estructura de carpetas en disco
        serializer.createProjectStructure(path);

        // Crear la escena inicial por defecto
        Scene mainScene = new Scene();
        mainScene.setName("main");

        // Guardar la escena inicial en disco
        serializer.saveScene(mainScene, path);

        // Guardar el project.json en disco
        serializer.saveProject(project, path);

        //Establece como proyecto actual
        this.currentProject = project;
        this.currentProjectPath = path;

        return project;
    }

    // ABRIR PROYECTO
    public Project openProject(String path) throws IOException {
        // Validaque existe project.json
        Project project = serializer.loadProject(path);

        //Establece como proyecto actual
        this.currentProject = project;
        this.currentProjectPath = path;

        return project;
    }

    // GUARDAR PROYECTO
    public void saveProject() throws IOException {
        checkProjectOpen();
        serializer.saveProject(currentProject, currentProjectPath);
    }

    // Guardar en una ruta distinta (como "Guardar como...")
    public void saveProjectAs(String newPath) throws IOException {
        checkProjectOpen();

        //Crear la nueva estructura de carpetas
        serializer.createProjectStructure(newPath);

        //Guardar el proyecto en la nueva ruta
        serializer.saveProject(currentProject, newPath);

        //Actualizar la ruta actual
        this.currentProjectPath = newPath;
    }

    // CERRAR PROYECTO
    public void closeProject() {
        this.currentProject = null;
        this.currentProjectPath = null;
    }

    public void deleteProject(String path) throws IOException {
        // 1. Validar que existe un project.json en esa ruta
        // para no borrar una carpeta cualquiera por error
        File projectFile = new File(path, "project.json");
        if (!projectFile.exists()) {
            throw new IOException("No se encontró un proyecto en: " + path);
        }

        // 2. Si es el proyecto actual, cerrarlo primero
        if (path.equals(currentProjectPath)) {
            closeProject();
        }

        // 3. Borrar toda la carpeta del proyecto
        deleteDirectory(new File(path));
    }

    // Elimina una carpeta y todo su contenido recursivamente
    private void deleteDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        if (!dir.delete()) {
            throw new IOException("No se pudo eliminar: " + dir.getAbsolutePath());
        }
    }



    // CONFIGURACIÓN DEL PROYECTO
    public void setProjectName(String name) throws IOException {
        checkProjectOpen();
        currentProject.setName(name);
        saveProject();
    }

    public void setWindowSize(int width, int height) throws IOException {
        checkProjectOpen();
        currentProject.getConfiguration().setWindowWidth(width);
        currentProject.getConfiguration().setWindowHeight(height);
        saveProject();
    }

    public void setFullScreen(boolean fullScreen) throws IOException {
        checkProjectOpen();
        currentProject.getConfiguration().setFullScreen(fullScreen);
        saveProject();
    }

    public void setFps(int fps) throws IOException {
        checkProjectOpen();
        currentProject.getConfiguration().setFps(fps);
        saveProject();
    }

    public void setInitialScene(String sceneName) throws IOException {
        checkProjectOpen();
        currentProject.setInitialScene("scenes/" + sceneName + ".json");
        saveProject();
    }

    // Getters
    public Project getCurrentProject() { return currentProject; }
    public String getCurrentProjectPath() { return currentProjectPath; }
    public boolean isProjectOpen() { return currentProject != null; }

    // Métodos privados de apoyo

    // Lanza excepción si no hay proyecto abierto
    // evita repetir este check en cada método
    private void checkProjectOpen() throws IOException {
        if (currentProject == null) {
            throw new IOException("No hay ningún proyecto abierto");
        }
    }
}