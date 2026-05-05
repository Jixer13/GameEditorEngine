package org.motor2d.graphics;

import org.motor2d.model.Entity;
import org.motor2d.model.Scene;
import org.motor2d.model.Tile;
import org.motor2d.model.Tilemap;
import org.motor2d.model.components.TilemapLayer;
import org.motor2d.model.Tileset;
import org.motor2d.model.components.Animation;
import org.motor2d.model.components.SpriteRenderer;
import org.motor2d.model.components.Transform;
import org.motor2d.model.ui.UIButton;
import org.motor2d.model.ui.UIElement;
import org.motor2d.model.ui.UIImage;
import org.motor2d.model.ui.UILabel;
import org.motor2d.utilities.GameColor;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Renderer - Encargado de dibujar la escena y sus componentes en pantalla.
 */
public class Renderer {

    private final Camara camara;
    private final String projectPath;

    public Renderer(Camara camara, String projectPath) {
        this.camara      = camara;
        this.projectPath = projectPath;
    }

    // ==================== PUNTO DE ENTRADA ====================

    /**
     * Dibuja toda la escena siguiendo el orden de capas.
     */
    public void render(Graphics2D g2, Scene scene, List<Tileset> tilesets) {
        configurarRenderizado(g2);

        // 1. Color de fondo
        renderBackground(g2, scene);

        // 2. Tilemap (Capas inferiores)
        if (scene.getTilemap() != null) {
            renderTilemapLayer(g2, scene.getTilemap(),
                    TilemapLayer.LayerType.BACKGROUND, tilesets);
            renderTilemapLayer(g2, scene.getTilemap(),
                    TilemapLayer.LayerType.MIDGROUND, tilesets);
        }

        // 3. Entidades (Ordenadas por su propia capa)
        renderEntities(g2, scene);

        // 4. Tilemap (Capas superiores)
        if (scene.getTilemap() != null) {
            renderTilemapLayer(g2, scene.getTilemap(),
                    TilemapLayer.LayerType.FOREGROUND, tilesets);
        }

        // 5. Interfaz de usuario
        renderUI(g2, scene);
    }

    private void configurarRenderizado(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    // ==================== DIBUJADO DE ELEMENTOS ====================

    private void renderBackground(Graphics2D g2, Scene scene) {
        if (scene.getBackgroundColor() == null) return;
        g2.setColor(GameColor.fromHex(scene.getBackgroundColor()).toAwtColor());
        g2.fillRect(0, 0, camara.getViewWidth(), camara.getViewHeight());
    }

    private void renderTilemapLayer(Graphics2D g2, Tilemap tilemap,
                                    TilemapLayer.LayerType layerType,
                                    List<Tileset> tilesets) {
        TilemapLayer layer = tilemap.getLayer(layerType);
        if (layer == null || layer.isEmpty() || !layer.isVisible()) return;

        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, layer.getOpacity()));

        for (Map.Entry<String, Integer> entry : layer.getTileGrid().entrySet()) {
            String[] parts = entry.getKey().split(",");
            int col    = Integer.parseInt(parts[0]);
            int row    = Integer.parseInt(parts[1]);
            int tileId = entry.getValue();

            Tile tile = findTileById(tilesets, tileId);
            if (tile == null) continue;

            float worldX = col * tilemap.getTileWidth();
            float worldY = row * tilemap.getTileHeight();

            if (!camara.isVisible(worldX, worldY,
                    tilemap.getTileWidth(), tilemap.getTileHeight())) continue;

            float screenX = camara.worldToScreenX(worldX);
            float screenY = camara.worldToScreenY(worldY);

            try {
                Sprite sprite = Sprite.load(projectPath, tile.getSpritePath());
                g2.drawImage(sprite.getImage(),
                        (int) screenX, (int) screenY,
                        (int)(tilemap.getTileWidth()  * camara.getZoom()),
                        (int)(tilemap.getTileHeight() * camara.getZoom()),
                        null);
            } catch (IOException e) {
                renderPlaceholder(g2, (int) screenX, (int) screenY,
                        tilemap.getTileWidth(), tilemap.getTileHeight());
            }
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    private void renderEntities(Graphics2D g2, Scene scene) {
        scene.getEntities().stream()
                .filter(Entity::isActive)
                .sorted((a, b) -> {
                    SpriteRenderer sa = a.getComponent(SpriteRenderer.class);
                    SpriteRenderer sb = b.getComponent(SpriteRenderer.class);
                    int la = sa != null ? sa.getLayer() : 0;
                    int lb = sb != null ? sb.getLayer() : 0;
                    return Integer.compare(la, lb);
                })
                .forEach(entity -> renderEntity(g2, entity));
    }

    private void renderEntity(Graphics2D g2, Entity entity) {
        Transform transform = entity.getComponent(Transform.class);
        SpriteRenderer sprite = entity.getComponent(SpriteRenderer.class);
        if (transform == null || sprite == null || !sprite.isEnabled()) return;

        if (!camara.isVisible(transform.getX(), transform.getY(),
                sprite.getFrameWidth(), sprite.getFrameHeight())) return;

        float screenX = camara.worldToScreenX(transform.getX());
        float screenY = camara.worldToScreenY(transform.getY());

        try {
            String imagePath = sprite.getSpritePath();
            Animation animation = entity.getComponent(Animation.class);
            if (animation != null && animation.isEnabled() && animation.isPlaying()) {
                imagePath = animation.getCurrentFramePath();
            }

            Sprite spr = Sprite.load(projectPath, imagePath);

            int drawWidth  = (int)(sprite.getFrameWidth() * transform.getScaleX() * camara.getZoom());
            int drawHeight = (int)(sprite.getFrameHeight() * transform.getScaleY() * camara.getZoom());

            AffineTransform at = new AffineTransform();
            at.translate(screenX, screenY);

            if (transform.getRotation() != 0) {
                at.rotate(Math.toRadians(transform.getRotation()), drawWidth / 2.0, drawHeight / 2.0);
            }

            if (sprite.isFlipX()) { at.translate(drawWidth, 0); at.scale(-1, 1); }
            if (sprite.isFlipY()) { at.translate(0, drawHeight); at.scale(1, -1); }

            AffineTransform original = g2.getTransform();
            g2.transform(at);
            g2.drawImage(spr.getImage(), 0, 0, drawWidth, drawHeight, null);
            g2.setTransform(original);

        } catch (IOException e) {
            renderPlaceholder(g2, (int) screenX, (int) screenY,
                    sprite.getFrameWidth(), sprite.getFrameHeight());
        }
    }

    private void renderUI(Graphics2D g2, Scene scene) {
        scene.getUiElements().stream()
                .filter(UIElement::isVisible)
                .sorted((a, b) -> Integer.compare(a.getZOrder(), b.getZOrder()))
                .forEach(element -> renderUIElement(g2, element));
    }

    private void renderUIElement(Graphics2D g2, UIElement element) {
        float[] pos = resolveAnchor(element);
        float x = pos[0];
        float y = pos[1];

        if (element instanceof UILabel label) {
            g2.setColor(GameColor.fromHex(label.getColor()).toAwtColor());
            g2.setFont(new Font("Arial", Font.PLAIN, label.getFontSize()));
            g2.drawString(label.getText(), x, y + label.getFontSize());
        } else if (element instanceof UIButton button) {
            g2.setColor(GameColor.fromHex(button.getNormalColor()).toAwtColor());
            g2.fillRoundRect((int) x, (int) y, (int) button.getWidth(), (int) button.getHeight(), 10, 10);
            g2.setColor(GameColor.fromHex(button.getTextColor()).toAwtColor());
            g2.setFont(new Font("Arial", Font.PLAIN, button.getFontSize()));
            g2.drawString(button.getText(), x + 10, y + button.getHeight() / 2f + button.getFontSize() / 2f);
        } else if (element instanceof UIImage image) {
            try {
                Sprite sprite = Sprite.load(projectPath, image.getImagePath());
                g2.drawImage(sprite.getImage(), (int) x, (int) y, (int) image.getWidth(), (int) image.getHeight(), null);
            } catch (IOException e) {
                renderPlaceholder(g2, (int) x, (int) y, (int) image.getWidth(), (int) image.getHeight());
            }
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private float[] resolveAnchor(UIElement element) {
        float x = element.getX();
        float y = element.getY();
        int w = camara.getViewWidth();
        int h = camara.getViewHeight();
        float ew = element.getWidth();
        float eh = element.getHeight();

        return switch (element.getAnchor()) {
            case TOP_LEFT -> new float[]{x, y};
            case TOP_CENTER -> new float[]{w/2f - ew/2f + x, y};
            case TOP_RIGHT -> new float[]{w - ew - x, y};
            case MIDDLE_LEFT -> new float[]{x, h/2f - eh/2f + y };
            case MIDDLE_CENTER -> new float[]{w/2f - ew/2f + x, h/2f - eh/2f + y};
            case MIDDLE_RIGHT -> new float[]{w - ew - x, h/2f - eh/2f + y};
            case BOTTOM_LEFT -> new float[]{x, h - eh - y };
            case BOTTOM_CENTER -> new float[]{w/2f - ew/2f + x, h - eh - y};
            case BOTTOM_RIGHT -> new float[]{w - ew - x, h - eh - y};
        };
    }

    private void renderPlaceholder(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(java.awt.Color.MAGENTA);
        g2.fillRect(x, y, width, height);
        g2.setColor(java.awt.Color.BLACK);
        g2.drawRect(x, y, width, height);
    }

    private Tile findTileById(List<Tileset> tilesets, int tileId) {
        for (Tileset tileset : tilesets) {
            Tile tile = tileset.getTileById(tileId);
            if (tile != null) return tile;
        }
        return null;
    }
}