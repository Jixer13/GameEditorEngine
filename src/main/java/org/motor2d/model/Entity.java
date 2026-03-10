package org.motor2d.model;

import org.motor2d.model.components.Component;

import java.util.ArrayList;
import java.util.List;

public class Entity {

    // Contador estático para IDs únicos
    private static int nextId = 0;

    // Identificación
    private int id;
    private String name;
    private String tag;

    // Estado
    private boolean active;

    // Lista de componentes
    private List<Component> components;

    public Entity() {
        this.id = nextId++;
        this.name = "Entity";
        this.tag = "";
        this.active = true;
        this.components = new ArrayList<>();
    }

    public int getId() { return id; }

    public static void resetIdCounter(int value){
        nextId = value;
    }
    public void setId(int id) {
        this.id = id;
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<Component> getComponents() { return components; }
    public void setComponents(List<Component> components) { this.components = components; }


    // Métodos auxiliares---
    public void addComponent(Component component) {
        component.setOwner(this); // le decimos al componente quién es su dueño
        components.add(component);
    }

    public <T extends Component> T getComponent(Class<T> type) {
        for (Component component : components) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
        }
        return null; // si no tiene ese componente devuelve null
    }

    public <T extends Component> void removeComponent(Class<T> type) {
        components.removeIf(component -> type.isInstance(component));
    }

    public <T extends Component> boolean hasComponent(Class<T> type) {
        return getComponent(type) != null;
    }


}