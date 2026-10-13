package br.com.thecruzader.gameplay;

/** Telegraphs commit to a direction; every offensive move ends in a punishable recovery. */
final class BossSystem {
    void update(Enemy boss, Player player, double dt) {
        boss.actionTime = Math.max(0, boss.actionTime - dt);
        double tempo = boss.health <= boss.maxHealth() / 2 ? 0.8 : 1;
        switch (boss.state) {
            case BLOCK -> {
                if (boss.actionTime == 0) recover(boss, 0.25 * tempo);
                return;
            }
            case DASH_WINDUP -> {
                if (boss.actionTime == 0) {
                    boss.state = Enemy.State.DASH;
                    boss.actionTime = 0.28;
                    boss.hitPlayer = false;
                }
                return;
            }
            case DASH -> {
                boss.vx = boss.facing * (boss.health <= 6 ? 580 : 480);
                if (boss.actionTime == 0) { boss.vx = 0; recover(boss, 0.65 * tempo); }
                return;
            }
            case JUMP_WINDUP -> {
                if (boss.actionTime == 0) {
                    boss.state = Enemy.State.JUMP;
                    boss.vy = 570;
                    boss.grounded = false;
                    boss.hitPlayer = false;
                }
                return;
            }
            case JUMP -> {
                boss.vx = boss.facing * 200;
                if (boss.grounded) { boss.vx = 0; recover(boss, 0.75 * tempo); }
                return;
            }
            case RECOVER -> {
                if (!boss.attack.running() && boss.actionTime > 0) return;
            }
            default -> {}
        }
        if (boss.attack.running()) {
            boss.state = switch (boss.attack.phase()) {
                case WINDUP -> Enemy.State.WINDUP;
                case ACTIVE -> Enemy.State.ATTACK;
                case RECOVERY -> Enemy.State.RECOVER;
                case READY -> throw new IllegalStateException("Running attack cannot be ready");
            };
            return;
        }
        if (boss.alertTime == 0) { boss.state = Enemy.State.PATROL; return; }
        double distance = player.x + Player.WIDTH / 2 - (boss.x + boss.bounds().width() / 2);
        boss.facing = distance < 0 ? -1 : 1;
        if (Math.abs(distance) > 300) {
            boss.state = Enemy.State.CHASE;
            boss.vx = boss.facing * 145;
            return;
        }
        if (!boss.grounded) return;
        switch (boss.nextMove++ % 4) {
            case 0 -> { boss.state = Enemy.State.BLOCK; boss.actionTime = 0.75 * tempo; }
            case 1 -> { boss.state = Enemy.State.DASH_WINDUP; boss.actionTime = 0.45 * tempo; }
            case 2 -> { boss.state = Enemy.State.JUMP_WINDUP; boss.actionTime = 0.50 * tempo; }
            case 3 -> {
                boss.attack.start();
                boss.hitPlayer = false;
                boss.state = Enemy.State.WINDUP;
            }
            default -> throw new IllegalStateException("Unknown boss move");
        }
    }

    private void recover(Enemy boss, double duration) {
        boss.state = Enemy.State.RECOVER;
        boss.actionTime = duration;
    }
}
