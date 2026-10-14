package br.com.thecruzader;

import br.com.thecruzader.presentation.AudioPlayer;
import br.com.thecruzader.presentation.GameAssets;
import br.com.thecruzader.presentation.DisplaySettings;
import br.com.thecruzader.screen.MenuScreen;
import br.com.thecruzader.screen.PlayScreen;

import br.com.thecruzader.i18n.Language;
import br.com.thecruzader.level.Campaign;
import br.com.thecruzader.level.CampaignLoader;
import br.com.thecruzader.gameplay.Journey;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public final class CrusaderGame extends Game {
    public static final String TITLE = "O Templário";
    private GameAssets assets;
    private AudioPlayer audio;
    private final String campaignPath;
    private Campaign campaign;
    private Journey journey;

    public CrusaderGame(String campaignPath) { this.campaignPath = java.util.Objects.requireNonNull(campaignPath); }

    public Campaign campaign() { return campaign; }
    public Journey journey() { return journey; }

    @Override
    public void create() {
        DisplaySettings.apply(true);
        campaign = CampaignLoader.load(Gdx.files.internal(campaignPath));
        journey = new Journey(campaign);
        assets = new GameAssets();
        audio = new AudioPlayer(assets);
        showMenu();
    }

    public Language language() { return assets.texts.language(); }
    public void setLanguage(Language language) {
        assets.texts.setLanguage(language);
        Gdx.app.getPreferences("o-templario").putString("language", language.name()).flush();
    }

    public void showMenu() {
        audio.endDeath();
        replaceScreen(new MenuScreen(this, assets, audio));
    }

    public void startJourney() {
        journey = new Journey(campaign);
        replaceScreen(new br.com.thecruzader.screen.IntroScreen(this, assets));
    }

    public void startGame() {
        replaceScreen(new PlayScreen(this, assets, audio));
    }

    public void continueAfterVictory() {
        journey.continueAfterVictory();
        startGame();
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
