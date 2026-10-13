package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.gameplay.Player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public final class Hud implements Disposable {
    private final Stage stage = new Stage(new FitViewport(960, 540));
    private final Label health, objective, message, overlayTitle, overlayText;
    private final Table overlay;
    private double messageTime;

    public Hud(GameAssets assets) {
        Table root = new Table();
        root.setFillParent(true);
        root.top();
        stage.addActor(root);
        Table top = new Table();
        top.setBackground(assets.panel(new Color(.04f, .06f, .10f, .94f)));
        Label title = new Label("THE CRUZADER", assets.skin);
        title.setColor(GameAssets.GOLD);
        title.setFontScale(1.25f);
        health = new Label("", assets.skin);
        objective = new Label("", assets.skin);
        top.add(title).left().expandX();
        top.add(health).padLeft(25).left().expandX();
        top.add(objective).right();
        root.add(top).growX().height(64).padBottom(12);
        top.pad(18);
        message = new Label("", assets.skin);
        message.setColor(GameAssets.GOLD);
        root.row();
        root.add(message).expandX();
        root.row();
        root.add().expand().fill();
        Label controls =
                new Label(
                        "A D / SETAS mover   ESPACO saltar   J espada   S + J no ar\n"
                            + "L / X escudo   SHIFT esquiva   ESC pausa   M som",
                        assets.skin);
        controls.setFontScale(.85f);
        controls.setAlignment(Align.center);
        Table bottom = new Table();
        bottom.setBackground(assets.panel(new Color(.04f, .06f, .10f, .94f)));
        bottom.add(controls).pad(12);
        root.row();
        root.add(bottom).growX();
        overlay = new Table();
        overlay.setFillParent(true);
        overlay.setBackground(assets.panel(new Color(.03f, .04f, .08f, .91f)));
        overlayTitle = new Label("", assets.skin);
        overlayTitle.setFontScale(2.2f);
        overlayTitle.setColor(GameAssets.GOLD);
        overlayText = new Label("", assets.skin);
        overlayText.setAlignment(Align.center);
        overlay.add(overlayTitle).padBottom(32);
        overlay.row();
        overlay.add(overlayText);
        overlay.setVisible(false);
        stage.addActor(overlay);
    }

    public void announce(String text) {
        message.setText(text);
        messageTime = 4;
    }

    public void render(GameWorld world, float delta, boolean muted) {
        Player p = world.player();
        health.setText(
                "VIDA "
                        + p.health()
                        + "/5    ESQUIVA "
                        + (p.dodgeCooldown() == 0 ? "PRONTA" : "...")
                        + (p.blocking() ? "    ESCUDO" : "")
                        + (muted ? "    SOM OFF" : ""));
        objective.setText(
                world.player().x() > 2350 && world.gateLocked()
                        ? "DERROTE O GUARDIAO"
                        : "ALCANCE A PORTA  |  RELIQUIA " + (world.relic() ? "1/1" : "0/1"));
        if (!world.paused()) messageTime = Math.max(0, messageTime - delta);
        message.setVisible(messageTime > 0);
        overlay.setVisible(world.paused() || world.state() != GameWorld.State.PLAYING);
        if (world.state() == GameWorld.State.DEAD) {
            overlayTitle.setText("O VOTO PERMANECE");
            overlayText.setText(
                    "A cinza guarda os passos de quem caiu.\n\n"
                        + "R / ENTER  -  Retornar ao santuario");
        } else if (world.state() == GameWorld.State.WON) {
            overlayTitle.setText("ALEM DAS MURALHAS");
            overlayText.setText(
                    "A passagem dos peregrinos esta livre.\n"
                        + "Seu juramento foi cumprido.\n\n"
                        + "ENTER  -  Nova jornada");
        } else {
            overlayTitle.setText("EM VIGILIA");
            overlayText.setText(
                    "ESC  -  Continuar     |     M  -  Som\n\n"
                        + "O jogo pausa ao perder o foco da janela.");
        }
        stage.getViewport().apply();
        stage.act(Math.min(delta, .1f));
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
