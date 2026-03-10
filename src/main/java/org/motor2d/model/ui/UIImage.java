package org.motor2d.model.ui;

public class UIImage extends UIElement {

    private String imagePath;
    private boolean maintainAspectRatio;

    public UIImage() {
        super();
        setName("Image" + getId());
        this.imagePath = "";
        this.maintainAspectRatio = true;
    }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public boolean isMaintainAspectRatio() { return maintainAspectRatio; }
    public void setMaintainAspectRatio(boolean maintainAspectRatio) {
        this.maintainAspectRatio = maintainAspectRatio;
    }
}