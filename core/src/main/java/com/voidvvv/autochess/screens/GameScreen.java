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
import com.voidvvv.autochess.event.PlayerDeathEvent;
import com.voidvvv.autochess.game.AutoChessGameMode;
import com.voidvvv.autochess.input.GameInputHandler;
import com.voidvvv.autochess.logic.CharacterStatsLoader;
import com.voidvvv.autochess.logic.SynergyManager;
import com.voidvvv.autochess.manage.BattleManager;
import com.voidvvv.autochess.manage.CardManager;
import com.voidvvv.autochess.manage.EconomyManager;
import com.voidvvv.autochess.manage.PlayerLifeManager;
import com.voidvvv.autochess.model.Battlefield;
import com.voidvvv.autochess.model.CardPool;
import com.voidvvv.autochess.model.CardShop;
import com.voidvvv.autochess.model.GamePhase;
import com.voidvvv.autochess.model.PlayerDeck;
import com.voidvvv.autochess.model.PlayerEconomy;
import com.voidvvv.autochess.render.RenderCoordinator;
import com.voidvvv.autochess.ui.GameUIManager;
import com.voidvvv.autochess.utils.CameraController;
import com.voidvvv.autochess.utils.TiledAssetLoader;

/**
 * 游戏运行界面（DDD 重构版）
 *
 * 职责简化为:
 * - 创建和组装所有 Manager
 * - 管理 Screen 生命周期
 * - 委托 update/render 给 AutoChessGameMode
 * - 委托 UI 给 GameUIManager
 * - 管理 InputMultiplexer
 * - 监听玩家死亡事件
 */
public class GameScreen implements Screen, GameUIManager.ButtonCallback, GameEventListener {

    private final KzAutoChess game;
    private final int level;

    // 共享数据模型（由 GameScreen 创建，注入到各 Manager）
    private CardPool cardPool;
    private CardShop cardShop;
    private PlayerDeck playerDeck;
    private PlayerEconomy playerEconomy;
    private PlayerLifeBlackboard playerLifeBlackboard;
    private Battlefield battlefield;
    private SynergyManager synergyManager;
    private CharacterStatsLoader characterStatsLoader;

    // 新架构核心
    private GameEventSystem gameEventSystem;
    private AutoChessGameMode gameMode;
    private PlayerLifeManager playerLifeManager;
    private GameUIManager gameUIManager;
    private GameInputHandler gameInputHandler;
    private RenderCoordinator renderCoordinator;

    // 渲染资源
    private ShapeRenderer shapeRenderer;
    private CameraController cameraController;
    private TiledMap tiledMap;

    public GameScreen(KzAutoChess game, int level) {
        this.game = game;
        this.level = level;
    }

    @Override
    public void show() {
        // 1. 初始化共享数据
        initGameData();

        // 2. 初始化渲染资源
        initRenderResources();

        // 3. 加载 Tiled 资源
        loadTiledResources();

        // 4. 组装新架构
        assembleArchitecture();

        // 5. 配置输入
        setupInput();

        // 6. 启动 GameMode
        gameMode.onEnter();

        // 7. 同步游戏阶段
        game.setGamePhase(GamePhase.PLACEMENT);

        Gdx.app.log("GameScreen", "Screen shown with new architecture");
    }

    private void initGameData() {
        cardPool = new CardPool();
        cardShop = new CardShop(cardPool);
        playerEconomy = new PlayerEconomy();
        cardShop.setPlayerLevel(playerEconomy.getPlayerLevel());
        cardShop.refresh();
        playerDeck = new PlayerDeck();
        // 使用全局的 PlayerLifeBlackboard（关卡间共享）
        playerLifeBlackboard = game.getPlayerLifeBlackboard();
        playerLifeBlackboard.setCurrentLevel(level);
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
                gameEventSystem, cardPool, cardShop, playerDeck);

        playerLifeManager = new PlayerLifeManager(
                playerLifeBlackboard, gameEventSystem);

        // 渲染协调器
        renderCoordinator = new RenderCoordinator(game.getBatch(), shapeRenderer);

        // 输入处理器
        gameInputHandler = new GameInputHandler(game, gameEventSystem);

        // GameMode 协调器
        gameMode = new AutoChessGameMode(
                battleState, battleManager, economyManager, cardManager,
                playerLifeManager, renderCoordinator, gameEventSystem, gameInputHandler, level);

        // UI 管理器
        gameUIManager = new GameUIManager(game, level, this);
        gameUIManager.setGameData(battlefield, cardShop, playerDeck, playerEconomy, playerLifeBlackboard, synergyManager);
        gameEventSystem.registerListener(gameUIManager);
        gameEventSystem.registerListener(this); // 注册 GameScreen 作为事件监听器

        // 将 UI 管理器注入到输入处理器
        gameInputHandler.setGameUIManager(gameUIManager);
    }

    private void setupInput() {
        InputMultiplexer multiplexer = new InputMultiplexer();

        // UI Stage 优先处理按钮点击
        multiplexer.addProcessor(gameUIManager.getStage());

        // GameInputHandler 处理游戏世界交互
        multiplexer.addProcessor(gameInputHandler);

        // 全局快捷键和滚轮
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
                    game.setScreen(new LevelSelectScreen(game));
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
                Gdx.app.log("GameScreen", "Tiled resources loaded");
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to load Tiled resources: " + e.getMessage());
        }
    }

    // ========== Screen 生命周期 ==========

    @Override
    public void render(float delta) {
        // 1. GameMode 处理: 事件分发 → Manager 更新 → 世界渲染
        gameMode.update(delta);
        gameMode.render(renderCoordinator.getHolder());

        // 2. UI 渲染（Scene2D + 自定义 UI，独立于 GameMode）
        gameUIManager.act(delta);
        gameUIManager.renderCustomUI();
        gameUIManager.renderDragPreview();
        gameUIManager.draw();
    }

    @Override
    public void resize(int width, int height) {
        game.getViewManagement().update(width, height);
        gameUIManager.resize(width, height);
    }

    @Override
    public void pause() {
        if (gameMode != null) gameMode.pause();
    }

    @Override
    public void resume() {
        if (gameMode != null) gameMode.resume();
    }

    @Override
    public void hide() {
        if (gameInputHandler != null) gameInputHandler.cancelDrag();
        if (gameMode != null) gameMode.onExit();
    }

    @Override
    public void dispose() {
        if (gameMode != null) gameMode.dispose();
        if (gameUIManager != null) gameUIManager.dispose();
        if (gameEventSystem != null) gameEventSystem.clear();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (tiledMap != null) tiledMap.dispose();
    }

    // ========== GameUIManager.ButtonCallback ==========

    @Override
    public void onBackButtonClicked() {
        game.setScreen(new LevelSelectScreen(game));
    }

    @Override
    public void onStartBattleButtonClicked() {
        if (gameMode.getPhase() != GamePhase.PLACEMENT) return;
        gameMode.startBattle();
        gameUIManager.setBattleButtonVisible(false);
    }

    // ========== GameEventListener ==========

    @Override
    public void onGameEvent(GameEvent event) {
        // 处理玩家死亡事件，跳转到游戏结束界面
        if (event instanceof PlayerDeathEvent) {
            PlayerDeathEvent deathEvent = (PlayerDeathEvent) event;
            game.setScreen(new GameOverScreen(game, deathEvent.getMaxReachedLevel()));
        }
        // 处理战斗结束事件，重新显示开始战斗按钮
        else if (event instanceof com.voidvvv.autochess.event.BattleEndEvent) {
            gameUIManager.setBattleButtonVisible(true);
        }
    }
}
