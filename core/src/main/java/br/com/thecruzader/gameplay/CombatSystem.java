package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

final class CombatSystem {
    void resolve(Player player, List<Enemy> enemies, List<GameEvent> events) {
        Bounds sword = player.weaponBounds();
        for (Enemy enemy : enemies) {
            if (enemy.health <= 0) continue;
            if (player.attack.phase() == Attack.Phase.ACTIVE
                    && (player.attackDirection != Player.AttackDirection.DOWN || !player.grounded)
                    && enemy.lastPlayerAttack != player.attack.sequence()
                    && sword.overlaps(enemy.bounds())) {
                enemy.lastPlayerAttack = player.attack.sequence();
                boolean front = (player.x + Player.WIDTH / 2
                        - (enemy.x + enemy.bounds().width() / 2)) * enemy.facing > 0;
                if (enemy.blocking() && front && player.attackDirection != Player.AttackDirection.DOWN) {
                    enemy.flash = 0.12;
                    events.add(GameEvent.BLOCK);
                    continue;
                }
                enemy.health--;
                if (!enemy.boss && enemy.health > 0) {
                    enemy.calmTime = 0;
                    EnemyPerception.alert(enemy, player);
                }
                enemy.flash = 0.14;
                if (enemy.health == 0) enemy.state = Enemy.State.DEAD;
                events.add(GameEvent.HIT);
            }
            boolean bodyAttack = enemy.state == Enemy.State.DASH || enemy.state == Enemy.State.JUMP;
            if (enemy.health <= 0 || enemy.archer() || enemy.hitPlayer
                    || (!bodyAttack && enemy.attack.phase() != Attack.Phase.ACTIVE))
                continue;
            if ((bodyAttack ? enemy.bounds() : enemy.weaponBounds()).overlaps(player.bounds())) {
                enemy.hitPlayer = true;
                hitPlayer(player, enemy.x + enemy.bounds().width() / 2, events);
            }
        }
    }
    static void hitPlayer(Player player, double sourceX, List<GameEvent> events) {
        if (player.invulnerable > 0) return;
        if (player.blocking && (sourceX - (player.x + Player.WIDTH / 2)) * player.facing > 0) {
            player.blockFlash = 0.16;
            events.add(GameEvent.BLOCK);
        } else {
            player.health--;
            player.invulnerable = 0.85;
            events.add(GameEvent.HURT);
        }
    }
}
