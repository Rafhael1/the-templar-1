package br.com.thecruzader.input;

/** Frame-independent intent. Press actions are consumed once per simulation step. */
public record PlayerInput(int direction, boolean jump, boolean attack, boolean dodge, boolean down, boolean block) {
    public static final PlayerInput NONE = new PlayerInput(0, false, false, false, false, false);

    public PlayerInput {
        if (direction < -1 || direction > 1)
            throw new IllegalArgumentException("Direction must be -1, 0 or 1");
    }
}
