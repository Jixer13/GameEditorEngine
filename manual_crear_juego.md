# 🚀 Guía Paso a Paso: Crea tu primer juego en Motor2D

Esta guía te enseñará a crear un nivel básico (un personaje que choca con plataformas) utilizando únicamente la interfaz del editor.

---

## Paso 1: Inicialización del Proyecto
1. Abre el motor y en la pantalla inicial selecciona **"Nuevo Proyecto"**.
2. Ponle un nombre (ej: `MiSuperJuego`).
3. Selecciona una carpeta vacía en tu ordenador.
4. El editor se abrirá automáticamente con una escena vacía llamada `main`.

---

## Paso 2: Importar tus Assets (Sprites)
Antes de trabajar en el editor, prepara tus imágenes:
1. Ve a la carpeta de tu proyecto en Windows.
2. Entra en `assets/sprites/`.
3. Copia allí tus imágenes `.png` (ej: `jugador.png` y `suelo.png`).
4. Vuelve al editor y en el panel **Assets** (inferior), haz clic derecho y selecciona **🔄 Refrescar**. Ahora verás tus imágenes.

---

## Paso 3: Crear el Escenario (Suelo y Paredes)
1. En el panel **Hierarchy** (izquierda), haz clic derecho y selecciona **"Crear Entidad"**.
2. Cámbiale el nombre a `Suelo` en el panel de **Properties** (derecha).
3. En la sección **SpriteRenderer**, haz clic en el botón `...` y selecciona tu imagen `suelo.png`.
4. Ajusta su **Transform**:
   - Pon la **Pos Y** en un valor alto (ej: `600`) para que esté en la parte inferior.
   - Aumenta la **Escala X** (ej: `20`) para que sea una plataforma larga.
5. **Físicas:** 
   - Añade un componente **Collider**.
   - **¡MUY IMPORTANTE!**: Marca la casilla `isStatic`. Esto hará que el suelo no se caiga ni se mueva cuando algo lo golpee.

---

## Paso 4: Crear al Jugador
1. Crea una nueva entidad y llámala `Jugador`.
2. Asígnale la imagen `jugador.png` en el **SpriteRenderer**.
3. Colócalo en el aire (ej: **Pos X**: `100`, **Pos Y**: `100`).
4. Añade un componente **Collider**.
5. **Físicas:** Deja la casilla `isStatic` **desmarcada**. Esto significa que el jugador es un objeto dinámico que reaccionará a las colisiones.

---

## Paso 5: Preparar el Audio
1. Copia tus archivos `.wav` a la carpeta `assets/audio/`.
2. En el panel de **Properties** del Jugador, baja hasta la sección **Audio Preview**.
3. Selecciona tu sonido de salto o música y pulsa **▶ Play** para verificar que se escucha correctamente.

---

## Paso 6: Guardar y Probar
1. Pulsa el botón de **Guardar** (icono de disco) en la barra superior o en el menú de proyecto.
2. Tu configuración de posiciones, escalas y colisiones ya está lista para que el motor las procese.

---

## Próximos Pasos:
Para que el jugador se mueva con las flechas, deberás crear un **Behavior** (ver `MANUAL.md` para la parte técnica) y asignarlo al objeto `Jugador`.

¡Felicidades! Ya tienes un nivel con físicas base configurado desde la UI.
