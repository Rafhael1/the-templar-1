package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

final class SentinelBehavior extends GroundEnemyBehavior {
    SentinelBehavior() {
        super(3, .45, .16, .65);
    }

    @Override
    double sightRange() {
        return 230;
    }

    @Override
    double preferredSpacing() {
        return 0;
    }

    @Override
    boolean canAttack(Enemy enemy, Player player, CollisionWorld collision, boolean gateLocked) {
        return enemy.weaponBounds().overlaps(player.bounds());
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
        return enemy.weaponBounds();
    }
}
