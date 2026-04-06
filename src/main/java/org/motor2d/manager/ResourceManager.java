package org.motor2d.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ResourceManager {

    private final ProjectManager projectManager;
    private final org.motor2d.serialization.Serializer serializer;

    public enum ResourceType {
        SPRITE,
        AUDIO,
        FONT,
        TILESET
    }

    public ResourceManager(ProjectManager projectManager) {
        this.projectManager = projectManager;
        this.serializer = new org.motor2d.serialization.Serializer();
    }

    // IMPORTAR

    // Copia un archivo externo a la carpeta correcta del proyecto
    public String importResource(String absolutePath,
                                 ResourceType type) throws IOException {
        checkProjectOpen();

        File source = new File(absolutePath);
        if (!source.exists()) {
            throw new IOException("El archivo no existe: " + absolutePath);
        }

        // Validar extension segun el tipo
        validateExtension(source.getName(), type);

        // Carpeta destino segun el tipo
        String folder = getFolderForType(type);
        File destFolder = new File(projectManager.getCurrentProjectPath(), folder);
        File dest = new File(destFolder, source.getName());

        // Copiar el archivo
        Files.copy(source.toPath(), dest.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        // Devolver el path relativo al proyecto
        return folder + File.separator + source.getName();
    }

    // ELIMINAR

    public void deleteResource(String relativePath) throws IOException {
        checkProjectOpen();

        File file = new File(projectManager.getCurrentProjectPath(), relativePath);
        if (!file.exists()) {
            throw new IOException("El recurso no existe: " + relativePath);
        }

        if (!file.delete()) {
            throw new IOException("No se pudo eliminar el recurso: " + relativePath);
        }
    }

    // LISTAR

    public List<String> listSprites() throws IOException {
        checkProjectOpen();
        return serializer.listSprites(projectManager.getCurrentProjectPath());
    }

    public List<String> listAudio() throws IOException {
        checkProjectOpen();
        return serializer.listAudio(projectManager.getCurrentProjectPath());
    }

    public List<String> listFonts() throws IOException {
        checkProjectOpen();
        return serializer.listFonts(projectManager.getCurrentProjectPath());
    }

    public List<String> listTilesets() throws IOException {
        checkProjectOpen();
        return serializer.listTilesets(projectManager.getCurrentProjectPath());
    }

    public List<String> listByType(ResourceType type) throws IOException {
        return switch (type) {
            case SPRITE  -> listSprites();
            case AUDIO   -> listAudio();
            case FONT    -> listFonts();
            case TILESET -> listTilesets();
        };
    }

    // VALIDAR

    public boolean resourceExists(String relativePath) throws IOException {
        checkProjectOpen();
        return serializer.assetExists(
                projectManager.getCurrentProjectPath(), relativePath);
    }

    public String toRelativePath(String absolutePath) throws IOException {
        checkProjectOpen();
        return serializer.toRelativePath(
                projectManager.getCurrentProjectPath(), absolutePath);
    }

    // Privados

    private String getFolderForType(ResourceType type) {
        return switch (type) {
            case SPRITE  -> "assets" + File.separator + "sprites";
            case AUDIO   -> "assets" + File.separator + "audio";
            case FONT    -> "assets" + File.separator + "fonts";
            case TILESET -> "assets" + File.separator + "tilesets";
        };
    }

    private void validateExtension(String fileName,
                                   ResourceType type) throws IOException {
        String lower = fileName.toLowerCase();
        boolean valid = switch (type) {
            case SPRITE  -> lower.endsWith(".png") || lower.endsWith(".jpg")
                    || lower.endsWith(".jpeg");
            case AUDIO   -> lower.endsWith(".mp3") || lower.endsWith(".wav")
                    || lower.endsWith(".ogg");
            case FONT    -> lower.endsWith(".ttf") || lower.endsWith(".otf");
            case TILESET -> lower.endsWith(".png") || lower.endsWith(".jpg")
                    || lower.endsWith(".jpeg");
        };

        if (!valid) {
            throw new IOException("Extension no valida para " + type + ": " + fileName);
        }
    }

    private void checkProjectOpen() throws IOException {
        if (!projectManager.isProjectOpen()) {
            throw new IOException("No hay ningun proyecto abierto");
        }
    }
}