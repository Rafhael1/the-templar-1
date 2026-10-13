package br.com.thecruzader.screen;

import br.com.thecruzader.CrusaderGame;
import br.com.thecruzader.presentation.GameAssets;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Three illustrated, player-paced scenes. Dialogue is original historical fiction. */
public final class IntroScreen extends ScreenAdapter {
    private static final String[] SCENES = {"council", "call", "march"};
    private final CrusaderGame game;
    private final GameAssets assets;
    private final Stage stage = new Stage(new FitViewport(960, 540));
    private final Table root = new Table();
    private int scene;
    private boolean finished;
    private float elapsed;
    private final InputAdapter input = new InputAdapter() {
        @Override public boolean keyDown(int key) {
            if (key == Input.Keys.ESCAPE) { finished = true; return true; }
            if (key == Input.Keys.ENTER || key == Input.Keys.SPACE) { advance(); return true; }
            return false;
        }
        @Override public boolean touchDown(int x, int y, int pointer, int button) { advance(); return true; }
    };
    public IntroScreen(CrusaderGame game, GameAssets assets) {
        this.game = game;
        this.assets = assets;
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
        root.pad(24);
        Label title = new Label(assets.texts.text("intro." + SCENES[scene] + ".title"), assets.skin);
        title.setColor(GameAssets.GOLD);
        root.add(title).padBottom(12).row();
        Image illustration = new Image(assets.texture(scene == 2 ? "march" : "council"));
        illustration.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.forever(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(4, 0, 3),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(-4, 0, 3))));
        root.add(illustration).size(816, 340).row();
        Label story = new Label(assets.texts.text("intro." + SCENES[scene] + ".text"), assets.skin);
        story.setAlignment(com.badlogic.gdx.utils.Align.center);
        root.add(story).padTop(12).row();
        Label controls = new Label(assets.texts.text("intro.navigation"), assets.skin);
        controls.setFontScale(.75f * GameAssets.UI_FONT_SCALE);
        root.add(controls).padTop(10);
    }
    @Override public void show() { Gdx.input.setInputProcessor(input); }
    @Override public void render(float delta) {
        if (finished) { game.startGame(); return; }
        elapsed += Math.min(delta, .1f);
        ScreenUtils.clear(.025f, .04f, .065f, 1);
        stage.getViewport().apply();
        root.setColor(1, 1, 1, Math.min(1, elapsed * 2));
        stage.act(delta); stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { stage.dispose(); }
}
