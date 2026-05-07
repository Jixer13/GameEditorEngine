# 📖 Manual del Editor - Motor2D

Este manual explica cómo utilizar la interfaz gráfica (UI) de **Motor2D** para diseñar y configurar tu videojuego.

---

## 1. Gestión de Proyectos
Al iniciar, verás la pantalla de **Index**.
- **Nuevo Proyecto:** Selecciona una carpeta vacía y dale un nombre. El motor creará automáticamente la estructura de carpetas (`assets`, `scenes`, etc.).
- **Abrir Proyecto:** Busca una carpeta que ya contenga un archivo `project.json`.

---

## 2. El Explorador de Assets (Panel Inferior)
Aquí puedes gestionar todos tus archivos externos.
- **Navegación:** Haz clic en las carpetas para abrirlas.
- **Previsualización:** Haz clic en una imagen (PNG/JPG) para verla en grande en el visor central.
- **Organización:** Haz clic derecho sobre cualquier carpeta para:
  - Crear nuevas subcarpetas.
  - Abrir la carpeta en el explorador de Windows.
  - Eliminar archivos o carpetas.

---

## 3. Diseño de Escena (Panel Central y Jerarquía)
- **Canvas (Centro):** Es el mundo de tu juego. Puedes ver cómo quedan colocados tus objetos.
- **Hierarchy (Izquierda):** Muestra una lista de todas las entidades presentes en la escena actual. 
  - Haz clic en un nombre para seleccionarlo y editar sus propiedades.
  - Haz clic derecho para duplicar o eliminar entidades.

---

## 4. Edición de Propiedades (Panel Derecho)
Cuando seleccionas un objeto, este panel se activa.
- **Transform:** Cambia la posición (X, Y), la escala y la rotación.
- **SpriteRenderer:** 
  - Haz clic en `...` para buscar y asignar una imagen PNG de tu carpeta de assets.
  - Activa `Flip X/Y` para invertir la imagen.
- **Collider (Físicas):**
  - **isStatic:** ¡Importante! Marca esta casilla para objetos que no deben moverse (como el suelo o las paredes).
  - **isTrigger:** Márcalo si quieres que el objeto detecte choques pero deje pasar a través (ej. una moneda).
- **Audio Preview:**
  - Al final del panel verás una lista de tus audios. 
  - Selecciona uno y pulsa **Play** para escucharlo. Esto sirve para testear los efectos de sonido antes de programarlos.

---

## 5. Tips de Flujo de Trabajo
1. **Importar imágenes:** Copia tus PNG a la carpeta `assets/sprites` de tu proyecto usando Windows. Luego, en el editor, refresca el panel de Assets.
2. **Crear Paredes:** Crea una entidad, dale una imagen de bloque, añade un `Collider` y marca la casilla `isStatic`.
3. **Probar Sonidos:** Pon tus archivos `.wav` en `assets/audio` y usa el reproductor del panel derecho para revisarlos.

---

*Manual de Usuario (UI) actualizado el 7 de mayo de 2026.*
