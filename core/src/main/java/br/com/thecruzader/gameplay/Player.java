package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;

public final class Player {
    public static final double WIDTH = LevelData.PLAYER_WIDTH, HEIGHT = LevelData.PLAYER_HEIGHT, DODGE_COOLDOWN = 1.4;
    private final int maxHealth;
    double previousX, previousY;
    double x, y, vx, vy, invulnerable, dodge, dodgeCooldown, coyote, jumpBuffer;
    int facing = 1, health;
    public enum AttackDirection { SIDE, DOWN }

    boolean grounded, blocking;
    double blockFlash, hurtFlash;
    double parryWindow, parryFlash, attackBuffer;
    boolean shieldHeld, bufferedDown, bounced;
    AttackDirection attackDirection = AttackDirection.SIDE;
    final Attack attack = new Attack(0.10, 0.12, 0.22);

    Player(LevelData.Point point) {
        this(point, GameRules.DEFAULT);
    }

    Player(LevelData.Point point, GameRules rules) {
        maxHealth = rules.playerHearts();
        health = maxHealth;
        previousX = x = point.x();
        previousY = y = point.y();
    }

    public double interpolatedX(double alpha) { return previousX + (x - previousX) * alpha; }
    public double interpolatedY(double alpha) { return previousY + (y - previousY) * alpha; }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double speed() {
        return vx;
    }

    public int facing() {
        return facing;
    }

    public int maxHealth() {
        return maxHealth;
    }

    public int health() {
        return health;
    }

    public boolean grounded() {
        return grounded;
    }

    public boolean dodging() {
        return dodge > 0;
    }

    public double dodgeCooldown() {
        return dodgeCooldown;
    }

    public double invulnerable() {
        return invulnerable;
    }

    public Attack attack() {
        return attack;
    }

    public boolean blocking() {
        return blocking;
    }

    public double blockFlash() {
        return blockFlash;
    }

    public double hurtFlash() {
        return hurtFlash;
    }

    public double parryFlash() {
        return parryFlash;
    }

    public AttackDirection attackDirection() {
        return attackDirection;
    }

    public Bounds weaponBounds() {
        return attackDirection == AttackDirection.DOWN
                ? new Bounds(x - 6, y - 48, WIDTH + 12, 52)
                : new Bounds(facing > 0 ? x + 18 : x - 64, y + 9, 74, 39);
    }

    public Bounds bounds() {
        return new Bounds(x, y, WIDTH, HEIGHT);
    }
}
