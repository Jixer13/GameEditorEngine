package org.motor2d.manager;

import org.motor2d.model.Entity;
import org.motor2d.model.Scene;
import org.motor2d.model.components.*;
import org.motor2d.model.ui.UIButton;
import org.motor2d.model.ui.UIElement;
import org.motor2d.model.ui.UIImage;
import org.motor2d.model.ui.UILabel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EntityManager {

    private final SceneManager sceneManager;

    public EntityManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    // Crear
    // Crea una entidad vacía con solo Transform
    public Entity createEntity(String name) throws IOException {
        checkSceneOpen();

        Entity entity = new Entity();
        entity.setName(name);

        // Toda entidad tiene Transform por defecto, como en Unity/Godot
        Transform transform = new Transform();
        transform.registerInSystem(sceneManager.getCurrentScene().getTransformSystem());
        entity.addComponent(transform);

        sceneManager.getCurrentScene().addEntity(entity);
        sceneManager.saveScene();

        return entity;
    }

    // Crea una entidad con SpriteRenderer ya configurado
    public Entity createSpriteEntity(String name, String spritePath) throws IOException {
        checkSceneOpen();

        Entity entity = new Entity();
        entity.setName(name);

        Transform transform = new Transform();
        transform.registerInSystem(sceneManager.getCurrentScene().getTransformSystem());
        entity.addComponent(transform);

        SpriteRenderer sprite = new SpriteRenderer();
        sprite.setSpritePath(spritePath);
        entity.addComponent(sprite);

        sceneManager.getCurrentScene().addEntity(entity);
        sceneManager.saveScene();

        return entity;
    }

    // ==================== UI ELEMENTS ====================
    
    public UILabel createUILabel(String name, String text) throws IOException {
        checkSceneOpen();
        UILabel label = new UILabel();
        label.setName(name);
        label.setText(text);
        label.setX(100);
        label.setY(100);
        addUIElement(label);
        return label;
    }

    public UIButton createUIButton(String name, String text) throws IOException {
        checkSceneOpen();
        UIButton button = new UIButton();
        button.setName(name);
        button.setText(text);
        button.setX(100);
        button.setY(150);
        button.setWidth(120);
        button.setHeight(40);
        addUIElement(button);
        return button;
    }

    public UIImage createUIImage(String name, String spritePath) throws IOException {
        checkSceneOpen();
        UIImage image = new UIImage();
        image.setName(name);
        image.setImagePath(spritePath);
        image.setX(100);
        image.setY(200);
        image.setWidth(100);
        image.setHeight(100);
        addUIElement(image);
        return image;
    }

    // Eliminar
    public void removeEntity(Entity entity) throws IOException {
        checkSceneOpen();
        sceneManager.getCurrentScene().removeEntity(entity);
        sceneManager.saveScene();
    }

    public void removeEntityById(int id) throws IOException {
        checkSceneOpen();
        Entity entity = getEntityById(id);
        if (entity == null) {
            throw new IOException("No se encontró la entidad con id: " + id);
        }
        removeEntity(entity);
    }

    //Buscar
    public Entity getEntityById(int id) throws IOException {
        checkSceneOpen();
        for (Entity entity : sceneManager.getCurrentScene().getEntities()) {
            if (entity.getId() == id) return entity;
        }
        return null;
    }

    public Entity getEntityByName(String name) throws IOException {
        checkSceneOpen();
        for (Entity entity : sceneManager.getCurrentScene().getEntities()) {
            if (entity.getName().equals(name)) return entity;
        }
        return null;
    }

    // Devuelve todas las entidades con un tag concreto
    // por ejemplo todas las que tienen tag "enemy"
    public List<Entity> getEntitiesByTag(String tag) throws IOException {
        checkSceneOpen();
        List<Entity> result = new ArrayList<>();
        for (Entity entity : sceneManager.getCurrentScene().getEntities()) {
            if (entity.getTag().equals(tag)) result.add(entity);
        }
        return result;
    }

    public List<Entity> getAllEntities() throws IOException {
        checkSceneOpen();
        return sceneManager.getCurrentScene().getEntities();
    }

    //Duplicar
    // Útil en el editor para copiar entidades existentes
    public Entity duplicateEntity(Entity original) throws IOException {
        checkSceneOpen();

        Entity copy = new Entity();
        copy.setName(original.getName() + "_copy");
        copy.setTag(original.getTag());
        copy.setActive(original.isActive());

        // Copiar componentes
        for (Component component : original.getComponents()) {
            if (component instanceof Transform t) {
                Transform newT = new Transform();
                newT.registerInSystem(sceneManager.getCurrentScene().getTransformSystem());
                // Pequeño offset para que no quede encima del original
                newT.setX(t.getX() + 10);
                newT.setY(t.getY() + 10);
                newT.setScaleX(t.getScaleX());
                newT.setScaleY(t.getScaleY());
                newT.setRotation(t.getRotation());
                copy.addComponent(newT);

            } else if (component instanceof SpriteRenderer s) {
                SpriteRenderer newS = new SpriteRenderer();
                newS.setSpritePath(s.getSpritePath());
                newS.setFrameWidth(s.getFrameWidth());
                newS.setFrameHeight(s.getFrameHeight());
                newS.setLayer(s.getLayer());
                newS.setFlipX(s.isFlipX());
                newS.setFlipY(s.isFlipY());
                copy.addComponent(newS);

            } else if (component instanceof Collider c) {
                Collider newC = new Collider();
                newC.setShape(c.getShape());
                newC.setOffsetX(c.getOffsetX());
                newC.setOffsetY(c.getOffsetY());
                newC.setWidth(c.getWidth());
                newC.setHeight(c.getHeight());
                newC.setRadius(c.getRadius());
                newC.setTrigger(c.isTrigger());
                copy.addComponent(newC);

            } else if (component instanceof Animation a) {
                Animation newA = new Animation();
                newA.setName(a.getName());
                newA.setFrames(new ArrayList<>(a.getFrames()));
                newA.setFrameDuration(a.getFrameDuration());
                newA.setLooping(a.isLooping());
                copy.addComponent(newA);
            }
        }
        sceneManager.getCurrentScene().addEntity(copy);
        sceneManager.saveScene();

        return copy;
    }

    // COMPONENTES
    public void addComponent(Entity entity, Component component) throws IOException {
        checkSceneOpen();

        // No permitir componentes duplicados, como Unity
        if (entity.hasComponent(component.getClass())) {
            throw new IOException("La entidad ya tiene un componente de tipo: "
                    + component.getClass().getSimpleName());
        }

        if (component instanceof Transform t) {
            t.registerInSystem(sceneManager.getCurrentScene().getTransformSystem());
        }

        entity.addComponent(component);
        sceneManager.saveScene();
    }

    public <T extends Component> void removeComponent(Entity entity,
                                                      Class<T> type) throws IOException {
        checkSceneOpen();

        // No se puede quitar el Transform, como en Unity
        if (type == Transform.class) {
            throw new IOException("No se puede eliminar el componente Transform");
        }

        entity.removeComponent(type);
        sceneManager.saveScene();
    }

    // UI ELEMENTS
    public void addUIElement(UIElement uiElement) throws IOException {
        checkSceneOpen();
        sceneManager.getCurrentScene().addUIElement(uiElement);
        sceneManager.saveScene();
    }

    public void removeUIElement(UIElement uiElement) throws IOException {
        checkSceneOpen();
        sceneManager.getCurrentScene().removeUIElement(uiElement);
        sceneManager.saveScene();
    }

    public UIElement getUIElementByName(String name) throws IOException {
        checkSceneOpen();
        for (UIElement element : sceneManager.getCurrentScene().getUiElements()) {
            if (element.getName().equals(name)) return element;
        }
        return null;
    }

    public List<UIElement> getAllUIElements() throws IOException {
        checkSceneOpen();
        return sceneManager.getCurrentScene().getUiElements();
    }

    // Métodos apoyo
    private void checkSceneOpen() throws IOException {
        if (!sceneManager.isSceneOpen()) {
            throw new IOException("No hay ninguna escena abierta");
        }
    }
}