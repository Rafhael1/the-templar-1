package br.com.thecruzader;

import br.com.thecruzader.presentation.AudioPlayer;
import br.com.thecruzader.presentation.GameAssets;
import br.com.thecruzader.screen.MenuScreen;
import br.com.thecruzader.screen.PlayScreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public final class CrusaderGame extends Game {
    private GameAssets assets;
    private AudioPlayer audio;

    @Override
    public void create() {
        assets = new GameAssets();
        audio = new AudioPlayer(assets);
        setScreen(new MenuScreen(this, assets));
    }

    public void startGame() {
        replaceScreen(new PlayScreen(this, assets, audio));
    }

    private void replaceScreen(Screen next) {
        Screen previous = getScreen();
        setScreen(next);
        if (previous != null) previous.dispose();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        assets.dispose();
    }
}
