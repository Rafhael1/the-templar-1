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
    private final EnemySystem enemySystem = new EnemySystem();
    private final CombatSystem combat = new CombatSystem();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Enemy> enemyView = java.util.Collections.unmodifiableList(enemies);
    private final List<GameEvent> events = new ArrayList<>();
    private Player player;
    private State state = State.PLAYING;
    private boolean checkpoint, relic, paused;
    private double time;

    public GameWorld(LevelData level) {
        this.level = Objects.requireNonNull(level);
        collision = new CollisionWorld(level);
        player = new Player(level.spawn());
        populate();
    }

    private void populate() {
        enemies.clear();
        level.enemies().forEach(spawn -> enemies.add(new Enemy(spawn)));
    }

    public void update(double dt, PlayerInput input) {
        if (!Double.isFinite(dt) || dt <= 0 || dt > 0.025)
            throw new IllegalArgumentException("Simulation step must be in (0, 0.025]");
        Objects.requireNonNull(input);
        if (paused || state != State.PLAYING) return;
        time += dt;
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
        movement.move(player, input, dt, collision, gateLocked());
        for (Enemy enemy : enemies) {
            enemySystem.update(enemy, player, dt);
            collision.move(enemy, dt, gateLocked());
        }
        combat.resolve(player, enemies, events);
        if (player.y < -100) player.health = 0;
        if (player.health <= 0) {
            state = State.DEAD;
            events.add(GameEvent.DEATH);
            return;
        }
        Bounds shrine = new Bounds(level.checkpoint().x() - 20, level.checkpoint().y(), 60, 80);
        if (!checkpoint && shrine.overlaps(player.bounds())) {
            checkpoint = true;
            player.health = Player.MAX_HEALTH;
            events.add(GameEvent.CHECKPOINT);
        }
        if (!relic && level.relic().overlaps(player.bounds())) {
            relic = true;
            player.health = Math.min(Player.MAX_HEALTH, player.health + 2);
            events.add(GameEvent.RELIC);
        }
        if (!gateLocked() && level.exit().overlaps(player.bounds())) {
            state = State.WON;
            events.add(GameEvent.VICTORY);
        }
    }

    public void respawn() {
        if (state != State.DEAD) throw new IllegalStateException("Respawn requires a dead player");
        player = new Player(checkpoint ? level.checkpoint() : level.spawn());
        populate();
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
        return enemies.stream().anyMatch(enemy -> enemy.boss && enemy.health > 0);
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

    public boolean relic() {
        return relic;
    }

    public boolean paused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
