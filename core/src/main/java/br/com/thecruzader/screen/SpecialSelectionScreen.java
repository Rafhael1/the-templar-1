package br.com.thecruzader.screen;

import br.com.thecruzader.CrusaderGame;
import br.com.thecruzader.gameplay.SpecialAbility;
import br.com.thecruzader.presentation.GameAssets;
import com.badlogic.gdx.*;
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
 * No world runs until the player confirms the level's loadout.
 */
public final class SpecialSelectionScreen extends ScreenAdapter {
    private final CrusaderGame game;
    private final GameAssets assets;
    private final Stage stage = new Stage(new FitViewport(960, 540));
    private final List<SpecialAbility> available;
    private final List<SpecialAbility> selected = new ArrayList<>();
    private final List<TextButton> buttons = new ArrayList<>();
    private final Label slots;
    private int focused;
    private boolean confirmed;

    public SpecialSelectionScreen(CrusaderGame game, GameAssets assets) {
        this.game = game;
        this.assets = assets;
        available = game.journey().unlockedAbilities();
        Table root = new Table();
        root.setFillParent(true);
        root.pad(32);
        stage.addActor(root);
        Label title = new Label(assets.texts.text("special.select.title"), assets.skin);
        title.setColor(GameAssets.GOLD);
        title.setFontScale(1.4f * GameAssets.UI_FONT_SCALE);
        root.add(title).padBottom(14).row();
        root.add(new Label(assets.texts.text(game.journey().currentLevel().texts().title()), assets.skin)).padBottom(18).row();
        root.add(new Label(assets.texts.text("special.select.help"), assets.skin)).padBottom(18).row();
        slots = new Label("", assets.skin);
        slots.setColor(GameAssets.GOLD);
        root.add(slots).padBottom(18).row();
        for (SpecialAbility ability : available) {
            TextButton button = button(assets.texts.text(ability.textKey()), () -> toggle(ability));
            root.add(button).width(430).height(44).padBottom(8).row();
            Label description = new Label(assets.texts.text(ability.textKey() + ".description"), assets.skin);
            description.setFontScale(.75f * GameAssets.UI_FONT_SCALE);
            root.add(description).padBottom(20).row();
        }
        root.add(button(assets.texts.text("special.select.start"), () -> confirmed = true))
            .width(430).height(44).padTop(12).row();
        root.add(new Label(assets.texts.text("special.select.navigation"), assets.skin)).padTop(18);
        refresh();
    }

    private TextButton button(String text, Runnable action) {
        TextButton button = new TextButton(text, assets.skin);
        button.getLabel().setFontScale(.9f * GameAssets.UI_FONT_SCALE);
        buttons.add(button);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });
        return button;
    }

    private void toggle(SpecialAbility ability) {
        if (selected.contains(ability)) selected.remove(ability);
        else if (selected.size() < 2) selected.add(ability);
        refresh();
    }

    private void refresh() {
        String empty = assets.texts.text("special.empty");
        slots.setText(assets.texts.format("special.select.slots",
            selected.isEmpty() ? empty : assets.texts.text(selected.getFirst().textKey()),
            selected.size() < 2 ? empty : assets.texts.text(selected.get(1).textKey())));
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setChecked(i == focused);
            buttons.get(i).setColor(i < available.size() && selected.contains(available.get(i))
                ? GameAssets.GOLD : GameAssets.PAPER);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(new InputAdapter() {
            @Override
            public boolean keyDown(int key) {
                switch (key) {
                    case Input.Keys.UP, Input.Keys.W -> focused = Math.floorMod(focused - 1, buttons.size());
                    case Input.Keys.DOWN, Input.Keys.S -> focused = (focused + 1) % buttons.size();
                    case Input.Keys.ENTER, Input.Keys.SPACE -> {
                        if (focused == available.size()) confirmed = true;
                        else toggle(available.get(focused));
                    }
                    default -> {
                        return false;
                    }
                }
                refresh();
                return true;
            }
        }, stage));
    }

    @Override
    public void render(float delta) {
        if (confirmed) {
            game.journey().selectAbilities(selected);
            game.startGame();
            return;
        }
        ScreenUtils.clear(.03f, .04f, .08f, 1);
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
