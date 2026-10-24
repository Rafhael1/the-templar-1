package br.com.thecruzader.screen;

import br.com.thecruzader.CrusaderGame;
import br.com.thecruzader.presentation.AudioPlayer;
import br.com.thecruzader.presentation.GameAssets;
import br.com.thecruzader.presentation.IntroAnimation;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Three animated pixel-art, player-paced scenes. Dialogue is original historical fiction. */
public final class IntroScreen extends ScreenAdapter {
    private static final String[] SCENES = {"council", "call", "march"};
    private final CrusaderGame game;
    private final GameAssets assets;
    private final AudioPlayer audio;
    private final Stage stage = new Stage(new FitViewport(960, 540));
    private final Table root = new Table();
    private final IntroAnimation illustration;
    private int scene;
    private boolean finished, paused;
    private float elapsed;
    private final InputAdapter input = new InputAdapter() {
        @Override public boolean keyDown(int key) {
            if (key == Input.Keys.M) {
                audio.toggleMute();
                return true;
            }
            if (key == Input.Keys.ESCAPE) { finished = true; return true; }
            if (key == Input.Keys.ENTER || key == Input.Keys.SPACE) { advance(); return true; }
            return false;
        }
        @Override public boolean touchDown(int x, int y, int pointer, int button) { advance(); return true; }
    };

    public IntroScreen(CrusaderGame game, GameAssets assets, AudioPlayer audio) {
        this.game = game;
        this.assets = assets;
        this.audio = audio;
        illustration = new IntroAnimation(assets);
        illustration.setBounds(0, 140, 960, 400);
        stage.addActor(illustration);
        root.setFillParent(true);
        stage.addActor(root);
        build();
    }
    public int scene() { return scene; }
    private void advance() {
        if (finished) return;
        if (++scene == SCENES.length) finished = true;
        else { elapsed = 0; build(); }
    }
    private void build() {
        root.clearChildren();
        root.top();
        Label title = new Label(assets.texts.text("intro." + SCENES[scene] + ".title"), assets.skin);
        title.setColor(GameAssets.GOLD);
        root.add(title).padTop(12).row();
        root.add().expandY().row();
        Table caption = new Table();
        caption.setBackground(assets.panel(new com.badlogic.gdx.graphics.Color(.025f, .04f, .065f, 1)));
        caption.pad(12);
        Label story = new Label(assets.texts.text("intro." + SCENES[scene] + ".text"), assets.skin);
        story.setAlignment(com.badlogic.gdx.utils.Align.center);
        caption.add(story).expandX().padBottom(12).row();
        Label controls = new Label(assets.texts.text("intro.navigation"), assets.skin);
        controls.setFontScale(.75f * GameAssets.UI_FONT_SCALE);
        caption.add(controls);
        root.add(caption).growX().height(140);
    }
    @Override public void show() { Gdx.input.setInputProcessor(input); }
    @Override public void render(float delta) {
        if (finished) { game.startGame(); return; }
        if (!paused) elapsed += Math.min(delta, .1f);
        illustration.setFrame(scene, elapsed);
        ScreenUtils.clear(.025f, .04f, .065f, 1);
        stage.getViewport().apply();
        root.setColor(1, 1, 1, Math.min(1, elapsed * 2));
        stage.act(paused ? 0 : Math.min(delta, .1f)); stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }

    @Override
    public void pause() {
        paused = true;
        audio.setPaused(true);
    }

    @Override
    public void resume() {
        paused = false;
        audio.setPaused(false);
    }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { stage.dispose(); }
}
