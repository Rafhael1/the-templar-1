package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;

import java.util.*;

/** Session state and ordered simulation. No graphics, keyboard, filesystem or audio access. */
public final class GameWorld {
    public static final double STEP = 1.0 / 120;

    public enum State {
        PLAYING,
        DEAD,
        WON
    }

    private final LevelData level;
    private final GameRules rules;
    private final Optional<SpecialAbility> bossReward;
    private final Runnable onBossDefeated;
    private final Set<String> pendingBriefings;
    private final CollisionWorld collision;
    private final MovementSystem movement = new MovementSystem();
    private final EnemySystem enemySystem;
    private final CombatSystem combat = new CombatSystem();
    private final BodyCollisionSystem bodies = new BodyCollisionSystem();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Enemy> enemyView = Collections.unmodifiableList(enemies);
    private final List<GameEvent> events = new ArrayList<>();
    private final List<CombatEffect> effects = new ArrayList<>();
    private final List<CombatEffect> effectView = Collections.unmodifiableList(effects);
    private Player player;
    private Enemy boss;
    private final Bounds shrine, arenaEntrance;
    private State state = State.PLAYING;
    private boolean checkpoint, paused, arenaEntered, bossDefeated;
    private final boolean[] collectedBandages;
    private final List<Arrow> arrows = new ArrayList<>();
    private final List<Arrow> arrowView = Collections.unmodifiableList(arrows);
    private final List<OilPot> oilPots = new ArrayList<>();
    private final List<OilPot> oilPotView = Collections.unmodifiableList(oilPots);
    private final OilSystem oilSystem = new OilSystem();
    private final ProjectileSystem projectiles = new ProjectileSystem();
    private double time;
    private double bossReleaseElapsed;
    private double hitStop;
    private PlayerInput pendingInput = PlayerInput.NONE;

    public GameWorld(LevelData level) {
        this(level, GameRules.DEFAULT);
    }

    public GameWorld(LevelData level, GameRules rules) {
        this(level, rules, List.of(), () -> {
        });
    }

    GameWorld(LevelData level, GameRules rules, List<SpecialAbility> abilities, Runnable onBossDefeated) {
        this(level, rules, abilities, onBossDefeated, SpecialAbility.GOLDEN_STRIKE);
    }

    GameWorld(LevelData level, GameRules rules, List<SpecialAbility> abilities, Runnable onBossDefeated,
              SpecialAbility bossReward) {
        this(level, rules, abilities, onBossDefeated, Optional.of(bossReward), List.of());
    }

    GameWorld(LevelData level, GameRules rules, List<SpecialAbility> abilities, Runnable onBossDefeated,
              Optional<SpecialAbility> bossReward, List<String> briefings) {
        this.bossReward = Objects.requireNonNull(bossReward);
        this.onBossDefeated = Objects.requireNonNull(onBossDefeated);
        pendingBriefings = new HashSet<>(briefings);
        this.rules = Objects.requireNonNull(rules);
        this.level = Objects.requireNonNull(level);
        collectedBandages = new boolean[level.bandages().size()];
        collision = new CollisionWorld(level);
        collision.setArenaClosed(arenaEntranceClosed());
        enemySystem = new EnemySystem(collision, oilPots);
        arenaEntrance = level.arenaEntrance();
        shrine = new Bounds(level.checkpoint().x() - 20, level.checkpoint().y(), 60, 80);
        player = new Player(level.spawn(), rules);
        player.equip(abilities);
        populate();
    }

    private void populate() {
        enemies.clear();
        level.enemies().forEach(spawn -> enemies.add(new Enemy(spawn)));
        boss = enemies.stream().filter(Enemy::boss).findFirst().orElseThrow();
    }

    public void update(double dt, PlayerInput input) {
        if (!Double.isFinite(dt) || dt <= 0 || dt > 0.025)
            throw new IllegalArgumentException("Simulation step must be in (0, 0.025]");
        Objects.requireNonNull(input);
        if (paused || state != State.PLAYING) return;
        if (hitStop > 0) {
            pendingInput = mergeInput(pendingInput, input);
            hitStop = Math.max(0, hitStop - dt);
            player.previousX = player.x;
            player.previousY = player.y;
            for (Enemy enemy : enemies) {
                enemy.previousX = enemy.x;
                enemy.previousY = enemy.y;
            }
            return;
        }
        input = mergeInput(pendingInput, input);
        pendingInput = PlayerInput.NONE;
        int firstEvent = events.size();
        for (CombatEffect effect : effects) effect.update(dt);
        effects.removeIf(CombatEffect::expired);
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
                effects.clear();
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
        if (player.blocking && !player.shieldHeld) player.parryWindow = .12;
        if (!player.blocking) player.parryWindow = 0;
        player.shieldHeld = input.block();
        if (player.blocking || player.dodging()) player.attackBuffer = 0;
        else if (input.attack() && (!player.attack.running() || player.attack.lateRecovery(.12))) {
            player.attackBuffer = .14;
            player.bufferedDown = input.down();
        }
        if (input.specialSlot() >= 0 && player.startSpecial(input.specialSlot()) && !player.drawingBow())
            events.add(GameEvent.SPECIAL_SWING);
        if (player.attackBuffer > 0 && !player.blocking && !player.dodging() && player.attack.start()) {
            player.attackAbility = null;
            player.attackBuffer = 0;
            player.bounced = false;
            player.attackDirection = player.bufferedDown && !player.grounded
                    ? Player.AttackDirection.DOWN : Player.AttackDirection.SIDE;
            events.add(GameEvent.SWING);
        }
        movement.move(player, input, dt, collision, locked);
        enterArena();
        for (Enemy enemy : enemies) {
            enemy.previousX = enemy.x;
            enemy.previousY = enemy.y;
            Enemy.State previous = enemy.state();
            if (!enemy.boss() || arenaEntered) enemySystem.update(enemy, player, dt, arrows, locked);
            else enemy.vx = 0;
            collision.move(enemy, dt, locked);
            emitEnemyCue(enemy, previous);
        }
        combat.resolve(player, enemies, events);
        if (player.releaseArrow(arrows)) events.add(GameEvent.BOW_SHOT);
        projectiles.update(arrows, player, enemies, collision, gateLocked(), dt, events);
        if (arenaEntered && !bossDefeated && boss.health <= 0) {
            bossDefeated = true;
            onBossDefeated.run();
            if (bossReward.orElse(null) == SpecialAbility.BOW) player.equipUnlockedBow();
            arrows.clear();
            oilPots.clear();
            effects.clear();
            events.add(GameEvent.BOSS_DEFEATED);
        }
        bodies.resolve(player, enemies, collision, gateLocked(), dt);
        oilSystem.update(oilPots, player, collision, gateLocked(), dt, events, effects);
        for (int i = firstEvent; i < events.size(); i++) {
            if (events.get(i) == GameEvent.HIT) hitStop = Math.max(hitStop, .035);
            if (events.get(i) == GameEvent.PARRY) hitStop = Math.max(hitStop, .055);
        }
        if (player.y < -100) player.health = 0;
        if (player.health <= 0) {
            effects.clear();
            state = State.DEAD;
            events.add(GameEvent.DEATH);
            return;
        }
        if (!checkpoint && shrine.overlaps(player.bounds())) {
            checkpoint = true;
            player.health = player.maxHealth();
            events.add(GameEvent.CHECKPOINT);
        }
        for (int i = 0; i < collectedBandages.length; i++) {
            if (!collectedBandages[i] && player.health < player.maxHealth()
                    && level.bandages().get(i).overlaps(player.bounds())) {
                collectedBandages[i] = true;
                player.health = Math.min(player.maxHealth(), player.health + 2);
                events.add(GameEvent.BANDAGE);
            }
        }
        if (!gateLocked() && level.exit().overlaps(player.bounds())) {
            state = State.WON;
            events.add(GameEvent.VICTORY);
        }
    }

    private void emitEnemyCue(Enemy enemy, Enemy.State previous) {
        if (previous == enemy.state() || enemy.health() <= 0 || enemy.stagger() > 0
            || Math.abs(enemy.x() - player.x()) > 600
            || level.insideArena(enemy.x()) != insideArena()) return;
        GameEvent cue = switch (enemy.kind()) {
            case BRUTE -> enemy.state() == Enemy.State.WINDUP ? GameEvent.MACE_LIFT
                : enemy.state() == Enemy.State.ATTACK ? GameEvent.MACE_SWING
                : previous == Enemy.State.ATTACK && enemy.state() == Enemy.State.RECOVER && enemy.grounded
                ? GameEvent.MACE_IMPACT : null;
            case SPEARMAN -> enemy.state() == Enemy.State.WINDUP ? GameEvent.SPEAR_DRAW
                : enemy.state() == Enemy.State.ATTACK ? GameEvent.SPEAR_THRUST : null;
            case OIL_THROWER -> enemy.state() == Enemy.State.WINDUP ? GameEvent.OIL_PRIME
                : enemy.state() == Enemy.State.ATTACK ? GameEvent.OIL_THROW : null;
            case HARBOR_CAPTAIN -> enemy.state() == Enemy.State.SHOT_WINDUP ? GameEvent.CAPTAIN_DRAW
                : enemy.state() == Enemy.State.SHOT_RELEASE ? GameEvent.CAPTAIN_SHOT : null;
            case RELIEF_DUELIST -> enemy.state() == Enemy.State.TWIN_SLASH ? GameEvent.DUELIST_SLASH
                : enemy.state() == Enemy.State.TWIN_CROSS ? GameEvent.DUELIST_CROSS : null;
            default -> null;
        };
        if (cue == null) return;
        events.add(cue);
        if (cue == GameEvent.MACE_IMPACT)
            effects.add(new CombatEffect(CombatEffect.Kind.MACE_IMPACT,
                enemy.x() + enemy.width() / 2 + enemy.facing() * 84, enemy.y()));
    }

    public List<CombatEffect> effects() {
        return effectView;
    }

    private void enterArena() {
        if (arenaEntered || briefingsPending() || boss.health <= 0 || player.x < level.arena().x()
                || player.x >= level.arena().right()) return;
        arenaEntered = true;
        collision.setArenaClosed(true);
        arrows.clear();
        oilPots.clear();
        effects.clear();
        Bounds entrance = arenaEntrance;
        for (Enemy enemy : enemies)
            if (!enemy.boss() && enemy.bounds().overlaps(entrance)) {
                enemy.x = entrance.x() - enemy.width();
                enemy.previousX = enemy.x;
            }
        events.add(GameEvent.ARENA_START);
    }

    private static PlayerInput mergeInput(PlayerInput pending, PlayerInput current) {
        return new PlayerInput(current.direction(), pending.jump() || current.jump(),
            pending.attack() || current.attack(), pending.dodge() || current.dodge(),
            current.attack() ? current.down() : pending.attack() ? pending.down() : current.down(),
            current.block(), current.specialSlot() >= 0 ? current.specialSlot() : pending.specialSlot());
    }

    public boolean hitStopped() {
        return hitStop > 0;
    }

    public Bounds arenaEntrance() { return arenaEntrance; }
    public boolean insideArena() { return level.insideArena(player.x()); }
    public boolean arenaEntered() { return arenaEntered; }
    public boolean arenaLocked() { return arenaEntered && gateLocked(); }

    public boolean briefingsPending() {
        return !pendingBriefings.isEmpty();
    }

    public boolean arenaEntranceClosed() {
        return briefingsPending() || arenaLocked();
    }

    public void confirmBriefing(String textKey) {
        if (pendingBriefings.remove(textKey)) collision.setArenaClosed(arenaEntranceClosed());
    }

    public Enemy boss() {
        return boss;
    }
    public boolean bossDefeated() { return bossDefeated; }
    public double bossReleaseElapsed() { return bossReleaseElapsed; }
    public boolean bossReleaseActive() { return bossDefeated && bossReleaseElapsed < BossRelease.DURATION; }

    public void respawn() {
        if (state != State.DEAD) throw new IllegalStateException("Respawn requires a dead player");
        Player previous = player;
        player = new Player(checkpoint ? level.checkpoint() : level.spawn(), rules);
        player.restoreLoadout(previous);
        populate();
        arenaEntered = false;
        bossDefeated = false;
        bossReleaseElapsed = 0;
        hitStop = 0;
        pendingInput = PlayerInput.NONE;
        collision.setArenaClosed(arenaEntranceClosed());
        arrows.clear();
        oilPots.clear();
        effects.clear();
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

    public List<OilPot> oilPots() {
        return oilPotView;
    }

    public List<Arrow> arrows() { return arrowView; }

    public boolean paused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
