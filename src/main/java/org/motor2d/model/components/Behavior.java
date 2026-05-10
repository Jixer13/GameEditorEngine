package org.motor2d.model.components;

import org.motor2d.core.Engine;
import org.motor2d.core.InputManager;
import org.motor2d.model.Entity;

/**
 * Behavior - Clase base para la lógica personalizada de las entidades.
 * Similar a MonoBehaviour en Unity o Node._process en Godot.
 */
public abstract class Behavior extends Component {

    private boolean started = false;

    /**
     * Se llama una sola vez antes del primer frame de actualización.
     */
    public void start() {}

    /**
     * Se llama en cada frame de actualización.
     */
    public abstract void update(float deltaTime);

    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }

    protected Entity getEntity() { return getOwner(); }

    // Helpers rápidos para el programador
    protected boolean isKeyDown(int key) { return InputManager.isKeyDown(key); }
    protected boolean isKeyPressed(int key) { return InputManager.isKeyPressed(key); }

    protected void playSound(String path, float volume) {
        Engine.getAudioManager().playSound(path, volume);
    }

    protected void playMusic(String path, float volume, boolean loop) {
        Engine.getAudioManager().playMusic(path, volume, loop);
    }

    protected void stopMusic() {
        Engine.getAudioManager().stopMusic();
    }

    // Cámara
    protected void setCameraTarget(Entity target) {
        Engine.getCamara().setTarget(target);
    }

    protected void setCameraLerpSpeed(float speed) {
        Engine.getCamara().setLerpSpeed(speed);
    }

    @Override
    public void initialize() {
        this.started = false;
    }
    }