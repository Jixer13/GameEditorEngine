package org.motor2d.editor;

import org.motor2d.utilities.Color;

import javax.swing.*;
import java.awt.*;

/**
 * Panel derecho del editor.
 * Muestra y permite editar las propiedades del elemento seleccionado.
 */
public class PanelProperties extends JPanel {

    // ==================== CONSTRUCTOR ====================
    public PanelProperties() {
        setOpaque(false);
        setLayout(new BorderLayout());
        construirUI();
    }

    // ==================== CONSTRUCCIÓN UI ====================
    private void construirUI() {
        // Cabecera
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Color.PANEL_BACKGROUND);
        cabecera.setPreferredSize(new Dimension(0, 28));
        cabecera.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                Color.BORDER_COLOR));

        JLabel titulo = new JLabel("  Properties");
        titulo.setForeground(Color.TEXT_SECONDARY);
        titulo.setFont(new Font("Arial", Font.BOLD, 11));
        cabecera.add(titulo, BorderLayout.CENTER);
        add(cabecera, BorderLayout.NORTH);

        // Contenido
        JPanel cuerpo = new JPanel();
    }

    // ==================== SCROLLBAR ====================
    private static class ScrollBarPersonalizado
            extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = Color.SCROLLBAR_THUMB;
            this.trackColor = Color.BACKGROUND;
        }
    }
}
