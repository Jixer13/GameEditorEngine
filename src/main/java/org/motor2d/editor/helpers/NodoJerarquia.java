package org.motor2d.editor.helpers;

import org.motor2d.model.Entity;
import org.motor2d.model.ui.UIElement;
import org.motor2d.model.Tileset;

/**
 * NodoJerarquia - Clase de apoyo para representar elementos en el JTree de la jerarquía.
 */
public class NodoJerarquia {
    public enum Tipo { ESCENA, ENTIDAD, UI_ELEMENT, TILESET }
    
    public String nombre;
    public Tipo tipo;
    public Entity entidad;
    public UIElement uiElement;
    public Tileset tileset;

    public NodoJerarquia(String nombre, Tipo tipo, Entity entidad) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.entidad = entidad;
    }

    public NodoJerarquia(String nombre, Tipo tipo, UIElement ui) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.uiElement = ui;
    }

    public NodoJerarquia(String nombre, Tipo tipo, Tileset ts) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.tileset = ts;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
