package br.com.thecruzader.screen;

import br.com.thecruzader.CrusaderGame;
import br.com.thecruzader.gameplay.GameEvent;
import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.input.InputMapper;
import br.com.thecruzader.presentation.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;

import java.util.List;

public final class PlayScreen extends ScreenAdapter {
    private final CrusaderGame game;
    private final GameWorld world;
    private final InputMapper input = new InputMapper();
    private final WorldRenderer renderer;
    private final Hud hud;
    private final AudioPlayer audio;
    private double accumulator;
    private final LevelResources level;
    private final SceneryTransition scenery;
    private final boolean[] visitedRegions;

    public PlayScreen(CrusaderGame game, GameAssets assets, AudioPlayer audio) {
        this.game = game;
        this.audio = audio;
        level = new LevelResources(game.journey().currentLevel(), assets);
        world = game.journey().enterLevel(level.data());
        scenery = new SceneryTransition(level.definition());
        visitedRegions = new boolean[level.definition().regions().size()];
        renderer = new WorldRenderer(assets, level);
        hud = new Hud(assets, level.definition(), game.journey().hasNextLevel(), world.player());
    }

    public GameWorld world() {
        return world;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input);
        audio.endDeath();
    }

    @Override
    public void render(float delta) {
        if (input.takeMute()) {
            audio.toggleMute();
            audio.setPaused(world.paused());
        }
        if (input.takePause() && world.state() != GameWorld.State.WON) {
            world.setPaused(!world.paused());
            input.clear();
            accumulator = 0;
            audio.setPaused(world.paused());
        }
        if (input.takeRestart()) {
            if (world.state() == GameWorld.State.DEAD && renderer.deathElapsed() >= TemplarDeathAnimation.DURATION) {
                world.respawn();
                audio.endDeath();
                input.clear();
                accumulator = 0;
            } else if (world.state() == GameWorld.State.WON) {
                game.continueAfterVictory();
                return;
            }
        }
        if (!world.paused() && world.state() == GameWorld.State.PLAYING && renderer.transitionOpacity() == 0) {
            accumulator += Math.min(delta, .10f);
            while (accumulator >= GameWorld.STEP) {
                boolean inside = world.insideArena();
                world.update(GameWorld.STEP, input.consume());
                accumulator -= GameWorld.STEP;
                if (inside != world.insideArena()) {
                    accumulator = 0;
                    input.clear();
                    break;
                }
            }
        } else {
            accumulator = 0;
            input.consume();
        }
        List<GameEvent> events = world.drainEvents();
        audio.play(events);
        for (GameEvent event : events) {
            if (event == GameEvent.CHECKPOINT)
                hud.announce("notice.checkpoint");
            if (event == GameEvent.ARENA_START) hud.announce("notice.arena");
            if (event == GameEvent.BOSS_DEFEATED) hud.announce("notice.bossDefeated");
            if (event == GameEvent.GATES_OPEN) hud.announce("notice.gatesOpen");
            if (event == GameEvent.BANDAGE)
                hud.announce("notice.bandage");
        }
        int nextRegion = scenery.regionAt(world.player().x());
        if (!visitedRegions[nextRegion]) {
            visitedRegions[nextRegion] = true;
            hud.announce(level.definition().regions().get(nextRegion).announcementKey());
        }
        renderer.render(world, delta, world.paused() || world.state() != GameWorld.State.PLAYING
                ? 1 : (float) (accumulator / GameWorld.STEP));
        hud.render(world, delta, audio.muted(), renderer.deathElapsed());
    }

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
        hud.resize(width, height);
    }

    @Override
    public void pause() {
        world.setPaused(true);
        input.clear();
        accumulator = 0;
        audio.setPaused(true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        input.clear();
    }

    @Override
    public void dispose() {
        renderer.dispose();
        hud.dispose();
        level.dispose();
    }
}
