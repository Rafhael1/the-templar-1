package br.com.thecruzader.gameplay;

/**
 * Session configuration, shared by every level and respawn.
 */
public record GameRules(int playerHearts) {
    public static final GameRules DEFAULT = new GameRules(3);

    public GameRules {
        if (playerHearts <= 0) throw new IllegalArgumentException("Player hearts must be positive");
    }
}
