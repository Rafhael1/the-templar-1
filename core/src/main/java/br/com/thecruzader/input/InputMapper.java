package br.com.thecruzader.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.IntSet;

/** Keeps fast presses until a simulation step consumes them, even at high render rates. */
public final class InputMapper extends InputAdapter {
    private final IntSet held = new IntSet();
    private boolean jump, attack, dodge, pause, restart, mute;

    @Override
    public boolean keyDown(int key) {
        if (!held.add(key)) return true;
        switch (key) {
            case Input.Keys.SPACE, Input.Keys.W, Input.Keys.UP -> jump = true;
            case Input.Keys.J, Input.Keys.Z -> attack = true;
            case Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT, Input.Keys.K -> dodge = true;
            case Input.Keys.ESCAPE -> pause = true;
            case Input.Keys.R, Input.Keys.ENTER -> restart = true;
            case Input.Keys.M -> mute = true;
            default -> {}
        }
        return true;
    }

    @Override
    public boolean keyUp(int key) {
        held.remove(key);
        return true;
    }

    public PlayerInput consume() {
        int direction =
                (held.contains(Input.Keys.D) || held.contains(Input.Keys.RIGHT) ? 1 : 0)
                        - (held.contains(Input.Keys.A) || held.contains(Input.Keys.LEFT) ? 1 : 0);
        PlayerInput result = new PlayerInput(direction, jump, attack, dodge,
                held.contains(Input.Keys.S) || held.contains(Input.Keys.DOWN),
                held.contains(Input.Keys.L) || held.contains(Input.Keys.X));
        jump = false;
        attack = false;
        dodge = false;
        return result;
    }

    public boolean takePause() {
        boolean result = pause;
        pause = false;
        return result;
    }

    public boolean takeRestart() {
        boolean result = restart;
        restart = false;
        return result;
    }

    public boolean takeMute() {
        boolean result = mute;
        mute = false;
        return result;
    }

    public void clear() {
        held.clear();
        jump = false;
        attack = false;
        dodge = false;
        pause = false;
        restart = false;
        mute = false;
    }
}
