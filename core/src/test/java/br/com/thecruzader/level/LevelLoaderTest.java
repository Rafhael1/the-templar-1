package br.com.thecruzader.level;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.files.FileHandle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class LevelLoaderTest {
    @TempDir Path temporary;

    private FileHandle actualMap() {
        return new FileHandle(System.getProperty("assets.path") + "/maps/exiles-road.tmx");
    }

    @Test
    void loadsCommittedMapAndConvertsTiledCoordinates() {
        LevelData map = LevelLoader.load(actualMap());
        assertEquals(8960, map.width());
        assertEquals(64, map.spawn().y());
        assertEquals(17, map.enemies().size());
        assertEquals(34, map.solids().size());
        assertEquals(10, map.breads().size());
        assertEquals(5, map.enemies().stream()
                .filter(enemy -> enemy.kind() == LevelData.EnemyKind.ARCHER).count());
        assertEquals(160, map.solids().get(1).top());
    }

    @Test
    void encounterSpawnsAndHealingAreClearOfSolidTerrain() {
        LevelData map = LevelLoader.load(actualMap());
        for (Bounds bread : map.breads())
            for (Bounds solid : map.solids())
                assertFalse(bread.overlaps(solid), "Bread inside terrain at " + bread.x());
        for (LevelData.Spawn spawn : map.enemies()) {
            Bounds feet = new Bounds(spawn.x(), spawn.y(), 30, 60);
            for (Bounds solid : map.solids())
                assertFalse(feet.overlaps(solid), "Enemy inside terrain at " + spawn.x());
        }
    }

    @Test
    void unknownEntityFailsAtLoadTime() {
        FileHandle file = new FileHandle(temporary.resolve("bad.tmx").toFile());
        file.writeString(
                actualMap().readString().replace("type=\"sentinel\"", "type=\"typo\""), false);
        assertThrows(IllegalArgumentException.class, () -> LevelLoader.load(file));
    }

    @Test
    void missingSpawnFailsAtLoadTime() {
        FileHandle file = new FileHandle(temporary.resolve("bad.tmx").toFile());
        file.writeString(
                actualMap().readString().replace("type=\"spawn\"", "type=\"sentinel\""), false);
        assertThrows(IllegalArgumentException.class, () -> LevelLoader.load(file));
    }
    @Test
    void polygonColliderFailsWithClearContractError() {
        FileHandle file = new FileHandle(temporary.resolve("polygon.tmx").toFile());
        String map = actualMap().readString();
        int name = map.indexOf("name=\"stair-0\"");
        int start = map.lastIndexOf("<object ", name);
        int end = map.indexOf("/>", start) + 2;
        file.writeString(map.substring(0, start)
                + "<object name=\"polygon-stair\" x=\"2208\" y=\"448\">"
                + "<polygon points=\"0,0 32,0 32,32\"/></object>" + map.substring(end), false);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> LevelLoader.load(file));
        assertTrue(error.getMessage().contains("unrotated rectangles"));
    }
    @Test
    void expandedHalfContainsTerrainEncountersAndFinalObjectives() {
        LevelData map = LevelLoader.load(actualMap());
        assertTrue(map.solids().stream().filter(solid -> solid.x() >= 3200).count() >= 10);
        assertEquals(12, map.enemies().stream().filter(enemy -> enemy.x() >= 3200).count());
        assertTrue(map.checkpoint().x() >= 4200 && map.checkpoint().x() <= 4700);
        assertTrue(map.gate().x() >= 6000);
        assertTrue(map.exit().x() > map.gate().right());
        assertTrue(map.exit().right() <= map.width());
        for (Bounds solid : map.solids()) {
            assertTrue(solid.x() >= 0 && solid.right() <= map.width());
            assertEquals(0, solid.x() % 32, .001);
            assertEquals(0, solid.width() % 32, .001);
            assertEquals(0, solid.y() % 32, .001);
            assertEquals(0, solid.height() % 32, .001);
        }
    }
}
