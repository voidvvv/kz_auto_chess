package com.voidvvv.autochess.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.voidvvv.autochess.KzAutoChess;
import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.event.GameEventListener;
import com.voidvvv.autochess.event.drag.DragEvent;
import com.voidvvv.autochess.event.drag.DragMovedEvent;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.Battlefield;
import com.voidvvv.autochess.model.CardShop;
import com.voidvvv.autochess.model.PlayerDeck;
import com.voidvvv.autochess.model.PlayerEconomy;
import com.voidvvv.autochess.logic.SynergyManager;
import com.voidvvv.autochess.utils.FontUtils;
import com.voidvvv.autochess.utils.I18N;
import com.voidvvv.autochess.logic.CardUpgradeLogic;

import java.util.List;

/**
 * 游戏UI管理器
 * 负责 UI 布局、渲染和事件响应
 * 使用 Scene2D Table 进行布局管理
 */
public class GameUIManager implements GameEventListener {

    /**
     * 按钮点击回调接口
     */
    public interface ButtonCallback {
        void onBackButtonClicked();
        void onStartBattleButtonClicked();
    }

    private final KzAutoChess game;
    private final Stage stage;
    private final Skin skin;
    private final ShapeRenderer shapeRenderer;
    private final ShapeRendererHelper shapeRendererHelper;
    private final ButtonCallback buttonCallback;

    // UI 字体
    private BitmapFont titleFont;
    private BitmapFont smallFont;

    // 临时GlyphLayout实例（用于避免每帧创建新对象，减少GC压力）
    private final GlyphLayout tempGlyphLayout = new GlyphLayout();

    // UI 布局 Tables
    private Table rootTable;
    private Table headerTable;       // 标题和信息区域
    private Table contentTable;      // 主要内容区域
    private Table shopTable;         // 商店区域
    private Table deckTable;         // 卡组区域

    // 按钮引用
    private TextButton refreshButton;
    private TextButton startBattleButton;
    private TextButton backButton;

    // 游戏数据引用
    private Battlefield battlefield;
    private CardShop cardShop;
    private PlayerDeck playerDeck;
    private PlayerEconomy playerEconomy;
    private SynergyManager synergyManager;

    // 拖拽状态（用于渲染预览）
    private Object draggingObject;
    private float dragX, dragY;
    private boolean isDragging = false;

    // 布局参数
    private int level;

    public GameUIManager(KzAutoChess game, int level, ButtonCallback buttonCallback) {
        this.game = game;
        this.level = level;
        this.buttonCallback = buttonCallback;
        this.stage = new Stage(game.getViewManagement().getUIViewport());

        // 加载皮肤
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        Gdx.app.log("GameUIManager", "Skin loaded successfully");

        this.shapeRenderer = new ShapeRenderer();
        this.shapeRendererHelper = new ShapeRendererHelper(shapeRenderer);
        this.titleFont = FontUtils.getDefaultFont();
        this.smallFont = FontUtils.getSmallFont();

        // 初始化布局
        createLayout();
    }

    /**
     * 创建 UI 布局
     */
    private void createLayout() {
        rootTable = new Table();
        rootTable.setFillParent(true);

        // 标题栏
        headerTable = createHeaderTable();
        rootTable.add(headerTable).growX().height(80).row();

        // 主内容区域
        contentTable = new Table();
        contentTable.top().left();

        // 商店区域（左侧）
        shopTable = createShopTable();
        contentTable.add(shopTable).width(Gdx.graphics.getWidth() * 0.45f).padRight(10).top();

        // 卡组区域（右侧）
        deckTable = createDeckTable();
        contentTable.add(deckTable).growX().top();

        rootTable.add(contentTable).grow().row();

        stage.addActor(rootTable);
    }

    /**
     * 创建标题栏
     */
    private Table createHeaderTable() {
        Table table = new Table();
        table.top().left().pad(10);

        // 背景色
//        table.setBackground(skin.getDrawable("default-pane"));

        // 创建按钮
        createHeaderButtons(table);

        return table;
    }

    /**
     * 创建标题栏按钮
     */
    private void createHeaderButtons(Table parentTable) {
        BitmapFont buttonFont = FontUtils.getDefaultFont();

        // 创建按钮样式
        TextButton.TextButtonStyle buttonStyle;
        if (skin.has("default", TextButton.TextButtonStyle.class)) {
            buttonStyle = new TextButton.TextButtonStyle(skin.get("default", TextButton.TextButtonStyle.class));
        } else {
            buttonStyle = new TextButton.TextButtonStyle();
            try {
                if (skin.has("button-normal", Drawable.class)) {
                    buttonStyle.up = skin.getDrawable("button-normal");
                }
                if (skin.has("button-normal-pressed", Drawable.class)) {
                    buttonStyle.down = skin.getDrawable("button-normal-pressed");
                }
                if (skin.has("button-normal-over", Drawable.class)) {
                    buttonStyle.over = skin.getDrawable("button-normal-over");
                }
            } catch (Exception e) {
                Gdx.app.debug("GameUIManager", "Could not load button drawables: " + e.getMessage());
            }
        }
        buttonStyle.font = buttonFont;
        buttonStyle.fontColor = Color.WHITE;

        // 返回按钮
        backButton = new TextButton(I18N.get("back"), buttonStyle);
        backButton.setSize(100, 40);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                // 由 GameScreen 处理返回操作
                onBackButtonClicked();
            }
        });
        parentTable.add(backButton).left();

        // 开始战斗按钮
        startBattleButton = new TextButton(I18N.get("start_battle"), buttonStyle);
        startBattleButton.setSize(120, 40);
        startBattleButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                onStartBattleButtonClicked();
            }
        });
        parentTable.add(startBattleButton).right();
    }

    /**
     * 创建商店区域
     */
    private Table createShopTable() {
        Table table = new Table();
        table.top().left().pad(10);

        // 标题在renderShopContent()方法中动态渲染，无需此处创建GlyphLayout

        // 使用 Label 显示标题（Scene2D 方式）
        table.top();

        return table;
    }

    /**
     * 创建卡组区域
     */
    private Table createDeckTable() {
        Table table = new Table();
        table.top().left().pad(10);
        return table;
    }

    /**
     * 设置游戏数据引用
     */
    public void setGameData(
            Battlefield battlefield,
            CardShop cardShop,
            PlayerDeck playerDeck,
            PlayerEconomy playerEconomy,
            SynergyManager synergyManager) {
        this.battlefield = battlefield;
        this.cardShop = cardShop;
        this.playerDeck = playerDeck;
        this.playerEconomy = playerEconomy;
        this.synergyManager = synergyManager;
    }

    /**
     * 更新拖拽状态（用于渲染预览）
     */
    public void updateDragState(Object draggingObject, float dragX, float dragY, boolean isDragging) {
        this.draggingObject = draggingObject;
        this.dragX = dragX;
        this.dragY = dragY;
        this.isDragging = isDragging;
    }

    /**
     * 渲染拖拽预览
     */
    public void renderDragPreview() {
        if (!isDragging || draggingObject == null) return;

        Gdx.gl20.glEnable(20);
        Gdx.gl20.glBlendFunc(770, 771);

        if (draggingObject instanceof Card) {
            renderCardPreview((Card) draggingObject);
        } else if (draggingObject instanceof BattleCharacter) {
            renderCharacterPreview((BattleCharacter) draggingObject);
        }

        Gdx.gl20.glDisable(20);
    }

    /**
     * 渲染卡牌预览
     */
    private void renderCardPreview(Card card) {
        // 使用 UI viewport
        game.getViewManagement().getUIViewport().apply();
        game.getBatch().setProjectionMatrix(game.getViewManagement().getUICamera().combined);
        shapeRenderer.setProjectionMatrix(game.getViewManagement().getUICamera().combined);

        game.getBatch().setColor(1, 1, 1, 0.5f);
        CardRenderer.render(shapeRenderer, game.getBatch(), card, dragX - 60, dragY - 80, 120, 160, false, false, 0, false);
        game.getBatch().setColor(1, 1, 1, 1);
    }

    /**
     * 渲染角色预览
     */
    private void renderCharacterPreview(BattleCharacter character) {
        // 使用游戏世界 viewport
        game.getViewManagement().getGameViewport().apply();
        game.getBatch().setProjectionMatrix(game.getViewManagement().getWorldCamera().combined);
        shapeRenderer.setProjectionMatrix(game.getViewManagement().getWorldCamera().combined);

        // 临时修改角色位置进行渲染
        float oldX = character.getX();
        float oldY = character.getY();
        character.setPosition(dragX, dragY);
        com.voidvvv.autochess.utils.CharacterRenderer.renderWithAlpha(shapeRenderer, character, 0.5f);
        character.setPosition(oldX, oldY);
    }

    /**
     * 更新 UI 内容
     */
    public void updateUI() {
        if (cardShop != null && refreshButton != null) {
            String refreshText = I18N.format("refresh_cost", cardShop.getRefreshCost());
            refreshButton.setText(refreshText);
        }

        // 更新卡组标题
        if (playerDeck != null) {
            String deckTitle = I18N.format("deck", playerDeck.getTotalCardCount());
            // TODO: 更新 deckTable 标题
        }
    }

    /**
     * 设置战斗按钮可见性
     */
    public void setBattleButtonVisible(boolean visible) {
        if (startBattleButton != null) {
            startBattleButton.setVisible(visible);
        }
    }

    /**
     * 获取 Stage
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * 更新 Stage
     */
    public void act(float delta) {
        stage.act(delta);
    }

    /**
     * 绘制 Stage
     */
    public void draw() {
        stage.draw();
    }

    /**
     * 渲染自定义 UI 内容（商店、卡组等非 Scene2D 部分）
     */
    public void renderCustomUI() {
        // 使用 UI viewport
        game.getViewManagement().getUIViewport().apply();
        game.getBatch().setProjectionMatrix(game.getViewManagement().getUICamera().combined);
        shapeRenderer.setProjectionMatrix(game.getViewManagement().getUICamera().combined);

        // 渲染标题和游戏信息
        renderHeaderInfo();

        // 渲染商店内容
        renderShopContent();

        // 渲染卡组内容
        renderDeckContent();
    }

    /**
     * 渲染标题信息
     */
    private void renderHeaderInfo() {
        game.getBatch().begin();
        titleFont.setColor(Color.WHITE);
        titleFont.getData().setScale(1.0f);

        String titleText = I18N.format("stage_level", level);
        tempGlyphLayout.setText(titleFont, titleText);
        float uiHeight = game.getViewManagement().getUIViewport().getWorldHeight();
        titleFont.draw(game.getBatch(), tempGlyphLayout, 50, uiHeight - 30);

        if (playerEconomy != null) {
            String infoText = playerEconomy.getEconomyInfoString();
            tempGlyphLayout.setText(titleFont, infoText);
            float uiWidth = game.getViewManagement().getUIViewport().getWorldWidth();
            titleFont.draw(game.getBatch(), tempGlyphLayout, uiWidth - tempGlyphLayout.width - 50, uiHeight - 30);
        }

        // 渲染羁绊信息
        if (synergyManager != null) {
            smallFont.setColor(Color.LIGHT_GRAY);
            smallFont.getData().setScale(0.8f);
            String synergyInfo = synergyManager.getSynergyInfoString();
            String[] lines = synergyInfo.split("\n");
            int maxLines = 3;
            float uiWidth = game.getViewManagement().getUIViewport().getWorldWidth();
            for (int i = 0; i < Math.min(lines.length, maxLines); i++) {
                tempGlyphLayout.setText(smallFont, lines[i]);
                smallFont.draw(game.getBatch(), tempGlyphLayout, uiWidth - tempGlyphLayout.width - 50, uiHeight - 60 - i * 20);
            }
        }

        game.getBatch().end();
    }

    /**
     * 渲染商店内容
     */
    private void renderShopContent() {
        if (cardShop == null) return;

        List<Card> shopCards = cardShop.getCurrentShopCards();

        // 渲染商店背景
        float shopX = 50;
        float shopY = 100;
        float shopWidth = game.getViewManagement().getUIViewport().getWorldWidth() * 0.45f;
        float shopHeight = 200;
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin();
        shapeRendererHelper.setShapeType(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1);
        shapeRenderer.rect(shopX, shopY, shopWidth, shopHeight);

        shapeRendererHelper.setShapeType(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(shopX, shopY, shopWidth, shopHeight);
        shapeRenderer.end();
        // 渲染商店标题
        game.getBatch().begin();
        BitmapFont font = FontUtils.getSmallFont();
        font.setColor(Color.YELLOW);
        font.getData().setScale(1.0f);
        tempGlyphLayout.setText(font, I18N.get("shop"));
        font.draw(game.getBatch(), tempGlyphLayout, shopX + 10, shopY + shopHeight - 10);
        game.getBatch().end();

        // 渲染卡牌
        float cardStartX = shopX + 20;
        float cardStartY = shopY + 30;
        float cardWidth = 120;
        float cardHeight = 160;
        float cardSpacing = 10;

        // 获取鼠标位置用于悬停检测
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();
        Vector2 uiCoords = game.getViewManagement().screenToUI(screenX, screenY);

        for (int i = 0; i < shopCards.size(); i++) {
            Card card = shopCards.get(i);
            float cardX = cardStartX + i * (cardWidth + cardSpacing);
            float cardY = cardStartY;
            boolean hover = uiCoords.x >= cardX && uiCoords.x <= cardX + cardWidth &&
                           uiCoords.y >= cardY && uiCoords.y <= cardY + cardHeight;
            CardRenderer.render(shapeRenderer, game.getBatch(), card, cardX, cardY, cardWidth, cardHeight, hover, false, 0, false);
        }
    }

    /**
     * 渲染卡组内容
     */
    private void renderDeckContent() {
        if (playerDeck == null) return;

        List<Card> ownedCards = playerDeck.getAllUniqueCards();

        // 渲染卡组背景
        float deckX = 50 + game.getViewManagement().getUIViewport().getWorldWidth() * 0.45f + 10;
        float deckY = 100;
        float deckWidth = game.getViewManagement().getUIViewport().getWorldWidth() - deckX - 50;
        float deckHeight = 250;
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin();
        shapeRendererHelper.setShapeType(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.2f, 0.25f, 1);
        shapeRenderer.rect(deckX, deckY, deckWidth, deckHeight);

        shapeRendererHelper.setShapeType(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(deckX, deckY, deckWidth, deckHeight);
        shapeRenderer.end();
        // 渲染标题
        game.getBatch().begin();
        BitmapFont font = FontUtils.getSmallFont();
        font.setColor(Color.CYAN);
        font.getData().setScale(1.0f);
        String deckTitle = I18N.format("deck", playerDeck.getTotalCardCount());
        tempGlyphLayout.setText(font, deckTitle);
        font.draw(game.getBatch(), tempGlyphLayout, deckX + 10, deckY + deckHeight - 10);
        game.getBatch().end();

        // 空卡组提示
        if (ownedCards.isEmpty()) {
            game.getBatch().begin();
            font.setColor(Color.GRAY);
            tempGlyphLayout.setText(font, I18N.get("deck_empty"));
            float emptyX = deckX + (deckWidth - tempGlyphLayout.width) / 2;
            float emptyY = deckY + deckHeight / 2;
            font.draw(game.getBatch(), tempGlyphLayout, emptyX, emptyY);
            game.getBatch().end();
            return;
        }

        // 渲染卡牌
        float cardStartX = deckX + 10;
        float cardStartY = deckY + deckHeight - 40;
        float deckCardWidth = 100;
        float deckCardHeight = 130;
        int cardsPerRow = (int) ((deckWidth - 20) / (deckCardWidth + 10));
        if (cardsPerRow < 1) cardsPerRow = 1;

        // 获取鼠标位置用于悬停检测
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();
        Vector2 uiCoords = game.getViewManagement().screenToUI(screenX, screenY);

        for (int i = 0; i < ownedCards.size(); i++) {
            Card card = ownedCards.get(i);
            int row = i / cardsPerRow;
            int col = i % cardsPerRow;

            float cardX = cardStartX + col * (deckCardWidth + 10);
            float cardY = cardStartY - row * (deckCardHeight + 10) - deckCardHeight;

            if (cardY < deckY + 10) continue;

            int cardCount = playerDeck.getCardCount(card);
            boolean hover = uiCoords.x >= cardX && uiCoords.x <= cardX + deckCardWidth &&
                           uiCoords.y >= cardY && uiCoords.y <= cardY + deckCardHeight;
            boolean upgradable = isCardUpgradable(card);
            CardRenderer.render(shapeRenderer, game.getBatch(), card, cardX, cardY, deckCardWidth, deckCardHeight, hover, true, cardCount, upgradable);
        }
    }

    /**
     * 检查卡牌是否可升级
     */
    private boolean isCardUpgradable(Card card) {
        return CardUpgradeLogic.canUpgradeCard(playerDeck, card);
    }

    /**
     * GameEventListener 接口实现 - 处理游戏事件
     */
    @Override
    public void onGameEvent(GameEvent event) {
        if (event instanceof DragMovedEvent) {
            DragMovedEvent dragMovedEvent = (DragMovedEvent) event;
            updateDragState(draggingObject, dragMovedEvent.getX(), dragMovedEvent.getY(), true);
        }
        // 未来可在此处处理其他事件类型（如拖拽结束、放置事件等）
    }

    /**
     * 处理返回按钮点击
     */
    private void onBackButtonClicked() {
        if (buttonCallback != null) {
            buttonCallback.onBackButtonClicked();
        }
    }

    /**
     * 处理开始战斗按钮点击
     */
    private void onStartBattleButtonClicked() {
        if (buttonCallback != null) {
            buttonCallback.onStartBattleButtonClicked();
        }
    }

    /**
     * 调整 UI 大小
     */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        // 重新计算布局参数
        rootTable.invalidate();
    }

    /**
     * 释放资源
     */
    public void dispose() {
        stage.dispose();
        if (skin != null) {
            skin.dispose();
        }
        shapeRenderer.dispose();
    }
}
