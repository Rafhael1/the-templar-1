package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

final class ArcherBehavior extends GroundEnemyBehavior {
    ArcherBehavior() {
        super(2, .65, .08, 1.10);
    }

    @Override
    double sightRange() {
        return 280;
    }

    @Override
    double preferredSpacing() {
        return 160;
    }

    @Override
    void trackAttackTarget(Enemy enemy) {
        enemy.aimX = enemy.targetX + enemy.width() / 2;
        enemy.aimY = enemy.targetY + Player.HEIGHT / 2;
        double dx = enemy.aimX - (enemy.x + enemy.width() / 2);
        if (dx != 0) enemy.facing = dx > 0 ? 1 : -1;
    }

    @Override
    boolean canAttack(Enemy enemy, Player player, CollisionWorld collision, boolean gateLocked) {
        return Math.abs(enemy.targetX - enemy.x) < 400
            && collision.clearShot(enemy.x + enemy.width() / 2, enemy.y + 32,
            player.x + Player.WIDTH / 2, player.y + Player.HEIGHT / 2, gateLocked);
    }

    @Override
    void onActiveAttack(Enemy enemy, List<Arrow> arrows, List<OilPot> oilPots) {
        enemy.shotFired = true;
        arrows.add(new Arrow(enemy.x + enemy.width() / 2, enemy.y + 32, enemy.aimX, enemy.aimY));
    }

    @Override
    boolean contactAttackActive(Enemy enemy) {
        return false;
    }

    @Override
    Bounds contactAttackBounds(Enemy enemy) {
        throw new IllegalStateException("Archers attack with projectiles");
    }
}
