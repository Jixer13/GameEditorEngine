package org.motor2d.model.components;

public class SpriteRenderer extends Component {

    private String spritePath; // ruta a la imagen,
    private int frameWidth;    // ancho de cada frame
    private int frameHeight;   // alto de cada frame
    private int layer;         // capa de dibujo, mayor número = más adelante
    private boolean flipX;     // voltear horizontalmente
    private boolean flipY;     // voltear verticalmente

    public SpriteRenderer() {
        super(); //llama a component
        this.spritePath = "";
        this.frameWidth = 32;
        this.frameHeight = 32;
        this.layer = 0;
        this.flipX = false;
        this.flipY = false;
    }

    // Getters y Setters
    public String getSpritePath() { return spritePath; }
    public void setSpritePath(String spritePath) { this.spritePath = spritePath; }

    public int getFrameWidth() { return frameWidth; }
    public void setFrameWidth(int frameWidth) { this.frameWidth = frameWidth; }

    public int getFrameHeight() { return frameHeight; }
    public void setFrameHeight(int frameHeight) { this.frameHeight = frameHeight; }

    public int getLayer() { return layer; }
    public void setLayer(int layer) { this.layer = layer; }

    public boolean isFlipX() { return flipX; }
    public void setFlipX(boolean flipX) { this.flipX = flipX; }

    public boolean isFlipY() { return flipY; }
    public void setFlipY(boolean flipY) { this.flipY = flipY; }
}