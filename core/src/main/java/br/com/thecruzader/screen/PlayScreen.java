package br.com.thecruzader.screen;

import br.com.thecruzader.CrusaderGame;
import br.com.thecruzader.gameplay.GameEvent;
import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.input.InputMapper;
import br.com.thecruzader.level.LevelLoader;
import br.com.thecruzader.presentation.AudioPlayer;
import br.com.thecruzader.presentation.GameAssets;
import br.com.thecruzader.presentation.Hud;
import br.com.thecruzader.presentation.WorldRenderer;

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

    public PlayScreen(CrusaderGame game, GameAssets assets, AudioPlayer audio) {
        this.game = game;
        this.audio = audio;
        world = new GameWorld(LevelLoader.load(Gdx.files.internal(GameAssets.MAP)));
        renderer = new WorldRenderer(assets);
        hud = new Hud(assets);
        hud.announce("A ESTRADA DOS EXILADOS  -  Encontre o santuario e liberte a passagem.");
    }

    public GameWorld world() {
        return world;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input);
        audio.setPaused(false);
    }

    @Override
    public void render(float delta) {
        if (input.takeMute()) {
            audio.toggleMute();
            audio.setPaused(world.paused());
        }
        if (input.takePause() && world.state() == GameWorld.State.PLAYING) {
            world.setPaused(!world.paused());
            input.clear();
            accumulator = 0;
            audio.setPaused(world.paused());
        }
        if (input.takeRestart()) {
            if (world.state() == GameWorld.State.DEAD) {
                world.respawn();
                input.clear();
                accumulator = 0;
            } else if (world.state() == GameWorld.State.WON) {
                game.startGame();
                return;
            }
        }
        if (!world.paused() && world.state() == GameWorld.State.PLAYING) {
            accumulator += Math.min(delta, .10f);
            while (accumulator >= GameWorld.STEP) {
                world.update(GameWorld.STEP, input.consume());
                accumulator -= GameWorld.STEP;
            }
        } else {
            accumulator = 0;
            input.consume();
        }
        List<GameEvent> events = world.drainEvents();
        audio.play(events);
        for (GameEvent event : events) {
            if (event == GameEvent.CHECKPOINT)
                hud.announce("SANTUARIO ACESO  -  Vida restaurada. Ponto de retorno ativado.");
            if (event == GameEvent.RELIC)
                hud.announce("FRAGMENTO DO VOTO  -  Reliquia encontrada.");
        }
        renderer.render(world);
        hud.render(world, delta, audio.muted());
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
    }
}
