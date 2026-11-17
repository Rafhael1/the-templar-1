package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.gameplay.Player;
import br.com.thecruzader.gameplay.SpecialAbility;
import br.com.thecruzader.level.LevelDefinition;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.List;

public final class Hud implements Disposable {
    private static final Color SHIELD_IDLE = Color.valueOf("66717a");
    private static final Color SPECIAL_SPENT = new Color(.4f, .43f, .48f, .3f);

    private final Stage stage = new Stage(new FitViewport(960, 540));
    private final GameAssets assets;
    private final LevelDefinition definition;
    private final boolean hasRequiredConversations;
    private final boolean hasNextLevel;
    private final Image[] hearts;
    private final Image shield;
    private final ProgressBar dodge;
    private final Table specials = new Table();
    private final List<Image[]> specialIcons = new ArrayList<>();
    private List<SpecialAbility> displayedAbilities = List.of();
    private final Label objective;
    private final Label bandageCount;
    private final Label sound;
    private final Label messageTitle;
    private final Label message;
    private final Label overlayTitle;
    private final Label overlayText;
    private final Table messagePanel;
    private final Label conversationTitle;
    private final Label conversationBody;
    private final Label conversationHint;
    private final Table conversationPanel;
    private final Table promptPanel;
    private final Table overlay;
    private final Table bossPanel;
    private final ProgressBar bossHealth;

    private int lastBandageCount = -1;
    private double messageTime;
    private LevelDefinition.Conversation displayedConversation;

    public Hud(GameAssets assets, LevelDefinition definition, boolean hasNextLevel, Player player) {
        hearts = new Image[player.maxHealth()];
        this.assets = assets;
        this.definition = definition;
        hasRequiredConversations = definition.hasRequiredConversations();
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
        root.add(specials).left().padTop(8);

        messagePanel = new Table();
        messagePanel.setFillParent(true);
        messagePanel.top().padTop(104);
        messagePanel.setTouchable(Touchable.disabled);
        Table messageCard = new Table();
        messageCard.setBackground(assets.frame.tint(new Color(1, 1, 1, .65f)));
        messageCard.pad(8, 14, 10, 14);
        messageTitle = new Label("", assets.skin);
        messageTitle.setColor(GameAssets.GOLD);
        messageTitle.setAlignment(Align.center);
        messageTitle.setWrap(true);
        messageTitle.setFontScale(.85f * GameAssets.UI_FONT_SCALE);
        messageCard.add(messageTitle).width(412).padBottom(5).row();
        message = new Label("", assets.skin);
        message.setAlignment(Align.center);
        message.setWrap(true);
        message.setFontScale(.7f * GameAssets.UI_FONT_SCALE);
        messageCard.add(message).width(412);
        messagePanel.add(messageCard).width(440);
        messagePanel.setVisible(false);
        stage.addActor(messagePanel);

        bossPanel = new Table();
        bossPanel.setFillParent(true);
        bossPanel.top().padTop(194);
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
        conversationHint = conversationLine(conversationCard, 0);
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
        conversationHint.setText(assets.texts.text(open.required() ? "conversation.confirm" : "conversation.close"));
    }

    public void announce(String key) {
        showMessage(assets.texts.text("notice.title"), assets.texts.text(key), 4);
    }

    private void showMessage(String title, String body, double duration) {
        messageTitle.setText(title);
        message.setText(body);
        messageTime = duration;
    }

    public void announceReward(SpecialAbility reward, Player player) {
        int slot = player.abilities().indexOf(reward);
        if (reward == SpecialAbility.BOW && slot >= 0) {
            showMessage(assets.texts.text("special.bow.unlocked"),
                assets.texts.format("special.bow.unlocked.hint", slot == 0 ? "Q" : "E"), 7);
            return;
        }
        showMessage(assets.texts.format("special.unlocked", assets.texts.text(reward.textKey())),
            assets.texts.text("special.unlocked.hint"), 4);
    }

    public void render(GameWorld world, float delta, boolean muted, float deathElapsed) {
        Player player = world.player();
        updateSpecials(player);
        updateVitals(player, muted);
        updateWorldIndicators(world);
        updateMessage(world, delta);
        updateOverlay(world, deathElapsed);

        stage.getViewport().apply();
        stage.act(Math.min(delta, .1f));
        stage.draw();
    }

    private void updateSpecials(Player player) {
        if (!displayedAbilities.equals(player.abilities())) {
            rebuildSpecials(player.abilities());
        }
        for (int slot = 0; slot < specialIcons.size(); slot++) {
            Color available = displayedAbilities.get(slot) == SpecialAbility.GOLDEN_STRIKE
                ? GameAssets.GOLD : Color.WHITE;
            Image[] icons = specialIcons.get(slot);
            for (int charge = 0; charge < icons.length; charge++) {
                icons[charge].setColor(charge < player.specialCharges(slot) ? available : SPECIAL_SPENT);
            }
        }
    }

    private void rebuildSpecials(List<SpecialAbility> abilities) {
        specials.clearChildren();
        specialIcons.clear();
        displayedAbilities = List.copyOf(abilities);
        for (int slot = 0; slot < abilities.size(); slot++) {
            SpecialAbility ability = abilities.get(slot);
            Table card = new Table();
            Label key = new Label(slot == 0 ? "Q" : "E", assets.skin);
            key.setFontScale(.7f * GameAssets.UI_FONT_SCALE);
            key.setColor(GameAssets.GOLD);
            card.add(key).padRight(5);
            TextureRegion icon = switch (ability) {
                case GOLDEN_STRIKE -> assets.swords[0];
                case BOW -> new TextureRegion(assets.texture("arrow"));
            };
            Image[] icons = new Image[ability.charges()];
            for (int charge = 0; charge < icons.length; charge++) {
                icons[charge] = new Image(icon);
                card.add(icons[charge]).size(24,
                    24f * icon.getRegionHeight() / icon.getRegionWidth()).padRight(2);
            }
            specialIcons.add(icons);
            specials.add(card).padRight(12);
        }
    }

    private void updateVitals(Player player, boolean muted) {
        for (int i = 0; i < hearts.length; i++) {
            hearts[i].setDrawable(assets.hearts[i < player.health() ? 0 : 1]);
        }
        shield.setColor(player.blocking() ? GameAssets.GOLD : SHIELD_IDLE);
        dodge.setValue((float) (1 - player.dodgeCooldown() / Player.DODGE_COOLDOWN));
        sound.setText(muted ? assets.texts.text("hud.muted") : "");
    }

    private void updateWorldIndicators(GameWorld world) {
        String objectiveKey;
        if (world.bossReleaseActive()) {
            objectiveKey = "hud.angels";
        } else if (world.arenaLocked()) {
            objectiveKey = definition.texts().bossObjective();
        } else if (world.bossDefeated()) {
            objectiveKey = definition.texts().exitObjective();
        } else if (hasRequiredConversations && !world.briefingsPending()) {
            objectiveKey = "hud.briefingsConfirmed";
        } else {
            objectiveKey = definition.texts().travelObjective();
        }
        objective.setText(assets.texts.text(objectiveKey));
        bossPanel.setVisible(world.arenaLocked() && world.boss().health() > 0);
        bossHealth.setValue((float) world.boss().health() / world.boss().maxHealth());

        int count = world.collectedBandageCount();
        if (count != lastBandageCount) {
            lastBandageCount = count;
            bandageCount.setText(assets.texts.format("hud.bandageCount", count, world.level().bandages().size()));
        }
    }

    private void updateMessage(GameWorld world, float delta) {
        if (!world.paused()) messageTime = Math.max(0, messageTime - delta);
        messagePanel.setVisible(messageTime > 0 && displayedConversation == null
            && !world.paused() && world.state() == GameWorld.State.PLAYING);
        messagePanel.getColor().a = (float) Math.max(0, Math.min(1, messageTime / .6));
    }

    private void updateOverlay(GameWorld world, float deathElapsed) {
        GameWorld.State state = world.state();
        overlay.setVisible(world.paused() || state == GameWorld.State.WON
            || (state == GameWorld.State.DEAD && deathElapsed >= TemplarDeathAnimation.DURATION));

        if (state == GameWorld.State.WON) {
            overlayTitle.setText(assets.texts.text(definition.texts().completionTitle()));
            String prompt = hasNextLevel ? "overlay.levelComplete.next" : "overlay.victory.restart";
            overlayText.setText(assets.texts.text(definition.texts().completionText()) + "\n\n" + assets.texts.text(prompt));
            return;
        }

        String overlayKey = world.paused() ? "pause" : state == GameWorld.State.DEAD ? "death" : "pause";
        overlayTitle.setText(assets.texts.text("overlay." + overlayKey + ".title"));
        overlayText.setText(assets.texts.text("overlay." + overlayKey + ".text"));
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
