package br.com.thecruzader.level;

import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LevelLoaderTest {
    @TempDir Path temporary;

    private FileHandle actualMap() {
        return new FileHandle(System.getProperty("assets.path") + "/levels/exiles-road/maps/exiles-road.tmx");
    }

    @Test
    void loadsCommittedMapAndConvertsTiledCoordinates() {
        LevelData map = LevelLoader.load(actualMap());
        assertEquals(8960, map.width());
        assertEquals(64, map.spawn().y());
        assertEquals(27, map.enemies().size());
        assertTrue(map.solids().size() > 40);
        assertEquals(13, map.bandages().size());
        assertEquals(4, map.enemies().stream()
                .filter(enemy -> enemy.kind() == LevelData.EnemyKind.ARCHER).count());
        assertEquals(List.of(new LevelData.Spawn(4640, 64, LevelData.EnemyKind.SPEARMAN),
                new LevelData.Spawn(5980, 128, LevelData.EnemyKind.SPEARMAN),
                new LevelData.Spawn(6824, 352, LevelData.EnemyKind.SPEARMAN)),
            map.enemies().stream().filter(enemy -> enemy.kind() == LevelData.EnemyKind.SPEARMAN).toList());
        assertEquals(3, map.enemies().stream().filter(enemy -> enemy.kind() == LevelData.EnemyKind.SHIELD_BEARER).count());
        for (var kind : List.of(LevelData.EnemyKind.BRUTE, LevelData.EnemyKind.OIL_THROWER))
            assertEquals(2, map.enemies().stream().filter(enemy -> enemy.kind() == kind).count(), kind.name());
        assertEquals(160, map.solids().get(1).top());
    }

    @Test
    void galleryRoofHasReachableApproach() {
        LevelData map = LevelLoader.load(actualMap());
        double previous = 64;
        for (int x : new int[]{4544, 4640, 4736, 4768}) {
            double top = map.solids().stream()
                .filter(solid -> solid.x() <= x && solid.right() >= x + LevelData.PLAYER_WIDTH)
                .mapToDouble(Bounds::top).max().orElseThrow();
            assertTrue(top > previous && top - previous <= 96, "Roof access step at " + x);
            previous = top;
        }
        assertEquals(384, previous);
    }

    @Test
    void mountainHasContinuousRockAndClimbableSteps() {
        LevelData map = LevelLoader.load(actualMap());
        double previous = 160;
        for (int x = 6144; x < 7328; x += 32) {
            final int column = x;
            Bounds rock = map.solids().stream()
                .filter(solid -> solid.x() <= column && solid.right() >= column + 32)
                .max(java.util.Comparator.comparingDouble(Bounds::top)).orElseThrow();
            assertEquals(64, rock.y(), "Ridge must not hide a trapping pit at " + x);
            assertTrue(Math.abs(rock.top() - previous) <= 96, "Unclimbable step at " + x);
            previous = rock.top();
        }
    }

    @Test
    void encounterSpawnsAndHealingAreClearOfSolidTerrain() {
        LevelData map = LevelLoader.load(actualMap());
        for (Bounds bandage : map.bandages())
            for (Bounds solid : map.solids())
                assertFalse(bandage.overlaps(solid), "Bandage inside terrain at " + bandage.x());
        for (LevelData.Spawn spawn : map.enemies()) {
            Bounds feet = new Bounds(spawn.x(), spawn.y(), 30, 60);
            for (Bounds solid : map.solids())
                assertFalse(feet.overlaps(solid), "Enemy inside terrain at " + spawn.x());
        }
    }

    @Test
    void arenaIsOneScreenWideWithClearFloorAndOnlyTheGuardian() {
        LevelData map = LevelLoader.load(actualMap());
        assertEquals(map.gate().x(), map.arena().right(), .001);
        assertTrue(map.arena().width() <= 960);
        assertTrue(map.enemies().stream().filter(enemy -> enemy.x() >= map.arena().x())
                .allMatch(enemy -> enemy.kind() == LevelData.EnemyKind.GUARDIAN));
        assertTrue(map.solids().stream().filter(solid -> solid.top() > 64)
                .noneMatch(solid -> solid.overlaps(map.arena())));
    }

    @Test
    void missingArenaFailsAtLoadTime() {
        FileHandle file = new FileHandle(temporary.resolve("missing-arena.tmx").toFile());
        file.writeString(actualMap().readString().replace("type=\"arena\"", "type=\"bandage\""), false);
        assertThrows(IllegalArgumentException.class, () -> LevelLoader.load(file));
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
        assertEquals(17, map.enemies().stream().filter(enemy -> enemy.x() >= 3200).count());
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
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"infinite", "offset", "parallax", "group", "duplicate",
            "rotatedPoint", "rectangleSpawn", "outsideSpawn", "outsideGuardian", "wideArena", "shortGate", "earlyExit", "zeroTiles", "tileCollider", "blockedExit", "blockedGate"})
    void rejectsUnsupportedOrUnplayableAuthoredMaps(String change) {
        String original = actualMap().readString();
        String modified = switch (change) {
            case "infinite" -> original.replace("infinite=\"0\"", "infinite=\"1\"");
            case "offset" -> original.replace("name=\"collisions\"", "name=\"collisions\" offsetx=\"128\"");
            case "parallax" -> original.replace("name=\"terrain\"", "name=\"terrain\" parallaxx=\"0.5\"");
            case "group" -> original.replace("</map>", "<group name=\"nested\" /></map>");
            case "duplicate" -> original.replace("</map>", "<objectgroup name=\"collisions\" /></map>");
            case "rotatedPoint" -> original.replace("name=\"templar\"", "name=\"templar\" rotation=\"15\"");
            case "rectangleSpawn" -> original.replace("type=\"spawn\"", "type=\"spawn\" width=\"28\"");
            case "outsideSpawn" -> original.replace("name=\"templar\" x=\"96\"", "name=\"templar\" x=\"-500\"");
            case "outsideGuardian" -> original.replace("name=\"gate-guardian\" x=\"8576\"", "name=\"gate-guardian\" x=\"600\"");
            case "wideArena" -> original.replace("name=\"boss-arena\" x=\"8032\" y=\"224\" width=\"736\"", "name=\"boss-arena\" x=\"7008\" y=\"224\" width=\"1760\"");
            case "shortGate" -> original.replace("width=\"40\" height=\"176\"", "width=\"40\" height=\"32\"");
            case "earlyExit" -> original.replace("name=\"pilgrims-exit\" x=\"8832\"", "name=\"pilgrims-exit\" x=\"7000\"");
            case "tileCollider" -> original.replace("name=\"stair-0\"", "name=\"stair-0\" gid=\"1\"");
            case "blockedExit" -> original.replaceFirst("</objectgroup>", "<object name=\"blocking-exit\" x=\"8832\" y=\"380\" width=\"96\" height=\"100\" /></objectgroup>");
            case "blockedGate" -> original.replaceFirst("</objectgroup>", "<object name=\"blocking-gate\" x=\"8768\" y=\"304\" width=\"40\" height=\"176\" /></objectgroup>");
            case "zeroTiles" -> original.replace("tilewidth=\"32\"", "tilewidth=\"0\"");
            default -> throw new IllegalArgumentException(change);
        };
        assertNotEquals(original, modified, "Probe must actually change the map");
        FileHandle file = new FileHandle(temporary.resolve(change + ".tmx").toFile());
        file.writeString(modified, false);
        assertThrows(IllegalArgumentException.class, () -> LevelLoader.load(file), change);
    }
    @Test
    void roadEncountersAreSpreadAcrossRegionsAndMostlySupportedByPlatforms() {
        LevelData level = LevelLoader.load(actualMap());
        int[] regions = new int[4];
        int elevated = 0;
        for (var spawn : level.enemies()) {
            if (spawn.kind() == LevelData.EnemyKind.GUARDIAN) continue;
            regions[(int) (spawn.x() / 2240)]++;
            if (spawn.y() > 64) elevated++;
            assertTrue(level.solids().stream().anyMatch(solid -> solid.top() == spawn.y()
                    && solid.x() <= spawn.x() && solid.right() >= spawn.x() + spawn.kind().width),
                    "Spawn must be supported by its authored surface");
        }
        assertEquals(19, elevated);
        assertArrayEquals(new int[]{7, 6, 7, 6}, regions);
    }

    @Test
    void alternateRoutesHaveEncountersAndSanctuaryHasBreathingRoom() {
        LevelData level = LevelLoader.load(actualMap());
        assertTrue(level.enemies().stream().anyMatch(e -> e.x() >= 1792 && e.x() < 2560 && e.y() == 320));
        assertTrue(level.enemies().stream().anyMatch(e -> e.x() >= 4768 && e.x() < 5600 && e.y() == 384));
        assertTrue(level.enemies().stream().noneMatch(e -> Math.abs(e.x() - level.checkpoint().x()) < 160));
        var encounters = level.enemies().stream().filter(e -> !e.kind().boss)
            .sorted(java.util.Comparator.comparingDouble(LevelData.Spawn::x)).toList();
        for (int i = 1; i < encounters.size(); i++)
            assertTrue(encounters.get(i).x() - encounters.get(i - 1).x() <= 900,
                "Long empty stretch before " + encounters.get(i).x());
    }
    @Test void partiallyUnsupportedPlatformSpawnFailsBeforeGameplay() {
        FileHandle file = new FileHandle(temporary.resolve("off-platform.tmx").toFile());
        file.writeString(actualMap().readString().replace("name=\"sentinel-0\" x=\"520\"",
            "name=\"sentinel-0\" x=\"590\""), false);
        var error = assertThrows(IllegalArgumentException.class, () -> LevelLoader.load(file));
        assertTrue(error.getMessage().contains("stand fully"));
    }
}
