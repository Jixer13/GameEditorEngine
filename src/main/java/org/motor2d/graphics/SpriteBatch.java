package org.motor2d.graphics;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * SpriteBatch - Mantiene una lista de comandos de dibujo para respetar el orden secuencial.
 */
public class SpriteBatch {

    private final Graphics2D g2;
    private final List<DrawCall> drawCalls;
    private boolean isDrawing = false;

    public SpriteBatch(Graphics2D g2) {
        this.g2 = g2;
        this.drawCalls = new ArrayList<>();
    }

    public Graphics2D getGraphics() {
        return g2;
    }

    public void begin() {
        if (isDrawing) throw new IllegalStateException("Batch already drawing");
        isDrawing = true;
        drawCalls.clear();
    }

    public void draw(Image image, float x, float y, float width, float height, 
                     float rotation, float scaleX, float scaleY, float opacity) {
        if (!isDrawing) throw new IllegalStateException("Must call begin() before draw()");
        if (image == null) return;
        
        drawCalls.add(new DrawCall(image, x, y, width, height, rotation, scaleX, scaleY, opacity));
    }

    public void end() {
        if (!isDrawing) throw new IllegalStateException("Must call begin() before end()");
        
        for (DrawCall call : drawCalls) {
            renderCall(call.image, call);
        }
        
        isDrawing = false;
    }

    private void renderCall(Image img, DrawCall call) {
        AffineTransform oldTransform = g2.getTransform();
        Composite oldComposite = g2.getComposite();

        // Aplicamos opacidad
        if (call.opacity < 1.0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, call.opacity));
        }

        // --- TRANSFORMACIÓN ---
        // 1. Posición base
        g2.translate(call.x, call.y);
        
        // 2. Rotación (alrededor del centro)
        if (call.rotation != 0) {
            g2.rotate(Math.toRadians(call.rotation), call.width / 2.0, call.height / 2.0);
        }
        
        // 3. Escala / Flip
        // Si hay flip (escala negativa), debemos desplazar para que no "salte"
        if (call.scaleX < 0) {
            g2.translate(call.width, 0);
        }
        if (call.scaleY < 0) {
            g2.translate(0, call.height);
        }
        
        if (call.scaleX != 1.0f || call.scaleY != 1.0f) {
            g2.scale(call.scaleX, call.scaleY);
        }

        g2.drawImage(img, 0, 0, (int)call.width, (int)call.height, null);

        // Restauramos estado
        g2.setTransform(oldTransform);
        g2.setComposite(oldComposite);
    }

    private static class DrawCall {
        Image image;
        float x, y, width, height, rotation, scaleX, scaleY, opacity;

        DrawCall(Image image, float x, float y, float width, float height, float rotation, 
                 float scaleX, float scaleY, float opacity) {
            this.image = image;
            this.x = x; this.y = y; this.width = width; this.height = height;
            this.rotation = rotation; this.scaleX = scaleX; this.scaleY = scaleY;
            this.opacity = opacity;
        }
    }
}
