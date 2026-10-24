package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

final class BruteBehavior extends GroundEnemyBehavior {
    BruteBehavior() {
        super(5, 76, .95, .18, 1.25);
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
    double movementScale() {
        return .65;
    }

    @Override
    boolean heavyAttack(Enemy enemy) {
        return enemy.attack.running();
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
