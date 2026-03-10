package org.motor2d.model.ui;

public class UIButton extends UIElement {

    private String text;
    private int fontSize;
    private String textColor;
    private String normalColor;
    private String hoverColor;
    private String pressedColor;
    private String imagePath;

    public UIButton() {
        super();
        setName("Button" + getId());
        this.text = "Button";
        this.fontSize = 16;
        this.textColor = "#000000";
        this.normalColor = "#CCCCCC";
        this.hoverColor = "#AAAAAA";
        this.pressedColor = "#888888";
        this.imagePath = "";
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getFontSize() { return fontSize; }
    public void setFontSize(int fontSize) { this.fontSize = fontSize; }

    public String getTextColor() { return textColor; }
    public void setTextColor(String textColor) { this.textColor = textColor; }

    public String getNormalColor() { return normalColor; }
    public void setNormalColor(String normalColor) { this.normalColor = normalColor; }

    public String getHoverColor() { return hoverColor; }
    public void setHoverColor(String hoverColor) { this.hoverColor = hoverColor; }

    public String getPressedColor() { return pressedColor; }
    public void setPressedColor(String pressedColor) { this.pressedColor = pressedColor; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}