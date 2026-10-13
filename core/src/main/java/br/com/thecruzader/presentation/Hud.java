package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.gameplay.Player;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public final class Hud implements Disposable {
    private static final Color SHIELD_IDLE = Color.valueOf("66717a");
    private int lastBreadCount = -1;
    private final Stage stage = new Stage(new FitViewport(960, 540));
    private final GameAssets assets;
    private final Image[] hearts = new Image[Player.MAX_HEALTH];
    private final Image shield;
    private final ProgressBar dodge;
    private final Label objective, breadCount, sound, message, overlayTitle, overlayText;
    private final Table overlay;
    private double messageTime;

    public Hud(GameAssets assets) {
        this.assets = assets;
        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(16);
        stage.addActor(root);
        Table top = new Table();
        Table vitals = new Table();
        vitals.setBackground(assets.frame);
        vitals.pad(12);
        Table health = new Table();
        for (int i = 0; i < hearts.length; i++) {
            hearts[i] = new Image(assets.hearts[0]);
            health.add(hearts[i]).size(20, 20).padRight(4);
        }
        vitals.add(health).left();
        shield = new Image(new TextureRegion(assets.texture("shields"), 0, 0, 24, 36));
        vitals.add(shield).size(16, 24).padLeft(10);
        Table cooldown = new Table();
        Label dodgeLabel = new Label("ESQUIVA", assets.skin);
        dodgeLabel.setFontScale(.65f * GameAssets.UI_FONT_SCALE);
        dodgeLabel.setColor(GameAssets.GOLD);
        cooldown.add(dodgeLabel).left();
        cooldown.row();
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = assets.panel(Color.valueOf("282c37"));
        style.background.setMinHeight(4);
        style.knobBefore = assets.panel(GameAssets.GOLD);
        style.knobBefore.setMinHeight(4);
        dodge = new ProgressBar(0, 1, .001f, false, style);
        cooldown.add(dodge).width(66).height(10);
        vitals.add(cooldown).padLeft(14);
        top.add(vitals).height(58).left();
        sound = new Label("", assets.skin);
        sound.setFontScale(.7f * GameAssets.UI_FONT_SCALE);
        sound.setColor(GameAssets.GOLD);
        top.add(sound).padLeft(12).expandX().left();
        Table goal = new Table();
        goal.setBackground(assets.frame);
        goal.pad(10, 14, 10, 14);
        objective = new Label("", assets.skin);
        objective.setFontScale(.85f * GameAssets.UI_FONT_SCALE);
        goal.add(objective).left().padRight(18);
        goal.add(new Image(assets.texture("bread-of-life"))).size(24);
        breadCount = new Label("", assets.skin);
        breadCount.setColor(GameAssets.GOLD);
        goal.add(breadCount).padLeft(7);
        top.add(goal).height(58).right();
        root.add(top).growX();
        root.row();
        message = new Label("", assets.skin);
        message.setColor(GameAssets.GOLD);
        message.setAlignment(Align.center);
        message.setWrap(true);
        message.setFontScale(.9f * GameAssets.UI_FONT_SCALE);
        root.add(message).width(760).height(48).padTop(8);

        overlay = new Table();
        overlay.setFillParent(true);
        overlay.setBackground(assets.panel(new Color(.03f, .04f, .08f, .88f)));
        Table card = new Table();
        card.setBackground(assets.frame);
        card.pad(36);
        Label chapter = new Label("THE CRUZADER", assets.skin);
        chapter.setFontScale(.8f * GameAssets.UI_FONT_SCALE);
        chapter.setColor(GameAssets.GOLD);
        card.add(chapter).padBottom(24);
        card.row();
        overlayTitle = new Label("", assets.skin);
        overlayTitle.setFontScale(1.8f * GameAssets.UI_FONT_SCALE);
        overlayTitle.setColor(GameAssets.PAPER);
        card.add(overlayTitle).padBottom(24);
        card.row();
        card.add(new Image(assets.panel(GameAssets.GOLD))).width(72).height(2).padBottom(24);
        card.row();
        overlayText = new Label("", assets.skin);
        overlayText.setAlignment(Align.center);
        card.add(overlayText);
        overlay.add(card).width(590);
        overlay.setVisible(false);
        stage.addActor(overlay);
    }

    public void announce(String text) {
        message.setText(text);
        messageTime = 4;
    }

    public void render(GameWorld world, float delta, boolean muted, float deathElapsed) {
        Player player = world.player();
        for (int i = 0; i < hearts.length; i++)
            hearts[i].setDrawable(assets.hearts[i < player.health() ? 0 : 1]);
        shield.setColor(player.blocking() ? GameAssets.GOLD : SHIELD_IDLE);
        dodge.setValue((float) (1 - player.dodgeCooldown() / Player.DODGE_COOLDOWN));
        sound.setText(muted ? "SOM OFF" : "");
        objective.setText(player.x() > world.level().gate().x() - 600 && world.gateLocked()
                ? "DERROTE O GUARDIÃO" : "ALCANCE O PORTÃO");
        int count = world.collectedBreadCount();
        if (count != lastBreadCount) {
            lastBreadCount = count;
            breadCount.setText(count + "/" + world.level().breads().size());
        }
        if (!world.paused()) messageTime = Math.max(0, messageTime - delta);
        message.setVisible(messageTime > 0);
        message.getColor().a = (float) Math.min(1, Math.min((4 - messageTime) / .35, messageTime / .6));
        overlay.setVisible(world.paused() || world.state() == GameWorld.State.WON
                || (world.state() == GameWorld.State.DEAD && deathElapsed >= 1));
        if (world.state() == GameWorld.State.DEAD) {
            overlayTitle.setText("O VOTO PERMANECE");
            overlayText.setText("A cinza guarda os passos de quem caiu.\n\n"
                    + "R / ENTER  -  Retornar ao santuário");
        } else if (world.state() == GameWorld.State.WON) {
            overlayTitle.setText("ALÉM DAS MURALHAS");
            overlayText.setText("A passagem dos peregrinos está livre.\n"
                    + "Seu juramento foi cumprido.\n\nENTER  -  Nova jornada");
        } else {
            overlayTitle.setText("EM VIGÍLIA");
            overlayText.setText("ESC  -  Continuar     |     M  -  Som\n\n"
                    + "O jogo pausa ao perder o foco da janela.");
        }
        stage.getViewport().apply();
        stage.act(Math.min(delta, .1f));
        stage.draw();
    }

    public void resize(int width, int height) { stage.getViewport().update(width, height, true); }

    @Override
    public void dispose() { stage.dispose(); }
}
