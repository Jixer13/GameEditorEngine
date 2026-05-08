package org.motor2d.editor;

import org.motor2d.editor.helpers.EditorController;
import org.motor2d.model.Tileset;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

/**
 * PanelPaleta - Muestra los tiles de un tileset en una rejilla.
 */
public class PanelPaleta extends JPanel {
    private EditorController controller;
    private Tileset tilesetActual;
    private BufferedImage texturaTileset;
    private int tileSeleccionadoId = -1;

    public PanelPaleta(EditorController controller) {
        this.controller = controller;
        setBackground(org.motor2d.utilities.Color.BACKGROUND);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                seleccionarTile(e.getX(), e.getY());
            }
        });
    }

    public void cargarTileset(Tileset ts) {
        this.tilesetActual = ts;
        try {
            this.texturaTileset = ImageIO.read(new File(controller.getProjectPath(), ts.getImagePath()));
        } catch (Exception e) {
            this.texturaTileset = null;
        }
        repaint();
    }

    private PanelCanvas canvas;

    public void setCanvas(PanelCanvas canvas) {
        this.canvas = canvas;
    }

    private void seleccionarTile(int x, int y) {
        if (tilesetActual == null) return;
        int col = x / tilesetActual.getTileWidth();
        int row = y / tilesetActual.getTileHeight();
        if (col < tilesetActual.getCols() && row < tilesetActual.getRows()) {
            this.tileSeleccionadoId = row * tilesetActual.getCols() + col;
            
            // Activar modo pintura en el canvas automáticamente
            if (canvas != null) {
                canvas.setModoPintura(true, tileSeleccionadoId);
            }
            
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (texturaTileset != null) {
            g.drawImage(texturaTileset, 0, 0, null);
            // Dibujar grid
            g.setColor(new java.awt.Color(255, 255, 255, 50));
            for (int x = 0; x < texturaTileset.getWidth(); x += tilesetActual.getTileWidth())
                g.drawLine(x, 0, x, texturaTileset.getHeight());
            for (int y = 0; y < texturaTileset.getHeight(); y += tilesetActual.getTileHeight())
                g.drawLine(0, y, texturaTileset.getWidth(), y);
            
            // Highlight
            if (tileSeleccionadoId != -1) {
                int col = tileSeleccionadoId % tilesetActual.getCols();
                int row = tileSeleccionadoId / tilesetActual.getCols();
                g.setColor(java.awt.Color.YELLOW);
                g.drawRect(col * tilesetActual.getTileWidth(), row * tilesetActual.getTileHeight(), 
                           tilesetActual.getTileWidth(), tilesetActual.getTileHeight());
            }
        }
    }

    public int getTileSeleccionadoId() { return tileSeleccionadoId; }
}
