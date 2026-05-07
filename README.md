# 🎮 Motor2D - Status Log

### [CHANGELOG]
- Implementado `InputManager` con polling de teclado y ratón (KeyListener/MouseListener).
- Creada clase base `Behavior` para permitir lógica de usuario personalizada por entidad.
- Actualizado `GameLoop` para procesar automáticamente `Behaviors` y `Animations`.
- Registrado `Behavior` en `Serializer` para persistencia en JSON.
- Finalizada resolución de colisiones AABB en `PhysicsSystem` con cálculo de MTV y respuesta física.
- Añadida sección de "Audio Preview" en `PanelProperties` para previsualizar sonidos del proyecto.
- Creado `MANUAL.md` con instrucciones detalladas de uso del motor para desarrolladores.
- Creado `manual_crear_juego.md` con una guía paso a paso para crear un nivel desde la interfaz.
- Implementado `AudioManager` global para gestión de música de fondo (BGM) y efectos de sonido (SFX) en runtime.
- Integrado el sistema de audio en `Behavior`, permitiendo disparar sonidos por código fácilmente.
- Añadido sistema de "Camera Follow" con suavizado (Interpolación lineal/Lerp) y límites de escena automáticos.
- Implementado sistema de propiedades contextuales para assets (Info de archivo y previsualización de medios).
- Implementado sistema de "Prefabs" que permite cargar y clonar entidades desde JSON en tiempo de ejecución.
- Añadida función `instantiate(path)` en `Behavior` para spawn dinámico de objetos (balas, enemigos).
- Corregido problema de renderizado en el Toolbar superior tras refactor de UI:
  - Implementado `paintComponent` en `Toolbar` para el dibujado autónomo de botones.
  - Añadido `getPreferredSize` para asegurar la visibilidad del panel en `BorderLayout`.
  - Limpieza de lógica de dibujado redundante en `Editor.java`.

### 📋 [PENDING]
