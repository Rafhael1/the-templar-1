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
        assertEquals(3200, map.width());
        assertEquals(64, map.spawn().y());
        assertEquals(5, map.enemies().size());
        assertEquals(12, map.solids().size());
        assertEquals(160, map.solids().get(1).top());
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
}
