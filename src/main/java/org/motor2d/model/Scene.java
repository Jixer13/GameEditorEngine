package org.motor2d.model;

import org.motor2d.model.ui.UIElement;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    private String name;
    private List<Entity> entities;
    private List<UIElement> uiElements;
    private Tilemap tilemap;
    private List<Audio> backgroundMusic;
    private String backgroundColor;
    private int width;
    private int height;

    //default
    public Scene() {
        this.name = "Scene";
        this.entities = new ArrayList<>();
        this.uiElements = new ArrayList<>();
        this.tilemap = null; //if is something like a menu it does not have tiles
        this.backgroundMusic = new ArrayList<>();
        this.backgroundColor = "#FFFFFF";
        this.width = 3000;
        this.height = 1000;

    }

    public Scene(String name, ArrayList<Entity> entities, ArrayList<UIElement> uiElements, Tilemap tilemap,
                 ArrayList<Audio> backgroundMusic, String backgroundColor, int width, int height) {
        this.name = name;
        this.entities = entities;
        this.uiElements = uiElements;
        this.tilemap = tilemap;
        this.backgroundMusic = backgroundMusic;
        this.backgroundColor = backgroundColor;
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(ArrayList<Entity> entities) {
        this.entities = entities;
    }

    public List<UIElement> getUiElements() {
        return uiElements;
    }

    public void setUiElements(ArrayList<UIElement> uiElements) {
        this.uiElements = uiElements;
    }

    public Tilemap getTilemap() {
        return tilemap;
    }

    public void setTilemap(Tilemap tilemap) {
        this.tilemap = tilemap;
    }

    public List<Audio> getBackgroundMusic() {
        return backgroundMusic;
    }

    public void setBackgroundMusic(ArrayList<Audio> backgroundMusic) {
        this.backgroundMusic = backgroundMusic;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void addEntity(Entity entity) {
        this.entities.add(entity);
    }

    public void removeEntity(Entity entity) {
        this.entities.remove(entity);
    }

    public void addUIElement(UIElement uiElement) {
        this.uiElements.add(uiElement);
    }
    public void removeUIElement(UIElement uiElement) {
        this.uiElements.remove(uiElement);
    }

    public void addAudio(Audio audio) {
        this.backgroundMusic.add(audio);
    }
    public void removeAudio(Audio audio) {
        this.backgroundMusic.remove(audio);
    }
}
