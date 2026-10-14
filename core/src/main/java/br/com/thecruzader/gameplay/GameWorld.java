package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Session state and ordered simulation. No graphics, keyboard, filesystem or audio access. */
public final class GameWorld {
    public static final double STEP = 1.0 / 120;

    public enum State {
        PLAYING,
        DEAD,
        WON
    }

    private final LevelData level;
    private final CollisionWorld collision;
    private final MovementSystem movement = new MovementSystem();
    private final EnemySystem enemySystem;
    private final CombatSystem combat = new CombatSystem();
    private final BodyCollisionSystem bodies = new BodyCollisionSystem();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Enemy> enemyView = java.util.Collections.unmodifiableList(enemies);
    private final List<GameEvent> events = new ArrayList<>();
    private Player player;
    private Enemy guardian;
    private final Bounds shrine, arenaEntrance;
    private State state = State.PLAYING;
    private boolean checkpoint, paused, arenaEntered, bossDefeated;
    private final boolean[] collectedBandages;
    private final List<Arrow> arrows = new ArrayList<>();
    private final List<Arrow> arrowView = java.util.Collections.unmodifiableList(arrows);
    private final ProjectileSystem projectiles = new ProjectileSystem();
    private double time;
    private double bossReleaseElapsed;

    public GameWorld(LevelData level) {
        this.level = Objects.requireNonNull(level);
        collectedBandages = new boolean[level.bandages().size()];
        collision = new CollisionWorld(level);
        enemySystem = new EnemySystem(collision);
        arenaEntrance = level.arenaEntrance();
        shrine = new Bounds(level.checkpoint().x() - 20, level.checkpoint().y(), 60, 80);
        player = new Player(level.spawn());
        populate();
    }

    private void populate() {
        enemies.clear();
        level.enemies().forEach(spawn -> enemies.add(new Enemy(spawn)));
        guardian = enemies.stream().filter(Enemy::boss).findFirst().orElseThrow();
    }

    public void update(double dt, PlayerInput input) {
        if (!Double.isFinite(dt) || dt <= 0 || dt > 0.025)
            throw new IllegalArgumentException("Simulation step must be in (0, 0.025]");
        Objects.requireNonNull(input);
        if (paused || state != State.PLAYING) return;
        time += dt;
        player.previousX = player.x;
        player.previousY = player.y;
        if (bossReleaseActive()) {
            double previous = bossReleaseElapsed;
            bossReleaseElapsed = Math.min(BossRelease.DURATION, bossReleaseElapsed + dt);
            if (previous < BossRelease.OPEN_END && bossReleaseElapsed >= BossRelease.OPEN_END) {
                collision.setArenaClosed(false);
                events.add(GameEvent.GATES_OPEN);
            }
            player.vx = 0;
            player.dodge = 0;
            player.blocking = false;
            player.attack.update(dt);
            movement.prepare(player, PlayerInput.NONE, dt, events);
            movement.move(player, PlayerInput.NONE, dt, collision, gateLocked());
            if (player.health <= 0) {
                state = State.DEAD;
                events.add(GameEvent.DEATH);
            }
            return;
        }
        boolean locked = gateLocked();
        player.attack.update(dt);
        movement.prepare(player, input, dt, events);
        if (input.direction() != 0 && !player.attack.running() && !player.dodging())
            player.facing = input.direction();
        player.blocking = input.block() && !player.dodging() && !player.attack.running();
        if (input.attack() && !player.blocking && !player.dodging() && player.attack.start()) {
            player.attackDirection = input.down() && !player.grounded
                    ? Player.AttackDirection.DOWN : Player.AttackDirection.SIDE;
            events.add(GameEvent.SWING);
        }
        movement.move(player, input, dt, collision, locked);
        enterArena();
        for (Enemy enemy : enemies) {
            enemy.previousX = enemy.x;
            enemy.previousY = enemy.y;
            if (!enemy.boss || arenaEntered) enemySystem.update(enemy, player, dt, arrows, locked);
            else enemy.vx = 0;
            collision.move(enemy, dt, locked);
        }
        combat.resolve(player, enemies, events);
        if (arenaEntered && !bossDefeated && guardian.health <= 0) {
            bossDefeated = true;
            arrows.clear();
            events.add(GameEvent.BOSS_DEFEATED);
        }
        bodies.resolve(player, enemies, collision, gateLocked());
        projectiles.update(arrows, player, collision, gateLocked(), dt, events);
        if (player.y < -100) player.health = 0;
        if (player.health <= 0) {
            state = State.DEAD;
            events.add(GameEvent.DEATH);
            return;
        }
        if (!checkpoint && shrine.overlaps(player.bounds())) {
            checkpoint = true;
            player.health = Player.MAX_HEALTH;
            events.add(GameEvent.CHECKPOINT);
        }
        for (int i = 0; i < collectedBandages.length; i++) {
            if (!collectedBandages[i] && player.health < Player.MAX_HEALTH
                    && level.bandages().get(i).overlaps(player.bounds())) {
                collectedBandages[i] = true;
                player.health = Math.min(Player.MAX_HEALTH, player.health + 2);
                events.add(GameEvent.BANDAGE);
            }
        }
        if (!gateLocked() && level.exit().overlaps(player.bounds())) {
            state = State.WON;
            events.add(GameEvent.VICTORY);
        }
    }

    private void enterArena() {
        if (arenaEntered || guardian.health <= 0 || player.x < level.arena().x()
                || player.x >= level.arena().right()) return;
        arenaEntered = true;
        collision.setArenaClosed(true);
        arrows.clear();
        Bounds entrance = arenaEntrance;
        for (Enemy enemy : enemies)
            if (!enemy.boss && enemy.bounds().overlaps(entrance)) {
                enemy.x = entrance.x() - enemy.width();
                enemy.previousX = enemy.x;
            }
        events.add(GameEvent.ARENA_START);
    }

    public Bounds arenaEntrance() { return arenaEntrance; }
    public boolean insideArena() { return level.insideArena(player.x()); }
    public boolean arenaEntered() { return arenaEntered; }
    public boolean arenaLocked() { return arenaEntered && gateLocked(); }
    public Enemy guardian() { return guardian; }
    public boolean bossDefeated() { return bossDefeated; }
    public double bossReleaseElapsed() { return bossReleaseElapsed; }
    public boolean bossReleaseActive() { return bossDefeated && bossReleaseElapsed < BossRelease.DURATION; }

    public void respawn() {
        if (state != State.DEAD) throw new IllegalStateException("Respawn requires a dead player");
        player = new Player(checkpoint ? level.checkpoint() : level.spawn());
        populate();
        arenaEntered = false;
        bossDefeated = false;
        bossReleaseElapsed = 0;
        collision.setArenaClosed(false);
        arrows.clear();
        events.clear();
        paused = false;
        state = State.PLAYING;
    }

    public List<GameEvent> drainEvents() {
        List<GameEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public boolean gateLocked() {
        return !bossDefeated || bossReleaseElapsed < BossRelease.OPEN_END;
    }

    public Player player() {
        return player;
    }

    public List<Enemy> enemies() {
        return enemyView;
    }

    public LevelData level() {
        return level;
    }

    public State state() {
        return state;
    }

    public double time() {
        return time;
    }

    public boolean checkpoint() {
        return checkpoint;
    }

    public boolean bandageCollected(int index) { return collectedBandages[index]; }

    public int collectedBandageCount() {
        int count = 0;
        for (boolean collected : collectedBandages) if (collected) count++;
        return count;
    }

    public List<Arrow> arrows() { return arrowView; }

    public boolean paused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
