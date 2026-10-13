package br.com.thecruzader.gameplay;

import static org.junit.jupiter.api.Assertions.*;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

class DefenseAndAirAttackTest {
    private GameWorld world() {
        return new GameWorld(new LevelData(3200, 544,
                new LevelData.Point(100, 64), new LevelData.Point(1400, 64),
                List.of(new Bounds(740, 232, 22, 30)), new Bounds(2950, 64, 40, 176),
                new Bounds(3010, 64, 110, 100), new Bounds(64, 64, 2886, 256), List.of(new Bounds(0, 0, 3200, 64)),
                List.of(new LevelData.Spawn(620, 64, LevelData.EnemyKind.SENTINEL), new LevelData.Spawn(2700, 64, LevelData.EnemyKind.GUARDIAN))));
    }

    @Test
    void simultaneousJumpAndDownAttackLocksDirection() {
        GameWorld w = world();
        w.update(GameWorld.STEP, PlayerInput.NONE);
        w.update(GameWorld.STEP, new PlayerInput(0, true, true, false, true, false));
        assertFalse(w.player().grounded());
        assertEquals(Player.AttackDirection.DOWN, w.player().attackDirection());
        w.update(GameWorld.STEP, new PlayerInput(-1, false, true, false, false, false));
        assertEquals(Player.AttackDirection.DOWN, w.player().attackDirection());
        assertEquals(1, w.player().attack().sequence());
    }

    @Test
    void groundedDownInputUsesSideAttack() {
        GameWorld w = world();
        w.update(GameWorld.STEP, PlayerInput.NONE);
        w.update(GameWorld.STEP, new PlayerInput(0, false, true, false, true, false));
        assertEquals(Player.AttackDirection.SIDE, w.player().attackDirection());
    }

    @Test
    void downwardAttackHitsBelowOnceAndStopsDealingDamageOnLanding() {
        Player p = new Player(new LevelData.Point(100, 150));
        p.attackDirection = Player.AttackDirection.DOWN;
        Enemy below = new Enemy(new LevelData.Spawn(104, 64, LevelData.EnemyKind.SENTINEL));
        Enemy beside = new Enemy(new LevelData.Spawn(150, 150, LevelData.EnemyKind.SENTINEL));
        var events = new ArrayList<GameEvent>();
        CombatSystem combat = new CombatSystem();
        p.attack.start();
        combat.resolve(p, List.of(below, beside), events);
        assertEquals(3, below.health());
        p.attack.update(.11);
        combat.resolve(p, List.of(below, beside), events);
        combat.resolve(p, List.of(below, beside), events);
        assertEquals(2, below.health());
        assertEquals(3, beside.health());
        p.grounded = true;
        below.lastPlayerAttack = -1;
        combat.resolve(p, List.of(below), events);
        assertEquals(2, below.health());
    }

    @Test
    void shieldBlocksFrontFromEitherFacingButNotRear() {
        for (int facing : new int[] {-1, 1}) {
            Player p = new Player(new LevelData.Point(100, 64));
            p.blocking = true;
            p.facing = facing;
            Enemy front = strike(100 + facing * 45, -facing);
            var events = new ArrayList<GameEvent>();
            CombatSystem combat = new CombatSystem();
            combat.resolve(p, List.of(front), events);
            combat.resolve(p, List.of(front), events);
            assertEquals(5, p.health());
            assertEquals(List.of(GameEvent.BLOCK), events);
            assertTrue(p.blockFlash() > 0);
            Enemy rear = strike(100 - facing * 45, facing);
            combat.resolve(p, List.of(rear), events);
            assertEquals(4, p.health());
        }
    }

    private Enemy strike(double x, int facing) {
        Enemy enemy = new Enemy(new LevelData.Spawn(x, 64, LevelData.EnemyKind.SENTINEL));
        enemy.facing = facing;
        enemy.attack.start();
        enemy.attack.update(.56);
        return enemy;
    }

    @Test
    void blockingSlowsMovementPreventsAttackAndDodgeAndEndsOnRelease() {
        GameWorld w = world();
        w.update(GameWorld.STEP, new PlayerInput(1, false, true, true, false, true));
        assertTrue(w.player().blocking());
        assertEquals(70, w.player().speed());
        assertFalse(w.player().attack().running());
        assertFalse(w.player().dodging());
        w.update(GameWorld.STEP, new PlayerInput(1, false, true, false, false, false));
        assertFalse(w.player().blocking());
        assertTrue(w.player().attack().running());
        w.update(GameWorld.STEP, new PlayerInput(0, false, false, false, false, true));
        assertFalse(w.player().blocking());
    }

    @Test
    void shieldCanBlockInAirButCannotInterruptDodge() {
        GameWorld w = world();
        w.update(GameWorld.STEP, PlayerInput.NONE);
        w.update(GameWorld.STEP, new PlayerInput(0, true, false, false, false, true));
        assertFalse(w.player().grounded());
        assertTrue(w.player().blocking());
        w.update(GameWorld.STEP, new PlayerInput(0, false, false, true, false, false));
        assertTrue(w.player().dodging());
        w.update(GameWorld.STEP, new PlayerInput(0, false, false, false, false, true));
        assertFalse(w.player().blocking());
    }
}
