package org.motor2d.model;

//Ventana
public class Configuration {
    private String title;
    private int windowWidth;
    private int windowHeight;
    private boolean fullScreen;
    private int fps;

    // Constructor por defecto
    public Configuration() {
        this.title = "New Project";
        this.windowWidth = 1280;
        this.windowHeight = 720;
        this.fullScreen = false;
        this.fps = 60;
    }

    // Constructor personalizado
    public Configuration(String title, int windowWidth, int windowHeight, boolean fullScreen, int fps) {
        this.title = title;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.fullScreen = fullScreen;
        this.fps = fps;
    }

    public String getTitle() {
        return title;
    }

    public void setTutle(String tutle) {
        this.title = tutle;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }
}
