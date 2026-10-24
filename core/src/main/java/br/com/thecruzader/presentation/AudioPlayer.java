package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.GameEvent;
import com.badlogic.gdx.audio.Music;

import java.util.List;

public final class AudioPlayer {
    private final GameAssets assets;
    private final Music[] themes;
    private Music theme;
    private boolean muted;
    private boolean paused, death, deathFinished;

    public AudioPlayer(GameAssets assets) {
        this.assets = assets;
        themes = new Music[]{assets.menuMusic(), assets.ambience(), assets.bossMusic(), assets.victoryMusic()};
        for (Music music : themes) music.setLooping(true);
        assets.menuMusic().setVolume(.48f);
        assets.ambience().setVolume(.38f);
        assets.bossMusic().setVolume(.46f);
        assets.victoryMusic().setVolume(.48f);
        theme = assets.menuMusic();
        assets.requiem().setLooping(false);
        assets.requiem().setVolume(.80f);
        assets.requiem().setOnCompletionListener(music -> deathFinished = true);
        updatePlayback();
    }

    public void play(List<GameEvent> events) {
        if (events.contains(GameEvent.DEATH)) {
            if (!death) {
                for (String name : GameAssets.ENEMY_SOUNDS) assets.sound(name).stop();
                death = true;
                deathFinished = false;
                assets.sound("angelic").stop();
                assets.sound("hurt").stop();
                assets.requiem().stop();
                updatePlayback();
            }
            return;
        }
        if (events.contains(GameEvent.ARENA_START) || events.contains(GameEvent.BOSS_DEFEATED))
            for (String name : GameAssets.ENEMY_SOUNDS) assets.sound(name).stop();
        if (events.contains(GameEvent.BOSS_DEFEATED)) selectTheme(assets.victoryMusic());
        else if (events.contains(GameEvent.ARENA_START)) selectTheme(assets.bossMusic());
        if (muted) return;
        for (GameEvent event : events) {
            String sound =
                    switch (event) {
                        case MACE_LIFT -> "mace-lift";
                        case MACE_SWING -> "mace-swing";
                        case MACE_IMPACT -> "mace-impact";
                        case SPEAR_DRAW -> "spear-draw";
                        case SPEAR_THRUST -> "spear-thrust";
                        case OIL_PRIME -> "oil-prime";
                        case OIL_THROW -> "oil-throw";
                        case POT_SHATTER -> "pot-shatter";
                        case FIRE_IGNITE -> "fire-ignite";
                        case JUMP -> "jump";
                        case SWING -> "sword";
                        case HIT, BLOCK -> "hit";
                        case PARRY -> "chime";
                        case HURT -> "hurt";
                        case DEATH -> throw new IllegalStateException("Death must use the requiem");
                        case BOSS_DEFEATED -> "angelic";
                        case CHECKPOINT, BANDAGE, ARENA_START, GATES_OPEN, VICTORY -> "chime";
                    };
            float volume = switch (event) {
                case BOSS_DEFEATED -> .55f;
                case PARRY, SPEAR_THRUST, POT_SHATTER -> .4f;
                case MACE_IMPACT -> .5f;
                case HURT -> .60f;
                case MACE_LIFT, MACE_SWING, SPEAR_DRAW, OIL_PRIME, OIL_THROW -> .55f;
                case FIRE_IGNITE -> .35f;
                default -> .28f;
            };
            assets.sound(sound).play(volume, event == GameEvent.PARRY ? 1.35f : 1, 0);
        }
    }

    public boolean muted() {
        return muted;
    }

    public void toggleMute() {
        muted = !muted;
        if (muted) {
            assets.sound("angelic").stop();
            assets.sound("hurt").stop();
            for (String name : GameAssets.ENEMY_SOUNDS) assets.sound(name).stop();
        }
        updatePlayback();
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        for (String name : GameAssets.ENEMY_SOUNDS) {
            if (paused) assets.sound(name).pause();
            else if (!muted) assets.sound(name).resume();
        }
        if (paused) assets.sound("angelic").pause();
        else if (!muted) assets.sound("angelic").resume();
        if (paused) assets.sound("hurt").pause();
        else if (!muted) assets.sound("hurt").resume();
        updatePlayback();
    }

    public void endDeath() {
        reset(assets.ambience());
    }

    public void startMenu() {
        reset(assets.menuMusic());
    }

    private void reset(Music next) {
        for (String name : GameAssets.ENEMY_SOUNDS) assets.sound(name).stop();
        assets.sound("angelic").stop();
        assets.sound("hurt").stop();
        assets.requiem().stop();
        death = false;
        deathFinished = false;
        paused = false;
        selectTheme(next);
    }

    private void selectTheme(Music next) {
        if (theme != next) theme.stop();
        theme = next;
        updatePlayback();
    }

    private void updatePlayback() {
        var active = death ? assets.requiem() : theme;
        for (Music music : themes) {
            if (music == theme && death) music.pause();
            else if (music != theme) music.stop();
        }
        if (!death) assets.requiem().stop();
        if (muted || paused || (death && deathFinished)) active.pause();
        else active.play();
    }
}
