package org.motor2d.model;

public class Audio {

    public enum AudioType {
        MUSIC,
        EFFECT
    }

    private String name;
    private String path;
    private float volume;
    private boolean looping;
    private AudioType type;

    public Audio() {
        this.name = "audio";
        this.path = "";
        this.volume = 1.0f;
        this.looping = false;
        this.type = AudioType.EFFECT;
    }

    public Audio(String name, String path, float volume, boolean looping, AudioType type) {
        this.name = name;
        this.path = path;
        this.volume = volume;
        this.looping = looping;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public AudioType getType() {
        return type;
    }

    public void setType(AudioType type) {
        this.type = type;
    }
}
