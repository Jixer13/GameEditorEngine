package org.motor2d.model;

public class Project {
    private String name;
    private String version;
    private String initialScene;
    private Configuration configuration;


    //default
    public Project() {
        this.name = "New Project";
        this.version = "1.0";
        this.initialScene = "scenes/main.json";
        this.configuration = new Configuration();
    }

    public Project(String name, String version, String initialScene, Configuration configuration) {
        this.name = name;
        this.version = version;
        this.initialScene = initialScene;
        this.configuration = configuration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInitialScene() {
        return initialScene;
    }

    public void setInitialScene(String initialScene) {
        this.initialScene = initialScene;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
