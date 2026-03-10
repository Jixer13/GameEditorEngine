package org.motor2d.model.ui;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = UILabel.class,  name = "Label"),
        @JsonSubTypes.Type(value = UIButton.class, name = "Button"),
        @JsonSubTypes.Type(value = UIImage.class,  name = "Image")
})
public abstract class UIElement {

    public enum Anchor {
        TOP_LEFT,    TOP_CENTER,    TOP_RIGHT,
        MIDDLE_LEFT, MIDDLE_CENTER, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    private static int nextId = 1;

    private int id;
    private String name;
    private float x;            // offset relativo al anchor
    private float y;
    private float width;
    private float height;
    private Anchor anchor;      // punto de referencia en la pantalla
    private boolean visible;
    private int zOrder;         // orden de dibujo, mayor = más adelante
    private List<UIElement> children; // jerarquía como en Godot/Unity

    public UIElement() {
        this.id = nextId++;
        this.name = "UIElement" + this.id;
        this.x = 0;
        this.y = 0;
        this.width = 100;
        this.height = 50;
        this.anchor = Anchor.TOP_LEFT;
        this.visible = true;
        this.zOrder = 0;
        this.children = new ArrayList<>();
    }

    // Mismo fix de id que en Entity y Tile
    public void setId(int id) {
        this.id = id;
        if (id >= nextId) nextId = id + 1;
    }

    public static void resetIdCounter(int value) { nextId = value; }

    // Métodos helper de jerarquía
    public void addChild(UIElement child) { this.children.add(child); }
    public void removeChild(UIElement child) { this.children.remove(child); }

    // Getters y Setters
    public int getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public Anchor getAnchor() { return anchor; }
    public void setAnchor(Anchor anchor) { this.anchor = anchor; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public int getZOrder() { return zOrder; }
    public void setZOrder(int zOrder) { this.zOrder = zOrder; }

    public List<UIElement> getChildren() { return children; }
    public void setChildren(List<UIElement> children) { this.children = children; }
}