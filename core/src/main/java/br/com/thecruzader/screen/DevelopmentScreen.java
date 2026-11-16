package br.com.thecruzader.screen;

import br.com.thecruzader.CrusaderGame;
import br.com.thecruzader.presentation.GameAssets;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.List;

/**
 * Selects a fresh campaign starting point from its manifest in development mode.
 */
public final class DevelopmentScreen extends ScreenAdapter {
    private final CrusaderGame game;
    private final Stage stage = new Stage(new FitViewport(960, 540));
    private final List<TextButton> levels = new ArrayList<>();
    private int focus;
    private int chosen = -1;

    public DevelopmentScreen(CrusaderGame game, GameAssets assets) {
        this.game = game;
        var root = new Table();
        root.setFillParent(true);
        root.pad(24);
        root.setBackground(assets.frame);
        stage.addActor(root);
        var title = new Label(assets.texts.text("dev.title"), assets.skin);
        title.setFontScale(1.4f * GameAssets.UI_FONT_SCALE);
        title.setColor(GameAssets.GOLD);
        root.add(title).padBottom(18).row();
        for (var level : game.campaign().levels()) {
            int index = levels.size();
            var button = new TextButton((index + 1) + " / " + assets.texts.text(level.texts().title()), assets.skin);
            button.getLabel().setFontScale(GameAssets.UI_FONT_SCALE);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    chosen = index;
                }
            });
            levels.add(button);
            root.add(button).width(510).height(48).padBottom(10).row();
        }
        var help = new Label(assets.texts.text("dev.help"), assets.skin);
        help.setFontScale(.82f * GameAssets.UI_FONT_SCALE);
        root.add(help).padTop(12);
        levels.getFirst().setChecked(true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(new InputAdapter() {
            @Override
            public boolean keyDown(int key) {
                switch (key) {
                    case Input.Keys.W, Input.Keys.UP -> move(-1);
                    case Input.Keys.S, Input.Keys.DOWN -> move(1);
                    case Input.Keys.ENTER -> chosen = focus;
                    default -> {
                        return false;
                    }
                }
                return true;
            }
        }, stage));
    }

    private void move(int direction) {
        levels.get(focus).setChecked(false);
        focus = Math.floorMod(focus + direction, levels.size());
        levels.get(focus).setChecked(true);
    }

    @Override
    public void render(float delta) {
        if (chosen >= 0) {
            game.startDevelopmentLevel(chosen);
            return;
        }
        ScreenUtils.clear(.04f, .05f, .08f, 1);
        stage.getViewport().apply();
        stage.act(Math.min(delta, .1f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
