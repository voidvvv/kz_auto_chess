package com.voidvvv.autochess.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.voidvvv.autochess.battle.BattleState;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.input.GameInputHandler;
import com.voidvvv.autochess.input.InputContext;
import com.voidvvv.autochess.manage.BattleManager;
import com.voidvvv.autochess.manage.CardManager;
import com.voidvvv.autochess.manage.CharacterEffectManager;
import com.voidvvv.autochess.manage.EconomyManager;
import com.voidvvv.autochess.manage.PlayerLifeManager;
import com.voidvvv.autochess.manage.SkillEffectManager;
import com.voidvvv.autochess.manage.SynergyPanelManager;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.GamePhase;
import com.voidvvv.autochess.render.RenderCoordinator;
import com.voidvvv.autochess.render.RenderHolder;

/**
 * AutoChess 游戏模式实现
 * 作为中央协调器，管理所有 Manager 之间的协作
 *
 * 设计决策:
 * - Manager 之间通过 GameEventSystem 通信
 * - 金币操作由 EconomyManager 处理，卡牌操作由 CardManager 处理
 * - AutoChessGameMode 负责协调跨 Manager 的复合操作（如买卡 = 扣金币 + 拿卡）
 * - 输入处理委托给 GameInputHandler
 */
public class AutoChessGameMode implements GameMode {

    private final BattleState battleState;
    private final BattleManager battleManager;
    private final EconomyManager economyManager;
    private final CardManager cardManager;
    private final PlayerLifeManager playerLifeManager;
    private final SkillEffectManager skillEffectManager;
    private final CharacterEffectManager characterEffectManager;
    private final SynergyPanelManager synergyPanelManager;
    private final RenderCoordinator renderCoordinator;
    private final GameEventSystem eventSystem;
    private final GameInputHandler inputHandler;
    private final com.voidvvv.autochess.utils.ViewManagement viewManagement;

    private int currentLevel = 1;
    private boolean isInitialized = false;

    public AutoChessGameMode(BattleState battleState,
                             BattleManager battleManager,
                             EconomyManager economyManager,
                             CardManager cardManager,
                             PlayerLifeManager playerLifeManager,
                             SynergyPanelManager synergyPanelManager,
                             RenderCoordinator renderCoordinator,
                             GameEventSystem eventSystem,
                             GameInputHandler inputHandler,
                             com.voidvvv.autochess.utils.ViewManagement viewManagement,
                             int level) {
        this.battleState = battleState;
        this.battleManager = battleManager;
        this.economyManager = economyManager;
        this.cardManager = cardManager;
        this.playerLifeManager = playerLifeManager;
        this.synergyPanelManager = synergyPanelManager;
        this.renderCoordinator = renderCoordinator;
        this.eventSystem = eventSystem;
        this.inputHandler = inputHandler;
        this.viewManagement = viewManagement;
        this.currentLevel = level;

        this.skillEffectManager = new SkillEffectManager(eventSystem, viewManagement);

        this.characterEffectManager = battleManager.getCharacterEffectManager();
    }

    @Override
    public void onEnter() {
        battleManager.onEnter();
        economyManager.onEnter();
        cardManager.onEnter();
        playerLifeManager.onEnter();
        synergyPanelManager.onEnter();
        skillEffectManager.onEnter();

        inputHandler.initialize(this);

        renderCoordinator.addRenderer(battleManager);
        // 羁绊面板移到 GameScreen 中单独渲染，确保在商店面板之上
        // renderCoordinator.addRenderer(synergyPanelManager);
        renderCoordinator.addRenderer(skillEffectManager);

        isInitialized = true;
        Gdx.app.log("AutoChessGameMode", "Game mode initialized");
    }

    @Override
    public void update(float delta) {
        eventSystem.dispatch();
        eventSystem.clear();

        battleManager.update(delta);
        economyManager.update(delta);
        cardManager.update(delta);
        synergyPanelManager.update(delta);
        skillEffectManager.update(delta, battleManager.getBattleTime());
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
        playerLifeManager.pause();
        synergyPanelManager.pause();
        inputHandler.pause();
    }

    @Override
    public void resume() {
        battleManager.resume();
        economyManager.resume();
        cardManager.resume();
        playerLifeManager.resume();
        synergyPanelManager.resume();
        inputHandler.resume();
    }

    @Override
    public void onExit() {
        battleManager.onExit();
        economyManager.onExit();
        cardManager.onExit();
        playerLifeManager.onExit();
        synergyPanelManager.onExit();
        skillEffectManager.onExit();
        inputHandler.onExit();

        isInitialized = false;
        Gdx.app.log("AutoChessGameMode", "Game mode exited");
    }

    @Override
    public void dispose() {
        battleManager.dispose();
        economyManager.dispose();
        cardManager.dispose();
        inputHandler.dispose();
    }

    // ========== 复合操作 API（协调多个 Manager）==========

    public void startBattle() {
        if (getPhase() == GamePhase.PLACEMENT) {
            battleManager.startBattle(currentLevel);
        }
    }

    /**
     * 买卡 = 扣金币(EconomyManager) + 卡牌入手(CardManager)
     */
    public boolean buyCard(Card card) {
        if (!economyManager.canAfford(card.getCost())) return false;
        if (cardManager.buyCard(card)) {
            economyManager.buyCard(card.getCost());
            return true;
        }
        return false;
    }

    /**
     * 刷新商店 = 扣金币(EconomyManager) + 刷新卡池(CardManager)
     */
    public boolean refreshShop() {
        int cost = cardManager.getRefreshCost();
        if (economyManager.payForRefresh(cost)) {
            cardManager.refreshShop();
            return true;
        }
        return false;
    }

    /**
     * 升级卡牌 = 扣金币(EconomyManager) + 合成(CardManager)
     */
    public boolean upgradeCard(Card card, int upgradeCost) {
        if (!economyManager.canAfford(upgradeCost)) return false;
        if (cardManager.upgradeCard(card)) {
            economyManager.payForUpgrade(upgradeCost);
            return true;
        }
        return false;
    }

    // ========== 委托操作 API ==========

    public BattleCharacter placeCharacter(Card card, float x, float y) {
        return battleManager.placeCharacter(card, x, y);
    }

    public boolean moveCharacter(BattleCharacter character, float x, float y) {
        return battleManager.moveCharacter(character, x, y);
    }

    public void removeCharacter(BattleCharacter character) {
        battleManager.removeCharacter(character);
    }

    public BattleCharacter getCharacterAt(float x, float y) {
        return battleManager.getCharacterAt(x, y);
    }

    public boolean battlefieldContains(float x, float y) {
        return battleManager.contains(x, y);
    }

    public boolean canAfford(int amount) {
        return economyManager.canAfford(amount);
    }

    public int getGold() {
        return economyManager.getGold();
    }

    // ========== Accessors ==========

    public BattleManager getBattleManager() { return battleManager; }
    public CardManager getCardManager() { return cardManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public BattleState getBattleState() { return battleState; }
    public CharacterEffectManager getCharacterEffectManager() { return characterEffectManager; }

    public GamePhase getPhase() {
        return battleState.getContext().getPhase();
    }

    public int getCurrentLevel() { return currentLevel; }
    public boolean isBattleActive() { return battleManager.isBattleActive(); }
    public boolean isInitialized() { return isInitialized; }
}
