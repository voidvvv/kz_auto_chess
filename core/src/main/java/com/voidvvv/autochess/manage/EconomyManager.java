package com.voidvvv.autochess.manage;

import com.voidvvv.autochess.event.BattleEndEvent;
import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.event.GameEventListener;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.event.gold.GoldSpendEvent;
import com.voidvvv.autochess.event.economy.GoldEarnEvent;
import com.voidvvv.autochess.event.economy.RefreshEvent;
import com.voidvvv.autochess.logic.EconomyCalculator;
import com.voidvvv.autochess.model.CardShop;
import com.voidvvv.autochess.model.GamePhase;
import com.voidvvv.autochess.model.PlayerEconomy;
import com.voidvvv.autochess.model.PlayerDeck;

/**
 * EconomyManager - Manages all economy-related operations
 *
 * RESPONSIBILITIES:
 * - Gold operations (spend, earn, calculate interest)
 * - Shop refresh management
 * - Round reward calculation
 * - Battle button visibility management
 * - Event-driven communication with other managers
 *
 * DESIGN DECISIONS:
 * - Implements GameEventListener for event-driven communication
 * - Uses EconomyCalculator for economy logic
 * - No direct dependencies on other managers (communicate via events)
 */
public class EconomyManager implements GameEventListener {

    // ========== Inner Classes ==========

    /**
     * GoldManager - Manages gold operations
     * Handles gold spending, earning, and interest calculation
     */
    public class GoldManager {
        private final PlayerEconomy playerEconomy;
        private final GameEventSystem eventSystem;

        public GoldManager(PlayerEconomy playerEconomy, GameEventSystem eventSystem) {
            this.playerEconomy = playerEconomy;
            this.eventSystem = eventSystem;
        }

        /**
         * Check if player has enough gold
         */
        public boolean canSpend(int amount) {
            return playerEconomy.getGold() >= amount;
        }

        /**
         * Spend gold for a purchase
         * @param amount Amount to spend
         * @param reason Reason for spending (e.g., "buy_card", "refresh_shop")
         * @return true if successful, false if insufficient gold
         */
        public boolean spend(int amount, String reason) {
            if (!canSpend(amount)) {
                return false;
            }
            if (playerEconomy.spendGold(amount)) {
                eventSystem.postEvent(new GoldSpendEvent(amount, reason));
                return true;
            }
            return false;
        }

        /**
         * Add gold to player's balance
         * @param amount Amount to add
         * @param source Source of gold (e.g., "battle_reward", "interest", "level_up")
         */
        public void earn(int amount, String source) {
            if (amount > 0) {
                playerEconomy.addGold(amount);
                eventSystem.postEvent(new GoldEarnEvent(amount, source));
            }
        }

        /**
         * Get current gold balance
         */
        public int getCurrentGold() {
            return playerEconomy.getGold();
        }

        /**
         * Get current interest
         */
        public int getCurrentInterest() {
            return playerEconomy.getInterest();
        }

        /**
         * Get round income preview
         * @param assumeWin true to calculate for win, false for loss
         */
        public int getRoundIncomePreview(boolean assumeWin) {
            return EconomyCalculator.getRoundIncomePreview(playerEconomy, assumeWin);
        }
    }

    /**
     * RoundRewardCalculator - Calculates round end rewards
     * Handles win/loss rewards, interest, and streak bonuses
     */
    public class RoundRewardCalculator {
        private final PlayerEconomy playerEconomy;
        private final GameEventSystem eventSystem;

        public RoundRewardCalculator(PlayerEconomy playerEconomy, GameEventSystem eventSystem) {
            this.playerEconomy = playerEconomy;
            this.eventSystem = eventSystem;
        }

        /**
         * Calculate and apply round rewards
         * @param playerWon true if player won the battle
         */
        public void calculateAndApplyRewards(boolean playerWon) {
            int initialGold = playerEconomy.getGold();
            int initialInterest = playerEconomy.getInterest();

            // Apply round end calculations (this updates gold automatically)
            playerEconomy.endRound(playerWon);

            int finalGold = playerEconomy.getGold();
            int finalInterest = playerEconomy.getInterest();

            // Calculate gold earned
            int goldEarned = finalGold - initialGold;

            // Calculate interest earned
            int interestEarned = finalInterest - initialInterest;

            // Send events for each type of gold income
            if (goldEarned > initialInterest) {
                // Base income + win/loss rewards
                eventSystem.postEvent(new GoldEarnEvent(goldEarned - initialInterest, "round_base"));
            }
            if (interestEarned > 0) {
                // Interest income
                eventSystem.postEvent(new GoldEarnEvent(interestEarned, "interest"));
            }
        }

        /**
         * Get detailed round income breakdown
         * @param assumeWin true to calculate for win, false for loss
         */
        public RoundIncomeBreakdown getIncomeBreakdown(boolean assumeWin) {
            return new RoundIncomeBreakdown(
                playerEconomy,
                EconomyCalculator.getRoundIncomePreview(playerEconomy, assumeWin)
            );
        }
    }

    /**
     * Data class for round income breakdown
     */
    public static class RoundIncomeBreakdown {
        public final int baseIncome;
        public final int interest;
        public final int winStreakBonus;
        public final int loseStreakBonus;
        public final int total;

        public RoundIncomeBreakdown(PlayerEconomy economy, int totalIncome) {
            this.baseIncome = 5; // BASE_INCOME_PER_ROUND
            this.interest = economy.getInterest();
            // Streak bonuses depend on win/loss
            this.winStreakBonus = 0; // Simplified - actual calculation in EconomyCalculator
            this.loseStreakBonus = 0; // Simplified - actual calculation in EconomyCalculator
            this.total = totalIncome;
        }

        @Override
        public String toString() {
            return String.format("Base: %d, Interest: %d, Total: %d", baseIncome, interest, total);
        }
    }

    // ========== Main Class ==========

    private final GameEventSystem eventSystem;
    private final PlayerEconomy playerEconomy;
    private final CardShop cardShop;

    private final GoldManager goldManager;
    private final RoundRewardCalculator rewardCalculator;

    // Battle button visibility state
    private boolean battleButtonVisible = true;

    public EconomyManager(GameEventSystem eventSystem,
                        PlayerEconomy playerEconomy,
                        CardShop cardShop) {
        this.eventSystem = eventSystem;
        this.playerEconomy = playerEconomy;
        this.cardShop = cardShop;

        this.goldManager = new GoldManager(playerEconomy, eventSystem);
        this.rewardCalculator = new RoundRewardCalculator(playerEconomy, eventSystem);
    }

    // ========== Lifecycle Methods ==========

    public void onEnter() {
        // Register as event listener
        eventSystem.registerListener(this);
    }

    public void update(float delta) {
        // Economy updates are event-driven, no per-frame updates needed
    }

    public void pause() {
        // No specific pause logic needed
    }

    public void resume() {
        // No specific resume logic needed
    }

    public void onExit() {
        // Unregister event listener
        eventSystem.unregisterListener(this);
    }

    public void dispose() {
        // No resources to dispose
    }

    // ========== Event Handling ==========

    @Override
    public void onGameEvent(GameEvent event) {
        // Handle BattleEndEvent for round rewards
        if (event instanceof BattleEndEvent) {
            BattleEndEvent battleEndEvent = (BattleEndEvent) event;
            rewardCalculator.calculateAndApplyRewards(battleEndEvent.playerWon);

            // Show battle button after round ends
            setBattleButtonVisible(true);
        }
    }

    // ========== Public API ==========

    /**
     * Check if player can afford something
     */
    public boolean canAfford(int amount) {
        return goldManager.canSpend(amount);
    }

    /**
     * Buy a card from the shop
     */
    public boolean buyCard(int cost) {
        if (goldManager.spend(cost, "buy_card")) {
            // CardShop handles removing from shop and adding to deck
            // This is just the gold transaction part
            return true;
        }
        return false;
    }

    /**
     * 支付刷新商店的金币（纯金币操作，商店刷新由 CardManager 负责）
     */
    public boolean payForRefresh(int cost) {
        return goldManager.spend(cost, "refresh_shop");
    }

    /**
     * 支付升级卡牌的金币（纯金币操作，升级由 CardManager 负责）
     */
    public boolean payForUpgrade(int cost) {
        return goldManager.spend(cost, "upgrade_card");
    }

    /**
     * Get current gold
     */
    public int getGold() {
        return goldManager.getCurrentGold();
    }

    /**
     * Get current interest
     */
    public int getInterest() {
        return goldManager.getCurrentInterest();
    }

    /**
     * Get player level
     */
    public int getPlayerLevel() {
        return playerEconomy.getPlayerLevel();
    }

    /**
     * Get economy info string for display
     */
    public String getEconomyInfoString() {
        return playerEconomy.getEconomyInfoString();
    }

    /**
     * Try to level up player
     */
    public boolean tryLevelUp() {
        return playerEconomy.tryLevelUp();
    }

    /**
     * Get shop refresh cost
     */
    public int getRefreshCost() {
        return cardShop.getRefreshCost();
    }

    /**
     * Set battle button visibility
     */
    public void setBattleButtonVisible(boolean visible) {
        this.battleButtonVisible = visible;
    }

    /**
     * Check if battle button should be visible
     */
    public boolean isBattleButtonVisible() {
        return battleButtonVisible;
    }

    /**
     * Get the gold manager
     */
    public GoldManager getGoldManager() {
        return goldManager;
    }

    /**
     * Get the round reward calculator
     */
    public RoundRewardCalculator getRewardCalculator() {
        return rewardCalculator;
    }

    /**
     * Get round income preview
     */
    public int getRoundIncomePreview(boolean assumeWin) {
        return goldManager.getRoundIncomePreview(assumeWin);
    }
}
