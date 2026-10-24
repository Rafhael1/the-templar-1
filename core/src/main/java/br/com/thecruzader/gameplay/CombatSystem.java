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
                resolveSwordStrike(player, enemy, events);
            }
            if (enemy.health <= 0 || enemy.hitPlayer || !enemy.behavior.contactAttackActive(enemy))
                continue;
            if (enemy.behavior.contactAttackBounds(enemy).overlaps(player.bounds())) {
                enemy.hitPlayer = true;
                boolean heavy = enemy.heavyAttack();
                if (hitPlayer(player, enemy.x + enemy.bounds().width() / 2, heavy, events)) {
                    stagger(enemy, enemy.boss() ? .40 : .45, enemy.boss() ? 0 : player.facing * 110);
                    enemy.riposteReady = enemy.boss();
                }
            }
        }
    }

    private static void resolveSwordStrike(Player player, Enemy enemy, List<GameEvent> events) {
        enemy.lastPlayerAttack = player.attack.sequence();
        boolean front = (player.x + Player.WIDTH / 2
            - (enemy.x + enemy.bounds().width() / 2)) * enemy.facing > 0;
        if (enemy.blocking() && front && player.attackDirection != Player.AttackDirection.DOWN) {
            enemy.flash = 0.12;
            events.add(GameEvent.BLOCK);
            return;
        }
        enemy.health = Math.max(0, enemy.health - (enemy.vulnerable() ? 2 : 1));
        enemy.riposteReady = false;
        enemy.impactDirection = player.attackDirection == Player.AttackDirection.DOWN ? 0 : player.facing;
        if (!enemy.boss() && enemy.health > 0) {
            enemy.calmTime = 0;
            EnemyPerception.alert(enemy, player);
            // Ordinary damage cannot reset a committed windup, strike or recovery.
            if (!enemy.attack.running() && enemy.stagger == 0)
                stagger(enemy, .10, player.facing * 110);
        }
        if (player.attackDirection == Player.AttackDirection.DOWN && !player.bounced) {
            player.vy = 420;
            player.grounded = false;
            player.coyote = player.jumpBuffer = 0;
            player.bounced = true;
        }
        enemy.flash = 0.14;
        if (enemy.health == 0) enemy.state = Enemy.State.DEAD;
        events.add(GameEvent.HIT);
    }

    private static void stagger(Enemy enemy, double duration, double knockback) {
        enemy.attack.cancel();
        enemy.stagger = duration;
        enemy.knockback = knockback;
        enemy.vx = 0;
        enemy.jumpVx = 0;
        enemy.actionTime = 0;
        enemy.comboSword = false;
        enemy.state = Enemy.State.RECOVER;
    }

    static boolean hitPlayer(Player player, double sourceX, boolean heavy, List<GameEvent> events) {
        if (player.invulnerable > 0) return false;
        if (player.blocking && (sourceX - (player.x + Player.WIDTH / 2)) * player.facing > 0) {
            player.blockFlash = 0.16;
            if (player.parryWindow > 0) {
                player.parryWindow = 0;
                player.parryFlash = .25;
                events.add(GameEvent.PARRY);
                return true;
            }
            if (!heavy) {
                events.add(GameEvent.BLOCK);
                return false;
            }
        }
        hurtPlayer(player, events);
        return false;
    }

    static void hurtPlayer(Player player, List<GameEvent> events) {
        if (player.invulnerable > 0) return;
        player.health--;
        player.invulnerable = 0.85;
        player.hurtFlash = .28;
        events.add(GameEvent.HURT);
    }
}
