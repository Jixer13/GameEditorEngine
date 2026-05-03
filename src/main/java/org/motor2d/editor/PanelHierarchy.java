package org.motor2d.editor;

import org.motor2d.utilities.Color;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Panel izquierdo del editor.
 * Muestra la jerarquía de entidades/objetos de la escena actual.
 */
public class PanelHierarchy extends JPanel {

    // ==================== ATRIBUTOS ====================
    private JTree arbolEntidades;
    private DefaultMutableTreeNode raizArbol;

    // ==================== CONSTRUCTOR ====================
    public PanelHierarchy() {
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
        cabecera.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BORDER_COLOR));

        JLabel titulo = new JLabel("  Hierarchy");
        titulo.setForeground(Color.TEXT_SECONDARY);
        titulo.setFont(new Font("Arial", Font.BOLD, 11));
        cabecera.add(titulo, BorderLayout.CENTER);
        add(cabecera, BorderLayout.NORTH);

        // Árbol de entidades
        raizArbol = new DefaultMutableTreeNode("🎮 Scene");

        arbolEntidades = new JTree(raizArbol);
        arbolEntidades.setBackground(Color.BACKGROUND);
        arbolEntidades.setForeground(Color.TEXT_PRIMARY);
        arbolEntidades.setFont(new Font("Arial", Font.PLAIN, 12));
        arbolEntidades.setCellRenderer(new RendererEntidades());
        arbolEntidades.setRowHeight(24);
        arbolEntidades.setToggleClickCount(1);
        arbolEntidades.setRootVisible(true);
        arbolEntidades.setShowsRootHandles(true);
        arbolEntidades.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JScrollPane scroll = new JScrollPane(arbolEntidades);
        scroll.setBackground(Color.BACKGROUND);
        scroll.getViewport().setBackground(Color.BACKGROUND);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUI(new ScrollBarPersonalizado());
        scroll.getHorizontalScrollBar().setUI(new ScrollBarPersonalizado());
        add(scroll, BorderLayout.CENTER);
    }

    // ==================== RENDERER ====================
    private static class RendererEntidades extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected,
                    expanded, leaf, row, hasFocus);
            setBackground(selected ? Color.TREE_SELECTION : Color.BACKGROUND);
            setForeground(Color.TEXT_PRIMARY);
            setFont(new Font("Arial", Font.PLAIN, 12));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 0));
            return this;
        }
    }

    private static class ScrollBarPersonalizado
            extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = Color.SCROLLBAR_THUMB;
            this.trackColor = Color.BACKGROUND;
        }
    }
}
