package org.motor2d.graphics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Sprite {

    private static final Map<String, BufferedImage> cache = new HashMap<>();

    private BufferedImage image;
    private String path;
    private int width;
    private int height;

    private Sprite(String path, BufferedImage image) {
        this.path   = path;
        this.image  = image;
        this.width  = image.getWidth();
        this.height = image.getHeight();
    }

    // Carga una imagen del disco con cache
    public static Sprite load(String projectPath,
                              String relativePath) throws IOException {

        if (relativePath == null || relativePath.isBlank()) {
            throw new IOException("Ruta de sprite vacía");
        }

        File file = new File(relativePath);

        // Si NO existe como absoluta/relativa actual,
        // probar relativa al proyecto
        if (!file.exists()) {
            file = new File(projectPath, relativePath);
        }

        String fullPath = file.getAbsolutePath();

        if (cache.containsKey(fullPath)) {
            return new Sprite(fullPath, cache.get(fullPath));
        }

        if (!file.exists()) {
            throw new IOException("No se encontró la imagen: " + fullPath);
        }

        BufferedImage image = ImageIO.read(file);

        if (image == null) {
            throw new IOException("No se pudo leer la imagen: " + fullPath);
        }

        cache.put(fullPath, image);

        return new Sprite(fullPath, image);
    }

    public static void clearCache() { cache.clear(); }

    public BufferedImage getImage() { return image; }
    public String getPath()         { return path;  }
    public int getWidth()           { return width; }
    public int getHeight()          { return height;}
}