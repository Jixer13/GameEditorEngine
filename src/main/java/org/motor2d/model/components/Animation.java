package org.motor2d.model.components;

import java.util.ArrayList;
import java.util.List;

public class Animation extends Component {

    private String name;
    private List<String> frames;
    private float frameDuration;
    private boolean looping;

    // Estado de la animación en runtime
    // no se guarda en JSON, se calcula en tiempo de ejecución
    private transient int currentFrame;
    private transient float frameTimer;
    private transient boolean playing;

    public Animation() {
        super();
        this.name          = "animation";
        this.frames        = new ArrayList<>();
        this.frameDuration = 0.1f; // 10 FPS por defecto
        this.looping       = true;
        this.currentFrame  = 0;
        this.frameTimer    = 0;
        this.playing       = false;
    }

    // Control de reproducción
    public void play() {
        this.playing      = true;
        this.currentFrame = 0;
        this.frameTimer   = 0;
    }

    public void stop() {
        this.playing      = false;
        this.currentFrame = 0;
        this.frameTimer   = 0;
    }

    public void pause() {
        this.playing = false;
    }

    public void resume() {
        this.playing = true;
    }

    // Avanza la animación según el tiempo transcurrido
    // llamado por el GameLoop en cada frame
    public void update(float deltaTime) {
        if (!playing || frames.isEmpty()) return;

        frameTimer += deltaTime;

        if (frameTimer >= frameDuration) {
            frameTimer = 0;
            currentFrame++;

            if (currentFrame >= frames.size()) {
                if (looping) {
                    currentFrame = 0;
                } else {
                    currentFrame = frames.size() - 1;
                    playing = false;
                }
            }
        }
    }

    // Devuelve la ruta del frame actual
    public String getCurrentFramePath() {
        if (frames.isEmpty()) return "";
        return frames.get(currentFrame);
    }

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getFrames() { return frames; }
    public void setFrames(List<String> frames) { this.frames = frames; }

    public float getFrameDuration() { return frameDuration; }
    public void setFrameDuration(float frameDuration) {
        this.frameDuration = frameDuration;
    }

    public boolean isLooping() { return looping; }
    public void setLooping(boolean looping) { this.looping = looping; }

    public int getCurrentFrame() { return currentFrame; }
    public boolean isPlaying()   { return playing; }

    // Helpers
    public void addFrame(String framePath) { this.frames.add(framePath); }
    public void removeFrame(String framePath) { this.frames.remove(framePath); }
    public int getFrameCount() { return frames.size(); }
}