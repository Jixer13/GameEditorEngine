package org.motor2d.utilities;

public class GameColor {

    private int r;
    private int g;
    private int b;
    private int a;

    public GameColor() {
        this.r = 255;
        this.g = 255;
        this.b = 255;
        this.a = 255;
    }

    public GameColor(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public GameColor(int r, int g, int b, int a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
    }

    // Colores predefinidos
    public static GameColor white()       { return new GameColor(255, 255, 255); }
    public static GameColor black()       { return new GameColor(0,   0,   0  ); }
    public static GameColor red()         { return new GameColor(255, 0,   0  ); }
    public static GameColor green()       { return new GameColor(0,   255, 0  ); }
    public static GameColor blue()        { return new GameColor(0,   0,   255); }
    public static GameColor yellow()      { return new GameColor(255, 255, 0  ); }
    public static GameColor transparent() { return new GameColor(0,   0,   0, 0); }

    // Desde hex "#FFFFFF" o "#FFFFFFFF"
    public static GameColor fromHex(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        int a = hex.length() == 8
                ? Integer.parseInt(hex.substring(6, 8), 16)
                : 255;
        return new GameColor(r, g, b, a);
    }

    // A hex "#RRGGBB"
    public String toHex() {
        return String.format("#%02X%02X%02X", r, g, b);
    }

    // A hex con alpha "#RRGGBBAA"
    public String toHexWithAlpha() {
        return String.format("#%02X%02X%02X%02X", r, g, b, a);
    }

    // A float 0.0-1.0 para Graphics2D
    public float getRf() { return r / 255f; }
    public float getGf() { return g / 255f; }
    public float getBf() { return b / 255f; }
    public float getAf() { return a / 255f; }

    // Mezclar dos colores
    public GameColor mix(GameColor other, float t) {
        t = Math.max(0, Math.min(1, t));
        return new GameColor(
                (int)(r + (other.r - r) * t),
                (int)(g + (other.g - g) * t),
                (int)(b + (other.b - b) * t),
                (int)(a + (other.a - a) * t)
        );
    }

    // Cambiar solo el alpha
    public GameColor withAlpha(int alpha) {
        return new GameColor(r, g, b, clamp(alpha));
    }

    // Conversión para usar con Swing/AWT
    public java.awt.Color toAwtColor() {
        return new java.awt.Color(r, g, b, a);
    }

    public static GameColor fromAwtColor(java.awt.Color awtColor) {
        return new GameColor(
                awtColor.getRed(),
                awtColor.getGreen(),
                awtColor.getBlue(),
                awtColor.getAlpha()
        );
    }

    // Getters y Setters
    public int getR() { return r; }
    public void setR(int r) { this.r = clamp(r); }

    public int getG() { return g; }
    public void setG(int g) { this.g = clamp(g); }

    public int getB() { return b; }
    public void setB(int b) { this.b = clamp(b); }

    public int getA() { return a; }
    public void setA(int a) { this.a = clamp(a); }

    @Override
    public String toString() {
        return "GameColor(" + r + ", " + g + ", " + b + ", " + a + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GameColor other)) return false;
        return r == other.r && g == other.g &&
                b == other.b && a == other.a;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}