package org.motor2d.serialization;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.motor2d.model.Project;
import org.motor2d.model.Scene;
import org.motor2d.model.components.*;
import org.motor2d.model.ui.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Serializer {

    // Archivos y carpetas del proyecto
    private static final String PROJECT_FILE    = "project.json";
    private static final String SCENES_FOLDER   = "scenes";
    private static final String ASSETS_FOLDER   = "assets";
    private static final String PREFABS_FOLDER  = "assets/prefabs";
    private static final String SPRITES_FOLDER  = "assets/sprites";
    private static final String AUDIO_FOLDER    = "assets/audio";
    private static final String FONTS_FOLDER    = "assets/fonts";
    private static final String TILESETS_FOLDER = "assets/tilesets";

    private final ObjectMapper mapper;

    public Serializer() {
        this.mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Subtipos de Component
        mapper.registerSubtypes(
                Transform.class,
                SpriteRenderer.class,
                Collider.class,
                Animation.class,
                Behavior.class
        );

        // Subtipos de UIElement
        mapper.registerSubtypes(
                UILabel.class,
                UIButton.class,
                UIImage.class
        );
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    // CREAR PROYECTO NUEVO
    public void createProjectStructure(String projectPath) throws IOException {
        createFolder(projectPath, "");                // raíz
        createFolder(projectPath, SCENES_FOLDER);     // scenes/
        createFolder(projectPath, ASSETS_FOLDER);     // assets/
        createFolder(projectPath, PREFABS_FOLDER);    // assets/prefabs/
        createFolder(projectPath, SPRITES_FOLDER);    // assets/sprites/
        createFolder(projectPath, AUDIO_FOLDER);      // assets/audio/
        createFolder(projectPath, FONTS_FOLDER);      // assets/fonts/
        createFolder(projectPath, TILESETS_FOLDER);   // assets/tilesets/
    }


    // PROJECT

    public void saveProject(Project project, String projectPath) throws IOException {
        File file = new File(projectPath, PROJECT_FILE);
        ensureDirectoryExists(file.getParentFile());
        mapper.writeValue(file, project);
    }

    public Project loadProject(String projectPath) throws IOException {
        File file = new File(projectPath, PROJECT_FILE);
        if (!file.exists()) {
            throw new IOException("No se encontró project.json en: " + projectPath);
        }
        Project project = mapper.readValue(file, Project.class);
        project.setPath(projectPath); // Aseguramos que el objeto conoce su ubicación en disco
        return project;
    }

    // PREFABS
    public void savePrefab(org.motor2d.model.Entity entity, String projectPath) throws IOException {
        File file = new File(projectPath + File.separator + PREFABS_FOLDER, entity.getName() + ".json");
        ensureDirectoryExists(file.getParentFile());
        mapper.writeValue(file, entity);
    }

    // SCENES
    public void saveScene(Scene scene, String projectPath) throws IOException {
        File file = getSceneFile(projectPath, scene.getName());
        ensureDirectoryExists(file.getParentFile());
        mapper.writeValue(file, scene);
    }

    public Scene loadScene(String projectPath, String sceneName) throws IOException {
        File file = getSceneFile(projectPath, sceneName);
        if (!file.exists()) {
            throw new IOException("No se encontró la escena: " + sceneName);
        }
        return mapper.readValue(file, Scene.class);
    }

    public boolean deleteScene(String projectPath, String sceneName) {
        File file = getSceneFile(projectPath, sceneName);
        return file.exists() && file.delete();
    }

    // Devuelve los nombres de todas las escenas disponibles en el proyecto
    public List<String> listScenes(String projectPath) {
        List<String> sceneNames = new ArrayList<>();
        File scenesDir = new File(projectPath, SCENES_FOLDER);

        if (!scenesDir.exists()) return sceneNames;

        File[] files = scenesDir.listFiles(
                (dir, name) -> name.endsWith(".json") // solo archivos .json
        );

        if (files != null) {
            for (File file : files) {
                // "main.json" → "main"
                sceneNames.add(file.getName().replace(".json", ""));
            }
        }

        return sceneNames;
    }


    // ASSETS — listar recursos disponibles por tipo

    public List<String> listSprites(String projectPath) {
        return listAssets(projectPath, SPRITES_FOLDER,
                ".png", ".jpg", ".jpeg");
    }

    public List<String> listAudio(String projectPath) {
        return listAssets(projectPath, AUDIO_FOLDER,
                ".mp3", ".wav", ".ogg");
    }

    public List<String> listFonts(String projectPath) {
        return listAssets(projectPath, FONTS_FOLDER,
                ".ttf", ".otf");
    }

    public List<String> listTilesets(String projectPath) {
        return listAssets(projectPath, TILESETS_FOLDER,
                ".png", ".jpg", ".jpeg");
    }

    // VALIDACIÓN DE PATHS
    // Comprueba que un path de asset existe en el proyecto
    public boolean assetExists(String projectPath, String assetPath) {
        File file = new File(projectPath, assetPath);
        return file.exists();
    }

    // Convierte un path absoluto a relativo al proyecto
    // "/mis-proyectos/juego/assets/sprites/player.png"
    // → "assets/sprites/player.png"
    public String toRelativePath(String projectPath, String absolutePath) {
        return absolutePath.replace(projectPath + File.separator, "");
    }

    // Métodos privados de apoyo
    private List<String> listAssets(String projectPath, String folder, String... extensions) {
        List<String> assets = new ArrayList<>();
        File dir = new File(projectPath, folder);

        if (!dir.exists()) return assets;

        File[] files = dir.listFiles((d, name) -> {
            for (String ext : extensions) {
                if (name.toLowerCase().endsWith(ext)) return true;
            }
            return false;
        });

        if (files != null) {
            for (File file : files) {
                // Guardar el path relativo al proyecto
                // "assets/sprites/player.png"
                assets.add(folder + "/" + file.getName());
            }
        }

        return assets;
    }

    private File getSceneFile(String projectPath, String sceneName) {
        return new File(projectPath + File.separator + SCENES_FOLDER,
                sceneName + ".json");
    }

    private void createFolder(String projectPath, String folderName) {
        File folder = folderName.isEmpty()
                ? new File(projectPath)
                : new File(projectPath, folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    private void ensureDirectoryExists(File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}