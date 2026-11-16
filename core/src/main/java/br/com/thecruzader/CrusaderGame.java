package br.com.thecruzader;

import br.com.thecruzader.gameplay.Journey;
import br.com.thecruzader.i18n.Language;
import br.com.thecruzader.level.Campaign;
import br.com.thecruzader.level.CampaignLoader;
import br.com.thecruzader.persistence.JourneySave;
import br.com.thecruzader.presentation.AudioPlayer;
import br.com.thecruzader.presentation.DisplaySettings;
import br.com.thecruzader.presentation.GameAssets;
import br.com.thecruzader.presentation.WindowMode;
import br.com.thecruzader.screen.DevelopmentScreen;
import br.com.thecruzader.screen.MenuScreen;
import br.com.thecruzader.screen.PlayScreen;
import com.badlogic.gdx.*;

import java.nio.file.Path;

public final class CrusaderGame extends Game {
    public static final String TITLE = "O Templário";
    private GameAssets assets;
    private AudioPlayer audio;
    private final String campaignPath;
    private final WindowMode windowMode;
    private final boolean developmentMode;
    private final Path savePath;
    private JourneySave save;
    private boolean savedJourney;
    private boolean developmentMenuRequested;
    private final InputAdapter developmentControls = new InputAdapter() {
        @Override
        public boolean keyDown(int key) {
            if (key != Input.Keys.F2) return false;
            if (!(getScreen() instanceof DevelopmentScreen)) developmentMenuRequested = true;
            return true;
        }
    };
    private Campaign campaign;
    private Journey journey;

    public CrusaderGame(String campaignPath, WindowMode windowMode) {
        this(campaignPath, windowMode, false);
    }

    public CrusaderGame(String campaignPath, WindowMode windowMode, boolean developmentMode) {
        this(campaignPath, windowMode, developmentMode,
            Path.of(System.getProperty("user.home"), ".o-templario", "progress.properties"));
    }

    public CrusaderGame(String campaignPath, WindowMode windowMode, boolean developmentMode, Path savePath) {
        this.campaignPath = java.util.Objects.requireNonNull(campaignPath);
        this.windowMode = java.util.Objects.requireNonNull(windowMode);
        this.developmentMode = developmentMode;
        this.savePath = java.util.Objects.requireNonNull(savePath);
    }

    public boolean fullscreen() {
        return windowMode.fullscreen();
    }

    public void toggleFullscreen() {
        windowMode.toggleFullscreen();
    }

    public Campaign campaign() { return campaign; }
    public Journey journey() { return journey; }

    @Override
    public void create() {
        DisplaySettings.apply(true);
        campaign = CampaignLoader.load(Gdx.files.internal(campaignPath));
        journey = new Journey(campaign);
        assets = new GameAssets();
        save = new JourneySave(savePath, campaignPath, campaign);
        savedJourney = !developmentMode && save.load().isPresent();
        audio = new AudioPlayer(assets);
        if (developmentMode) showDevelopmentMenu();
        else showMenu();
    }

    @Override
    public void render() {
        if (developmentMenuRequested) {
            developmentMenuRequested = false;
            showDevelopmentMenu();
        }
        super.render();
    }

    private void showDevelopmentMenu() {
        audio.startMenu();
        replaceScreen(new DevelopmentScreen(this, assets));
    }

    public void startDevelopmentLevel(int index) {
        if (!developmentMode) throw new IllegalStateException("Development mode is disabled");
        journey = Journey.startingAt(campaign, index);
        startGame();
    }

    public Language language() { return assets.texts.language(); }
    public void setLanguage(Language language) {
        assets.texts.setLanguage(language);
        Gdx.app.getPreferences("o-templario").putString("language", language.name()).flush();
    }

    public void showMenu() {
        replaceScreen(new MenuScreen(this, assets, audio));
    }

    public void startJourney() {
        clearSave();
        journey = new Journey(campaign);
        replaceScreen(new br.com.thecruzader.screen.IntroScreen(this, assets, audio));
    }

    public void startGame() {
        if (journey.selectionRequired()) {
            audio.startMenu();
            replaceScreen(new br.com.thecruzader.screen.SpecialSelectionScreen(this, assets));
        } else replaceScreen(new PlayScreen(this, assets, audio));
    }

    public boolean hasSavedJourney() {
        return savedJourney;
    }

    public void resumeJourney() {
        if (developmentMode) throw new IllegalStateException("Development mode cannot resume saved progress");
        journey = save.load().orElseThrow(() -> new IllegalStateException("No journey to resume"));
        startGame();
    }

    public void saveVictory() {
        if (developmentMode) return;
        save.saveVictory(journey);
        savedJourney = journey.hasNextLevel();
    }

    private void clearSave() {
        if (developmentMode) return;
        save.clear();
        savedJourney = false;
    }

    public void continueAfterVictory() {
        boolean restart = !journey.hasNextLevel();
        journey.continueAfterVictory();
        if (restart) clearSave();
        startGame();
    }

    private void replaceScreen(Screen next) {
        Screen previous = getScreen();
        setScreen(next);
        if (developmentMode)
            Gdx.input.setInputProcessor(new InputMultiplexer(developmentControls, Gdx.input.getInputProcessor()));
        if (previous != null) previous.dispose();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        assets.dispose();
    }
}
