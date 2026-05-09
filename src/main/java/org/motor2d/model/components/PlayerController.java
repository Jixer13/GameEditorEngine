package org.motor2d.model.components;

/**
 * PlayerController - Ejemplo de controlador para el jugador.
 */
public class PlayerController extends Behavior {

    private float speed = 200.0f;
    private float jumpForce = 10.0f;
    
    // Nombres de animaciones personalizables
    private String animIdle = "idle";
    private String animWalkSide = "walk_side";
    private String animWalkUp = "walk_up";
    private String animWalkDown = "walk_down";
    private String animJump = "jump";

    private float verticalVelocity = 0;
    private boolean isGrounded = true;

    @Override
    public void start() {
        setCameraTarget(getEntity());
        org.motor2d.core.Engine.getCamara().setZoom(1.5f);
    }

    @Override
    public void update(float deltaTime) {
        if (getEntity() == null) return;
        Transform transform = getEntity().getComponent(Transform.class);
        SpriteRenderer sprite = getEntity().getComponent(SpriteRenderer.class);
        Animation anim = getEntity().getComponent(Animation.class);
        
        if (transform == null) return;

        float dx = 0;
        float dy = 0;
        String nextAnim = animIdle;

        // --- MOVIMIENTO HORIZONTAL ---
        if (isKeyDown(java.awt.event.KeyEvent.VK_A)) {
            dx -= speed;
            if (sprite != null) sprite.setFlipX(true);
            nextAnim = animWalkSide;
        }
        if (isKeyDown(java.awt.event.KeyEvent.VK_D)) {
            dx += speed;
            if (sprite != null) sprite.setFlipX(false);
            nextAnim = animWalkSide;
        }

        // --- MOVIMIENTO VERTICAL ---
        if (isKeyDown(java.awt.event.KeyEvent.VK_W)) {
            dy -= speed;
            nextAnim = animWalkUp;
        }
        if (isKeyDown(java.awt.event.KeyEvent.VK_S)) {
            dy += speed;
            nextAnim = animWalkDown;
        }

        // --- SALTO ---
        if (isKeyDown(java.awt.event.KeyEvent.VK_SPACE) && isGrounded) {
            nextAnim = animJump;
        }

        transform.setX(transform.getX() + dx * deltaTime);
        transform.setY(transform.getY() + dy * deltaTime);

        // --- GESTIÓN DE ANIMACIONES ---
        if (anim != null) {
            if (dx == 0 && dy == 0 && nextAnim.equals(animIdle)) {
                anim.play(animIdle);
            } else {
                // Intentar reproducir la animación detectada
                if (anim.getSequences().containsKey(nextAnim)) {
                    anim.play(nextAnim);
                } else {
                    // Si no existe la específica, intentar usar la genérica "walk"
                    anim.play("walk");
                }
            }
        }
    }

    // Getters y Setters para que el Serializer y el Editor los vean
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }

    public float getJumpForce() { return jumpForce; }
    public void setJumpForce(float jumpForce) { this.jumpForce = jumpForce; }

    public String getAnimIdle() { return animIdle; }
    public void setAnimIdle(String animIdle) { this.animIdle = animIdle; }

    public String getAnimWalkSide() { return animWalkSide; }
    public void setAnimWalkSide(String animWalkSide) { this.animWalkSide = animWalkSide; }

    public String getAnimWalkUp() { return animWalkUp; }
    public void setAnimWalkUp(String animWalkUp) { this.animWalkUp = animWalkUp; }

    public String getAnimWalkDown() { return animWalkDown; }
    public void setAnimWalkDown(String animWalkDown) { this.animWalkDown = animWalkDown; }

    public String getAnimJump() { return animJump; }
    public void setAnimJump(String animJump) { this.animJump = animJump; }
}

