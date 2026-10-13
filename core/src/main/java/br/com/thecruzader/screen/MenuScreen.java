package br.com.thecruzader.screen;

import br.com.thecruzader.CrusaderGame;
import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.level.LevelLoader;
import br.com.thecruzader.presentation.AudioPlayer;
import br.com.thecruzader.presentation.GameAssets;
import br.com.thecruzader.presentation.DisplaySettings;
import br.com.thecruzader.presentation.WorldRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.ArrayList;
import java.util.List;

public final class MenuScreen extends ScreenAdapter {
    public enum Page { MAIN, INSTRUCTIONS, OPTIONS }

    private final CrusaderGame game;
    private final GameAssets assets;
    private final AudioPlayer audio;
    private final Stage stage = new Stage(new FitViewport(960, 540));
    private final Table root = new Table();
    private final WorldRenderer renderer;
    private final GameWorld backdrop;
    private final List<TextButton> buttons = new ArrayList<>();
    private final List<Runnable> actions = new ArrayList<>();
    private Page page = Page.MAIN;
    private int selected;
    private boolean startRequested, quitRequested;
    private final InputAdapter keyboard = new InputAdapter() {
        @Override
        public boolean keyDown(int key) {
            switch (key) {
                case Input.Keys.UP, Input.Keys.W -> select(selected - 1);
                case Input.Keys.DOWN, Input.Keys.S -> select(selected + 1);
                case Input.Keys.ENTER, Input.Keys.SPACE -> actions.get(selected).run();
                case Input.Keys.ESCAPE -> { if (page != Page.MAIN) open(Page.MAIN); }
                case Input.Keys.I -> { if (page == Page.MAIN) open(Page.INSTRUCTIONS); }
                case Input.Keys.O -> { if (page == Page.MAIN) open(Page.OPTIONS); }
                default -> { return false; }
            }
            return true;
        }
    };

    public MenuScreen(CrusaderGame game, GameAssets assets, AudioPlayer audio) {
        this.game = game;
        this.assets = assets;
        this.audio = audio;
        renderer = new WorldRenderer(assets);
        backdrop = new GameWorld(LevelLoader.load(Gdx.files.internal(GameAssets.MAP)));
        root.setFillParent(true);
        root.setBackground(assets.panel(new Color(.025f, .04f, .075f, .78f)));
        stage.addActor(root);
        open(Page.MAIN);
    }

    public Page page() { return page; }

    private void open(Page next) {
        page = next;
        selected = 0;
        buttons.clear();
        actions.clear();
        root.clearChildren();
        root.pad(40);
        switch (page) {
            case MAIN -> buildMain();
            case INSTRUCTIONS -> buildInstructions();
            case OPTIONS -> buildOptions();
        }
        select(0);
    }

    private Label label(String key, float scale, Color color) {
        Label label = new Label(assets.texts.text(key), assets.skin);
        label.setFontScale(scale * GameAssets.UI_FONT_SCALE);
        label.setColor(color);
        return label;
    }

    private Table card() {
        Table card = new Table();
        card.setBackground(assets.frame);
        card.pad(28);
        return card;
    }

    private TextButton button(String text, Runnable action) {
        TextButton button = new TextButton(text, assets.skin);
        button.setProgrammaticChangeEvents(false);
        button.getLabel().setFontScale(.95f * GameAssets.UI_FONT_SCALE);
        int index = buttons.size();
        buttons.add(button);
        actions.add(action);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                select(index);
                action.run();
            }
        });
        return button;
    }

    private void select(int index) {
        selected = Math.floorMod(index, buttons.size());
        for (int i = 0; i < buttons.size(); i++) buttons.get(i).setChecked(i == selected);
        stage.setKeyboardFocus(buttons.get(selected));
    }

    private void buildMain() {
        Table introduction = new Table();
        introduction.add(label("menu.chapter", .8f, GameAssets.GOLD))
                .left().padBottom(20);
        introduction.row();
        introduction.add(label("menu.title", 3.8f, GameAssets.PAPER)).left().padBottom(22);
        introduction.row();
        introduction.add(new Image(assets.panel(GameAssets.GOLD)))
                .left().width(80).height(2).padBottom(22);
        introduction.row();
        Table story = new Table();
        story.add(new Image(assets.characters[0][0])).size(112, 140).padRight(20);
        Label description = label("menu.story", .95f, GameAssets.PAPER);
        story.add(description).left();
        introduction.add(story).left();
        introduction.row();
        introduction.add(label("menu.art", .65f, GameAssets.GOLD))
                .left().padTop(20);
        root.add(introduction).width(450).left();
        root.add().width(42);
        Table menu = card();
        menu.add(label("menu.level", .85f, GameAssets.GOLD)).padBottom(12);
        menu.row();
        menu.add(label("menu.motto", .9f, GameAssets.PAPER)).padBottom(24);
        menu.row();
        menu.add(button(assets.texts.text("menu.newGame"), () -> startRequested = true)).width(244).height(46);
        menu.row();
        menu.add(button(assets.texts.text("menu.instructions"), () -> open(Page.INSTRUCTIONS))).width(244).height(46).padTop(8);
        menu.row();
        menu.add(button(assets.texts.text("menu.options"), () -> open(Page.OPTIONS))).width(244).height(46).padTop(8);
        menu.row();
        menu.add(button(assets.texts.text("menu.quit"), () -> quitRequested = true)).width(244).height(46).padTop(8);
        menu.row();
        menu.add(label("menu.navigation", .65f, GameAssets.GOLD)).padTop(22);
        root.add(menu).width(300);
    }

    private void buildInstructions() {
        Table instructions = card();
        instructions.add(label("menu.instructions", 2f, GameAssets.PAPER)).colspan(2).padBottom(10);
        instructions.row();
        instructions.add(label("instructions.goal",
                .85f, GameAssets.GOLD)).colspan(2).padBottom(20);
        String[] controls = {"move", "jump", "attack", "downAttack", "block", "dodge", "pause", "respawn"};
        for (String control : controls) {
            instructions.row();
            instructions.add(label("instructions." + control + ".keys", .85f, GameAssets.GOLD))
                    .width(245).left().pad(4);
            instructions.add(label("instructions." + control + ".action", .85f, GameAssets.PAPER))
                    .width(410).left().pad(4);
        }
        instructions.row();
        instructions.add(label("instructions.tips",
                .8f, GameAssets.PAPER)).colspan(2).padTop(18).padBottom(18);
        instructions.row();
        instructions.add(button(assets.texts.text("menu.back"), () -> open(Page.MAIN))).colspan(2).width(240).height(42);
        root.add(instructions).width(800);
    }

    private void buildOptions() {
        Table options = card();
        options.add(label("menu.options", 2f, GameAssets.PAPER)).padBottom(12);
        options.row();
        options.add(label("options.subtitle", .85f, GameAssets.GOLD)).padBottom(26);
        options.row();
        TextButton sound = button(soundText(), () -> {
            audio.toggleMute();
            buttons.get(0).setText(soundText());
        });
        options.add(sound).width(300).height(48);
        options.row();
        options.add(button(displayText(), () -> {
            DisplaySettings.apply(!DisplaySettings.fullHd());
            buttons.get(1).setText(displayText());
        })).width(300).height(48).padTop(10);
        options.row();
        options.add(button(assets.texts.text("options.language"), () -> {
            int selection = selected;
            game.setLanguage(game.language().next());
            open(Page.OPTIONS);
            select(selection);
        })).width(300).height(48).padTop(10);
        options.row();
        options.add(button(assets.texts.text("menu.back"), () -> open(Page.MAIN))).width(300).height(48).padTop(24);
        options.row();
        options.add(label("menu.navigation", .65f, GameAssets.GOLD)).padTop(20);
        root.add(options).width(430);
    }

    private String soundText() { return assets.texts.text(audio.muted() ? "options.soundOff" : "options.soundOn"); }
    private String displayText() { return assets.texts.format("options.resolution",
            Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight()); }


    @Override
    public void show() { Gdx.input.setInputProcessor(new InputMultiplexer(keyboard, stage)); }

    @Override
    public void render(float delta) {
        if (quitRequested) { Gdx.app.exit(); return; }
        if (startRequested) { game.startJourney(); return; }
        renderer.render(backdrop, delta, 1);
        stage.getViewport().apply();
        stage.act(Math.min(delta, .1f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() { Gdx.input.setInputProcessor(null); }

    @Override
    public void dispose() { stage.dispose(); renderer.dispose(); }
}
