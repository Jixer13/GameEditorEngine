package org.motor2d.model.components;

import java.util.ArrayList;
import java.util.List;

public class Animation extends Component {

    private String name;
    private List<String> frames;   // rutas de cada imagen
    private float frameDuration;   // segundos que dura cada frame
    private boolean looping;

    public Animation() {
        super();
        this.name = "animation";
        this.frames = new ArrayList<>();
        this.frameDuration = 0.1f; // 10 frames por segundo/ pero quizas es mejor 0.4 que son 25 por segundo, revisamos
        this.looping = true;
    }

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getFrames() { return frames; }
    public void setFrames(List<String> frames) { this.frames = frames; }

    public float getFrameDuration() { return frameDuration; }
    public void setFrameDuration(float frameDuration) { this.frameDuration = frameDuration; }

    public boolean isLooping() { return looping; }
    public void setLooping(boolean looping) { this.looping = looping; }

    // Métodos helper
    public void addFrame(String framePath) { this.frames.add(framePath); }
    public void removeFrame(String framePath) { this.frames.remove(framePath); }
}