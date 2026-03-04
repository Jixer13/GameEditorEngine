package org.motor2d.model;

import org.motor2d.model.components.Transform;

import java.util.HashMap;
import java.util.Map;

public class Entity {

    // Contador estático para IDs únicos
    private static int nextId = 1;

    // Identificación
    private int id;
    private String name;
    private String tag;

    // Transform (posición, rotación, escala)
    private Transform transform;

    // Visual
    private String sprite;
    private boolean visible;
    private int layer;

    // Estado
    private boolean active;

    // Propiedades personalizadas
    private Map<String, Object> properties;



    public Entity() {
        this.id = nextId++;
        this.name = "Entity";
        this.tag = "";
        this.transform = new Transform();
        this.sprite = null;
        this.visible = true;
        this.layer = 0;
        this.active = true;
        this.properties = new HashMap<>();
    }

    public Entity(String name, String tag) {
        this();
        this.name = name;
        this.tag = tag;
    }

    // ==================== MÉTODOS ESTÁTICOS PARA ID ====================

    public static void setNextId(int id) {
        nextId = id;
    }

    public static int getNextId() {
        return nextId;
    }

    // ==================== GETTERS Y SETTERS ====================

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }

    public Transform getTransform() {
        return transform;
    }
    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public String getSprite() {
        return sprite;
    }
    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    public boolean isVisible() {
        return visible;
    }
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getLayer() {
        return layer;
    }
    public void setLayer(int layer) {
        this.layer = layer;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    // ==================== MÉTODOS HELPER PARA PROPERTIES ====================

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    public Object getProperty(String key) {
        return properties.get(key);
    }
    public int getPropertyInt(String key, int defaultValue) {
        Object value = properties.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    public float getPropertyFloat(String key, float defaultValue) {
        Object value = properties.get(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return defaultValue;
    }
    public String getPropertyString(String key, String defaultValue) {
        Object value = properties.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }
    public boolean getPropertyBool(String key, boolean defaultValue) {
        Object value = properties.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    public void removeProperty(String key) {
        properties.remove(key);
    }
}