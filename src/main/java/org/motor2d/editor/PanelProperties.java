package org.motor2d.editor;

import org.motor2d.editor.helpers.EditorController;
import org.motor2d.editor.helpers.UIFactory;
import org.motor2d.model.Entity;
import org.motor2d.model.components.Animation;
import org.motor2d.model.components.Collider;
import org.motor2d.model.components.Component;
import org.motor2d.model.components.SpriteRenderer;
import org.motor2d.model.components.Transform;
import org.motor2d.model.ui.UIButton;
import org.motor2d.model.ui.UIElement;
import org.motor2d.model.ui.UIImage;
import org.motor2d.model.ui.UILabel;
import org.motor2d.utilities.Color;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Panel derecho del editor — Propiedades del elemento seleccionado.
 * Muestra: info general de la entidad, componentes (Transform, SpriteRenderer,
 * Collider, Animation) y una preview de la imagen si hay SpriteRenderer.
 */
public class PanelProperties extends JPanel {

    // ==================== ATRIBUTOS ====================
    private JPanel           cuerpo;
    private JScrollPane      scroll;
    private EditorController controller;
    private Entity           entidadActual;
    private File             assetActual;
    private UIElement uiElementActual;

    // ==================== CONSTRUCTOR ====================
    public PanelProperties() {
        setOpaque(false);
        setLayout(new BorderLayout());
        construirUI();
    }

    // ==================== API PÚBLICA ====================
    public void mostrarEntidad(Entity entidad, EditorController ctrl) {
        this.entidadActual = entidad;
        this.assetActual   = null;
        this.controller    = ctrl;
        reconstruirCuerpo();
    }

    public void mostrarAsset(File file) {
        this.entidadActual = null;
        this.assetActual   = file;
        this.uiElementActual = null;
        reconstruirCuerpo();
    }

    public void mostrarUIElement(UIElement ui, EditorController ctrl) {
        this.entidadActual = null;
        this.assetActual   = null;
        this.uiElementActual = ui;
        this.controller    = ctrl;
        reconstruirCuerpo();
    }

    public void limpiar() {
        this.entidadActual = null;
        this.assetActual   = null;
        this.uiElementActual = null;
        reconstruirCuerpo();
    }

    // ==================== CONSTRUCCIÓN UI BASE ====================
    private void construirUI() {
        // ── Cabecera ──
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Color.PANEL_BACKGROUND);
        cabecera.setPreferredSize(new Dimension(0, 28));
        cabecera.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BORDER_COLOR));

        JLabel titulo = new JLabel("  Properties");
        titulo.setForeground(Color.TEXT_SECONDARY);
        titulo.setFont(new Font("Arial", Font.BOLD, 11));
        cabecera.add(titulo, BorderLayout.CENTER);
        add(cabecera, BorderLayout.NORTH);

        // ── Cuerpo desplazable ──
        cuerpo = new JPanel();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setBackground(Color.BACKGROUND);

        scroll = new JScrollPane(cuerpo);
        scroll.setBackground(Color.BACKGROUND);
        scroll.getViewport().setBackground(Color.BACKGROUND);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUI(new ScrollBarPersonalizado());
        scroll.getHorizontalScrollBar().setUI(new ScrollBarPersonalizado());
        add(scroll, BorderLayout.CENTER);

        mostrarMensajeVacio();
    }

    // ==================== RECONSTRUCCIÓN DEL CUERPO ====================
    private void reconstruirCuerpo() {
        cuerpo.removeAll();

        if (entidadActual == null && assetActual == null && uiElementActual == null) {
            mostrarMensajeVacio();
        } else if (entidadActual != null) {
            agregarSeccionEntidad();
            agregarSeparador();

            for (Component comp : entidadActual.getComponents()) {
                if      (comp instanceof Transform t)       agregarSeccionTransform(t);
                else if (comp instanceof SpriteRenderer s)  agregarSeccionSprite(s);
                else if (comp instanceof Collider c)        agregarSeccionCollider(c);
                else if (comp instanceof Animation a)       agregarSeccionAnimation(a);
            }

            cuerpo.add(Box.createVerticalGlue());
        } else if (uiElementActual != null) {
            agregarSeccionUIBase(uiElementActual);
            
            if (uiElementActual instanceof UILabel label) {
                agregarSeccionUILabel(label);
            } else if (uiElementActual instanceof UIButton button) {
                agregarSeccionUIButton(button);
            } else if (uiElementActual instanceof UIImage image) {
                agregarSeccionUIImage(image);
            }
            
            cuerpo.add(Box.createVerticalGlue());
        } else if (assetActual != null) {
            agregarSeccionInfoArchivo(assetActual);
        }

        cuerpo.revalidate();
        cuerpo.repaint();
    }

    private void agregarSeccionUIBase(UIElement ui) {
        JPanel sec = crearSeccion("UI Element");
        
        JTextField campoNombre = crearTextField(ui.getName());
        campoNombre.addActionListener(e -> { ui.setName(campoNombre.getText().trim()); guardar(); });
        sec.add(crearFila("Nombre", campoNombre));
        
        sec.add(crearFila("X", crearFloatField(ui.getX(), v -> { ui.setX(v); guardar(); })));
        sec.add(crearFila("Y", crearFloatField(ui.getY(), v -> { ui.setY(v); guardar(); })));
        
        cuerpo.add(sec);
        agregarSeparador();
    }

    private void agregarSeccionUILabel(UILabel label) {
        JPanel sec = crearSeccion("UI Label");
        
        JTextField campoTexto = crearTextField(label.getText());
        campoTexto.addActionListener(e -> { label.setText(campoTexto.getText().trim()); guardar(); });
        sec.add(crearFila("Texto", campoTexto));
        
        sec.add(crearFila("Size", crearIntField(label.getFontSize(), v -> { label.setFontSize(v); guardar(); })));
        
        cuerpo.add(sec);
    }

    private void agregarSeccionUIButton(UIButton button) {
        JPanel sec = crearSeccion("UI Button");
        
        JTextField campoTexto = crearTextField(button.getText());
        campoTexto.addActionListener(e -> { button.setText(campoTexto.getText().trim()); guardar(); });
        sec.add(crearFila("Texto", campoTexto));
        
        sec.add(crearFila("Width",  crearFloatField(button.getWidth(),  v -> { button.setWidth(v);  guardar(); })));
        sec.add(crearFila("Height", crearFloatField(button.getHeight(), v -> { button.setHeight(v); guardar(); })));
        
        cuerpo.add(sec);
    }

    private void agregarSeccionUIImage(UIImage image) {
        JPanel sec = crearSeccion("UI Image");
        
        JTextField campoRuta = crearTextField(image.getImagePath());
        campoRuta.addActionListener(e -> { image.setImagePath(campoRuta.getText().trim()); guardar(); reconstruirCuerpo(); });
        sec.add(crearFila("Ruta", campoRuta));
        
        sec.add(crearFila("Width",  crearFloatField(image.getWidth(),  v -> { image.setWidth(v);  guardar(); })));
        sec.add(crearFila("Height", crearFloatField(image.getHeight(), v -> { image.setHeight(v); guardar(); })));
        
        if (image.getImagePath() != null && !image.getImagePath().isBlank()) {
            agregarPreviewImagen(sec, image.getImagePath());
        }
        
        cuerpo.add(sec);
    }

    private void agregarSeccionInfoArchivo(File file) {
        JPanel sec = crearSeccion("Asset: Info");
        
        sec.add(crearFila("Nombre", crearLabelValor(file.getName())));
        sec.add(crearFila("Tipo", crearLabelValor(obtenerExtension(file))));
        sec.add(crearFila("Tamaño", crearLabelValor(formatearTamano(file.length()))));
        
        // Ruta relativa si es posible
        String path = file.getAbsolutePath();
        if (controller != null && controller.getProjectPath() != null) {
            String base = controller.getProjectPath();
            if (path.startsWith(base)) {
                path = path.substring(base.length());
                if (path.startsWith(File.separator)) path = path.substring(1);
            }
        }
        
        // Campo de ruta que permita copiar
        JTextField txtPath = crearTextField(path);
        txtPath.setEditable(false);
        sec.add(crearFila("Ruta", txtPath));
        
        cuerpo.add(sec);
    }


    private String obtenerExtension(File f) {
        String n = f.getName();
        int i = n.lastIndexOf('.');
        return i > 0 ? n.substring(i + 1).toUpperCase() : "Archivo";
    }

    private String formatearTamano(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }

    private void mostrarMensajeVacio() {
        JLabel lbl = new JLabel("Selecciona una entidad");
        lbl.setForeground(Color.TEXT_SECONDARY);
        lbl.setFont(new Font("Arial", Font.ITALIC, 11));
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        cuerpo.add(lbl);
    }

    // ==================== SECCIÓN: ENTIDAD ====================
    private void agregarSeccionEntidad() {
        JPanel sec = crearSeccion("Entidad");

        // Nombre
        JTextField campoNombre = crearTextField(entidadActual.getName());
        campoNombre.addActionListener(e -> {
            String nuevo = campoNombre.getText().trim();
            if (!nuevo.isEmpty()) { entidadActual.setName(nuevo); guardar(); }
        });
        sec.add(crearFila("Nombre", campoNombre));

        // ID (solo lectura)
        sec.add(crearFila("ID", crearLabelValor(String.valueOf(entidadActual.getId()))));

        // Tag
        JTextField campoTag = crearTextField(
                entidadActual.getTag() != null ? entidadActual.getTag() : "");
        campoTag.addActionListener(e -> { entidadActual.setTag(campoTag.getText().trim()); guardar(); });
        sec.add(crearFila("Tag", campoTag));

        // Activo
        JCheckBox chkActivo = new JCheckBox();
        chkActivo.setSelected(entidadActual.isActive());
        chkActivo.setBackground(Color.PANEL_ALT_BACKGROUND);
        chkActivo.addActionListener(e -> { entidadActual.setActive(chkActivo.isSelected()); guardar(); });
        sec.add(crearFila("Activo", chkActivo));

        // Nº Componentes
        sec.add(crearFila("Componentes",
                crearLabelValor(String.valueOf(entidadActual.getComponents().size()))));

        // ── Imagen PNG (solo si la entidad NO tiene SpriteRenderer todavía) ──
        boolean tieneSprite = entidadActual.hasComponent(SpriteRenderer.class);
        if (!tieneSprite) {
            sec.add(crearFilaImagenPNG());
        }

        cuerpo.add(sec);
    }

    /**
     * Fila especial con campo de texto + botón "..." para elegir un PNG.
     * Al seleccionar un archivo crea automáticamente un SpriteRenderer
     * en la entidad y recarga el panel.
     */
    private JPanel crearFilaImagenPNG() {
        // Campo de texto con la ruta actual
        JTextField campoImg = crearTextField("");
        campoImg.setEditable(false);
        campoImg.setToolTipText("Ruta del PNG asignado a esta entidad");

        // Botón explorador
        JButton btnBuscar = new JButton("...");
        btnBuscar.setBackground(Color.PANEL_BACKGROUND);
        btnBuscar.setForeground(Color.TEXT_PRIMARY);
        btnBuscar.setFont(new Font("Arial", Font.BOLD, 11));
        btnBuscar.setFocusPainted(false);
        btnBuscar.setBorder(BorderFactory.createLineBorder(Color.BORDER_COLOR, 1));
        btnBuscar.setPreferredSize(new Dimension(30, 22));
        btnBuscar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnBuscar.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Selecciona una imagen PNG");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Imágenes PNG", "png"));

            // Punto de partida: carpeta Assets del proyecto
            if (controller != null && controller.getProjectPath() != null) {
                File assets = new File(controller.getProjectPath(), "assets");
                if (!assets.exists()) assets = new File(controller.getProjectPath(), "assets");
                if (assets.exists()) chooser.setCurrentDirectory(assets);
            }

            if (chooser.showOpenDialog(PanelProperties.this) != JFileChooser.APPROVE_OPTION) return;

            File archivo = chooser.getSelectedFile();
            if (!archivo.getName().toLowerCase().endsWith(".png")) {
                JOptionPane.showMessageDialog(PanelProperties.this,
                        "Por favor selecciona un archivo .png",
                        "Formato inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Convertir a ruta relativa al proyecto si es posible
            String rutaFinal = archivo.getAbsolutePath();

            if (controller != null && controller.getProjectPath() != null) {

                File baseDir = new File(controller.getProjectPath());

                String base = baseDir.getAbsolutePath();

                if (!base.endsWith(File.separator)) {
                    base += File.separator;
                }

                if (rutaFinal.startsWith(base)) {
                    rutaFinal = rutaFinal.substring(base.length());
                }

                rutaFinal = rutaFinal.replace("\\", "/");
            }

            // Usar el controlador para la orquestación (ruta, tamaño, componente, guardado)
            controller.setEntitySprite(entidadActual, rutaFinal);
            
            // Recargar el panel para que aparezca la sección SpriteRenderer
            reconstruirCuerpo();
        });

        // Panel fila con campo + botón
        JPanel contenedor = new JPanel(new BorderLayout(2, 0));
        contenedor.setBackground(Color.PANEL_ALT_BACKGROUND);
        contenedor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        contenedor.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        JLabel lbl = new JLabel("Imagen");
        lbl.setForeground(Color.TEXT_SECONDARY);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        lbl.setPreferredSize(new Dimension(70, 22));

        contenedor.add(lbl,       BorderLayout.WEST);
        contenedor.add(campoImg,  BorderLayout.CENTER);
        contenedor.add(btnBuscar, BorderLayout.EAST);

        return contenedor;
    }

    // ==================== SECCIÓN: TRANSFORM ====================
    private void agregarSeccionTransform(Transform t) {
        JPanel sec = crearSeccion("Transform");

        sec.add(crearFila("Pos X",    crearFloatField(t.getX(),       v -> { t.setX(v);        guardar(); })));
        sec.add(crearFila("Pos Y",    crearFloatField(t.getY(),       v -> { t.setY(v);        guardar(); })));
        sec.add(crearFila("Escala X", crearFloatField(t.getScaleX(),  v -> { t.setScaleX(v);   guardar(); })));
        sec.add(crearFila("Escala Y", crearFloatField(t.getScaleY(),  v -> { t.setScaleY(v);   guardar(); })));
        sec.add(crearFila("Rotación", crearFloatField(t.getRotation(),v -> { t.setRotation(v); guardar(); })));

        cuerpo.add(sec);
        agregarSeparador();
    }

    // ==================== SECCIÓN: SPRITE RENDERER ====================
    private void agregarSeccionSprite(SpriteRenderer s) {
        JPanel sec = crearSeccion("SpriteRenderer");

        JTextField campoRuta = crearTextField(s.getSpritePath() != null ? s.getSpritePath() : "");
        campoRuta.addActionListener(e -> {
            s.setSpritePath(campoRuta.getText().trim());
            guardar();
            reconstruirCuerpo();
        });
        sec.add(crearFila("Sprite", campoRuta));

        sec.add(crearFila("Frame W", crearIntField(s.getFrameWidth(),  v -> { s.setFrameWidth(v);  guardar(); })));
        sec.add(crearFila("Frame H", crearIntField(s.getFrameHeight(), v -> { s.setFrameHeight(v); guardar(); })));
        sec.add(crearFila("Layer",   crearIntField(s.getLayer(),       v -> { s.setLayer(v);       guardar(); })));

        JCheckBox chkFlipX = new JCheckBox(); chkFlipX.setSelected(s.isFlipX());
        chkFlipX.setBackground(Color.PANEL_ALT_BACKGROUND);
        chkFlipX.addActionListener(e -> { s.setFlipX(chkFlipX.isSelected()); guardar(); });
        sec.add(crearFila("Flip X", chkFlipX));

        JCheckBox chkFlipY = new JCheckBox(); chkFlipY.setSelected(s.isFlipY());
        chkFlipY.setBackground(Color.PANEL_ALT_BACKGROUND);
        chkFlipY.addActionListener(e -> { s.setFlipY(chkFlipY.isSelected()); guardar(); });
        sec.add(crearFila("Flip Y", chkFlipY));

        // Preview de imagen
        if (s.getSpritePath() != null && !s.getSpritePath().isBlank()) {
            agregarPreviewImagen(sec, s.getSpritePath());
        }

        cuerpo.add(sec);
        agregarSeparador();
    }

    // ==================== PREVIEW DE IMAGEN ====================
    private void agregarPreviewImagen(JPanel sec, String spritePath) {
        File imgFile = resolverRutaImagen(spritePath);
        if (imgFile == null || !imgFile.exists()) {
            sec.add(crearFila("Preview", crearLabelValor("Imagen no encontrada")));
            return;
        }
        try {
            BufferedImage img = ImageIO.read(imgFile);

            if (img == null) return;

            int maxW = 180, maxH = 180;
            double ratio = Math.min((double) maxW / img.getWidth(),
                                    (double) maxH / img.getHeight());
            int w = (int)(img.getWidth()  * ratio);
            int h = (int)(img.getHeight() * ratio);
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);

            JLabel preview = new JLabel(new ImageIcon(scaled));
            preview.setAlignmentX(CENTER_ALIGNMENT);
            preview.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));

            JLabel infoImg = new JLabel(img.getWidth() + " × " + img.getHeight() + " px");
            infoImg.setForeground(Color.TEXT_SECONDARY);
            infoImg.setFont(new Font("Arial", Font.PLAIN, 10));
            infoImg.setAlignmentX(CENTER_ALIGNMENT);

            JPanel contenedor = new JPanel();
            contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
            contenedor.setBackground(Color.PANEL_ALT_BACKGROUND);
            contenedor.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            contenedor.add(preview);
            contenedor.add(Box.createVerticalStrut(4));
            contenedor.add(infoImg);

            sec.add(contenedor);
        } catch (Exception ex) {
            sec.add(crearFila("Preview", crearLabelValor("Error al cargar imagen")));
        }
    }

    private File resolverRutaImagen(String spritePath) {
        if (controller == null || spritePath == null || spritePath.isBlank()) {
            return null;
        }

        // 1. Ruta absoluta
        File f = new File(spritePath);

        if (f.isAbsolute() && f.exists()) {
            return f;
        }

        // 2. Relativa al proyecto
        String projectPath = controller.getProjectPath();

        if (projectPath != null && !projectPath.isBlank()) {

            // assets/player.png
            f = new File(projectPath, spritePath);

            if (f.exists()) {
                return f;
            }

            // player.png -> assets/player.png
            if (!spritePath.startsWith("assets")) {

                f = new File(
                        projectPath,
                        "assets" + File.separator + spritePath
                );

                if (f.exists()) {
                    return f;
                }
            }
        }

        return null;
    }

    // ==================== SECCIÓN: COLLIDER ====================
    private void agregarSeccionCollider(Collider c) {
        JPanel sec = crearSeccion("Collider");

        String[] formas = {"RECTANGLE", "CIRCLE"};
        JComboBox<String> comboForma = new JComboBox<>(formas);
        comboForma.setSelectedItem(c.getShape().name());
        comboForma.setBackground(Color.PANEL_ALT_BACKGROUND);
        comboForma.setForeground(Color.TEXT_PRIMARY);
        comboForma.setFont(new Font("Arial", Font.PLAIN, 11));
        comboForma.addActionListener(e -> {
            c.setShape(Collider.ColliderShape.valueOf((String) comboForma.getSelectedItem()));
            guardar();
        });
        sec.add(crearFila("Forma", comboForma));

        sec.add(crearFila("Offset X", crearFloatField(c.getOffsetX(), v -> { c.setOffsetX(v); guardar(); })));
        sec.add(crearFila("Offset Y", crearFloatField(c.getOffsetY(), v -> { c.setOffsetY(v); guardar(); })));
        sec.add(crearFila("Ancho",    crearFloatField(c.getWidth(),   v -> { c.setWidth(v);   guardar(); })));
        sec.add(crearFila("Alto",     crearFloatField(c.getHeight(),  v -> { c.setHeight(v);  guardar(); })));
        sec.add(crearFila("Radio",    crearFloatField(c.getRadius(),  v -> { c.setRadius(v);  guardar(); })));

        JCheckBox chkTrigger = new JCheckBox(); chkTrigger.setSelected(c.isTrigger());
        chkTrigger.setBackground(Color.PANEL_ALT_BACKGROUND);
        chkTrigger.addActionListener(e -> { c.setTrigger(chkTrigger.isSelected()); guardar(); });
        sec.add(crearFila("Trigger", chkTrigger));

        cuerpo.add(sec);
        agregarSeparador();
    }

    // ==================== SECCIÓN: ANIMATION ====================
    private void agregarSeccionAnimation(Animation a) {
        JPanel sec = crearSeccion("🎞 Animation");

        JTextField campoNombre = crearTextField(a.getName() != null ? a.getName() : "");
        campoNombre.addActionListener(e -> { a.setName(campoNombre.getText().trim()); guardar(); });
        sec.add(crearFila("Nombre", campoNombre));

        sec.add(crearFila("Frame dur.", crearFloatField(a.getFrameDuration(),
                v -> { a.setFrameDuration(v); guardar(); })));
        sec.add(crearFila("Frames",
                crearLabelValor(a.getFrames() != null ? String.valueOf(a.getFrames().size()) : "0")));

        JCheckBox chkLoop = new JCheckBox(); chkLoop.setSelected(a.isLooping());
        chkLoop.setBackground(Color.PANEL_ALT_BACKGROUND);
        chkLoop.addActionListener(e -> { a.setLooping(chkLoop.isSelected()); guardar(); });
        sec.add(crearFila("Loop", chkLoop));

        cuerpo.add(sec);
        agregarSeparador();
    }

    // ==================== HELPERS DE UI ====================
    private JPanel crearSeccion(String titulo) {
        JPanel sec = new JPanel();
        sec.setLayout(new BoxLayout(sec, BoxLayout.Y_AXIS));
        sec.setBackground(Color.BACKGROUND);
        sec.setAlignmentX(LEFT_ALIGNMENT);
        sec.add(UIFactory.createHeaderLabel(titulo));
        return sec;
    }

    private JPanel crearFila(String etiqueta, JComponent control) {
        return UIFactory.createPropertyRow(etiqueta, control);
    }

    private JTextField crearTextField(String valor) {
        return UIFactory.createTextField(valor);
    }

    private JLabel crearLabelValor(String val) {
        return UIFactory.createValueLabel(val);
    }

    private JTextField crearFloatField(float valor, FloatConsumer onChange) {
        return UIFactory.createFloatField(valor, onChange::accept);
    }

    private JTextField crearIntField(int valor, IntConsumer onChange) {
        return UIFactory.createIntField(valor, onChange::accept);
    }

    private void agregarSeparador() {
        cuerpo.add(UIFactory.createSeparator());
    }

    private void guardar() {
        if (controller != null) controller.saveProject();
    }

    // ==================== FUNCTIONAL INTERFACES ====================
    @FunctionalInterface interface FloatConsumer { void accept(float v); }
    @FunctionalInterface interface IntConsumer   { void accept(int v);   }

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
