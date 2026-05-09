package org.motor2d.editor;

import org.motor2d.utilities.Color;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Barra de título personalizada del editor.
 */
public class Toolbar extends JPanel {

    private static final int MENUBAR_ALTO = 35;
    private static final int BTN_ANCHO    = 40;
    private static final int BTN_ALTO     = 22;

    private boolean btnCerrarHover    = false;
    private boolean btnMaximizarHover = false;
    private boolean btnMinimizarHover = false;
    private boolean btnPlayHover      = false;
    private boolean btnStopHover      = false;
    private boolean btnUndoHover      = false;
    private boolean btnRedoHover      = false;

    private JMenuBar menuBar;
    private JMenu menuArchivo;
    private final Editor editor;

    public Toolbar(Editor editor) {
        this.editor = editor;
        setOpaque(true); 
        setBackground(Color.MENUBAR_BACKGROUND); 
        setPreferredSize(new Dimension(0, MENUBAR_ALTO)); 
        setLayout(null);
        crearMenuArchivo();
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (btnPlayBounds().contains(p)) {
                    editor.getController().togglePlay();
                } else if (btnStopBounds().contains(p)) {
                    editor.getController().stopPlay();
                } else if (btnUndoBounds().contains(p)) {
                    editor.getController().undo();
                } else if (btnRedoBounds().contains(p)) {
                    editor.getController().redo();
                }
                repaint();
            }
        });
    }

    private void crearMenuArchivo() {
        menuBar = new JMenuBar();
        menuBar.setBackground(Color.MENUBAR_BACKGROUND);
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        menuArchivo = new JMenu("Archivo");
        menuArchivo.setForeground(Color.TEXT_PRIMARY);
        menuArchivo.setFont(new Font("Arial", Font.PLAIN, 12));

        JMenuItem itemNuevo = crearItem("Nuevo Proyecto", e -> {
            String nombre = JOptionPane.showInputDialog(editor, "Nombre del nuevo proyecto:");
            if (nombre != null && !nombre.isBlank()) {
                JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (chooser.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                    editor.getController().createProject(nombre.trim(), chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        JMenuItem itemAbrir = crearItem("Abrir Proyecto", e -> {
            JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
                editor.getController().openProject(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JMenuItem itemGuardar = crearItem("Guardar", e -> editor.getController().saveProject());
        JMenuItem itemSalir = crearItem("Salir", e -> editor.confirmarSalida());

        menuArchivo.add(itemNuevo);
        menuArchivo.add(itemAbrir);
        menuArchivo.addSeparator();
        menuArchivo.add(itemGuardar);
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

    public Rectangle btnMinBounds() { return new Rectangle(getWidth() - BTN_ANCHO * 3 - 6, (MENUBAR_ALTO - BTN_ALTO) / 2, BTN_ANCHO, BTN_ALTO); }
    public Rectangle btnMaxBounds() { return new Rectangle(getWidth() - BTN_ANCHO * 2 - 6, (MENUBAR_ALTO - BTN_ALTO) / 2, BTN_ANCHO, BTN_ALTO); }
    public Rectangle btnCerrarBounds() { return new Rectangle(getWidth() - BTN_ANCHO - 6, (MENUBAR_ALTO - BTN_ALTO) / 2, BTN_ANCHO, BTN_ALTO); }
    public Rectangle btnPlayBounds() { return new Rectangle(getWidth() / 2 - BTN_ANCHO, (MENUBAR_ALTO - BTN_ALTO) / 2, BTN_ANCHO, BTN_ALTO); }
    public Rectangle btnStopBounds() { return new Rectangle(getWidth() / 2, (MENUBAR_ALTO - BTN_ALTO) / 2, BTN_ANCHO, BTN_ALTO); }
    public Rectangle btnUndoBounds() { return new Rectangle(80, (MENUBAR_ALTO - BTN_ALTO) / 2, BTN_ANCHO, BTN_ALTO); }
    public Rectangle btnRedoBounds() { return new Rectangle(80 + BTN_ANCHO, (MENUBAR_ALTO - BTN_ALTO) / 2, BTN_ANCHO, BTN_ALTO); }

    public void actualizarHover(Point p) {
        btnCerrarHover    = btnCerrarBounds().contains(p);
        btnMaximizarHover = btnMaxBounds().contains(p);
        btnMinimizarHover = btnMinBounds().contains(p);
        btnPlayHover      = btnPlayBounds().contains(p);
        btnStopHover      = btnStopBounds().contains(p);
        btnUndoHover      = btnUndoBounds().contains(p);
        btnRedoHover      = btnRedoBounds().contains(p);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        Graphics2D g2 = (Graphics2D) g;
        pintarBoton(g2, btnMinBounds(), btnMinimizarHover, "−", false);
        pintarBoton(g2, btnMaxBounds(), btnMaximizarHover, editor.isMaximizado() ? "❐" : "□", false);
        pintarBoton(g2, btnCerrarBounds(), btnCerrarHover, "✕", true);
        
        boolean isPlaying = editor.getController().isPlaying();
        pintarBoton(g2, btnPlayBounds(), btnPlayHover, "▶", false, isPlaying ? java.awt.Color.GREEN : Color.TEXT_PRIMARY);
        pintarBoton(g2, btnStopBounds(), btnStopHover, "⏹", false);
        
        pintarBoton(g2, btnUndoBounds(), btnUndoHover, "⟲", false);
        pintarBoton(g2, btnRedoBounds(), btnRedoHover, "⟳", false);
    }

    private void pintarBoton(Graphics2D g2, Rectangle r, boolean hover, String txt, boolean danger) {
        pintarBoton(g2, r, hover, txt, danger, Color.TEXT_PRIMARY);
    }

    private void pintarBoton(Graphics2D g2, Rectangle r, boolean hover, String txt, boolean danger, java.awt.Color textColor) {
        if (hover) {
            g2.setColor(danger ? Color.CLOSE_HOVER : Color.BTN_HOVER);
            g2.fillRect(r.x, r.y, r.width, r.height);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(textColor);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(txt, r.x + (r.width - fm.stringWidth(txt)) / 2, r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent());
    }

    public static int getAlto() { return MENUBAR_ALTO; }
}
