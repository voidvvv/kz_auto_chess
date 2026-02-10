package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import io.github.some_example_name.KzAutoChess;
import io.github.some_example_name.battle.BattleUnitBlackboard;
import io.github.some_example_name.battle.UnitBehaviorTreeFactory;
import io.github.some_example_name.listener.damage.DamageRenderListener;
import io.github.some_example_name.listener.damage.DamageSettlementListener;
import io.github.some_example_name.model.*;
import io.github.some_example_name.render.BattleFieldRender;
import io.github.some_example_name.render.DamageLineRender;
import io.github.some_example_name.ui.CardRenderer;
import io.github.some_example_name.updater.BattleUpdater;
import io.github.some_example_name.updater.DamageRenderUpdater;
import io.github.some_example_name.utils.CharacterRenderer;
import io.github.some_example_name.utils.FontUtils;
import io.github.some_example_name.utils.CameraController;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏运行界面（重构版，使用Scene2D）
 * 包含选卡/购卡、刷新功能、战场和卡组管理
 */
public class GameScreen implements Screen {
    private KzAutoChess game;
    private int level;

    // 游戏数据
    private CardPool cardPool;
    private CardShop cardShop;
    private PlayerDeck playerDeck;
    private Battlefield battlefield;
    private BattleFieldRender battleFieldRender;

    // UI组件
    private Stage stage;
    private Skin skin;
    private ShapeRenderer shapeRenderer;
    private BitmapFont titleFont;
    private CameraController cameraController;

    // 游戏状态
    private int gold = 10;
    private int playerLevel = 1;
    private GamePhase phase = GamePhase.PLACEMENT;
    private float battleTime;
    private final List<BehaviorTree<BattleUnitBlackboard>> unitTrees = new ArrayList<>();

    // 拖拽状态
    private Card draggingCard;
    private BattleCharacter draggingCharacter;
    private float dragX, dragY;

    // UI布局
    private float shopAreaX = 50;
    private float shopAreaY = 50;
    private float shopAreaWidth;
    private float shopAreaHeight = 200;

    private float deckAreaX;
    private float deckAreaY;
    private float deckAreaWidth;
    private float deckAreaHeight = 250;

    private float battlefieldX = 50;
    private float battlefieldY = 280;
    private float battlefieldWidth;
    private float battlefieldHeight;

    private TextButton startBattleButton;

    // 战斗更新器
    private BattleUpdater battleUpdater;

    private ModelHolder<DamageShowModel> damageShowModelModelHolder;

    DamageRenderUpdater damageRenderUpdater;

    private DamageLineRender damageLineRender;

    public GameScreen(KzAutoChess game, int level) {
        this.game = game;
        this.level = level;
        // 初始化游戏数据
        this.cardPool = new CardPool();
        this.cardShop = new CardShop(cardPool);
        this.cardShop.setPlayerLevel(playerLevel);
        this.cardShop.refresh();
        this.playerDeck = new PlayerDeck();

        // 加载角色属性配置
        CharacterStats.Config.load();

        // 初始化UI
        // Stage使用UI viewport（用于按钮等UI元素）
        this.stage = new Stage(game.getViewManagement().getUIViewport());
        try {
            this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
            Gdx.app.log("GameScreen", "Skin loaded successfully");
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to load skin: " + e.getMessage());
            this.skin = new Skin();
        }
        this.shapeRenderer = new ShapeRenderer();
        this.titleFont = FontUtils.getDefaultFont();

        // 初始化相机控制器
        OrthographicCamera worldCamera = game.getViewManagement().getWorldCameraOrtho();
        float worldWidth = game.getViewManagement().getWorldSize().x;
        float worldHeight = game.getViewManagement().getWorldSize().y;
        this.cameraController = new CameraController(worldCamera, worldWidth, worldHeight);

        initUI();
        setupInput();

        // 验证字体是否正确加载
        Gdx.app.log("GameScreen", "Button font loaded: " + (FontUtils.getDefaultFont() != null));


        battleFieldRender = new BattleFieldRender(shapeRenderer,game);
        damageShowModelModelHolder = new ModelHolder<>();
    }

    private void initUI() {
        // UI元素使用UI viewport的尺寸
        float screenWidth = game.getViewManagement().getUIViewport().getWorldWidth();

        // 游戏世界尺寸（用于战场）
        float worldWidth = game.getViewManagement().getWorldSize().x;
        float worldHeight = game.getViewManagement().getWorldSize().y;

        // 商店区域：左侧上方（UI坐标）
        shopAreaWidth = screenWidth * 0.5f;

        // 卡组区域：右侧上方（UI坐标）
        deckAreaX = shopAreaX + shopAreaWidth + 20;
        deckAreaY = shopAreaY;
        deckAreaWidth = screenWidth - deckAreaX - 50;

        // 战场区域：占据下方主要位置（使用游戏世界坐标）
        battlefieldWidth = worldWidth - 100;
        battlefieldHeight = worldHeight * 0.5f; // 占据世界高度的一半
        battlefieldX = 50;
        battlefieldY = 50; // 从底部开始

        // 创建战场
        this.battlefield = new Battlefield(battlefieldX, battlefieldY, battlefieldWidth, battlefieldHeight);

        // 创建按钮
        createButtons();
    }

    private void createButtons() {
        // 获取支持中文的字体
        BitmapFont buttonFont = FontUtils.getDefaultFont();

        // 创建TextButton样式，基于skin的默认样式但使用我们的字体
        TextButton.TextButtonStyle buttonStyle;
        if (skin.has("default", TextButton.TextButtonStyle.class)) {
            buttonStyle = new TextButton.TextButtonStyle(skin.get("default", TextButton.TextButtonStyle.class));
        } else {
            buttonStyle = new TextButton.TextButtonStyle();
            // 尝试从skin获取drawable
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
                Gdx.app.debug("GameScreen", "Could not load button drawables: " + e.getMessage());
            }
        }
        // 替换字体为支持中文的字体
        buttonStyle.font = buttonFont;
        buttonStyle.fontColor = Color.WHITE;

        // 刷新按钮
        TextButton refreshButton = new TextButton("刷新 (" + cardShop.getRefreshCost() + "金币)", buttonStyle);
        refreshButton.setPosition(shopAreaX + shopAreaWidth - 120, shopAreaY + shopAreaHeight + 20);
        refreshButton.setSize(120, 40);
        // 确保按钮文字可见
        refreshButton.getLabel().setColor(Color.WHITE);
        refreshButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (gold >= cardShop.getRefreshCost()) {
                    gold -= cardShop.getRefreshCost();
                    cardShop.refresh();
                    refreshButton.setText("刷新 (" + cardShop.getRefreshCost() + "金币)");
                    refreshButton.getLabel().setColor(Color.WHITE);
                }
            }
        });
        stage.addActor(refreshButton);

        // 返回按钮
        TextButton backButton = new TextButton("返回", buttonStyle);
        float screenHeight = game.getViewManagement().getUIViewport().getWorldHeight();
        backButton.setPosition(50, screenHeight - 60);
        backButton.setSize(100, 40);
        backButton.getLabel().setColor(Color.WHITE);
        backButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new LevelSelectScreen(game));
            }
        });
        stage.addActor(backButton);

        // 开始战斗按钮（仅放置阶段可点）
        startBattleButton = new TextButton("开始战斗", buttonStyle);
        startBattleButton.setPosition(shopAreaX + shopAreaWidth - 260, shopAreaY + shopAreaHeight + 20);
        startBattleButton.setSize(120, 40);
        startBattleButton.getLabel().setColor(Color.WHITE);
        startBattleButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (phase != GamePhase.PLACEMENT) return;
                startBattle();
            }
        });
        stage.addActor(startBattleButton);
    }

    private void startBattle() {
        phase = GamePhase.BATTLE;
        battleTime = 0;
        unitTrees.clear();
        List<Integer> enemyIds = LevelEnemyConfig.getEnemyCardIdsForLevel(level);
        LevelEnemyConfig.spawnEnemiesInBattlefield(battlefield, enemyIds, cardPool);
        for (BattleCharacter c : battlefield.getCharacters()) {
            if (c.isDead()) continue;
            c.setNextAttackTime(0);
            c.setTarget(null);
            unitTrees.add(UnitBehaviorTreeFactory.create(c, battlefield));
        }
        if (startBattleButton != null) {
            startBattleButton.setVisible(false);
        }
    }

    private void endBattle() {
        phase = GamePhase.PLACEMENT;
        unitTrees.clear();
        List<BattleCharacter> toRemove = new ArrayList<>();
        for (BattleCharacter c : battlefield.getCharacters()) {
            if (c.isDead()) toRemove.add(c);
        }
        for (BattleCharacter c : toRemove) {
            battlefield.removeCharacter(c);
        }
        for (BattleCharacter c : battlefield.getCharacters()) {
            if (c.getStats() != null) {
                c.setCurrentHp(c.getStats().getHealth());
            }
            c.setTarget(null);
        }
        gold += 5;
        playerLevel = Math.min(5, playerLevel + 1);
        cardShop.setPlayerLevel(playerLevel);
        if (startBattleButton != null) {
            startBattleButton.setVisible(true);
        }
    }

    private void updateBattle(float delta) {
        battleTime += delta;
        for (BehaviorTree<BattleUnitBlackboard> tree : unitTrees) {
            BattleUnitBlackboard bb = tree.getObject();
            if (bb.getSelf().isDead()) continue;
            bb.setCurrentTime(battleTime);
            tree.step();
        }
        List<BattleCharacter> toRemove = new ArrayList<>();
        for (BattleCharacter c : battlefield.getCharacters()) {
            if (c.isDead()) toRemove.add(c);
        }
        for (BattleCharacter c : toRemove) {
            battlefield.removeCharacter(c);
        }
        unitTrees.removeIf(t -> t.getObject().getSelf().isDead());
        if (battlefield.getPlayerCharacters().isEmpty() || battlefield.getEnemyCharacters().isEmpty()) {
            endBattle();
        }
        battleUpdater.update(delta); // 通过damage event listener监听伤害事件

        damageRenderUpdater.update(delta);
    }

    private void setupInput() {
        // 创建输入多路复用器，同时处理Stage和相机控制器的输入
        InputMultiplexer multiplexer = new InputMultiplexer();

        // 添加Stage（处理UI按钮点击）
        multiplexer.addProcessor(stage);

        // 添加相机控制器的滚轮输入处理器
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                // 将滚轮事件传递给相机控制器
                cameraController.setScrollDelta(-amountY); // 反转Y轴，使向上滚动放大
                return true;
            }
        });

        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void show() {
        setupInput();
        battleUpdater = new BattleUpdater(battlefield.getDamageEventHolder(), battlefield.getDamageEventListenerHolder());
        this.damageShowModelModelHolder.clear();
        DamageRenderListener damageRenderListener = new DamageRenderListener(this.damageShowModelModelHolder);
        battlefield.getDamageEventListenerHolder().clear();
        battlefield.getDamageEventListenerHolder().addModel(damageRenderListener);
        battlefield.getDamageEventListenerHolder().addModel(new DamageSettlementListener());
        damageRenderUpdater = new DamageRenderUpdater(  this.damageShowModelModelHolder);
        damageLineRender = new DamageLineRender(this.damageShowModelModelHolder);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);



        stage.act(delta);
        cameraController.update(delta);

        if (phase == GamePhase.BATTLE) {
            updateBattle(delta);
        }

        handleInput();

        // 绘制游戏世界内容（战场和角色）- 使用游戏世界viewport
        drawWorldContent();

        // 绘制UI内容（商店、卡组、标题）- 使用UI viewport
        drawUIContent();

        // 绘制拖拽的卡牌/角色
        // 启用混合模式
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        drawDragging();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // 绘制UI（Scene2D）- 使用UI viewport
        stage.draw();
    }

    private void handleInput() {
        // 获取屏幕坐标
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();

        // 转换为UI坐标（用于商店、卡组）
        com.badlogic.gdx.math.Vector2 uiCoords = game.getViewManagement().screenToUI(screenX, screenY);

        // 转换为游戏世界坐标（用于战场）
        com.badlogic.gdx.math.Vector2 worldCoords = game.getViewManagement().screenToWorld(screenX, screenY);

        // 处理鼠标点击
        if (Gdx.input.justTouched()) {
            handleMouseClick(uiCoords.x, uiCoords.y, worldCoords.x, worldCoords.y);
        }

        // 更新拖拽位置（使用UI坐标用于UI元素，世界坐标用于战场）
        if (draggingCard != null) {
            dragX = uiCoords.x;
            dragY = uiCoords.y;
        }
        if (draggingCharacter != null) {
            dragX = worldCoords.x;
            dragY = worldCoords.y;
        }
    }

    private void handleMouseClick(float uiX, float uiY, float worldX, float worldY) {
        if (phase == GamePhase.BATTLE) {
            return;
        }
        if (draggingCharacter != null) {
            if (battlefield.contains(worldX, worldY)) {
                // 放置在战场上
                if (battlefield.moveCharacter(draggingCharacter, worldX, worldY)) {
                    draggingCharacter = null;
                }
            } else if (isInDeckArea(uiX, uiY)) {
                // 拖回卡组（使用UI坐标）
                playerDeck.addCard(draggingCharacter.getCard());
                battlefield.removeCharacter(draggingCharacter.getX(), draggingCharacter.getY());
                draggingCharacter = null;
            } else {
                // 取消拖拽
                draggingCharacter = null;
            }
            return;
        }

        // 如果正在拖拽卡牌，尝试放置（使用世界坐标）
        if (draggingCard != null) {
            if (battlefield.contains(worldX, worldY)) {
                // 在战场上放置角色
                CharacterStats stats = CharacterStats.Config.getStats(draggingCard.getId());
                if (stats != null && battlefield.placeCharacter(draggingCard, stats, worldX, worldY)) {
                    playerDeck.removeCard(draggingCard);
                    draggingCard = null;
                }
            } else {
                // 取消拖拽
                draggingCard = null;
            }
            return;
        }

        // 检查是否点击了战场上的角色（使用世界坐标）
        BattleCharacter character = battlefield.getCharacterAt(worldX, worldY);
        if (character != null) {
            draggingCharacter = character;
            return;
        }

        // 检查是否点击了卡组中的卡牌（使用UI坐标）
        Card clickedCard = getCardAtDeckPosition(uiX, uiY);
        if (clickedCard != null && playerDeck.getCardCount(clickedCard) > 0) {
            draggingCard = clickedCard;
            return;
        }

        // 检查是否点击了商店中的卡牌（使用UI坐标）
        Card shopCard = getCardAtShopPosition(uiX, uiY);
        if (shopCard != null) {
            if (gold >= shopCard.getCost()) {
                if (cardShop.buyCard(shopCard)) {
                    gold -= shopCard.getCost();
                    playerDeck.addCard(shopCard);
                }
            }
        }
    }

    /**
     * 绘制游戏世界内容（战场和角色）- 使用游戏世界viewport
     */
    private void drawWorldContent() {
        // 应用游戏世界viewport
        game.getViewManagement().getGameViewport().apply();
        game.getBatch().setProjectionMatrix(game.getViewManagement().getWorldCamera().combined);


        // 绘制战场
        drawBattlefield();
        shapeRenderer.end();
        damageLineRender.render(shapeRenderer, game.getBatch());
    }

    /**
     * 绘制UI内容（商店、卡组、标题）- 使用UI viewport
     */
    private void drawUIContent() {
        // 应用UI viewport
        game.getViewManagement().getUIViewport().apply();
        game.getBatch().setProjectionMatrix(game.getViewManagement().getUICamera().combined);
        shapeRenderer.setProjectionMatrix(game.getViewManagement().getUICamera().combined);

        // 绘制标题和游戏信息
        game.getBatch().begin();
        titleFont.setColor(Color.WHITE);
        titleFont.getData().setScale(1.0f);
        String titleText = "关卡 " + level + " - 自走棋";
        GlyphLayout titleLayout = new GlyphLayout(titleFont, titleText);
        float uiHeight = game.getViewManagement().getUIViewport().getWorldHeight();
        titleFont.draw(game.getBatch(), titleLayout, 50, uiHeight - 30);

        String infoText = "金币: " + gold + " | 等级: " + playerLevel;
        GlyphLayout infoLayout = new GlyphLayout(titleFont, infoText);
        float uiWidth = game.getViewManagement().getUIViewport().getWorldWidth();
        titleFont.draw(game.getBatch(), infoLayout, uiWidth - infoLayout.width - 50, uiHeight - 30);
        game.getBatch().end();

        // 绘制商店区域
        drawShopArea();

        // 绘制卡组区域
        drawDeckArea();
    }

    private void drawShopArea() {
        // 绘制商店背景
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1);
        shapeRenderer.rect(shopAreaX, shopAreaY, shopAreaWidth, shopAreaHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(shopAreaX, shopAreaY, shopAreaWidth, shopAreaHeight);
        shapeRenderer.end();

        // 绘制商店标题
        game.getBatch().begin();
        BitmapFont font = FontUtils.getSmallFont();
        font.setColor(Color.YELLOW);
        font.getData().setScale(1.0f);
        GlyphLayout titleLayout = new GlyphLayout(font, "商店");
        font.draw(game.getBatch(), titleLayout, shopAreaX + 10, shopAreaY + shopAreaHeight - 10);
        game.getBatch().end();

        // 绘制商店中的卡牌
        List<Card> shopCards = cardShop.getCurrentShopCards();
        float cardStartX = shopAreaX + 20;
        float cardStartY = shopAreaY + 30;
        float cardWidth = 120;
        float cardHeight = 160;
        float cardSpacing = 10;

        // 获取UI坐标用于悬停检测
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();
        com.badlogic.gdx.math.Vector2 uiCoords = game.getViewManagement().screenToUI(screenX, screenY);

        for (int i = 0; i < shopCards.size(); i++) {
            Card card = shopCards.get(i);
            float cardX = cardStartX + i * (cardWidth + cardSpacing);
            float cardY = cardStartY;
            boolean hover = uiCoords.x >= cardX && uiCoords.x <= cardX + cardWidth &&
                           uiCoords.y >= cardY && uiCoords.y <= cardY + cardHeight;
            CardRenderer.render(shapeRenderer, game.getBatch(), card, cardX, cardY, cardWidth, cardHeight, hover, false, 0);
        }
    }

    private void drawDeckArea() {
        // 绘制卡组背景
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.2f, 0.25f, 1);
        shapeRenderer.rect(deckAreaX, deckAreaY, deckAreaWidth, deckAreaHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(deckAreaX, deckAreaY, deckAreaWidth, deckAreaHeight);
        shapeRenderer.end();

        // 绘制标题
        game.getBatch().begin();
        BitmapFont font = FontUtils.getSmallFont();
        font.setColor(Color.CYAN);
        font.getData().setScale(1.0f);
        String deckTitle = "我的卡组 (" + playerDeck.getTotalCardCount() + " 张)";
        GlyphLayout titleLayout = new GlyphLayout(font, deckTitle);
        font.draw(game.getBatch(), titleLayout, deckAreaX + 10, deckAreaY + deckAreaHeight - 10);
        game.getBatch().end();

        // 绘制卡组中的卡牌
        List<Card> ownedCards = playerDeck.getAllUniqueCards();
        if (ownedCards.isEmpty()) {
            game.getBatch().begin();
            font.setColor(Color.GRAY);
            GlyphLayout emptyLayout = new GlyphLayout(font, "暂无卡牌\n在商店购买卡牌");
            float emptyX = deckAreaX + (deckAreaWidth - emptyLayout.width) / 2;
            float emptyY = deckAreaY + deckAreaHeight / 2;
            font.draw(game.getBatch(), emptyLayout, emptyX, emptyY);
            game.getBatch().end();
            return;
        }

        float cardStartX = deckAreaX + 10;
        float cardStartY = deckAreaY + deckAreaHeight - 40;
        float deckCardWidth = 100;
        float deckCardHeight = 130;
        int cardsPerRow = (int) ((deckAreaWidth - 20) / (deckCardWidth + 10));
        if (cardsPerRow < 1) cardsPerRow = 1;

        // 获取UI坐标用于悬停检测
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();
        com.badlogic.gdx.math.Vector2 uiCoords = game.getViewManagement().screenToUI(screenX, screenY);

        for (int i = 0; i < ownedCards.size(); i++) {
            Card card = ownedCards.get(i);
            int row = i / cardsPerRow;
            int col = i % cardsPerRow;

            float cardX = cardStartX + col * (deckCardWidth + 10);
            float cardY = cardStartY - row * (deckCardHeight + 10) - deckCardHeight;

            if (cardY < deckAreaY + 10) continue;

            int cardCount = playerDeck.getCardCount(card);
            boolean hover = uiCoords.x >= cardX && uiCoords.x <= cardX + deckCardWidth &&
                           uiCoords.y >= cardY && uiCoords.y <= cardY + deckCardHeight;
            CardRenderer.render(shapeRenderer, game.getBatch(), card, cardX, cardY, deckCardWidth, deckCardHeight, hover, true, cardCount);
        }
    }

    private void drawBattlefield() {
        battleFieldRender.render( battlefield);
    }

    private void drawDragging() {
        if (draggingCard != null) {
            // 绘制半透明的卡牌（使用UI viewport）
            game.getViewManagement().getUIViewport().apply();
            game.getBatch().setProjectionMatrix(game.getViewManagement().getUICamera().combined);
            shapeRenderer.setProjectionMatrix(game.getViewManagement().getUICamera().combined);

            game.getBatch().setColor(1, 1, 1, 0.5f);
            CardRenderer.render(shapeRenderer, game.getBatch(), draggingCard, dragX - 60, dragY - 80, 120, 160, false, false, 0);
            game.getBatch().setColor(1, 1, 1, 1);
        }

        if (draggingCharacter != null) {
            // 绘制半透明的角色（使用游戏世界viewport）
            game.getViewManagement().getGameViewport().apply();
            game.getBatch().setProjectionMatrix(game.getViewManagement().getWorldCamera().combined);
            shapeRenderer.setProjectionMatrix(game.getViewManagement().getWorldCamera().combined);

            // 临时修改角色位置进行渲染
            float oldX = draggingCharacter.getX();
            float oldY = draggingCharacter.getY();
            draggingCharacter.setPosition(dragX, dragY);
            CharacterRenderer.renderWithAlpha(shapeRenderer, draggingCharacter, 0.5f);
            draggingCharacter.setPosition(oldX, oldY);
        }
    }

    private boolean isInDeckArea(float x, float y) {
        return x >= deckAreaX && x <= deckAreaX + deckAreaWidth &&
               y >= deckAreaY && y <= deckAreaY + deckAreaHeight;
    }

    private Card getCardAtDeckPosition(float x, float y) {
        List<Card> ownedCards = playerDeck.getAllUniqueCards();
        float cardStartX = deckAreaX + 10;
        float cardStartY = deckAreaY + deckAreaHeight - 40;
        float deckCardWidth = 100;
        float deckCardHeight = 130;
        int cardsPerRow = (int) ((deckAreaWidth - 20) / (deckCardWidth + 10));
        if (cardsPerRow < 1) cardsPerRow = 1;

        for (int i = 0; i < ownedCards.size(); i++) {
            int row = i / cardsPerRow;
            int col = i % cardsPerRow;
            float cardX = cardStartX + col * (deckCardWidth + 10);
            float cardY = cardStartY - row * (deckCardHeight + 10) - deckCardHeight;

            if (x >= cardX && x <= cardX + deckCardWidth &&
                y >= cardY && y <= cardY + deckCardHeight) {
                return ownedCards.get(i);
            }
        }
        return null;
    }

    private Card getCardAtShopPosition(float x, float y) {
        List<Card> shopCards = cardShop.getCurrentShopCards();
        float cardStartX = shopAreaX + 20;
        float cardStartY = shopAreaY + 30;
        float cardWidth = 120;
        float cardHeight = 160;
        float cardSpacing = 10;

        for (int i = 0; i < shopCards.size(); i++) {
            float cardX = cardStartX + i * (cardWidth + cardSpacing);
            float cardY = cardStartY;

            if (x >= cardX && x <= cardX + cardWidth &&
                y >= cardY && y <= cardY + cardHeight) {
                return shopCards.get(i);
            }
        }
        return null;
    }

    @Override
    public void resize(int width, int height) {
        // 更新两个viewport
        game.getViewManagement().update(width, height);
        stage.getViewport().update(width, height, true);
//        initUI();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (skin != null) {
            skin.dispose();
        }
        shapeRenderer.dispose();
    }
}
