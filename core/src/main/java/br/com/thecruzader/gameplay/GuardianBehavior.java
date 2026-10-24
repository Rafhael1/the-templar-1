package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

/**
 * First level boss: shield, committed dash, leap, then sword attack.
 */
final class GuardianBehavior extends BossBehavior {
    GuardianBehavior() {
        super(16, 78, .55, .16, .45);
    }

    @Override
    boolean updateSkill(Enemy boss, double tempo) {
        if (boss.comboSword && boss.attack.phase() == Attack.Phase.RECOVERY) {
            boss.comboSword = false;
        }
        switch (boss.state) {
            case BLOCK -> {
                if (boss.actionTime == 0) recover(boss, 0.25 * tempo);
                return true;
            }
            case DASH_WINDUP -> {
                if (boss.actionTime == 0) {
                    boss.state = Enemy.State.DASH;
                    boss.actionTime = 0.28;
                    boss.hitPlayer = false;
                }
                return true;
            }
            case DASH -> {
                boss.vx = boss.facing * (boss.health <= boss.maxHealth() / 2 ? 580 : 480);
                if (boss.actionTime == 0) {
                    boss.vx = 0;
                    if (boss.comboSword) {
                        boss.attack.start();
                        boss.hitPlayer = false;
                        boss.state = Enemy.State.WINDUP;
                    } else {
                        recover(boss, .45);
                    }
                }
                return true;
            }
            case JUMP_WINDUP -> {
                if (boss.actionTime == 0) {
                    boss.state = Enemy.State.JUMP;
                    boss.vy = 570;
                    boss.grounded = false;
                    boss.hitPlayer = false;
                }
                return true;
            }
            case JUMP -> {
                boss.vx = boss.facing * 200;
                if (boss.grounded) {
                    boss.vx = 0;
                    recover(boss, .50);
                }
                return true;
            }
            default -> {
            }
        }
        return false;
    }

    @Override
    void selectSkill(Enemy boss, double tempo) {
        switch (Math.floorMod(boss.nextMove++, 4)) {
            case 0 -> {
                boss.state = Enemy.State.BLOCK;
                boss.actionTime = 0.75 * tempo;
            }
            case 1 -> {
                boss.state = Enemy.State.DASH_WINDUP;
                boss.actionTime = 0.45 * tempo;
                boss.comboSword = boss.health <= boss.maxHealth() / 2;
            }
            case 2 -> {
                boss.state = Enemy.State.JUMP_WINDUP;
                boss.actionTime = 0.50 * tempo;
            }
            case 3 -> {
                boss.attack.start();
                boss.hitPlayer = false;
                boss.state = Enemy.State.WINDUP;
            }
            default -> throw new IllegalStateException("Unknown boss move");
        }
    }

    @Override
    boolean heavyAttack(Enemy enemy) {
        return bodyAttack(enemy) || enemy.state == Enemy.State.DASH_WINDUP || enemy.state == Enemy.State.JUMP_WINDUP;
    }

    @Override
    boolean contactAttackActive(Enemy enemy) {
        return bodyAttack(enemy) || enemy.attack.phase() == Attack.Phase.ACTIVE;
    }

    @Override
    Bounds contactAttackBounds(Enemy enemy) {
        return bodyAttack(enemy) ? enemy.bounds() : enemy.weaponBounds();
    }

    private boolean bodyAttack(Enemy enemy) {
        return enemy.state == Enemy.State.DASH || enemy.state == Enemy.State.JUMP;
    }
}
