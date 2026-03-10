package org.motor2d.model;

import java.util.ArrayList;
import java.util.List;

public class Project {

    private String name;
    private String version;
    private String initialScene;        // nombre de la escena con la que arranca el juego
    private Configuration configuration;
    private List<Tileset> tilesets;     // assets de tilesets disponibles en el proyecto

    // Default
    public Project() {
        this.name = "New Project";
        this.version = "1.0";
        this.initialScene = "scenes/main.json";
        this.configuration = new Configuration();
        this.tilesets = new ArrayList<>();
    }

    public Project(String name, String version, String initialScene,
                   Configuration configuration, List<Tileset> tilesets) {
        this.name = name;
        this.version = version;
        this.initialScene = initialScene;
        this.configuration = configuration;
        this.tilesets = tilesets;
    }

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getInitialScene() { return initialScene; }
    public void setInitialScene(String initialScene) { this.initialScene = initialScene; }

    public Configuration getConfiguration() { return configuration; }
    public void setConfiguration(Configuration configuration) { this.configuration = configuration; }

    public List<Tileset> getTilesets() { return tilesets; }
    public void setTilesets(List<Tileset> tilesets) { this.tilesets = tilesets; }

    // Métodos helper
    public void addTileset(Tileset tileset) {
        this.tilesets.add(tileset);
    }

    public void removeTileset(Tileset tileset) {
        this.tilesets.remove(tileset);
    }


    public Tileset getTilesetByName(String name) {// Buscar un tileset por nombre
        for (Tileset tileset : tilesets) {
            if (tileset.getName().equals(name)) return tileset;
        }
        return null;
    }
}