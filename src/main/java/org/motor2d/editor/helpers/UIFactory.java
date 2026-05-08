package org.motor2d.editor.helpers;

import org.motor2d.utilities.Color;
import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * UIFactory - Centraliza la creación de componentes Swing con el estilo del motor.
 */
public class UIFactory {

    private static final Font FONT_BOLD = new Font("Arial", Font.BOLD, 11);
    private static final Font FONT_PLAIN = new Font("Arial", Font.PLAIN, 11);
    private static final Font FONT_ITALIC = new Font("Arial", Font.ITALIC, 11);

    public static JLabel createHeaderLabel(String text) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setForeground(Color.TEXT_PRIMARY);
        lbl.setFont(FONT_BOLD);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        lbl.setBackground(Color.PANEL_BACKGROUND);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 0));
        return lbl;
    }

    public static JLabel createPropertyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.TEXT_SECONDARY);
        lbl.setFont(FONT_PLAIN);
        lbl.setPreferredSize(new Dimension(70, 22));
        return lbl;
    }

    public static JLabel createValueLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.TEXT_PRIMARY);
        lbl.setFont(FONT_PLAIN);
        return lbl;
    }

    public static JTextField createTextField(String value) {
        JTextField tf = new JTextField(value);
        tf.setBackground(Color.PANEL_BACKGROUND);
        tf.setForeground(Color.TEXT_PRIMARY);
        tf.setCaretColor(Color.TEXT_PRIMARY);
        tf.setFont(FONT_PLAIN);
        tf.setBorder(BorderFactory.createLineBorder(Color.BORDER_COLOR, 1));
        return tf;
    }

    public static JTextField createFloatField(float value, Consumer<Float> onChange) {
        JTextField tf = createTextField(String.valueOf(value));
        tf.addActionListener(e -> tryParseFloat(tf.getText(), onChange));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                tryParseFloat(tf.getText(), onChange);
            }
        });
        return tf;
    }

    public static JTextField createIntField(int value, Consumer<Integer> onChange) {
        JTextField tf = createTextField(String.valueOf(value));
        tf.addActionListener(e -> tryParseInt(tf.getText(), onChange));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                tryParseInt(tf.getText(), onChange);
            }
        });
        return tf;
    }

    public static JPanel createPropertyRow(String label, JComponent control) {
        JPanel fila = new JPanel(new BorderLayout(4, 0));
        fila.setBackground(Color.PANEL_ALT_BACKGROUND);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        fila.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        fila.add(createPropertyLabel(label), BorderLayout.WEST);
        fila.add(control, BorderLayout.CENTER);
        return fila;
    }

    public static JSeparator createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Color.BORDER_COLOR);
        sep.setBackground(Color.BACKGROUND);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private static void tryParseFloat(String text, Consumer<Float> success) {
        try {
            success.accept(Float.parseFloat(text.trim()));
        } catch (NumberFormatException ignored) {}
    }

    private static void tryParseInt(String text, Consumer<Integer> success) {
        try {
            success.accept(Integer.parseInt(text.trim()));
        } catch (NumberFormatException ignored) {}
    }

    public static JButton createIconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(Color.TEXT_PRIMARY);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
