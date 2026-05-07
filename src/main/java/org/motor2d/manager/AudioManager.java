package org.motor2d.manager;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AudioManager - Gestor global de audio para música y efectos.
 */
public class AudioManager {

    private String projectPath;
    private Clip backgroundMusic;
    private final Map<String, AudioInputStream> cache;

    public AudioManager(String projectPath) {
        this.projectPath = projectPath;
        this.cache = new HashMap<>();
    }

    /**
     * Reproduce un efecto de sonido (SFX). Se puede solapar con otros.
     */
    public void playSound(String relativePath, float volume) {
        new Thread(() -> {
            try {
                File file = new File(projectPath, relativePath);
                if (!file.exists()) return;

                AudioInputStream stream = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                
                setVolume(clip, volume);
                clip.start();
                
                // Cerrar recursos cuando termine
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {
                System.err.println("Error al reproducir sonido: " + relativePath);
            }
        }).start();
    }

    /**
     * Reproduce música de fondo (BGM). Detiene la música anterior si existe.
     */
    public void playMusic(String relativePath, float volume, boolean loop) {
        stopMusic();
        try {
            File file = new File(projectPath, relativePath);
            if (!file.exists()) return;

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(stream);
            
            setVolume(backgroundMusic, volume);
            if (loop) {
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
            backgroundMusic.start();
        } catch (Exception e) {
            System.err.println("Error al reproducir música: " + relativePath);
        }
    }

    public void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
    }

    private void setVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            // Convertir lineal (0.0 a 1.0) a decibelios
            float db = (float) (Math.log(Math.max(volume, 0.0001)) / Math.log(10.0) * 20.0);
            gainControl.setValue(db);
        }
    }

    public void setProjectPath(String path) {
        this.projectPath = path;
    }
}