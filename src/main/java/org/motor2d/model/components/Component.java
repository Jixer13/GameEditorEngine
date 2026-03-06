package org.motor2d.model.components;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.motor2d.model.Entity;

@JsonTypeInfo(//le dice a jackson que cuando guarde la clase json incluya el tipo en el json
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({//indica que clases concretas puede encontrarse cuando lea ese campo
        @JsonSubTypes.Type(value = Transform.class,       name = "Transform"),
        @JsonSubTypes.Type(value = SpriteRenderer.class,  name = "SpriteRenderer"),
        @JsonSubTypes.Type(value = Collider.class,        name = "Collider"),
        @JsonSubTypes.Type(value = Animation.class,       name = "Animation")
})
//no se usa directamente, solo existe para heredar
public abstract class Component {

    private boolean enabled;//esta activo o no

    @JsonIgnore//este campo no se tiene porque guardar en el json
    private Entity owner; //owner es la entity a la que pertenece el componente,
    // por ejemplo que el transform de player entienda que es solo de player

    public Component() {
        this.enabled = true;
        this.owner = null;//al principio no pertenece a ninguna entidad
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @JsonIgnore
    public Entity getOwner() { return owner; }

    @JsonIgnore
    public void setOwner(Entity owner) { this.owner = owner; }
}