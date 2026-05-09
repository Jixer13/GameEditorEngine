package org.motor2d.editor;

import org.motor2d.editor.helpers.EditorController;
import org.motor2d.editor.helpers.UIFactory;
import org.motor2d.model.Entity;
import org.motor2d.model.components.Animation;
import org.motor2d.model.components.Collider;
import org.motor2d.model.components.Component;
import org.motor2d.model.components.PlayerController;
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

        // Envolver el cuerpo en un panel con BorderLayout para que el BoxLayout
        // no se estire y los elementos se mantengan arriba, permitiendo el scroll.
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.BACKGROUND);
        wrapper.add(cuerpo, BorderLayout.NORTH);

        scroll = new JScrollPane(wrapper);
        scroll.setBackground(Color.BACKGROUND);
        scroll.getViewport().setBackground(Color.BACKGROUND);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
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
                else if (comp instanceof PlayerController p) agregarSeccionPlayerController(p);
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

        // Activo
        JCheckBox chkActivo = new JCheckBox();
        chkActivo.setSelected(entidadActual.isActive());
        chkActivo.setBackground(Color.PANEL_ALT_BACKGROUND);
        chkActivo.addActionListener(e -> { entidadActual.setActive(chkActivo.isSelected()); guardar(); });
        sec.add(crearFila("Activo", chkActivo));

        // --- Gestión de Componentes ---
        agregarSeparador();
        
        // Es Jugador (PlayerController)
        JCheckBox chkJugador = new JCheckBox();
        chkJugador.setSelected(entidadActual.hasComponent(PlayerController.class));
        chkJugador.setBackground(Color.PANEL_ALT_BACKGROUND);
        chkJugador.addActionListener(e -> {
            if (chkJugador.isSelected()) entidadActual.addComponent(new PlayerController());
            else entidadActual.removeComponent(PlayerController.class);
            guardar(); reconstruirCuerpo();
        });
        sec.add(crearFila("Player", chkJugador));

        // Animación (Animation)
        JCheckBox chkAnim = new JCheckBox();
        chkAnim.setSelected(entidadActual.hasComponent(Animation.class));
        chkAnim.setBackground(Color.PANEL_ALT_BACKGROUND);
        chkAnim.addActionListener(e -> {
            if (chkAnim.isSelected()) entidadActual.addComponent(new Animation());
            else entidadActual.removeComponent(Animation.class);
            guardar(); reconstruirCuerpo();
        });
        sec.add(crearFila("Animator", chkAnim));

        // Físicas (Collider)
        JCheckBox chkColl = new JCheckBox();
        chkColl.setSelected(entidadActual.hasComponent(Collider.class));
        chkColl.setBackground(Color.PANEL_ALT_BACKGROUND);
        chkColl.addActionListener(e -> {
            if (chkColl.isSelected()) entidadActual.addComponent(new Collider());
            else entidadActual.removeComponent(Collider.class);
            guardar(); reconstruirCuerpo();
        });
        sec.add(crearFila("Collider", chkColl));

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
        JPanel sec = crearSeccion("Animación");

        // --- Configuración Global ---
        sec.add(crearFila("Frame dur.", crearFloatField(a.getFrameDuration(), v -> { a.setFrameDuration(v); guardar(); })));
        
        JCheckBox chkLoop = new JCheckBox(); chkLoop.setSelected(a.isLooping());
        chkLoop.setBackground(Color.PANEL_ALT_BACKGROUND);
        chkLoop.addActionListener(e -> { a.setLooping(chkLoop.isSelected()); guardar(); });
        sec.add(crearFila("Loop", chkLoop));

        JCheckBox chkAuto = new JCheckBox(); chkAuto.setSelected(a.isAutoPlay());
        chkAuto.setBackground(Color.PANEL_ALT_BACKGROUND);
        chkAuto.addActionListener(e -> { a.setAutoPlay(chkAuto.isSelected()); guardar(); });
        sec.add(crearFila("Auto Play", chkAuto));

        agregarSeparador();

        // --- Gestión de Secuencias ---
        JPanel panelSecuencias = new JPanel(new BorderLayout(5, 0));
        panelSecuencias.setBackground(org.motor2d.utilities.Color.PANEL_ALT_BACKGROUND);
        panelSecuencias.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        panelSecuencias.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Asegurar que al menos existe 'default' para poder añadir frames
        if (a.getSequences().isEmpty()) {
            a.addSequence("default");
            a.setCurrentSequence("default");
        }

        JComboBox<String> comboSeq = new JComboBox<>();
        a.getSequences().keySet().forEach(comboSeq::addItem);
        if (a.getCurrentSequence() != null) comboSeq.setSelectedItem(a.getCurrentSequence());
        
        comboSeq.addActionListener(e -> {
            String sel = (String) comboSeq.getSelectedItem();
            if (sel != null && !sel.equals(a.getCurrentSequence())) {
                a.setCurrentSequence(sel);
                guardar();
                reconstruirCuerpo();
            }
        });

        JButton btnAddSeq = new JButton("+");
        btnAddSeq.setPreferredSize(new Dimension(35, 22));
        btnAddSeq.setBackground(new java.awt.Color(70, 70, 70));
        btnAddSeq.setForeground(java.awt.Color.WHITE);
        btnAddSeq.setFocusPainted(false);
        btnAddSeq.setFont(new Font("Arial", Font.BOLD, 14));
        btnAddSeq.setBorder(BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)));
        btnAddSeq.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAddSeq.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Nombre de la nueva secuencia:");
            if (name != null && !name.isBlank()) {
                a.addSequence(name);
                a.setCurrentSequence(name);
                guardar();
                reconstruirCuerpo();
            }
        });

        JButton btnDelSeq = new JButton("-");
        btnDelSeq.setPreferredSize(new Dimension(35, 22));
        btnDelSeq.setBackground(new java.awt.Color(100, 50, 50));
        btnDelSeq.setForeground(java.awt.Color.WHITE);
        btnDelSeq.setFocusPainted(false);
        btnDelSeq.setFont(new Font("Arial", Font.BOLD, 14));
        btnDelSeq.setBorder(BorderFactory.createLineBorder(new java.awt.Color(120, 70, 70)));
        btnDelSeq.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDelSeq.addActionListener(e -> {
            String sel = (String) comboSeq.getSelectedItem();
            if (sel != null && !sel.equals("default")) {
                int resp = JOptionPane.showConfirmDialog(this, "¿Borrar la secuencia '" + sel + "'?", "Borrar", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) {
                    a.removeSequence(sel);
                    a.setCurrentSequence("default");
                    guardar();
                    reconstruirCuerpo();
                }
            }
        });

        JLabel lblSeq = new JLabel("Seq: ");
        lblSeq.setForeground(org.motor2d.utilities.Color.TEXT_SECONDARY);
        lblSeq.setFont(new Font("Arial", Font.PLAIN, 11));

        panelSecuencias.add(lblSeq, BorderLayout.WEST);
        panelSecuencias.add(comboSeq, BorderLayout.CENTER);
        
        JPanel panelBtns = new JPanel(new GridLayout(1, 2, 2, 0));
        panelBtns.setOpaque(false);
        panelBtns.add(btnAddSeq);
        panelBtns.add(btnDelSeq);
        panelSecuencias.add(panelBtns, BorderLayout.EAST);
        
        sec.add(panelSecuencias);

        // --- Gestión de Frames de la secuencia actual ---
        String current = a.getCurrentSequence();
        if (current != null && a.getSequences().containsKey(current)) {
            java.util.List<String> frames = a.getSequences().get(current);
            
            JPanel panelFrames = new JPanel();
            panelFrames.setLayout(new BoxLayout(panelFrames, BoxLayout.Y_AXIS));
            panelFrames.setBackground(org.motor2d.utilities.Color.PANEL_ALT_BACKGROUND);
            panelFrames.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 8));

            for (int i = 0; i < frames.size(); i++) {
                final int index = i;
                String path = frames.get(i);
                
                JPanel filaFrame = new JPanel(new BorderLayout(5, 0));
                filaFrame.setBackground(org.motor2d.utilities.Color.PANEL_ALT_BACKGROUND);
                filaFrame.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24)); // Altura fija
                
                JLabel lblFrame = new JLabel(new File(path).getName());
                lblFrame.setFont(new Font("Arial", Font.PLAIN, 10));
                lblFrame.setForeground(org.motor2d.utilities.Color.TEXT_SECONDARY);
                
                JButton btnRemove = new JButton("x");
                btnRemove.setPreferredSize(new Dimension(22, 20));
                btnRemove.setBackground(new java.awt.Color(180, 50, 50));
                btnRemove.setForeground(java.awt.Color.WHITE);
                btnRemove.setFocusPainted(false);
                btnRemove.setBorder(BorderFactory.createEmptyBorder());
                btnRemove.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btnRemove.addActionListener(ev -> {
                    frames.remove(index);
                    guardar();
                    reconstruirCuerpo();
                });

                filaFrame.add(lblFrame, BorderLayout.CENTER);
                filaFrame.add(btnRemove, BorderLayout.EAST);
                panelFrames.add(filaFrame);
            }

            JButton btnAddFrame = new JButton("Añadir Frames...");
            btnAddFrame.setBackground(new java.awt.Color(60, 60, 60));
            btnAddFrame.setForeground(java.awt.Color.WHITE);
            btnAddFrame.setFocusPainted(false);
            btnAddFrame.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnAddFrame.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(true);
                if (controller != null && controller.getProjectPath() != null) {
                    chooser.setCurrentDirectory(new File(controller.getProjectPath(), "assets"));
                }
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    for (File f : chooser.getSelectedFiles()) {
                        String rel = controller.getProjectManager().getCurrentProject().getPath();
                        String path = f.getAbsolutePath();
                        if (path.startsWith(rel)) {
                            path = path.substring(rel.length());
                            if (path.startsWith(File.separator)) path = path.substring(1);
                        }
                        frames.add(path.replace("\\", "/"));
                    }
                    guardar();
                    reconstruirCuerpo();
                }
            });
            btnAddFrame.setAlignmentX(CENTER_ALIGNMENT);
            panelFrames.add(Box.createVerticalStrut(5));
            panelFrames.add(btnAddFrame);
            sec.add(panelFrames);
        }

        cuerpo.add(sec);
        agregarSeparador();
    }

    // ==================== SECCIÓN: PLAYER CONTROLLER ====================
    private void agregarSeccionPlayerController(PlayerController p) {
        JPanel sec = crearSeccion("Player Controller");

        sec.add(crearFila("Velocidad", crearFloatField(p.getSpeed(),     v -> { p.setSpeed(v);     guardar(); })));
        sec.add(crearFila("Fuerza Salto", crearFloatField(p.getJumpForce(), v -> { p.setJumpForce(v); guardar(); })));

        agregarSeparador();
        sec.add(UIFactory.createHeaderLabel("  Mapping de Animaciones"));
        
        JTextField txtIdle = crearTextField(p.getAnimIdle());
        txtIdle.addActionListener(e -> { p.setAnimIdle(txtIdle.getText().trim()); guardar(); });
        sec.add(crearFila("Idle Seq", txtIdle));

        JTextField txtWalkSide = crearTextField(p.getAnimWalkSide());
        txtWalkSide.addActionListener(e -> { p.setAnimWalkSide(txtWalkSide.getText().trim()); guardar(); });
        sec.add(crearFila("Walk Side Seq", txtWalkSide));

        JTextField txtWalkUp = crearTextField(p.getAnimWalkUp());
        txtWalkUp.addActionListener(e -> { p.setAnimWalkUp(txtWalkUp.getText().trim()); guardar(); });
        sec.add(crearFila("Walk Up Seq", txtWalkUp));

        JTextField txtWalkDown = crearTextField(p.getAnimWalkDown());
        txtWalkDown.addActionListener(e -> { p.setAnimWalkDown(txtWalkDown.getText().trim()); guardar(); });
        sec.add(crearFila("Walk Down Seq", txtWalkDown));

        JTextField txtJump = crearTextField(p.getAnimJump());
        txtJump.addActionListener(e -> { p.setAnimJump(txtJump.getText().trim()); guardar(); });
        sec.add(crearFila("Jump Seq", txtJump));

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
