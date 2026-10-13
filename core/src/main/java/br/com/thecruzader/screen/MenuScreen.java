package br.com.thecruzader.screen;

import br.com.thecruzader.CrusaderGame;
import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.level.LevelLoader;
import br.com.thecruzader.presentation.GameAssets;
import br.com.thecruzader.presentation.WorldRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public final class MenuScreen extends ScreenAdapter {
    private final CrusaderGame game;
    private final Stage stage = new Stage(new FitViewport(960, 540));
    private final WorldRenderer renderer;
    private final GameWorld backdrop;
    private boolean startRequested;

    public MenuScreen(CrusaderGame game, GameAssets assets) {
        this.game = game;
        renderer = new WorldRenderer(assets);
        backdrop = new GameWorld(LevelLoader.load(Gdx.files.internal(GameAssets.MAP)));
        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(assets.panel(new Color(.025f, .04f, .075f, .77f)));
        Label subtitle = new Label("CRONICAS DAS CRUZADAS  /  CAPITULO I", assets.skin);
        subtitle.setColor(GameAssets.GOLD);
        Label title = new Label("THE CRUZADER", assets.skin);
        title.setFontScale(3.6f);
        title.setColor(GameAssets.PAPER);
        Label story =
                new Label(
                        "Um cavaleiro templario. Um juramento entre ruinas.\n\n"
                            + "Nas Cruzadas, voce se afasta da coluna para abrir uma passagem.\n"
                            + "Proteja os peregrinos e enfrente as sentinelas de cinza.",
                        assets.skin);
        story.setAlignment(Align.center);
        TextButton play = new TextButton("INICIAR JORNADA  /  ENTER", assets.skin);
        play.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        startRequested = true;
                    }
                });
        root.add(subtitle).padBottom(28);
        root.row();
        root.add(title).padBottom(34);
        root.row();
        root.add(story).padBottom(32);
        root.row();
        root.add(play).width(310).height(48);
        root.row();
        Label footer =
                new Label("FICCAO HISTORICA   /   ARTE ORIGINAL   /   PROTOTIPO 1.0", assets.skin);
        footer.setFontScale(.8f);
        footer.setColor(GameAssets.GOLD);
        root.add(footer).padTop(34);
        stage.addActor(root);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        if (startRequested || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.startGame();
            return;
        }
        renderer.render(backdrop);
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
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        renderer.dispose();
    }
}
