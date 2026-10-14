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
        BLOCK, DASH_WINDUP, DASH, JUMP_WINDUP, JUMP,
        DEAD
    }

    double previousX, previousY;
    double x, y, vx, vy, flash, alertTime, noticeTime;
    boolean grounded;
    double patrolCenter, targetX, targetY, chaseTime, calmTime, blockedTime, lastChaseX, lastChaseY, jumpVx;
    final boolean boss;
    final LevelData.EnemyKind kind;
    double actionTime, aimX, aimY;
    int nextMove;
    boolean shotFired;
    final Attack attack;
    int health, facing = -1;
    long lastPlayerAttack = -1;
    boolean hitPlayer;
    State state = State.PATROL;

    Enemy(LevelData.Spawn spawn) {
        previousX = x = spawn.x();
        previousY = y = spawn.y();
        patrolCenter = lastChaseX = x;
        lastChaseY = y;
        kind = spawn.kind();
        boss = kind == LevelData.EnemyKind.GUARDIAN;
        health = maxHealth();
        attack = new Attack(archer() ? 0.65 : boss ? 0.55 : 0.45,
                archer() ? 0.08 : 0.16, archer() ? 1.10 : 0.65);
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

    public int health() {
        return health;
    }

    public int maxHealth() {
        return boss ? 12 : archer() ? 2 : 3;
    }

    public boolean boss() {
        return boss;
    }

    public boolean archer() { return kind == LevelData.EnemyKind.ARCHER; }

    public boolean blocking() { return state == State.BLOCK; }

    public double flash() {
        return flash;
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

    public double range() {
        return boss ? 78 : 54;
    }
}
