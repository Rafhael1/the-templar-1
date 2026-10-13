package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.GameEvent;

import java.util.List;

public final class AudioPlayer {
    private final GameAssets assets;
    private boolean muted;

    public AudioPlayer(GameAssets assets) {
        this.assets = assets;
        assets.ambience().setLooping(true);
        assets.ambience().setVolume(0.65f);
        assets.ambience().play();
    }

    public void play(List<GameEvent> events) {
        if (muted) return;
        for (GameEvent event : events) {
            String sound =
                    switch (event) {
                        case JUMP -> "jump";
                        case SWING -> "sword";
                        case HIT, BLOCK -> "hit";
                        case HURT, DEATH -> "hurt";
                        case CHECKPOINT, RELIC, ARENA_START, BOSS_DEFEATED, VICTORY -> "chime";
                    };
            assets.sound(sound).play(0.28f);
        }
    }

    public boolean muted() {
        return muted;
    }

    public void toggleMute() {
        muted = !muted;
        setPaused(false);
    }

    public void setPaused(boolean paused) {
        if (muted || paused) assets.ambience().pause();
        else assets.ambience().play();
    }
}
