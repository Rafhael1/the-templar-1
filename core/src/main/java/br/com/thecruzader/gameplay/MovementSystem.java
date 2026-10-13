package br.com.thecruzader.gameplay;

import br.com.thecruzader.input.PlayerInput;

import java.util.List;

final class MovementSystem {
    void prepare(
            Player player,
            PlayerInput input,
            double dt,
            List<GameEvent> events) {
        player.blockFlash = Math.max(0, player.blockFlash - dt);
        player.invulnerable = Math.max(0, player.invulnerable - dt);
        player.dodge = Math.max(0, player.dodge - dt);
        player.dodgeCooldown = Math.max(0, player.dodgeCooldown - dt);
        player.coyote = player.grounded ? 0.08 : Math.max(0, player.coyote - dt);
        player.jumpBuffer = input.jump() ? 0.10 : Math.max(0, player.jumpBuffer - dt);
        if (player.jumpBuffer > 0 && player.coyote > 0) {
            player.vy = 570;
            player.coyote = 0;
            player.jumpBuffer = 0;
            player.grounded = false;
            events.add(GameEvent.JUMP);
        }
    }

    void move(Player player, PlayerInput input, double dt, CollisionWorld collision,
            boolean gateLocked) {
        if (input.dodge() && player.dodgeCooldown == 0 && !player.attack.running() && !player.blocking) {
            player.dodge = 0.20;
            player.dodgeCooldown = Player.DODGE_COOLDOWN;
            player.invulnerable = 0.24;
        }
        player.vx = player.dodging() ? player.facing * 500 : input.direction() * (player.blocking ? 70 : player.attack.running() ? 100 : 220);
        player.vy = Math.max(-700, player.vy - 1450 * dt);
        collision.move(player, dt, gateLocked);
    }
}
