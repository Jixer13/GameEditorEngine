package org.motor2d.model;

public class Tile {

    private static int nextId = 0;

    private int id;
    private String name;
    private String spritePath;
    private boolean solid;
    private boolean trigger;

    public Tile() {
        this.id = nextId++;
        this.name = "Tile" + this.id;
        this.spritePath = "";
        this.solid = false;
        this.trigger = false;
    }

    public static void resetIdCounter(int value) { nextId = value; }

    public int getId() { return id; }
    public void setId(int id) {
        this.id = id;
        if (id >= nextId) nextId = id + 1;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpritePath() { return spritePath; }
    public void setSpritePath(String spritePath) { this.spritePath = spritePath; }

    public boolean isSolid() { return solid; }
    public void setSolid(boolean solid) { this.solid = solid; }

    public boolean isTrigger() { return trigger; }
    public void setTrigger(boolean trigger) { this.trigger = trigger; }
}