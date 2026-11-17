package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

/**
 * A quick first cut followed by a telegraphed, guard-breaking cross cut.
 */
final class ReliefDuelistBehavior extends BossBehavior {
    ReliefDuelistBehavior() {
        super(15, 48, .36, .12, .62);
    }

    @Override
    double engagementRange() {
        return 130;
    }

    @Override
    double approachSpeed() {
        return 285;
    }

    @Override
    void selectSkill(Enemy duelist, double tempo) {
        begin(duelist, Enemy.State.TWIN_WINDUP, .36 * tempo);
    }

    @Override
    boolean updateSkill(Enemy duelist, double tempo, List<Arrow> arrows) {
        switch (duelist.state) {
            case TWIN_WINDUP -> {
                if (duelist.actionTime == 0) begin(duelist, Enemy.State.TWIN_SLASH, .13);
            }
            case TWIN_SLASH -> {
                if (duelist.actionTime > 0) duelist.vx = duelist.facing * 430;
                else begin(duelist, Enemy.State.TWIN_CROSS_WINDUP, .32);
            }
            case TWIN_CROSS_WINDUP -> {
                if (duelist.actionTime == 0) begin(duelist, Enemy.State.TWIN_CROSS, .15);
            }
            case TWIN_CROSS -> {
                if (duelist.actionTime > 0) duelist.vx = duelist.facing * 455;
                else recover(duelist, .62);
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private static void begin(Enemy duelist, Enemy.State state, double duration) {
        duelist.state = state;
        duelist.actionTime = duration;
        duelist.hitPlayer = false;
    }

    @Override
    boolean heavyAttack(Enemy duelist) {
        return duelist.state == Enemy.State.TWIN_CROSS_WINDUP || duelist.state == Enemy.State.TWIN_CROSS;
    }

    @Override
    boolean contactAttackActive(Enemy duelist) {
        return duelist.state == Enemy.State.TWIN_SLASH || duelist.state == Enemy.State.TWIN_CROSS;
    }

    @Override
    Bounds contactAttackBounds(Enemy duelist) {
        return duelist.weaponBounds();
    }
}
