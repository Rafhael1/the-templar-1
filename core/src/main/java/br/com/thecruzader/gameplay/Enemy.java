package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;

public final class Enemy {
    public enum State {
        PATROL,
        CHASE,
        WINDUP,
        ATTACK,
        RECOVER,
        DEAD
    }

    double x, y, vx, vy, flash, alertTime;
    boolean grounded;
    final double home;
    final boolean boss;
    final Attack attack;
    int health, facing = -1;
    long lastPlayerAttack = -1;
    boolean hitPlayer;
    State state = State.PATROL;

    Enemy(LevelData.Spawn spawn) {
        x = spawn.x();
        y = spawn.y();
        home = x;
        boss = spawn.boss();
        health = maxHealth();
        attack = new Attack(boss ? 0.46 : 0.32, 0.16, boss ? 0.48 : 0.40);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public int facing() {
        return facing;
    }

    public int health() {
        return health;
    }

    public int maxHealth() {
        return boss ? 9 : 3;
    }

    public boolean boss() {
        return boss;
    }

    public double flash() {
        return flash;
    }

    public State state() {
        return state;
    }

    public Bounds bounds() {
        return new Bounds(x, y, boss ? 42 : 30, boss ? 68 : 48);
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
