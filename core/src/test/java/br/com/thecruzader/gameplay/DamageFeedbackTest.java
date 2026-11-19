package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.LevelLoader;
import br.com.thecruzader.level.LevelData;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DamageFeedbackTest {
    @Test
    void anActualArrowHitReportsItsSourceAndInvulnerabilityDoesNotOverwriteIt() {
        var p = new Player(new LevelData.Point(112, 64));
        var map = LevelLoader.load(new FileHandle(System.getProperty("assets.path")
            + "/levels/dorylaeum/maps/dorylaeum.tmx"));
        var projectiles = new ProjectileSystem();
        var collision = new CollisionWorld(map);
        var events = new ArrayList<GameEvent>();
        var arrows = new ArrayList<>(List.of(new Arrow(120, 90, 112, 90, Arrow.Kind.COMMANDER)));
        projectiles.update(arrows, p, List.of(), collision, true, GameWorld.STEP, events);
        assertEquals(2, p.health());
        assertEquals(Player.DamageCause.COMMANDER_ARROW, p.lastDamageCause());
        assertEquals(3, p.damageNotice());
        assertTrue(events.contains(GameEvent.HURT));
        arrows.add(new Arrow(120, 90, 112, 90));
        projectiles.update(arrows, p, List.of(), collision, true, GameWorld.STEP, events);
        assertEquals(Player.DamageCause.COMMANDER_ARROW, p.lastDamageCause());
        new MovementSystem().prepare(p, PlayerInput.NONE, .5, events);
        assertEquals(2.5, p.damageNotice());
    }

    @Test
    void aBlockedArrowDoesNotShowAFalseDamageReason() {
        var p = new Player(new LevelData.Point(112, 64));
        p.blocking = true;
        p.facing = -1;
        var map = LevelLoader.load(new FileHandle(System.getProperty("assets.path")
            + "/levels/dorylaeum/maps/dorylaeum.tmx"));
        var arrows = new ArrayList<>(List.of(new Arrow(110, 90, 130, 90, Arrow.Kind.COMMANDER)));
        var events = new ArrayList<GameEvent>();
        new ProjectileSystem().update(arrows, p, List.of(), new CollisionWorld(map), true, GameWorld.STEP, events);
        assertEquals(3, p.health());
        assertNull(p.lastDamageCause());
        assertEquals(0, p.damageNotice());
        assertTrue(events.contains(GameEvent.BLOCK));
    }
}
