package org.motor2d.editor;

import org.motor2d.utilities.Color;
import javax.swing.*;
import java.awt.*;

/**
 * StatusBar - Barra inferior para mostrar información de estado y mensajes al usuario.
 */
public class StatusBar extends JPanel {

    private final JLabel labelEstado;
    private final JLabel labelProyecto;
    private Timer timerLimpiar;

    public StatusBar() {
        setLayout(new BorderLayout());
        setBackground(Color.PANEL_BACKGROUND);
        setPreferredSize(new Dimension(0, 25));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BORDER_COLOR));

        labelEstado = new JLabel(" Listo");
        labelEstado.setForeground(Color.TEXT_SECONDARY);
        labelEstado.setFont(new Font("Arial", Font.PLAIN, 11));

        labelProyecto = new JLabel("");
        labelProyecto.setForeground(Color.TEXT_SECONDARY);
        labelProyecto.setFont(new Font("Arial", Font.ITALIC, 11));
        labelProyecto.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        add(labelEstado, BorderLayout.WEST);
        add(labelProyecto, BorderLayout.EAST);
    }

    /**
     * Muestra un mensaje temporal en la barra de estado.
     */
    public void mostrarMensaje(String mensaje, int duracionMs) {
        labelEstado.setText(" " + mensaje);
        if (timerLimpiar != null && timerLimpiar.isRunning()) timerLimpiar.stop();
        
        timerLimpiar = new Timer(duracionMs, e -> labelEstado.setText(" Listo"));
        timerLimpiar.setRepeats(false);
        timerLimpiar.start();
    }

    public void mostrarMensajePermanente(String mensaje) {
        labelEstado.setText(" " + mensaje);
        if (timerLimpiar != null && timerLimpiar.isRunning()) timerLimpiar.stop();
    }

    public void setInfoProyecto(String info) {
        labelProyecto.setText(info);
    }
}