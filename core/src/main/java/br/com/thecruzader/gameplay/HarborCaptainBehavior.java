package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

/**
 * The captain alternates a heavy arrow, a committed charge and a saber strike.
 */
final class HarborCaptainBehavior extends BossBehavior {
    HarborCaptainBehavior() {
        super(14, 72, .38, .14, .72);
    }

    @Override
    boolean updateSkill(Enemy captain, double tempo, List<Arrow> arrows) {
        if (captain.state == Enemy.State.SHOT_WINDUP) {
            if (captain.actionTime == 0) {
                double origin = captain.x + captain.width() / 2 + captain.facing * 22;
                arrows.add(new Arrow(origin, captain.aimY, origin + captain.facing, captain.aimY, Arrow.Kind.CAPTAIN));
                captain.state = Enemy.State.SHOT_RELEASE;
                captain.actionTime = .16;
            }
            return true;
        }
        if (captain.state == Enemy.State.SHOT_RELEASE) {
            if (captain.actionTime == 0) recover(captain, .85);
            return true;
        }
        if (captain.state == Enemy.State.DASH_WINDUP) {
            if (captain.actionTime == 0) {
                captain.state = Enemy.State.DASH;
                captain.actionTime = .27;
                captain.hitPlayer = false;
            }
            return true;
        }
        if (captain.state != Enemy.State.DASH) return false;
        if (captain.actionTime > 0) captain.vx = captain.facing * 600;
        else recover(captain, .8);
        return true;
    }

    @Override
    void selectSkill(Enemy captain, double tempo) {
        switch (Math.floorMod(captain.nextMove++, 3)) {
            case 0 -> {
                captain.state = Enemy.State.SHOT_WINDUP;
                captain.actionTime = .78 * tempo;
                captain.aimY = captain.y + 36;
            }
            case 1 -> {
                captain.state = Enemy.State.DASH_WINDUP;
                captain.actionTime = .52 * tempo;
            }
            case 2 -> {
                captain.attack.start();
                captain.state = Enemy.State.WINDUP;
                captain.hitPlayer = false;
            }
            default -> throw new IllegalStateException("Unknown captain attack");
        }
    }

    @Override
    boolean heavyAttack(Enemy captain) {
        return captain.state == Enemy.State.DASH_WINDUP || captain.state == Enemy.State.DASH
            || captain.state == Enemy.State.SHOT_WINDUP || captain.state == Enemy.State.SHOT_RELEASE;
    }

    @Override
    boolean contactAttackActive(Enemy captain) {
        return captain.state == Enemy.State.DASH || captain.attack.phase() == Attack.Phase.ACTIVE;
    }

    @Override
    Bounds contactAttackBounds(Enemy captain) {
        return captain.state == Enemy.State.DASH ? captain.bounds() : captain.weaponBounds();
    }
}
