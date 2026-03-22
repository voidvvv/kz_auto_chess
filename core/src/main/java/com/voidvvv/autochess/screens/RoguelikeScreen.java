package com.voidvvv.autochess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.voidvvv.autochess.KzAutoChess;
import com.voidvvv.autochess.battle.BattleContext;
import com.voidvvv.autochess.battle.BattleState;
import com.voidvvv.autochess.battle.PlayerLifeBlackboard;
import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.event.GameEventListener;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.game.RoguelikeGameMode;
import com.voidvvv.autochess.input.GameInputHandler;
import com.voidvvv.autochess.logic.CharacterStatsLoader;
import com.voidvvv.autochess.logic.SynergyManager;
import com.voidvvv.autochess.manage.BattleManager;
import com.voidvvv.autochess.manage.CardManager;
import com.voidvvv.autochess.manage.EconomyManager;
import com.voidvvv.autochess.manage.SynergyPanelManager;
import com.voidvvv.autochess.model.*;
import com.voidvvv.autochess.render.RenderCoordinator;
import com.voidvvv.autochess.ui.GameUIManager;
import com.voidvvv.autochess.utils.CameraController;
import com.voidvvv.autochess.utils.FontUtils;
import com.voidvvv.autochess.utils.I18N;
import com.voidvvv.autochess.utils.TiledAssetLoader;

/**
 * Roguelike 模式专用屏幕
 * 显示备战界面、关卡进度和事件选择
 */
public class RoguelikeScreen implements Screen {

    private final KzAutoChess game;

    // 共享数据模型
    private CardPool cardPool;
    private CardShop cardShop;
    private PlayerDeck playerDeck;
    private PlayerEconomy playerEconomy;
    private PlayerLifeBlackboard playerLifeBlackboard;
    private Battlefield battlefield;
    private SynergyManager synergyManager;
    private CharacterStatsLoader characterStatsLoader;

    // Roguelike 核心组件
    private RoguelikeGameMode roguelikeGameMode;
    private SynergyPanelManager synergyPanelManager;
    private GameUIManager gameUIManager;
    private GameInputHandler gameInputHandler;
    private RenderCoordinator renderCoordinator;
    private GameEventSystem gameEventSystem;

    // 渲染资源
    private ShapeRenderer shapeRenderer;
    private CameraController cameraController;
    private TiledMap tiledMap;

    // Roguelike 专用
    private SharedCardPool roguelikeSharedCardPool;  // 独立的卡池

    public RoguelikeScreen(KzAutoChess game) {
        this.game = game;
    }

    @Override
    public void show() {
        // 1. 初始化共享数据
        initGameData();

        // 2. 初始化渲染资源
        initRenderResources();

        // 3. 加载 Tiled 资源
        loadTiledResources();

        // 4. 组装架构
        assembleArchitecture();

        // 5. 配置输入
        setupInput();

        // 6. 启动 GameMode
        roguelikeGameMode.onEnter();

        Gdx.app.log("RoguelikeScreen", "Screen shown");
    }

    private void initGameData() {
        // 创建 Roguelike 专属的独立卡池
        roguelikeSharedCardPool = new SharedCardPool();

        cardPool = new CardPool();
        cardShop = new CardShop(cardPool);
        playerEconomy = new PlayerEconomy();
        cardShop.setPlayerLevel(playerEconomy.getPlayerLevel());

        // 使用独立的卡池
        roguelikeSharedCardPool.initialize(cardPool);
        cardPool.setSharedCardPool(roguelikeSharedCardPool);

        cardShop.refresh();
        playerDeck = new PlayerDeck();

        // 创建新的 PlayerLifeBlackboard（不跨游戏共享）
        playerLifeBlackboard = new PlayerLifeBlackboard();
        playerLifeBlackboard.setCurrentLevel(1);

        synergyManager = new SynergyManager();

        characterStatsLoader = new CharacterStatsLoader();
        characterStatsLoader.load("character_stats.json");

        float worldWidth = game.getViewManagement().getWorldSize().x;
        float worldHeight = game.getViewManagement().getWorldSize().y;
        float battlefieldWidth = worldWidth - 100;
        float battlefieldHeight = worldHeight * 0.5f;
        battlefield = new Battlefield(50, 50, battlefieldWidth, battlefieldHeight);
    }

    private void initRenderResources() {
        shapeRenderer = new ShapeRenderer();

        OrthographicCamera worldCamera = game.getViewManagement().getWorldCameraOrtho();
        float worldWidth = game.getViewManagement().getWorldSize().x;
        float worldHeight = game.getViewManagement().getWorldSize().y;
        cameraController = new CameraController(worldCamera, worldWidth, worldHeight);
    }

    private void assembleArchitecture() {
        // 事件系统
        gameEventSystem = new GameEventSystem();

        // BattleContext + BattleState
        BattleContext battleContext = new BattleContext.Builder()
                .setBattlefield(battlefield)
                .setPhase(GamePhase.PLACEMENT)
                .setPlayerEconomy(playerEconomy)
                .setSynergyManager(synergyManager)
                .setRoundNumber(1)
                .build();
        BattleState battleState = new BattleState(battleContext, gameEventSystem);

        // 各 Manager
        BattleManager battleManager = new BattleManager(
                game, battleState, gameEventSystem, cameraController,
                battlefield, cardPool, synergyManager, characterStatsLoader);

        EconomyManager economyManager = new EconomyManager(
                gameEventSystem, playerEconomy, cardShop);

        CardManager cardManager = new CardManager(
                gameEventSystem, cardPool, cardShop, playerDeck, roguelikeSharedCardPool);

        // 羁绊面板管理器
        synergyPanelManager = new SynergyPanelManager(
                gameEventSystem, synergyManager, game.getViewManagement(), game);

        // 渲染协调器
        renderCoordinator = new RenderCoordinator(game.getBatch(), shapeRenderer);

        // 输入处理器
        gameInputHandler = new GameInputHandler(game, gameEventSystem);

        // Roguelike GameMode 协调器
        roguelikeGameMode = new RoguelikeGameMode(
                battleState, battleManager, economyManager, cardManager,
                synergyPanelManager, renderCoordinator,
                gameEventSystem, gameInputHandler, game.getViewManagement());

        // UI 管理器（使用 RoguelikeButtonCallback 支持商店交互）
        gameUIManager = new GameUIManager(game, 1, new GameUIManager.RoguelikeButtonCallback() {
            @Override
            public void onBackButtonClicked() {
                roguelikeGameMode.onExit();
                game.setScreen(new StartScreen(game));
            }

            @Override
            public void onStartBattleButtonClicked() {
                Gdx.app.log("RoguelikeScreen", "Start battle button clicked, current phase: " + roguelikeGameMode.getPhase());
                if (roguelikeGameMode.getPhase() == GamePhase.PLACEMENT) {
                    roguelikeGameMode.startBattle();
                } else {
                    Gdx.app.log("RoguelikeScreen", "Cannot start battle: not in PLACEMENT phase");
                }
            }

            @Override
            public void onRefreshButtonClicked() {
                if (roguelikeGameMode.getPhase() == GamePhase.PLACEMENT) {
                    boolean success = roguelikeGameMode.refreshShop();
                    if (!success) {
                        Gdx.app.log("RoguelikeScreen", "刷新商店失败：金币不足");
                    }
                }
            }

            @Override
            public void onShopCardClicked(Card card) {
                if (roguelikeGameMode.getPhase() == GamePhase.PLACEMENT) {
                    boolean success = roguelikeGameMode.buyCard(card);
                    if (!success) {
                        Gdx.app.log("RoguelikeScreen", "购买卡牌失败：金币不足或卡池无货");
                    }
                }
            }
        });
        gameUIManager.setGameData(battlefield, cardShop, playerDeck, playerEconomy, playerLifeBlackboard, synergyManager, roguelikeSharedCardPool);
        gameEventSystem.registerListener(gameUIManager);

        // 将 UI 管理器注入到输入处理器
        gameInputHandler.setGameUIManager(gameUIManager);
    }

    private void setupInput() {
        InputMultiplexer multiplexer = new InputMultiplexer();

        // UI Stage 优先处理按钮点击
        multiplexer.addProcessor(gameUIManager.getStage());

        // GameInputHandler 处理游戏世界交互
        multiplexer.addProcessor(gameInputHandler);

        // 全局快捷键
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
                    roguelikeGameMode.onExit();
                    game.setScreen(new StartScreen(game));
                    return true;
                }
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                cameraController.setScrollDelta(-amountY);
                return true;
            }
        });

        Gdx.input.setInputProcessor(multiplexer);
    }

    private void loadTiledResources() {
        try {
            tiledMap = new TmxMapLoader().load("tiled/demo/2.tmx");
            if (tiledMap != null) {
                for (TiledMapTileSet tileSet : tiledMap.getTileSets()) {
                    if (tileSet != null) {
                        TiledAssetLoader.loadBaseCollision(tileSet);
                    }
                }
                Gdx.app.log("RoguelikeScreen", "Tiled resources loaded");
            }
        } catch (Exception e) {
            Gdx.app.error("RoguelikeScreen", "Failed to load Tiled resources: " + e.getMessage());
        }
    }

    // ========== Screen 生命周期 ==========

    @Override
    public void render(float delta) {
        // 1. GameMode 处理: 事件分发 → Manager 更新 → 世界渲染
        roguelikeGameMode.update(delta);
        roguelikeGameMode.render(renderCoordinator.getHolder());

        // 2. UI 渲染
        gameUIManager.act(delta);
        gameUIManager.renderCustomUI();
        gameUIManager.renderDragPreview();
        gameUIManager.draw();

        // 3. 渲染 Roguelike 专属 UI（关卡进度等）
        renderRoguelikeUI();

        // 4. 羁绊面板渲染
        synergyPanelManager.render(renderCoordinator.getHolder());
    }

    /**
     * 渲染 Roguelike 专属 UI
     */
    private void renderRoguelikeUI() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        game.getBatch().begin();

        // 关卡进度
        String stageText = String.format(I18N.get("stage_progress"),
                roguelikeGameMode.getStageManager().getCurrentStage(),
                roguelikeGameMode.getStageManager().getMaxStages());

        FontUtils.getLargeFont().setColor(com.badlogic.gdx.graphics.Color.WHITE);
        FontUtils.getLargeFont().draw(game.getBatch(), stageText,
                (screenWidth - new com.badlogic.gdx.graphics.g2d.GlyphLayout(FontUtils.getLargeFont(), stageText).width) / 2,
                screenHeight - 80);

        // 关卡类型
        StageType stageType = roguelikeGameMode.getStageManager().getStageType();
        String typeText = getStageTypeName(stageType);
        FontUtils.getDefaultFont().setColor(new com.badlogic.gdx.graphics.Color(0.8f, 0.8f, 1f, 1));
        FontUtils.getDefaultFont().draw(game.getBatch(), typeText,
                (screenWidth - new com.badlogic.gdx.graphics.g2d.GlyphLayout(FontUtils.getDefaultFont(), typeText).width) / 2,
                screenHeight - 130);

        game.getBatch().end();
    }

    private String getStageTypeName(StageType type) {
        switch (type) {
            case BOSS: return I18N.get("stage_type_boss");
            case EVENT: return I18N.get("stage_type_event");
            case NORMAL: default: return I18N.get("stage_type_normal");
        }
    }

    @Override
    public void resize(int width, int height) {
        game.getViewManagement().update(width, height);
        gameUIManager.resize(width, height);
    }

    @Override
    public void pause() {
        if (roguelikeGameMode != null) roguelikeGameMode.pause();
    }

    @Override
    public void resume() {
        if (roguelikeGameMode != null) roguelikeGameMode.resume();
    }

    @Override
    public void hide() {
        if (gameInputHandler != null) gameInputHandler.cancelDrag();
        if (roguelikeGameMode != null) roguelikeGameMode.onExit();
    }

    @Override
    public void dispose() {
        if (roguelikeGameMode != null) roguelikeGameMode.dispose();
        if (gameUIManager != null) gameUIManager.dispose();
        if (gameEventSystem != null) gameEventSystem.clear();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }

    // ========== Roguelike 专属访问器 ==========

    public RoguelikeGameMode getRoguelikeGameMode() {
        return roguelikeGameMode;
    }
}
