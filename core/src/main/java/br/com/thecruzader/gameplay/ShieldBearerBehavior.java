package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

final class ShieldBearerBehavior extends GroundEnemyBehavior {
    ShieldBearerBehavior() {
        super(4, 54, .65, .16, .95);
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
        return .55;
    }

    @Override
    boolean guarding(Enemy enemy) {
        return enemy.health > 0 && enemy.grounded && enemy.stagger == 0
            && enemy.attack.phase() != Attack.Phase.ACTIVE
            && (enemy.attack.phase() != Attack.Phase.RECOVERY || enemy.attack.phaseProgress() >= .25);
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
