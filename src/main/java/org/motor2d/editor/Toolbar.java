package org.motor2d.editor;

import org.motor2d.utilities.Color;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Barra de título personalizada del editor.
 * Gestiona los botones de minimizar, maximizar y cerrar,
 * así como el arrastre de la ventana.
 */
public class Toolbar extends JPanel {

    // ==================== CONSTANTES ====================
    private static final int MENUBAR_ALTO = 35;
    private static final int BTN_ANCHO    = 40;
    private static final int BTN_ALTO     = 22;

    // ==================== ESTADO ====================
    private boolean btnCerrarHover    = false;
    private boolean btnMaximizarHover = false;
    private boolean btnMinimizarHover = false;

    // ==================== COMPONENTES SWING ====================
    private JMenuBar menuBar;
    private JMenu menuArchivo;

    // ==================== REFERENCIAS ====================
    private final Editor editor;

    // ==================== CONSTRUCTOR ====================
    public Toolbar(Editor editor) {
        this.editor = editor;
        setOpaque(false);
        setLayout(null);
        crearMenuArchivo();
    }

    private void crearMenuArchivo() {
        menuBar = new JMenuBar();
        menuBar.setBackground(Color.MENUBAR_BACKGROUND);
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        menuArchivo = new JMenu("Archivo");
        menuArchivo.setForeground(Color.TEXT_PRIMARY);
        menuArchivo.setFont(new Font("Arial", Font.PLAIN, 12));

        // Estilizar los items del menú
        JMenuItem itemNuevo = crearItem("Nuevo Proyecto", e -> {
            String nombre = JOptionPane.showInputDialog(editor, "Nombre del nuevo proyecto:");
            if (nombre != null && !nombre.isBlank()) {
                JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (chooser.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    editor.getController().createProject(nombre.trim(), chooser.getSelectedFile().getAbsolutePath());
                    editor.refrescarHierarchy();
                }
            }
        });

        JMenuItem itemAbrir = crearItem("Abrir Proyecto", e -> {
            JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                editor.getController().openProject(chooser.getSelectedFile().getAbsolutePath());
                editor.refrescarHierarchy();
            }
        });

        JMenuItem itemGuardar = crearItem("Guardar", e -> editor.getController().saveProject());

        JMenuItem itemGuardarComo = crearItem("Guardar como...", e -> {
            JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                try {
                    editor.getController().getProjectManager().saveProjectAs(chooser.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(editor, "Proyecto guardado en la nueva ubicación.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(editor, "Error al guardar: " + ex.getMessage());
                }
            }
        });

        JMenuItem itemSalir = crearItem("Salir", e -> editor.confirmarSalida());

        menuArchivo.add(itemNuevo);
        menuArchivo.add(itemAbrir);
        menuArchivo.addSeparator();
        menuArchivo.add(itemGuardar);
        menuArchivo.add(itemGuardarComo);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        menuBar.add(menuArchivo);
        menuBar.setBounds(5, 5, 60, 25);
        add(menuBar);
    }

    private JMenuItem crearItem(String texto, java.awt.event.ActionListener accion) {
        JMenuItem item = new JMenuItem(texto);
        item.setBackground(Color.POPUP_BACKGROUND);
        item.setForeground(Color.TEXT_PRIMARY);
        item.setFont(new Font("Arial", Font.PLAIN, 12));
        item.addActionListener(accion);
        return item;
    }

    // ==================== BOUNDS DE BOTONES ====================
    public Rectangle btnMinBounds() {
        int x = getX() + getWidth() - BTN_ANCHO * 3 - 6;
        int y = getY() + (MENUBAR_ALTO - BTN_ALTO) / 2;
        return new Rectangle(x, y, BTN_ANCHO, BTN_ALTO);
    }

    public Rectangle btnMaxBounds() {
        int x = getX() + getWidth() - BTN_ANCHO * 2 - 6;
        int y = getY() + (MENUBAR_ALTO - BTN_ALTO) / 2;
        return new Rectangle(x, y, BTN_ANCHO, BTN_ALTO);
    }

    public Rectangle btnCerrarBounds() {
        int x = getX() + getWidth() - BTN_ANCHO - 6;
        int y = getY() + (MENUBAR_ALTO - BTN_ALTO) / 2;
        return new Rectangle(x, y, BTN_ANCHO, BTN_ALTO);
    }

    // ==================== ACTUALIZAR HOVER ====================
    public void actualizarHover(Point p) {
        boolean dentroCerrar = btnCerrarBounds().contains(p);
        boolean dentroMax    = btnMaxBounds().contains(p);
        boolean dentroMin    = btnMinBounds().contains(p);

        if (dentroCerrar != btnCerrarHover ||
            dentroMax    != btnMaximizarHover ||
            dentroMin    != btnMinimizarHover) {
            btnCerrarHover    = dentroCerrar;
            btnMaximizarHover = dentroMax;
            btnMinimizarHover = dentroMin;
            editor.getRaizPanel().repaint();
        }
    }

    // ==================== PINTADO ====================
    public void pintarBotones(Graphics2D g2, boolean maximizado) {
        pintarBotonMinimizar(g2);
        pintarBotonMaximizar(g2, maximizado);
        pintarBotonCerrar(g2);
    }

    private void pintarBotonMinimizar(Graphics2D g2) {
        Rectangle r = btnMinBounds();
        if (btnMinimizarHover) {
            g2.setColor(Color.BTN_HOVER);
            g2.fillRect(r.x, r.y, r.width, r.height);
        }
        pintarTextoBoton(g2, r, "−");
    }

    private void pintarBotonMaximizar(Graphics2D g2, boolean maximizado) {
        Rectangle r = btnMaxBounds();
        if (btnMaximizarHover) {
            g2.setColor(Color.BTN_HOVER);
            g2.fillRect(r.x, r.y, r.width, r.height);
        }
        pintarTextoBoton(g2, r, maximizado ? "❐" : "□");
    }

    private void pintarBotonCerrar(Graphics2D g2) {
        Rectangle r = btnCerrarBounds();
        if (btnCerrarHover) {
            g2.setColor(Color.CLOSE_HOVER);
            g2.fillRect(r.x, r.y, r.width, r.height);
        }
        pintarTextoBoton(g2, r, "✕");
    }

    private void pintarTextoBoton(Graphics2D g2, Rectangle r, String txt) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.TEXT_PRIMARY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int tx = r.x + (r.width  - fm.stringWidth(txt)) / 2;
        int ty = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(txt, tx, ty);
    }

    // ==================== ALTURA ====================
    public static int getAlto() {
        return MENUBAR_ALTO;
    }
}
