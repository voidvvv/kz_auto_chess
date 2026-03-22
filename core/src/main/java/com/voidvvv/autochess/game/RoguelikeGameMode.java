package com.voidvvv.autochess.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.voidvvv.autochess.battle.BattleState;
import com.voidvvv.autochess.event.BattleEndEvent;
import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.event.GameEventListener;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.input.GameInputHandler;
import com.voidvvv.autochess.input.InputContext;
import com.voidvvv.autochess.logic.CharacterStatsLoader;
import com.voidvvv.autochess.manage.BattleManager;
import com.voidvvv.autochess.manage.CardManager;
import com.voidvvv.autochess.manage.EconomyManager;
import com.voidvvv.autochess.manage.StageManager;
import com.voidvvv.autochess.manage.SynergyPanelManager;
import com.voidvvv.autochess.model.*;
import com.voidvvv.autochess.render.RenderCoordinator;
import com.voidvvv.autochess.render.RenderHolder;
import com.voidvvv.autochess.utils.ViewManagement;

/**
 * Roguelike 游戏模式实现
 * 作为 Roguelike 闯关模式的中央协调器
 */
public class RoguelikeGameMode implements GameMode, GameEventListener {

    private final BattleState battleState;
    private final BattleManager battleManager;
    private final EconomyManager economyManager;
    private final CardManager cardManager;
    private final SynergyPanelManager synergyPanelManager;
    private final RenderCoordinator renderCoordinator;
    private final GameEventSystem eventSystem;
    private final GameInputHandler inputHandler;
    private final ViewManagement viewManagement;

    // Roguelike 专属组件
    private final StageManager stageManager;
    private final RoguelikeConfig config;
    private final RoguelikeEnemyPool enemyPool;
    private final SharedCardPool sharedCardPool;
    private final CardPool cardPool;
    private final CharacterStatsLoader statsLoader;

    private boolean isInitialized = false;
    private boolean waitingForEventChoice = false;
    private long battleEndTime = 0;  // 记录战斗结束时间，防止自动开始

    private static final long BATTLE_COOLDOWN_MS = 500;  // 战斗结束后冷却时间（毫秒）

    public RoguelikeGameMode(
            BattleState battleState,
            BattleManager battleManager,
            EconomyManager economyManager,
            CardManager cardManager,
            SynergyPanelManager synergyPanelManager,
            RenderCoordinator renderCoordinator,
            GameEventSystem eventSystem,
            GameInputHandler inputHandler,
            ViewManagement viewManagement) {
        this.battleState = battleState;
        this.battleManager = battleManager;
        this.economyManager = economyManager;
        this.cardManager = cardManager;
        this.synergyPanelManager = synergyPanelManager;
        this.renderCoordinator = renderCoordinator;
        this.eventSystem = eventSystem;
        this.inputHandler = inputHandler;
        this.viewManagement = viewManagement;

        // 加载配置
        this.config = RoguelikeConfig.loadFromJson("roguelike_config.json");
        this.stageManager = new StageManager(config);

        // 创建独立的卡池
        this.sharedCardPool = new SharedCardPool();
        this.cardPool = new CardPool();
        this.cardPool.setSharedCardPool(sharedCardPool);

        // 创建敌人池
        this.enemyPool = new RoguelikeEnemyPool();
        this.enemyPool.loadFromJson("roguelike_enemies.json");

        // 加载角色属性
        this.statsLoader = new CharacterStatsLoader();
    }

    @Override
    public void onEnter() {
        // 初始化经济系统（使用配置的初始金币）
        economyManager.getGoldManager().earn(config.getInitialGold(), "initial_gold");

        // 设置卡池
        cardManager.setCardPool(cardPool);

        // 刷新商店
        cardManager.getCardPoolManager().refreshShop();

        battleManager.onEnter();
        economyManager.onEnter();
        cardManager.onEnter();
        synergyPanelManager.onEnter();

        inputHandler.initialize(this);

        renderCoordinator.addRenderer(battleManager);
        renderCoordinator.addRenderer(synergyPanelManager);

        eventSystem.registerListener(this);

        isInitialized = true;
        Gdx.app.log("RoguelikeGameMode", "Game mode initialized - Stage " + stageManager.getCurrentStage());
    }

    @Override
    public void update(float delta) {
        if (waitingForEventChoice) {
            // 等待玩家选择事件选项时，只更新事件系统
            eventSystem.dispatch();
            eventSystem.clear();
            return;
        }

        eventSystem.dispatch();
        eventSystem.clear();

        battleManager.update(delta);
        economyManager.update(delta);
        cardManager.update(delta);
        synergyPanelManager.update(delta);
        inputHandler.update(delta);
    }

    @Override
    public void render(RenderHolder holder) {
        Gdx.gl.glClearColor(0.05f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderCoordinator.renderAll();
    }

    @Override
    public void handleInput(InputContext context) {
        inputHandler.handleInput(context);
    }

    @Override
    public void pause() {
        battleManager.pause();
        economyManager.pause();
        cardManager.pause();
        synergyPanelManager.pause();
        inputHandler.pause();
    }

    @Override
    public void resume() {
        battleManager.resume();
        economyManager.resume();
        cardManager.resume();
        synergyPanelManager.resume();
        inputHandler.resume();
    }

    @Override
    public void onExit() {
        eventSystem.unregisterListener(this);

        battleManager.onExit();
        economyManager.onExit();
        cardManager.onExit();
        synergyPanelManager.onExit();
        inputHandler.onExit();

        isInitialized = false;
        Gdx.app.log("RoguelikeGameMode", "Game mode exited");
    }

    @Override
    public void dispose() {
        battleManager.dispose();
        economyManager.dispose();
        cardManager.dispose();
        inputHandler.dispose();
    }

    @Override
    public void onGameEvent(GameEvent event) {
        Gdx.app.log("RoguelikeGameMode", "Received event: " + event.getClass().getSimpleName());
        if (event instanceof BattleEndEvent) {
            handleBattleEnd((BattleEndEvent) event);
        }
    }

    /**
     * 处理战斗结束事件
     */
    private void handleBattleEnd(BattleEndEvent event) {
        int oldStage = stageManager.getCurrentStage();

        // 记录战斗结束时间，防止自动开始新战斗
        battleEndTime = System.currentTimeMillis();

        if (event.playerWon) {
            // 玩家胜利
            StageType stageType = stageManager.getStageType();

            if (stageType == StageType.EVENT) {
                // 事件关：触发随机事件
                waitingForEventChoice = true;
                Gdx.app.log("RoguelikeGameMode", "Event stage " + oldStage + " completed, triggering event");
                // TODO: 触发事件选择界面
            } else {
                // 普通关/Boss关：给予奖励并进入下一关
                int reward = stageManager.getCurrentStageReward();
                economyManager.getGoldManager().earn(reward, "stage_reward");
                stageManager.nextStage();
                int newStage = stageManager.getCurrentStage();
                Gdx.app.log("RoguelikeGameMode", "Stage " + oldStage + " completed, reward: " + reward + " gold, now at stage " + newStage);
            }
        } else {
            // 玩家失败：游戏结束
            Gdx.app.log("RoguelikeGameMode", "Player defeated at stage " + stageManager.getCurrentStage());
            // TODO: 跳转到游戏结束界面
        }
    }

    // ========== Roguelike 专属 API ==========

    /**
     * 开始战斗（使用 Roguelike 敌人池）
     */
    public void startBattle() {
        if (battleState.getContext().getPhase() != GamePhase.PLACEMENT) {
            Gdx.app.log("RoguelikeGameMode", "Cannot start battle: current phase is " + battleState.getContext().getPhase());
            return;
        }

        // 防止战斗结束后立即自动开始（需要等待冷却时间）
        long timeSinceBattleEnd = System.currentTimeMillis() - battleEndTime;
        if (timeSinceBattleEnd < BATTLE_COOLDOWN_MS && battleEndTime > 0) {
            Gdx.app.log("RoguelikeGameMode", "Ignoring automatic battle start request (cooldown: " + timeSinceBattleEnd + "ms < " + BATTLE_COOLDOWN_MS + "ms)");
            return;
        }

        // 重置战斗结束时间（允许手动开始战斗）
        battleEndTime = 0;

        int currentStage = stageManager.getCurrentStage();
        StageType stageType = stageManager.getStageType();

        Gdx.app.log("RoguelikeGameMode", "Starting battle at stage " + currentStage + ", type: " + stageType);

        // 从敌人池获取敌人 ID 列表
        java.util.List<Integer> enemyIds = enemyPool.getEnemyCardIds(
                currentStage,
                stageType,
                config.getStatScaling()
        );

        Gdx.app.log("RoguelikeGameMode", "Generated " + enemyIds.size() + " enemies: " + enemyIds);

        // 使用自定义敌人列表开始战斗
        startBattleWithEnemies(enemyIds);
    }

    /**
     * 使用指定的敌人列表开始战斗
     */
    private void startBattleWithEnemies(java.util.List<Integer> enemyIds) {
        int currentStage = stageManager.getCurrentStage();
        float statScaling = config.getStatScaling();
        battleManager.getBattlePhaseManager().startBattleWithEnemies(enemyIds, currentStage, statScaling);
    }

    // ========== Accessors ==========

    public StageManager getStageManager() { return stageManager; }
    public RoguelikeConfig getConfig() { return config; }
    public RoguelikeEnemyPool getEnemyPool() { return enemyPool; }
    public BattleManager getBattleManager() { return battleManager; }
    public CardManager getCardManager() { return cardManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public BattleState getBattleState() { return battleState; }
    public GamePhase getPhase() { return battleState.getContext().getPhase(); }
    public boolean isInitialized() { return isInitialized; }
    public boolean isWaitingForEventChoice() { return waitingForEventChoice; }

    /**
     * 复合操作：买卡
     */
    public boolean buyCard(Card card) {
        if (!economyManager.getGoldManager().canSpend(card.getCost())) return false;
        if (cardManager.getCardTransactionManager().buyCard(card)) {
            economyManager.getGoldManager().spend(card.getCost(), "buy_card");
            return true;
        }
        return false;
    }

    /**
     * 复合操作：刷新商店
     */
    public boolean refreshShop() {
        int cost = cardManager.getCardPoolManager().getRefreshCost();
        if (economyManager.getGoldManager().spend(cost, "refresh_shop")) {
            cardManager.getCardPoolManager().refreshShop();
            return true;
        }
        return false;
    }
}
