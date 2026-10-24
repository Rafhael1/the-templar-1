package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

/**
 * Composes enemy-specific decisions with the shared simulation and collision systems.
 */
abstract class EnemyBehavior {
    final int maxHealth;
    final double range;
    private final double windup, active, recovery;

    EnemyBehavior(int maxHealth, double range, double windup, double active, double recovery) {
        this.maxHealth = maxHealth;
        this.range = range;
        this.windup = windup;
        this.active = active;
        this.recovery = recovery;
    }

    final Attack createAttack() {
        return new Attack(windup, active, recovery);
    }

    boolean guarding(Enemy enemy) {
        return false;
    }

    boolean heavyAttack(Enemy enemy) {
        return false;
    }

    double movementScale() {
        return 1;
    }

    abstract double sightRange();

    abstract boolean contactAttackActive(Enemy enemy);

    abstract Bounds contactAttackBounds(Enemy enemy);

    abstract void update(Enemy enemy, Player player, double dt, List<Arrow> arrows, List<OilPot> oilPots,
                         boolean gateLocked, CollisionWorld collision, EnemyNavigation navigation, EnemyPerception perception);
}
