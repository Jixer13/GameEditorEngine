package org.motor2d.graphics;

import org.motor2d.utilities.GameColor;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SpriteBatch - Agrupa comandos de dibujo para minimizar cambios de estado en Graphics2D.
 */
public class SpriteBatch {

    private final Graphics2D g2;
    private final Map<Image, List<DrawCall>> batches;
    private boolean isDrawing = false;

    public SpriteBatch(Graphics2D g2) {
        this.g2 = g2;
        this.batches = new HashMap<>();
    }

    public Graphics2D getGraphics() {
        return g2;
    }

    public void begin() {
        if (isDrawing) throw new IllegalStateException("Batch already drawing");
        isDrawing = true;
        batches.clear();
    }

    public void draw(Image image, float x, float y, float width, float height, 
                     float rotation, float scaleX, float scaleY, float opacity) {
        if (!isDrawing) throw new IllegalStateException("Must call begin() before draw()");
        
        batches.computeIfAbsent(image, k -> new ArrayList<>())
               .add(new DrawCall(x, y, width, height, rotation, scaleX, scaleY, opacity));
    }

    public void end() {
        if (!isDrawing) throw new IllegalStateException("Must call begin() before end()");
        
        for (Map.Entry<Image, List<DrawCall>> entry : batches.entrySet()) {
            Image img = entry.getKey();
            for (DrawCall call : entry.getValue()) {
                renderCall(img, call);
            }
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

        // Transformación (Posición, Rotación, Escala)
        g2.translate(call.x, call.y);
        g2.rotate(Math.toRadians(call.rotation), call.width / 2.0, call.height / 2.0);
        g2.scale(call.scaleX, call.scaleY);

        g2.drawImage(img, 0, 0, (int)call.width, (int)call.height, null);

        // Restauramos estado
        g2.setTransform(oldTransform);
        g2.setComposite(oldComposite);
    }

    private static class DrawCall {
        float x, y, width, height, rotation, scaleX, scaleY, opacity;

        DrawCall(float x, float y, float width, float height, float rotation, 
                 float scaleX, float scaleY, float opacity) {
            this.x = x; this.y = y; this.width = width; this.height = height;
            this.rotation = rotation; this.scaleX = scaleX; this.scaleY = scaleY;
            this.opacity = opacity;
        }
    }
}