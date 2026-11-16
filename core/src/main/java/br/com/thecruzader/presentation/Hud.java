package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.SpecialAbility;

import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.gameplay.Player;
import br.com.thecruzader.level.LevelDefinition;
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
    private int lastBandageCount = -1;
    private final Stage stage = new Stage(new FitViewport(960, 540));
    private final GameAssets assets;
    private final Image[] hearts;
    private final Image shield;
    private final ProgressBar dodge;
    private final Label specials;
    private final Label objective, bandageCount, sound, message, overlayTitle, overlayText;
    private final Label conversationTitle, conversationBody;
    private final Table conversationPanel, promptPanel;
    private LevelDefinition.Conversation displayedConversation;
    private final Table overlay, bossPanel;
    private final ProgressBar bossHealth;
    private double messageTime;
    private final LevelDefinition definition;
    private final boolean hasNextLevel;

    public Hud(GameAssets assets, LevelDefinition definition, boolean hasNextLevel, Player player) {
        hearts = new Image[player.maxHealth()];
        this.assets = assets;
        this.definition = definition;
        this.hasNextLevel = hasNextLevel;
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
        Label dodgeLabel = new Label(assets.texts.text("hud.dodge"), assets.skin);
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
        goal.add(new Image(assets.texture("bandages"))).size(24);
        bandageCount = new Label("", assets.skin);
        bandageCount.setColor(GameAssets.GOLD);
        goal.add(bandageCount).padLeft(7);
        top.add(goal).height(58).right();
        root.add(top).growX();
        root.row();
        specials = new Label("", assets.skin);
        specials.setFontScale(.7f * GameAssets.UI_FONT_SCALE);
        specials.setColor(GameAssets.GOLD);
        root.add(specials).left().padTop(8);
        root.row();
        message = new Label("", assets.skin);
        message.setColor(GameAssets.GOLD);
        message.setAlignment(Align.center);
        message.setWrap(true);
        message.setFontScale(.9f * GameAssets.UI_FONT_SCALE);
        root.add(message).width(760).height(48).padTop(8);

        bossPanel = new Table();
        bossPanel.setFillParent(true);
        bossPanel.top().padTop(145);
        Table bossCard = new Table();
        bossCard.setBackground(assets.frame);
        bossCard.pad(9);
        Label bossName = new Label(assets.texts.text(definition.texts().bossName()), assets.skin);
        bossName.setFontScale(.75f * GameAssets.UI_FONT_SCALE);
        bossName.setColor(GameAssets.GOLD);
        bossCard.add(bossName).padBottom(5).row();
        ProgressBar.ProgressBarStyle bossStyle = new ProgressBar.ProgressBarStyle();
        bossStyle.background = assets.panel(Color.valueOf("342e3c"));
        bossStyle.background.setMinHeight(8);
        bossStyle.knobBefore = assets.panel(Color.valueOf("973f4e"));
        bossStyle.knobBefore.setMinHeight(8);
        bossHealth = new ProgressBar(0, 1, .001f, false, bossStyle);
        bossCard.add(bossHealth).width(324).height(10);
        bossPanel.add(bossCard).width(344);
        bossPanel.setVisible(false);
        stage.addActor(bossPanel);

        conversationPanel = new Table();
        conversationPanel.setFillParent(true);
        conversationPanel.top().padTop(144);
        Table conversationCard = new Table();
        conversationCard.setBackground(assets.frame);
        conversationCard.pad(18, 24, 18, 24);
        conversationTitle = conversationLine(conversationCard, 8);
        conversationTitle.setColor(GameAssets.GOLD);
        conversationBody = conversationLine(conversationCard, 10);
        Label conversationHint = conversationLine(conversationCard, 0);
        conversationHint.setText(assets.texts.text("conversation.close"));
        conversationHint.setColor(GameAssets.GOLD);
        conversationPanel.add(conversationCard);
        conversationPanel.setVisible(false);
        stage.addActor(conversationPanel);

        promptPanel = new Table();
        promptPanel.setFillParent(true);
        promptPanel.bottom().padBottom(20);
        Label talkPrompt = new Label(assets.texts.text("conversation.open"), assets.skin);
        talkPrompt.setColor(GameAssets.GOLD);
        talkPrompt.setFontScale(.85f * GameAssets.UI_FONT_SCALE);
        Table promptCard = new Table();
        promptCard.setBackground(assets.frame);
        promptCard.pad(8, 16, 8, 16);
        promptCard.add(talkPrompt);
        promptPanel.add(promptCard);
        promptPanel.setVisible(false);
        stage.addActor(promptPanel);

        overlay = new Table();
        overlay.setFillParent(true);
        overlay.setBackground(assets.panel(new Color(.03f, .04f, .08f, .88f)));
        Table card = new Table();
        card.setBackground(assets.frame);
        card.pad(36);
        Label chapter = new Label(assets.texts.text("game.name"), assets.skin);
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
        overlayText.setWrap(true);
        card.add(overlayText).width(510);
        overlay.add(card).width(590);
        overlay.setVisible(false);
        stage.addActor(overlay);
    }

    private Label conversationLine(Table card, int bottomPadding) {
        Label label = new Label("", assets.skin);
        label.setWrap(true);
        label.setFontScale(.9f * GameAssets.UI_FONT_SCALE);
        card.add(label).width(590).left().padBottom(bottomPadding).row();
        return label;
    }

    public void showConversation(LevelDefinition.Conversation nearby, LevelDefinition.Conversation open) {
        promptPanel.setVisible(nearby != null && open == null);
        conversationPanel.setVisible(open != null);
        if (open == displayedConversation) return;
        displayedConversation = open;
        if (open == null) return;
        conversationTitle.setText(assets.texts.text(open.textKey() + ".title"));
        conversationBody.setText(assets.texts.text(open.textKey() + ".body"));
    }

    public void announce(String key) {
        message.setText(assets.texts.text(key));
        messageTime = 4;
    }

    public void announceReward(SpecialAbility reward, Player player) {
        int slot = player.abilities().indexOf(reward);
        if (reward == SpecialAbility.BOW && slot >= 0) {
            message.setText(assets.texts.format("special.bow.unlocked", slot == 0 ? "Q" : "E"));
            messageTime = 7;
            return;
        }
        message.setText(assets.texts.format("special.unlocked", assets.texts.text(reward.textKey())));
        messageTime = 4;
    }

    public void render(GameWorld world, float delta, boolean muted, float deathElapsed) {
        Player player = world.player();
        StringBuilder specialText = new StringBuilder();
        for (int slot = 0; slot < player.abilities().size(); slot++) {
            if (slot > 0) specialText.append("     ");
            specialText.append(assets.texts.format("special.hud", slot == 0 ? "Q" : "E",
                assets.texts.text(player.abilities().get(slot).textKey()), player.specialCharges(slot), player.abilities().get(slot).charges()));
        }
        specials.setText(specialText);
        for (int i = 0; i < hearts.length; i++)
            hearts[i].setDrawable(assets.hearts[i < player.health() ? 0 : 1]);
        shield.setColor(player.blocking() ? GameAssets.GOLD : SHIELD_IDLE);
        dodge.setValue((float) (1 - player.dodgeCooldown() / Player.DODGE_COOLDOWN));
        sound.setText(muted ? assets.texts.text("hud.muted") : "");
        objective.setText(assets.texts.text(world.bossReleaseActive() ? "hud.angels" : world.arenaLocked()
            ? definition.texts().bossObjective() : world.bossDefeated()
            ? definition.texts().exitObjective() : definition.texts().travelObjective()));
        bossPanel.setVisible(world.arenaLocked() && world.boss().health() > 0);
        bossHealth.setValue((float) world.boss().health() / world.boss().maxHealth());
        int count = world.collectedBandageCount();
        if (count != lastBandageCount) {
            lastBandageCount = count;
            bandageCount.setText(assets.texts.format("hud.bandageCount", count, world.level().bandages().size()));
        }
        if (!world.paused()) messageTime = Math.max(0, messageTime - delta);
        message.setVisible(messageTime > 0);
        message.getColor().a = (float) Math.max(0, Math.min(1, messageTime / .6));
        overlay.setVisible(world.paused() || world.state() == GameWorld.State.WON
                || (world.state() == GameWorld.State.DEAD && deathElapsed >= TemplarDeathAnimation.DURATION));
        String overlayKey = world.paused() ? "pause" : world.state() == GameWorld.State.DEAD ? "death"
                : world.state() == GameWorld.State.WON ? "victory" : "pause";
        boolean won = world.state() == GameWorld.State.WON;
        overlayTitle.setText(assets.texts.text(won ? definition.texts().completionTitle()
                : "overlay." + overlayKey + ".title"));
        overlayText.setText(assets.texts.text(won ? (hasNextLevel ? "overlay.levelComplete.text"
                : definition.texts().completionText()) : "overlay." + overlayKey + ".text"));
        stage.getViewport().apply();
        stage.act(Math.min(delta, .1f));
        stage.draw();
    }

    public void resize(int width, int height) { stage.getViewport().update(width, height, true); }

    @Override
    public void dispose() { stage.dispose(); }
}
