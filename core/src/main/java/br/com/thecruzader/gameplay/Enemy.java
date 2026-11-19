package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;

public final class Enemy {
    public enum State {
        PATROL,
        NOTICE,
        CHASE,
        WINDUP,
        ATTACK,
        RECOVER,
        BLOCK, DASH_WINDUP, DASH, JUMP_WINDUP, JUMP, SHOT_WINDUP, SHOT_RELEASE,
        TWIN_WINDUP, TWIN_SLASH, TWIN_CROSS_WINDUP, TWIN_CROSS,
        RIDER_RIDE, RIDER_DRAW, RIDER_VOLLEY, RIDER_CHARGE_WINDUP, RIDER_CHARGE,
        DEAD
    }

    double previousX, previousY;
    double x, y, vx, vy, flash, alertTime, noticeTime;
    boolean grounded;
    double patrolCenter, targetX, targetY, chaseTime, calmTime, blockedTime, lastChaseX, lastChaseY, jumpVx;
    final EnemyBehavior behavior;
    final LevelData.EnemyKind kind;
    double actionTime, aimX, aimY;
    double stagger, knockback;
    int impactDirection;
    boolean comboSword, riposteReady;
    int nextMove;
    boolean shotFired;
    final Attack attack;
    double health;
    int facing = -1;
    long lastPlayerAttack = -1;
    boolean hitPlayer;
    State state = State.PATROL;

    Enemy(LevelData.Spawn spawn) {
        this(spawn, behaviorFor(spawn.kind()));
    }

    Enemy(LevelData.Spawn spawn, EnemyBehavior behavior) {
        previousX = x = spawn.x();
        previousY = y = spawn.y();
        patrolCenter = lastChaseX = x;
        lastChaseY = y;
        kind = spawn.kind();
        this.behavior = java.util.Objects.requireNonNull(behavior);
        health = maxHealth();
        attack = behavior.createAttack();
    }

    private static EnemyBehavior behaviorFor(LevelData.EnemyKind kind) {
        return switch (kind) {
            case SENTINEL -> new SentinelBehavior();
            case SPEARMAN -> new SpearmanBehavior();
            case ARCHER -> new ArcherBehavior();
            case SHIELD_BEARER -> new ShieldBearerBehavior();
            case BRUTE -> new BruteBehavior();
            case OIL_THROWER -> new OilThrowerBehavior();
            case GUARDIAN -> new GuardianBehavior();
            case HARBOR_CAPTAIN -> new HarborCaptainBehavior();
            case RELIEF_DUELIST -> new ReliefDuelistBehavior();
            case MOUNTED_COMMANDER -> new MountedCommanderBehavior();
        };
    }

    public double interpolatedX(double alpha) { return previousX + (x - previousX) * alpha; }
    public double interpolatedY(double alpha) { return previousY + (y - previousY) * alpha; }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double vy() { return vy; }

    public int facing() {
        return facing;
    }

    public double health() {
        return health;
    }

    public int maxHealth() {
        return behavior.maxHealth;
    }

    public boolean boss() {
        return kind.boss;
    }

    public boolean archer() { return kind == LevelData.EnemyKind.ARCHER; }

    public boolean spearman() {
        return kind == LevelData.EnemyKind.SPEARMAN;
    }

    public LevelData.EnemyKind kind() {
        return kind;
    }

    public boolean blocking() {
        return state == State.BLOCK || behavior.guarding(this);
    }

    public boolean heavyAttack() {
        return behavior.heavyAttack(this);
    }

    public double flash() {
        return flash;
    }

    public double stagger() {
        return stagger;
    }

    public int impactDirection() {
        return impactDirection;
    }

    public boolean vulnerable() {
        return boss() && riposteReady && stagger > 0;
    }

    public State state() {
        return state;
    }

    public double width() { return kind.width; }
    public double height() { return kind.height; }

    public Bounds bounds() {
        return new Bounds(x, y, width(), height());
    }

    Bounds weaponBounds() {
        return new Bounds(
                facing > 0 ? bounds().right() : x - range(),
                y + 5,
                range(),
                bounds().height() - 5);
    }

    void updateAttackState() {
        state = switch (attack.phase()) {
            case WINDUP -> State.WINDUP;
            case ACTIVE -> State.ATTACK;
            case RECOVERY -> State.RECOVER;
            case READY -> throw new IllegalStateException("Running attack cannot be ready");
        };
    }

    public double attackProgress() {
        return attack.phaseProgress();
    }

    public double actionSecondsRemaining() {
        return actionTime;
    }

    public double aimX() {
        return aimX;
    }

    public double aimY() {
        return aimY;
    }

    public double range() {
        return behavior.range;
    }
}
