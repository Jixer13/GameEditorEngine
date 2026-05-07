package org.motor2d.editor;

import org.motor2d.model.Entity;
import org.motor2d.model.components.Animation;
import org.motor2d.model.components.Collider;
import org.motor2d.model.components.SpriteRenderer;
import org.motor2d.model.components.Transform;
import org.motor2d.utilities.Color;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
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
        reconstruirCuerpo();
    }

    public void limpiar() {
        this.entidadActual = null;
        this.assetActual   = null;
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

        if (entidadActual == null && assetActual == null) {
            mostrarMensajeVacio();
        } else if (entidadActual != null) {
            agregarSeccionEntidad();
            agregarSeparador();

            for (org.motor2d.model.components.Component comp : entidadActual.getComponents()) {
                if      (comp instanceof Transform t)       agregarSeccionTransform(t);
                else if (comp instanceof SpriteRenderer s)  agregarSeccionSprite(s);
                else if (comp instanceof Collider c)        agregarSeccionCollider(c);
                else if (comp instanceof Animation a)       agregarSeccionAnimation(a);
            }

            cuerpo.add(Box.createVerticalGlue());
            
            // Sección de Reproductor de Audio (Última sección)
            agregarSeccionAudioPlayer();
        } else if (assetActual != null) {
            agregarSeccionInfoArchivo(assetActual);
            
            String name = assetActual.getName().toLowerCase();
            if (name.endsWith(".mp3") || name.endsWith(".wav") || 
                name.endsWith(".ogg") || name.endsWith(".mp4")) {
                agregarSeccionAssetMedia(assetActual);
            }
        }

        cuerpo.revalidate();
        cuerpo.repaint();
    }

    private void agregarSeccionInfoArchivo(File file) {
        JPanel sec = crearSeccion("📄 Asset: Info");
        
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

    private void agregarSeccionAssetMedia(File file) {
        agregarSeparador();
        JPanel sec = crearSeccion("🔊 Asset: Media");

        // Nombre del archivo centrado
        JLabel lblNombre = new JLabel(file.getName(), SwingConstants.CENTER);
        lblNombre.setForeground(Color.TEXT_SECONDARY);
        lblNombre.setFont(new Font("Arial", Font.PLAIN, 10));
        lblNombre.setAlignmentX(CENTER_ALIGNMENT);
        lblNombre.setBorder(BorderFactory.createEmptyBorder(4, 8, 2, 8));
        sec.add(lblNombre);

        sec.add(crearControlsAudio(() -> {
            if (controller == null) return;
            String p = file.getAbsolutePath();
            String base = controller.getProjectPath();
            if (base != null && p.startsWith(base)) {
                p = p.substring(base.length());
                if (p.startsWith(File.separator)) p = p.substring(1);
            }
            controller.playAudioPreview(p);
        }));

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

    private void agregarSeccionAudioPlayer() {
        JPanel sec = crearSeccion("🔊 Audio Preview");

        // Lista de archivos de audio del proyecto
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setBackground(Color.PANEL_BACKGROUND);
        list.setForeground(Color.TEXT_PRIMARY);
        list.setFont(new Font("Arial", Font.PLAIN, 11));
        list.setSelectionBackground(Color.TREE_SELECTION);
        list.setSelectionForeground(Color.TEXT_PRIMARY);
        list.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        list.setFixedCellHeight(22);

        if (controller != null && controller.getProjectPath() != null) {
            File audioDir = new File(controller.getProjectPath(), "assets/audio");
            if (audioDir.exists()) {
                File[] files = audioDir.listFiles((d, name) ->
                    name.endsWith(".wav") || name.endsWith(".mp3") || name.endsWith(".ogg"));
                if (files != null)
                    for (File f : files) model.addElement("🔊  " + f.getName());
            }
        }

        if (model.isEmpty()) model.addElement("  — sin archivos de audio —");

        JScrollPane audioScroll = new JScrollPane(list);
        audioScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        audioScroll.setPreferredSize(new Dimension(0, 90));
        audioScroll.setBackground(Color.PANEL_BACKGROUND);
        audioScroll.getViewport().setBackground(Color.PANEL_BACKGROUND);
        audioScroll.setBorder(BorderFactory.createLineBorder(Color.BORDER_COLOR, 1));
        audioScroll.getVerticalScrollBar().setUI(new ScrollBarPersonalizado());
        sec.add(audioScroll);

        // Controles de reproducción
        sec.add(crearControlsAudio(() -> {
            String sel = list.getSelectedValue();
            if (sel == null || sel.startsWith("  —")) return;
            String nombre = sel.replace("🔊  ", "").trim();
            if (controller != null) controller.playAudioPreview("assets/audio/" + nombre);
        }));

        cuerpo.add(sec);
    }

    /**
     * Construye el bloque de controles Play/Stop con estilo consistente.
     * @param onPlay acción a ejecutar al pulsar Play
     */
    private JPanel crearControlsAudio(Runnable onPlay) {
        JPanel controls = new JPanel(new GridLayout(1, 2, 6, 0));
        controls.setBackground(Color.BACKGROUND);
        controls.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        controls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton btnPlay = crearBotonAudio("▶  Play",  Color.BUTTON_DEFAULT, Color.BUTTON_HOVER);
        JButton btnStop = crearBotonAudio("⏹  Stop",  Color.PANEL_BACKGROUND, Color.BUTTON_HOVER);

        btnPlay.addActionListener(e -> onPlay.run());
        btnStop.addActionListener(e -> { if (controller != null) controller.stopAudioPreview(); });

        controls.add(btnPlay);
        controls.add(btnStop);
        return controls;
    }

    private JButton crearBotonAudio(String texto, java.awt.Color bg, java.awt.Color hover) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(Color.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.TEXT_PRIMARY);
        btn.setFont(new Font("Arial", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
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
        JPanel sec = crearSeccion("📦 Entidad");

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
                File assets = new File(controller.getProjectPath(), "Assets");
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
                String base = controller.getProjectPath();
                if (rutaFinal.startsWith(base)) {
                    rutaFinal = rutaFinal.substring(base.length());
                    if (rutaFinal.startsWith(File.separator))
                        rutaFinal = rutaFinal.substring(1);
                }
            }

            campoImg.setText(rutaFinal);

            // Crear SpriteRenderer y asignarlo a la entidad
            SpriteRenderer sr = new SpriteRenderer();
            sr.setSpritePath(rutaFinal);
            entidadActual.addComponent(sr);
            guardar();

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
        JPanel sec = crearSeccion("📐 Transform");

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
        JPanel sec = crearSeccion("🖼 SpriteRenderer");

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
            sec.add(crearFila("Preview", crearLabelValor("⚠ Imagen no encontrada")));
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
        if (controller == null) return null;

        // 1. Ruta absoluta
        File f = new File(spritePath);
        if (f.isAbsolute() && f.exists()) return f;

        // 2. Relativa al proyecto
        String projectPath = controller.getProjectPath();
        if (projectPath != null && !projectPath.isBlank()) {
            f = new File(projectPath, spritePath);
            if (f.exists()) return f;
            // 3. Dentro de Assets
            f = new File(projectPath, "Assets" + File.separator + spritePath);
            if (f.exists()) return f;
        }
        return null;
    }

    // ==================== SECCIÓN: COLLIDER ====================
    private void agregarSeccionCollider(Collider c) {
        JPanel sec = crearSeccion("🟥 Collider");

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

        JLabel lbl = new JLabel("  " + titulo);
        lbl.setForeground(Color.TEXT_PRIMARY);
        lbl.setFont(new Font("Arial", Font.BOLD, 11));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        lbl.setBackground(Color.PANEL_BACKGROUND);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 0));
        sec.add(lbl);
        return sec;
    }

    private JPanel crearFila(String etiqueta, JComponent control) {
        JPanel fila = new JPanel(new BorderLayout(4, 0));
        fila.setBackground(Color.PANEL_ALT_BACKGROUND);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        fila.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        JLabel lbl = new JLabel(etiqueta);
        lbl.setForeground(Color.TEXT_SECONDARY);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        lbl.setPreferredSize(new Dimension(70, 22));
        fila.add(lbl, BorderLayout.WEST);
        fila.add(control, BorderLayout.CENTER);
        return fila;
    }

    private JTextField crearTextField(String valor) {
        JTextField tf = new JTextField(valor);
        tf.setBackground(Color.PANEL_BACKGROUND);
        tf.setForeground(Color.TEXT_PRIMARY);
        tf.setCaretColor(Color.TEXT_PRIMARY);
        tf.setFont(new Font("Arial", Font.PLAIN, 11));
        tf.setBorder(BorderFactory.createLineBorder(Color.BORDER_COLOR, 1));
        return tf;
    }

    private JLabel crearLabelValor(String val) {
        JLabel lbl = new JLabel(val);
        lbl.setForeground(Color.TEXT_PRIMARY);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        return lbl;
    }

    private JTextField crearFloatField(float valor, FloatConsumer onChange) {
        JTextField tf = crearTextField(String.valueOf(valor));
        tf.addActionListener(e -> {
            try { onChange.accept(Float.parseFloat(tf.getText().trim())); }
            catch (NumberFormatException ignored) {}
        });
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                try { onChange.accept(Float.parseFloat(tf.getText().trim())); }
                catch (NumberFormatException ignored) {}
            }
        });
        return tf;
    }

    private JTextField crearIntField(int valor, IntConsumer onChange) {
        JTextField tf = crearTextField(String.valueOf(valor));
        tf.addActionListener(e -> {
            try { onChange.accept(Integer.parseInt(tf.getText().trim())); }
            catch (NumberFormatException ignored) {}
        });
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                try { onChange.accept(Integer.parseInt(tf.getText().trim())); }
                catch (NumberFormatException ignored) {}
            }
        });
        return tf;
    }

    private void agregarSeparador() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Color.BORDER_COLOR);
        sep.setBackground(Color.BACKGROUND);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        cuerpo.add(sep);
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
