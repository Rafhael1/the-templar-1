package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

final class OilThrowerBehavior extends GroundEnemyBehavior {
    OilThrowerBehavior() {
        super(2, .85, .08, 1.80);
    }

    @Override
    double sightRange() {
        return 280;
    }

    @Override
    double preferredSpacing() {
        return 150;
    }

    @Override
    boolean canAttack(Enemy enemy, Player player, CollisionWorld collision, boolean gateLocked) {
        return Math.abs(enemy.targetX - enemy.x) <= 340
            && OilPot.clearArc(enemy.x + enemy.width() / 2, enemy.y + 40,
            player.x + Player.WIDTH / 2, player.y, collision, gateLocked);
    }

    @Override
    void onActiveAttack(Enemy enemy, List<Arrow> arrows, List<OilPot> oilPots) {
        enemy.shotFired = true;
        oilPots.add(new OilPot(enemy.x + enemy.width() / 2, enemy.y + 40, enemy.aimX, enemy.aimY - Player.HEIGHT / 2));
    }

    @Override
    boolean contactAttackActive(Enemy enemy) {
        return false;
    }

    @Override
    Bounds contactAttackBounds(Enemy enemy) {
        throw new IllegalStateException("Oil throwers attack with pots");
    }
}
