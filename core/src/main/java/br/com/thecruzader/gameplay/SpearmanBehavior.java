package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

final class SpearmanBehavior extends GroundEnemyBehavior {
    SpearmanBehavior() {
        super(3, 104, .60, .14, .90);
    }

    @Override
    double sightRange() {
        return 250;
    }

    @Override
    double preferredSpacing() {
        return 0;
    }

    @Override
    boolean canAttack(Enemy enemy, Player player, CollisionWorld collision, boolean gateLocked) {
        Bounds thrust = contactAttackBounds(enemy);
        return thrust.overlaps(player.bounds())
            && collision.clearShot(enemy.x + enemy.width() / 2, enemy.y + 28,
            player.x + Player.WIDTH / 2, enemy.y + 28, gateLocked);
    }

    @Override
    void onActiveAttack(Enemy enemy, List<Arrow> arrows, List<OilPot> oilPots) {
        enemy.shotFired = true;
    }

    @Override
    boolean contactAttackActive(Enemy enemy) {
        return enemy.attack.phase() == Attack.Phase.ACTIVE;
    }

    @Override
    Bounds contactAttackBounds(Enemy enemy) {
        return new Bounds(enemy.facing > 0 ? enemy.bounds().right() : enemy.x - range,
            enemy.y + 22, range, 12);
    }
}
