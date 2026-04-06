package org.motor2d.utilities;

public class Vector2 {

    public float x;
    public float y;

    public Vector2() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // Vectores predefinidos
    public static Vector2 zero()  { return new Vector2(0,  0 ); }
    public static Vector2 one()   { return new Vector2(1,  1 ); }
    public static Vector2 up()    { return new Vector2(0,  -1); }
    public static Vector2 down()  { return new Vector2(0,  1 ); }
    public static Vector2 left()  { return new Vector2(-1, 0 ); }
    public static Vector2 right() { return new Vector2(1,  0 ); }

    // Operaciones básicas
    public Vector2 add(Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public Vector2 subtract(Vector2 other) {
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 scale(float factor) {
        return new Vector2(this.x * factor, this.y * factor);
    }

    public Vector2 negate() {
        return new Vector2(-this.x, -this.y);
    }

    // Magnitud y normalización
    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float magnitudeSquared() {
        return x * x + y * y;
    }

    public Vector2 normalize() {
        float mag = magnitude();
        if (mag == 0) return Vector2.zero();
        return new Vector2(x / mag, y / mag);
    }

    // Distancia entre dos puntos
    public float distanceTo(Vector2 other) {
        return this.subtract(other).magnitude();
    }

    // Producto escalar
    public float dot(Vector2 other) {
        return this.x * other.x + this.y * other.y;
    }

    // Interpolacion lineal, t=0 devuelve this, t=1 devuelve other
    public Vector2 lerp(Vector2 other, float t) {
        t = Math.max(0, Math.min(1, t));
        return new Vector2(
                this.x + (other.x - this.x) * t,
                this.y + (other.y - this.y) * t
        );
    }

    // Angulo del vector en grados
    public float angle() {
        return (float) Math.toDegrees(Math.atan2(y, x));
    }

    // Rotar el vector un angulo en grados
    public Vector2 rotate(float degrees) {
        float radians = (float) Math.toRadians(degrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        return new Vector2(
                x * cos - y * sin,
                x * sin + y * cos
        );
    }

    @Override
    public String toString() {
        return "Vector2(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vector2 other)) return false;
        return Float.compare(x, other.x) == 0 &&
                Float.compare(y, other.y) == 0;
    }
}