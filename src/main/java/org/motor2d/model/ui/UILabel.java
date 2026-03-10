package org.motor2d.model.ui;

public class UILabel extends UIElement {

    private String text;
    private int fontSize;
    private String color;
    private String fontPath;

    public UILabel() {
        super();
        setName("Label" + getId());
        this.text = "Text";
        this.fontSize = 16;
        this.color = "#000000";
        this.fontPath = "";
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getFontSize() { return fontSize; }
    public void setFontSize(int fontSize) { this.fontSize = fontSize; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getFontPath() { return fontPath; }
    public void setFontPath(String fontPath) { this.fontPath = fontPath; }
}