package org.motor2d.model.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Animation - Gestiona múltiples secuencias de animación para una entidad.
 * Permite definir estados como "idle", "walk", "jump", etc.
 */
public class Animation extends Component {

    private Map<String, List<String>> sequences;
    private String currentSequence;
    private float frameDuration;
    private boolean looping;
    private boolean autoPlay;

    // Estado de la animación en runtime (no se guarda en JSON)
    private transient int currentFrame;
    private transient float frameTimer;
    private transient boolean playing;

    @JsonCreator
    public Animation() {
        super();
        this.sequences     = new HashMap<>();
        this.currentSequence = "default";
        this.sequences.put("default", new ArrayList<>()); // Inicializar secuencia por defecto
        this.frameDuration  = 0.1f;
        this.looping        = true;
        this.autoPlay       = true;
        
        // Inicializar runtime state
        resetRuntime();
    }

    /**
     * Sincroniza el estado de reproducción con la configuración.
     * Útil tras la deserialización o al cambiar parámetros en el editor.
     */
    public void resetRuntime() {
        this.currentFrame = 0;
        this.frameTimer   = 0;
        this.playing      = autoPlay;
    }

    @Override
    public void initialize() {
        resetRuntime();
    }

    // Control de reproducción
    public synchronized void play(String sequenceName) {
        if (sequences.containsKey(sequenceName)) {
            if (!sequenceName.equals(currentSequence) || !playing) {
                this.currentSequence = sequenceName;
                this.currentFrame = 0;
                this.frameTimer = 0;
                this.playing = true;
            }
        }
    }

    public synchronized void stop() {
        this.playing = false;
        this.currentFrame = 0;
    }

    public synchronized void update(float deltaTime) {
        if (!playing || sequences.isEmpty()) return;

        List<String> frames = sequences.get(currentSequence);
        if (frames == null || frames.isEmpty()) return;

        frameTimer += deltaTime;

        // Mientras el timer supere la duración, avanzamos frames
        // Esto evita que la animación se ralentice si el framerate baja
        while (frameTimer >= frameDuration && frameDuration > 0) {
            frameTimer -= frameDuration;
            currentFrame++;

            if (currentFrame >= frames.size()) {
                if (looping) {
                    currentFrame = 0;
                } else {
                    currentFrame = frames.size() - 1;
                    playing = false;
                    break;
                }
            }
        }
    }

    public synchronized String getCurrentFramePath() {
        List<String> frames = sequences.get(currentSequence);
        if (frames == null || frames.isEmpty()) return "";
        if (currentFrame >= frames.size()) currentFrame = 0;
        return frames.get(currentFrame);
    }

    // Getters y Setters
    public Map<String, List<String>> getSequences() { return sequences; }
    public void setSequences(Map<String, List<String>> sequences) { this.sequences = sequences; }

    public String getCurrentSequence() { return currentSequence; }
    public void setCurrentSequence(String currentSequence) { this.currentSequence = currentSequence; }

    public float getFrameDuration() { return frameDuration; }
    public void setFrameDuration(float frameDuration) { this.frameDuration = frameDuration; }

    public boolean isLooping() { return looping; }
    public void setLooping(boolean looping) { this.looping = looping; }

    public boolean isAutoPlay() { return autoPlay; }
    public void setAutoPlay(boolean autoPlay) { 
        this.autoPlay = autoPlay; 
        this.playing = autoPlay; // Sincronizar con el estado en vivo
    }

    public int getCurrentFrame() { return currentFrame; }
    public boolean isPlaying() { return playing; }

    // Helpers
    public void addSequence(String name) {
        if (!sequences.containsKey(name)) {
            sequences.put(name, new ArrayList<>());
        }
    }

    public void addFrame(String sequence, String framePath) {
        sequences.computeIfAbsent(sequence, k -> new ArrayList<>()).add(framePath);
    }
    
    public void removeSequence(String name) {
        sequences.remove(name);
    }
}
