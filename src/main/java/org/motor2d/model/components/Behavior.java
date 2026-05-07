package org.motor2d.model.components;

import org.motor2d.core.InputManager;

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
     * Se llama en cada frame fijo (FixedUpdate).
     */
    public abstract void update();

    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }

    // Helpers rápidos para el programador
    protected boolean isKeyDown(int key) { return InputManager.isKeyDown(key); }
    protected boolean isKeyPressed(int key) { return InputManager.isKeyPressed(key); }

    protected void playSound(String path, float volume) {
        org.motor2d.core.Engine.getAudioManager().playSound(path, volume);
    }

    protected void playMusic(String path, float volume, boolean loop) {
        org.motor2d.core.Engine.getAudioManager().playMusic(path, volume, loop);
    }

    protected void stopMusic() {
        org.motor2d.core.Engine.getAudioManager().stopMusic();
    }

    // Cámara
    protected void setCameraTarget(org.motor2d.model.Entity target) {
        org.motor2d.core.Engine.getCamara().setTarget(target);
    }

    protected void setCameraLerpSpeed(float speed) {
        org.motor2d.core.Engine.getCamara().setLerpSpeed(speed);
    }

    // Instanciación (Prefabs)
    protected org.motor2d.model.Entity instantiate(String prefabPath) {
        return org.motor2d.core.Engine.getPrefabManager().instantiate(
                prefabPath, org.motor2d.core.Engine.getCurrentScene());
    }
}