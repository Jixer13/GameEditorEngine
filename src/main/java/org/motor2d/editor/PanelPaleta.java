package org.motor2d.editor;

import org.motor2d.editor.helpers.EditorController;
import org.motor2d.model.Tile;
import org.motor2d.model.Tileset;
import org.motor2d.model.components.TilemapLayer;
import org.motor2d.utilities.Color;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * PanelPaleta - Muestra los tiles de un tileset como una galería de iconos.
 */
public class PanelPaleta extends JPanel {
    private EditorController controller;
    private Tileset tilesetActual;
    private int tileSeleccionadoId = -1;
    private PanelCanvas canvas;
    private final Map<String, ImageIcon> iconCache = new HashMap<>();
    
    private JComboBox<TilemapLayer.LayerType> comboCapas;

    public PanelPaleta(EditorController controller) {
        this.controller = controller;
        setBackground(Color.BACKGROUND);
        setLayout(new BorderLayout());
        
        // Panel superior para controles de la paleta
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelControles.setOpaque(false);
        panelControles.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BORDER_COLOR));
        
        JLabel lblCapa = new JLabel("Capa de pintura:");
        lblCapa.setForeground(Color.TEXT_SECONDARY);
        lblCapa.setFont(new Font("Arial", Font.BOLD, 10));
        
        comboCapas = new JComboBox<>(TilemapLayer.LayerType.values());
        comboCapas.setSelectedItem(TilemapLayer.LayerType.BACKGROUND); // Por defecto BACKGROUND
        comboCapas.setPreferredSize(new Dimension(120, 20));
        comboCapas.setBackground(Color.BACKGROUND);
        comboCapas.setForeground(Color.TEXT_PRIMARY);
        comboCapas.setFont(new Font("Arial", Font.PLAIN, 10));
        
        panelControles.add(lblCapa);
        panelControles.add(comboCapas);
        
        JButton btnGoma = new JButton("Goma");
        btnGoma.setPreferredSize(new Dimension(80, 20));
        btnGoma.setBackground(Color.PANEL_ALT_BACKGROUND);
        btnGoma.setForeground(Color.TEXT_PRIMARY);
        btnGoma.setFont(new Font("Arial", Font.PLAIN, 10));
        btnGoma.addActionListener(e -> {
            if (canvas == null && controller != null && controller.getEditor() != null) {
                canvas = controller.getEditor().getPanelCanvas();
            }
            if (canvas != null) {
                canvas.setModoBorrado(true);
            }
            // Quitar selección visual
            for (java.awt.Component c : panelGaleria.getComponents()) {
                if (c instanceof JButton b) {
                    b.setBorder(BorderFactory.createLineBorder(Color.BORDER_COLOR, 2));
                }
            }
        });
        panelControles.add(btnGoma);
        
        add(panelControles, BorderLayout.NORTH);
        
        // Panel central para la galería
        panelGaleria = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panelGaleria.setOpaque(false);
        add(panelGaleria, BorderLayout.CENTER);
    }
    
    private JPanel panelGaleria;

    public void setCanvas(PanelCanvas canvas) {
        this.canvas = canvas;
    }
    
    public TilemapLayer.LayerType getCapaSeleccionada() {
        return (TilemapLayer.LayerType) comboCapas.getSelectedItem();
    }

    public void cargarTileset(Tileset ts) {
        this.tilesetActual = ts;
        this.iconCache.clear();
        reconstruirGaleria();
    }

    private void reconstruirGaleria() {
        panelGaleria.removeAll();
        if (tilesetActual == null || controller == null) {
            JLabel lbl = new JLabel("Selecciona un tileset");
            lbl.setForeground(Color.TEXT_SECONDARY);
            panelGaleria.add(lbl);
        } else if (tilesetActual.getTiles().isEmpty()) {
            JLabel lbl = new JLabel("Haz clic derecho en una imagen para añadirla");
            lbl.setForeground(Color.TEXT_SECONDARY);
            panelGaleria.add(lbl);
        } else {
            for (Tile tile : tilesetActual.getTiles()) {
                JButton btn = crearBotonTile(tile);
                panelGaleria.add(btn);
            }
        }
        revalidate();
        repaint();
    }

    private JButton crearBotonTile(Tile tile) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setPreferredSize(new Dimension(80, 80));
        btn.setBackground(Color.PANEL_ALT_BACKGROUND);
        btn.setBorder(BorderFactory.createLineBorder(
                tile.getId() == tileSeleccionadoId ? java.awt.Color.YELLOW : Color.BORDER_COLOR, 2));
        
        // Cargar icono
        try {
            String path = new File(controller.getProjectPath(), tile.getSpritePath()).getAbsolutePath();
            if (!iconCache.containsKey(path)) {
                BufferedImage img = ImageIO.read(new File(path));
                if (img != null) {
                    Image scaled = img.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                    iconCache.put(path, new ImageIcon(scaled));
                }
            }
            btn.add(new JLabel(iconCache.get(path)), BorderLayout.CENTER);
        } catch (Exception e) {
            btn.add(new JLabel("Err"), BorderLayout.CENTER);
        }

        // Mostrar estado de propiedades
        JPanel panelProps = new JPanel(new GridLayout(2, 1));
        panelProps.setOpaque(false);
        JLabel lblProps = new JLabel((tile.isSolid() ? "S" : "-") + "/" + (tile.isTrigger() ? "T" : "-"));
        lblProps.setFont(new Font("Arial", Font.PLAIN, 9));
        lblProps.setForeground(Color.TEXT_SECONDARY);
        panelProps.add(lblProps);
        btn.add(panelProps, BorderLayout.SOUTH);

        // Menú contextual
        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemEdit = new JMenuItem("Editar");
        itemEdit.addActionListener(e -> editarTile(tile));
        JMenuItem itemDelete = new JMenuItem("Eliminar");
        itemDelete.addActionListener(e -> eliminarTile(tile));
        menu.add(itemEdit);
        menu.add(itemDelete);
        btn.setComponentPopupMenu(menu);

        btn.addActionListener(e -> {
            this.tileSeleccionadoId = tile.getId();
            if (canvas != null) {
                canvas.setModoPintura(true, tileSeleccionadoId);
            }
            // Actualizar bordes
            for (java.awt.Component c : panelGaleria.getComponents()) {
                if (c instanceof JButton b) {
                    b.setBorder(BorderFactory.createLineBorder(Color.BORDER_COLOR, 2));
                }
            }
            btn.setBorder(BorderFactory.createLineBorder(java.awt.Color.YELLOW, 2));
        });

        return btn;
    }

    private void editarTile(Tile tile) {
        JCheckBox chkSolid = new JCheckBox("Sólido", tile.isSolid());
        JCheckBox chkTrigger = new JCheckBox("Trigger", tile.isTrigger());
        JTextField txtName = new JTextField(tile.getName());
        Object[] msg = {"Nombre:", txtName, chkSolid, chkTrigger};
        int res = JOptionPane.showConfirmDialog(this, msg, "Editar Tile", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                controller.getTilesetManager().updateTile(tilesetActual, tile, txtName.getText(), chkSolid.isSelected(), chkTrigger.isSelected());
                reconstruirGaleria();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
            }
        }
    }

    private void eliminarTile(Tile tile) {
        int res = JOptionPane.showConfirmDialog(this, "¿Eliminar " + tile.getName() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            try {
                controller.getTilesetManager().removeTile(tilesetActual, tile);
                reconstruirGaleria();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
            }
        }
    }

    public int getTileSeleccionadoId() { return tileSeleccionadoId; }
}
