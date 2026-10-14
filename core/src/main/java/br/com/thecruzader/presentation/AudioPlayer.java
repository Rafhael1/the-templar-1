package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.GameEvent;

import java.util.List;

public final class AudioPlayer {
    private final GameAssets assets;
    private boolean muted;
    private boolean paused, death, deathFinished;

    public AudioPlayer(GameAssets assets) {
        this.assets = assets;
        assets.ambience().setLooping(true);
        assets.ambience().setVolume(0.65f);
        assets.requiem().setLooping(false);
        assets.requiem().setVolume(.80f);
        assets.requiem().setOnCompletionListener(music -> deathFinished = true);
        assets.ambience().play();
    }

    public void play(List<GameEvent> events) {
        if (events.contains(GameEvent.DEATH)) {
            if (!death) {
                death = true;
                deathFinished = false;
                assets.requiem().stop();
                updatePlayback();
            }
            return;
        }
        if (muted) return;
        for (GameEvent event : events) {
            String sound =
                    switch (event) {
                        case JUMP -> "jump";
                        case SWING -> "sword";
                        case HIT, BLOCK -> "hit";
                        case HURT -> "hurt";
                        case DEATH -> throw new IllegalStateException("Death must use the requiem");
                        case CHECKPOINT, BANDAGE, ARENA_START, BOSS_DEFEATED, GATES_OPEN, VICTORY -> "chime";
                    };
            assets.sound(sound).play(0.28f);
        }
    }

    public boolean muted() {
        return muted;
    }

    public void toggleMute() {
        muted = !muted;
        updatePlayback();
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        updatePlayback();
    }

    public void endDeath() {
        assets.requiem().stop();
        death = false;
        deathFinished = false;
        paused = false;
        updatePlayback();
    }

    private void updatePlayback() {
        var active = death ? assets.requiem() : assets.ambience();
        var inactive = death ? assets.ambience() : assets.requiem();
        inactive.pause();
        if (muted || paused || (death && deathFinished)) active.pause();
        else active.play();
    }
}
