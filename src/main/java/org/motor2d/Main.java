package org.motor2d;

import org.motor2d.manager.EntityManager;
import org.motor2d.manager.ProjectManager;
import org.motor2d.manager.SceneManager;
import org.motor2d.manager.TilesetManager;
import org.motor2d.model.*;
import org.motor2d.model.components.Collider;
import org.motor2d.model.components.Transform;
import org.motor2d.model.ui.UILabel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            testProjectManager();
            testSceneManager();
            testEntityManager();
            testTilesetManager();
        } catch (Exception e) {
            System.err.println("ERROR inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // TEST PROJECT MANAGER

    private static void testProjectManager() throws Exception {
        System.out.println("=== TEST PROJECT MANAGER ===");

        ProjectManager pm = new ProjectManager();
        String path = Files.createTempDirectory("motor2d-test").toString();

        // 1. Crear proyecto
        Project project = pm.createProject("Test Project", path);
        System.out.println("OK - Proyecto creado: " + project.getName());
        System.out.println("   Ruta: " + path);

        // 2. Verificar estructura de carpetas
        System.out.println("OK - project.json existe: " +
                new File(path, "project.json").exists());
        System.out.println("OK - scenes/ existe: " +
                new File(path, "scenes").exists());
        System.out.println("OK - assets/sprites/ existe: " +
                new File(path, "assets" + File.separator + "sprites").exists());
        System.out.println("OK - assets/audio/ existe: " +
                new File(path, "assets" + File.separator + "audio").exists());
        System.out.println("OK - assets/fonts/ existe: " +
                new File(path, "assets" + File.separator + "fonts").exists());
        System.out.println("OK - assets/tilesets/ existe: " +
                new File(path, "assets" + File.separator + "tilesets").exists());

        // 3. Configurar proyecto
        pm.setWindowSize(1280, 720);
        pm.setFps(60);
        System.out.println("OK - Configuracion aplicada");
        System.out.println("   Resolucion: " +
                pm.getCurrentProject().getConfiguration().getWindowWidth() +
                "x" +
                pm.getCurrentProject().getConfiguration().getWindowHeight());
        System.out.println("   FPS: " +
                pm.getCurrentProject().getConfiguration().getFps());

        // 4. Cerrar y reabrir
        pm.closeProject();
        System.out.println("OK - Proyecto cerrado");
        System.out.println("   isProjectOpen: " + pm.isProjectOpen());

        pm.openProject(path);
        System.out.println("OK - Proyecto reabierto: " +
                pm.getCurrentProject().getName());

        // 5. Guardar como
        String newPath = Files.createTempDirectory("motor2d-saveas").toString();
        pm.saveProjectAs(newPath);
        System.out.println("OK - Guardado en nueva ruta: " + newPath);

        // 6. Eliminar proyecto
        pm.deleteProject(newPath);
        System.out.println("OK - Proyecto eliminado: " +
                !new File(newPath).exists());

        // 7. Intentar abrir proyecto inexistente
        try {
            pm.openProject("/ruta/inexistente");
            System.out.println("FALLO - Deberia haber fallado");
        } catch (IOException e) {
            System.out.println("OK - Correcto, proyecto no encontrado: " +
                    e.getMessage());
        }

        // Limpiar
        deleteDirectory(new File(path));
        System.out.println("OK - Carpeta temporal limpiada");
    }

    // TEST SCENE MANAGER

    private static void testSceneManager() throws Exception {
        System.out.println("\n=== TEST SCENE MANAGER ===");

        ProjectManager pm = new ProjectManager();
        SceneManager sm = new SceneManager(pm);
        String path = Files.createTempDirectory("motor2d-test").toString();
        pm.createProject("Test Project", path);

        // 1. Crear escena
        Scene scene = sm.createScene("level1");
        System.out.println("OK - Escena creada: " + scene.getName());
        System.out.println("   Archivo existe: " +
                new File(path + File.separator + "scenes",
                        "level1.json").exists());

        // 2. Crear otra escena
        sm.createScene("menu");
        System.out.println("OK - Escena menu creada");

        // 3. Listar escenas
        List<String> scenes = sm.listScenes();
        System.out.println("OK - Escenas disponibles: " + scenes);

        // 4. No permitir nombre duplicado
        try {
            sm.createScene("level1");
            System.out.println("FALLO - Deberia haber fallado");
        } catch (IOException e) {
            System.out.println("OK - Correcto, nombre duplicado: " +
                    e.getMessage());
        }

        // 5. No permitir nombre vacio
        try {
            sm.createScene("");
            System.out.println("FALLO - Deberia haber fallado");
        } catch (IOException e) {
            System.out.println("OK - Correcto, nombre vacio: " +
                    e.getMessage());
        }

        // 6. Cerrar y recargar escena
        sm.closeScene();
        System.out.println("OK - Escena cerrada");
        System.out.println("   isSceneOpen: " + sm.isSceneOpen());

        sm.loadScene("level1");
        System.out.println("OK - Escena recargada: " +
                sm.getCurrentScene().getName());

        // 7. Cambiar configuracion
        sm.setSceneSize(5000, 2000);
        sm.setBackgroundColor("#000000");
        System.out.println("OK - Configuracion actualizada");
        System.out.println("   Tamano: " +
                sm.getCurrentScene().getWidth() +
                "x" + sm.getCurrentScene().getHeight());
        System.out.println("   Color: " +
                sm.getCurrentScene().getBackgroundColor());

        // 8. Intentar borrar escena inicial
        try {
            sm.deleteScene("main");
            System.out.println("FALLO - Deberia haber fallado");
        } catch (IOException e) {
            System.out.println("OK - Correcto, no se puede borrar escena inicial: "
                    + e.getMessage());
        }

        // 9. Borrar escena
        sm.deleteScene("menu");
        System.out.println("OK - Escena menu eliminada");
        System.out.println("   Escenas restantes: " + sm.listScenes());

        // Limpiar
        deleteDirectory(new File(path));
        System.out.println("OK - Carpeta temporal limpiada");
    }


    // TEST ENTITY MANAGER
    private static void testEntityManager() throws Exception {
        System.out.println("\n=== TEST ENTITY MANAGER ===");

        ProjectManager pm = new ProjectManager();
        SceneManager sm = new SceneManager(pm);
        EntityManager em = new EntityManager(sm);
        String path = Files.createTempDirectory("motor2d-test").toString();
        pm.createProject("Test", path);
        sm.createScene("level1");

        // 1. Crear entidad basica
        Entity player = em.createEntity("Player");
        System.out.println("OK - Entidad creada: " + player.getName());
        System.out.println("   Tiene Transform: " +
                player.hasComponent(Transform.class));

        // 2. Crear entidad con sprite
        Entity enemy = em.createSpriteEntity("Enemy",
                "assets/sprites/enemy.png");
        System.out.println("OK - Entidad con sprite creada: " + enemy.getName());

        // 3. Añadir tag y componente
        player.setTag("player");
        em.addComponent(player, new Collider());
        System.out.println("OK - Collider añadido a Player");

        // 4. No permitir componentes duplicados
        try {
            em.addComponent(player, new Collider());
            System.out.println("FALLO - Deberia haber fallado");
        } catch (IOException e) {
            System.out.println("OK - Correcto, no permite duplicados: " +
                    e.getMessage());
        }

        // 5. No permitir quitar Transform
        try {
            em.removeComponent(player, Transform.class);
            System.out.println("FALLO - Deberia haber fallado");
        } catch (IOException e) {
            System.out.println("OK - Correcto, no se puede quitar Transform: " +
                    e.getMessage());
        }

        // 6. Duplicar entidad
        Entity playerCopy = em.duplicateEntity(player);
        System.out.println("OK - Entidad duplicada: " + playerCopy.getName());
        System.out.println("   Tiene Collider: " +
                playerCopy.hasComponent(Collider.class));

        // 7. Buscar entidades
        System.out.println("OK - Buscar por id: " +
                em.getEntityById(player.getId()).getName());
        System.out.println("OK - Buscar por nombre: " +
                em.getEntityByName("Player").getName());
        System.out.println("OK - Buscar por tag 'player': " +
                em.getEntitiesByTag("player").size() + " entidad(es)");
        System.out.println("OK - Total entidades: " +
                em.getAllEntities().size());

        // 8. Añadir UI
        UILabel label = new UILabel();
        label.setText("Score: 0");
        em.addUIElement(label);
        System.out.println("OK - UILabel añadido");
        System.out.println("   Total UI elements: " +
                em.getAllUIElements().size());

        // 9. Buscar UI por nombre
        System.out.println("OK - Buscar UI por nombre: " +
                em.getUIElementByName(label.getName()).getName());

        // 10. Eliminar entidad
        em.removeEntity(enemy);
        System.out.println("OK - Entidad Enemy eliminada");
        System.out.println("   Total entidades: " +
                em.getAllEntities().size());

        // 11. Intentar operar sin escena abierta
        sm.closeScene();
        try {
            em.createEntity("Test");
            System.out.println("FALLO - Deberia haber fallado");
        } catch (IOException e) {
            System.out.println("OK - Correcto, sin escena abierta: " +
                    e.getMessage());
        }

        // Limpiar
        deleteDirectory(new File(path));
        System.out.println("OK - Carpeta temporal limpiada");
    }


    // Helper
    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) deleteDirectory(file);
            }
        }
        dir.delete();
    }

    private static void testTilesetManager() throws Exception {
        System.out.println("\n=== TEST TILESET MANAGER ===");

        ProjectManager pm = new ProjectManager();
        SceneManager sm = new SceneManager(pm);
        TilesetManager tm = new TilesetManager(pm, sm);
        String path = Files.createTempDirectory("motor2d-test").toString();
        pm.createProject("Test", path);
        sm.createScene("level1");

        // 1. Crear tileset
        Tileset tileset = tm.createTileset("mundo",
                "assets/tilesets/mundo.png", 32, 32);
        System.out.println("OK - Tileset creado: " + tileset.getName());

        // 2. No permitir nombre duplicado
        try {
            tm.createTileset("mundo", "assets/tilesets/mundo.png", 32, 32);
            System.out.println("FALLO - Deberia haber fallado");
        } catch (IOException e) {
            System.out.println("OK - Correcto, nombre duplicado: " + e.getMessage());
        }

        // 3. Añadir tiles al tileset
        Tile hierba = tm.addTile(tileset, "Hierba",
                "assets/tilesets/hierba.png", false);
        Tile roca = tm.addTile(tileset, "Roca",
                "assets/tilesets/roca.png", true);
        System.out.println("OK - Tiles añadidos: " +
                tileset.getTiles().size() + " tiles");
        System.out.println("   Hierba id: " + hierba.getId() +
                " solid: " + hierba.isSolid());
        System.out.println("   Roca id: " + roca.getId() +
                " solid: " + roca.isSolid());

        // 4. Buscar tile por id
        Tile found = tm.getTileById(tileset, hierba.getId());
        System.out.println("OK - Tile encontrado por id: " + found.getName());

        // 5. Crear tilemap en la escena
        Tilemap tilemap = tm.createTilemap(20, 10, 32, 32);
        System.out.println("OK - Tilemap creado: " +
                tilemap.getCols() + "x" + tilemap.getRows());

        // 6. Pintar tiles por coordenadas de cuadricula
        tm.paintTile(0, 0, hierba.getId());
        tm.paintTile(1, 0, hierba.getId());
        tm.paintTile(2, 0, roca.getId());
        System.out.println("OK - Tiles pintados por coordenada");
        System.out.println("   (0,0): " + tm.getTileIdAt(0, 0));
        System.out.println("   (1,0): " + tm.getTileIdAt(1, 0));
        System.out.println("   (2,0): " + tm.getTileIdAt(2, 0));

        // 7. Pintar tile por coordenadas de pixel
        tm.paintTileAtPixel(96, 0, roca.getId()); // 96px / 32px = col 3
        System.out.println("OK - Tile pintado por pixel (96,0) -> col 3: " +
                tm.getTileIdAt(3, 0));

        // 8. Borrar tile
        tm.eraseTile(1, 0);
        System.out.println("OK - Tile borrado en (1,0): " +
                tm.getTileIdAt(1, 0));

        // 9. Borrar tile por pixel
        tm.eraseTileAtPixel(0, 0); // col 0, row 0
        System.out.println("OK - Tile borrado por pixel (0,0): " +
                tm.getTileIdAt(0, 0));

        // 10. Limpiar tilemap
        tm.clearTilemap();
        System.out.println("OK - Tilemap limpiado");
        System.out.println("   Tiles restantes: " +
                tm.getCurrentTilemap().getTileGrid().size());

        // 11. Intentar pintar sin tilemap
        tm.removeTilemap();
        try {
            tm.paintTile(0, 0, hierba.getId());
            System.out.println("FALLO - Deberia haber fallado");
        } catch (IOException e) {
            System.out.println("OK - Correcto, sin tilemap: " + e.getMessage());
        }

        // 12. Eliminar tileset
        tm.deleteTileset("mundo");
        System.out.println("OK - Tileset eliminado");
        System.out.println("   Tilesets restantes: " +
                tm.getAllTilesets().size());

        // Limpiar
        deleteDirectory(new File(path));
        System.out.println("OK - Carpeta temporal limpiada");
    }
}