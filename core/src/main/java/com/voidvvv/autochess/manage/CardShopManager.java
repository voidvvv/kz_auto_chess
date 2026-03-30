package com.voidvvv.autochess.manage;

import com.badlogic.gdx.Gdx;
import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.event.GameEventListener;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.event.card.CardBuyEvent;
import com.voidvvv.autochess.event.card.CardSellEvent;
import com.voidvvv.autochess.event.economy.RefreshEvent;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.CardShop;
import com.voidvvv.autochess.model.PlayerDeck;
import com.voidvvv.autochess.model.PlayerEconomy;
import com.voidvvv.autochess.model.SharedCardPool;

import java.util.List;

/**
 * CardShopManager - 管理卡牌商店的所有操作
 *
 * 职责:
 * - 商店刷新（扣除金币并刷新卡牌列表）
 * - 购买卡牌（事务性：检查金币→扣除→移除→加入卡组→更新共享池）
 * - 出售卡牌（移除→给予金币→返还共享池）
 *
 * 设计原则:
 * - 事务性保证：购买/刷新失败时不扣除金币
 * - 日志输出：所有操作结果通过 log() 输出
 * - 事件驱动：操作成功后发送相应事件
 * - 职责单一：仅管理商店相关操作
 */
public class CardShopManager implements GameEventListener {

    // ========== Inner Classes ==========

    /**
     * ShopRefresher - 管理商店刷新操作
     */
    public class ShopRefresher {
        private final CardShop cardShop;
        private final PlayerEconomy playerEconomy;
        private final GameEventSystem eventSystem;

        public ShopRefresher(CardShop cardShop, PlayerEconomy playerEconomy, GameEventSystem eventSystem) {
            this.cardShop = cardShop;
            this.playerEconomy = playerEconomy;
            this.eventSystem = eventSystem;
        }

        /**
         * 刷新商店（事务性：金币不足则不扣金币）
         * @return true=刷新成功, false=刷新失败（金币不足）
         */
        public boolean refreshShop() {
            int cost = cardShop.getRefreshCost();

            // 检查金币
            if (playerEconomy.getGold() < cost) {
                log("CardShopManager", "刷新失败: 金币不足 (需要: " + cost + ", 当前: " + playerEconomy.getGold() + ")");
                return false;
            }

            // 扣除金币
            if (!playerEconomy.spendGold(cost)) {
                log("CardShopManager", "刷新失败: 金币扣除失败");
                return false;
            }

            // 刷新商店
            cardShop.refresh();

            // 发送事件
            eventSystem.postEvent(new RefreshEvent(cost));

            log("CardShopManager", "刷新成功 (花费: " + cost + " 金币)");
            return true;
        }

        /**
         * 获取当前商店中的卡牌列表
         */
        public List<Card> getShopCards() {
            return cardShop.getCurrentShopCards();
        }

        /**
         * 获取刷新费用
         */
        public int getRefreshCost() {
            return cardShop.getRefreshCost();
        }

        /**
         * 更新玩家等级（影响刷新概率）
         */
        public void updateForPlayerLevel(int level) {
            cardShop.setPlayerLevel(level);
        }
    }

    /**
     * ShopTransactionManager - 管理购买和出售交易
     */
    public class ShopTransactionManager {
        private final CardShop cardShop;
        private final PlayerEconomy playerEconomy;
        private final PlayerDeck playerDeck;
        private final SharedCardPool sharedCardPool;
        private final GameEventSystem eventSystem;

        public ShopTransactionManager(CardShop cardShop, PlayerEconomy playerEconomy,
                                      PlayerDeck playerDeck, SharedCardPool sharedCardPool,
                                      GameEventSystem eventSystem) {
            this.cardShop = cardShop;
            this.playerEconomy = playerEconomy;
            this.playerDeck = playerDeck;
            this.sharedCardPool = sharedCardPool;
            this.eventSystem = eventSystem;
        }

        /**
         * 购买卡牌（事务性：检查→扣金币→移除→加卡组→更新共享池→发送事件）
         * @param card 要购买的卡牌
         * @return true=购买成功, false=购买失败（金币不足/商店无该卡/操作失败）
         */
        public boolean buyCard(Card card) {
            // 1. 检查金币
            if (!canAfford(card)) {
                log("CardShopManager", "购买失败: 金币不足 (需要: " + card.getCost() + ", 当前: " + playerEconomy.getGold() + ")");
                return false;
            }

            // 2. 检查商店是否有该卡
            List<Card> shopCards = cardShop.getCurrentShopCards();
            if (!shopCards.contains(card)) {
                log("CardShopManager", "购买失败: 商店中没有该卡牌");
                return false;
            }

            // 3. 扣除金币（事务开始）
            if (!playerEconomy.spendGold(card.getCost())) {
                log("CardShopManager", "购买失败: 金币扣除失败");
                return false;
            }

            // 4. 从商店移除
            if (!cardShop.buyCard(card)) {
                // 回滚：返还金币
                playerEconomy.addGold(card.getCost());
                log("CardShopManager", "购买失败: 商店操作失败，已返还金币");
                return false;
            }

            // 5. 添加到玩家卡组
            playerDeck.addCard(card);

            // 6. 更新共享卡池
            if (sharedCardPool != null) {
                sharedCardPool.decrementCopies(card.getId());
            }

            // 7. 发送事件
            eventSystem.postEvent(new CardBuyEvent(card, card.getCost()));

            log("CardShopManager", "购买成功: " + card.getName() + " (花费: " + card.getCost() + " 金币)");
            return true;
        }

        /**
         * 出售卡牌
         * @param card 要出售的卡牌
         * @return true=出售成功, false=出售失败（玩家没有该卡）
         */
        public boolean sellCard(Card card) {
            // 1. 检查玩家是否拥有该卡
            int count = playerDeck.getCardCount(card);
            if (count <= 0) {
                log("CardShopManager", "出售失败: 玩家没有该卡牌");
                return false;
            }

            // 2. 从玩家卡组移除
            playerDeck.removeCard(card);

            // 3. 返还共享卡池
            if (sharedCardPool != null) {
                sharedCardPool.incrementCopies(card.getId());
            }

            // 4. 给予金币（出售价格为卡牌费用的 50%）
            int sellPrice = getSellPrice(card);
            playerEconomy.addGold(sellPrice);

            // 5. 发送事件
            eventSystem.postEvent(new CardSellEvent(card, sellPrice));

            log("CardShopManager", "出售成功: " + card.getName() + " (获得: " + sellPrice + " 金币)");
            return true;
        }

        /**
         * 检查是否买得起某张卡牌
         */
        public boolean canAfford(Card card) {
            return playerEconomy.getGold() >= card.getCost();
        }

        /**
         * 获取卡牌的出售价格（费用的 50%）
         */
        public int getSellPrice(Card card) {
            return card.getCost() / 2;
        }
    }

    // ========== Main Class ==========

    private final CardShop cardShop;

    /**
     * 日志辅助方法（支持单元测试环境）
     */
    private static void log(String tag, String message) {
        if (Gdx.app != null) {
            Gdx.app.log(tag, message);
        } else {
            // 单元测试环境：使用标准输出
            System.out.println("[" + tag + "] " + message);
        }
    }
    private final PlayerEconomy playerEconomy;
    private final PlayerDeck playerDeck;
    private final SharedCardPool sharedCardPool;
    private final GameEventSystem eventSystem;

    private final ShopRefresher shopRefresher;
    private final ShopTransactionManager shopTransactionManager;

    /**
     * 构造函数 - 依赖注入
     * @param cardShop 商店模型
     * @param playerEconomy 玩家经济（金币）
     * @param playerDeck 玩家卡组
     * @param sharedCardPool 共享卡池（可为 null）
     * @param eventSystem 事件系统
     */
    public CardShopManager(CardShop cardShop,
                           PlayerEconomy playerEconomy,
                           PlayerDeck playerDeck,
                           SharedCardPool sharedCardPool,
                           GameEventSystem eventSystem) {
        this.cardShop = cardShop;
        this.playerEconomy = playerEconomy;
        this.playerDeck = playerDeck;
        this.sharedCardPool = sharedCardPool;
        this.eventSystem = eventSystem;

        this.shopRefresher = new ShopRefresher(cardShop, playerEconomy, eventSystem);
        this.shopTransactionManager = new ShopTransactionManager(
            cardShop, playerEconomy, playerDeck, sharedCardPool, eventSystem
        );
    }

    // ========== Lifecycle ==========

    public void onEnter() {
        eventSystem.registerListener(this);
    }

    public void update(float delta) {
        // 商店管理是事件驱动的，无需每帧更新
    }

    public void pause() {
        // 无需暂停逻辑
    }

    public void resume() {
        // 无需恢复逻辑
    }

    public void onExit() {
        eventSystem.unregisterListener(this);
    }

    public void dispose() {
        // 无资源需要释放
    }

    // ========== Event Handling ==========

    @Override
    public void onGameEvent(GameEvent event) {
        // 未来可处理 PlayerLevelUpEvent 等事件
        // 例如：收到等级提升事件时更新商店刷新概率
    }

    // ========== Public API (委托给内部类) ==========

    /**
     * 刷新商店
     * @return true=刷新成功, false=刷新失败（金币不足）
     */
    public boolean refreshShop() {
        return shopRefresher.refreshShop();
    }

    /**
     * 购买卡牌
     * @param card 要购买的卡牌
     * @return true=购买成功, false=购买失败
     */
    public boolean buyCard(Card card) {
        return shopTransactionManager.buyCard(card);
    }

    /**
     * 出售卡牌
     * @param card 要出售的卡牌
     * @return true=出售成功, false=出售失败
     */
    public boolean sellCard(Card card) {
        return shopTransactionManager.sellCard(card);
    }

    /**
     * 获取当前商店中的卡牌列表
     */
    public List<Card> getShopCards() {
        return shopRefresher.getShopCards();
    }

    /**
     * 获取刷新费用
     */
    public int getRefreshCost() {
        return shopRefresher.getRefreshCost();
    }

    /**
     * 检查是否买得起某张卡牌
     */
    public boolean canAfford(Card card) {
        return shopTransactionManager.canAfford(card);
    }

    /**
     * 获取卡牌的出售价格
     */
    public int getSellPrice(Card card) {
        return shopTransactionManager.getSellPrice(card);
    }

    /**
     * 更新玩家等级（影响刷新概率）
     */
    public void updateForPlayerLevel(int level) {
        shopRefresher.updateForPlayerLevel(level);
    }

    /**
     * 检查商店是否为空
     */
    public boolean isShopEmpty() {
        return cardShop.getCurrentShopCards().isEmpty();
    }

    /**
     * 获取当前金币数量
     */
    public int getCurrentGold() {
        return playerEconomy.getGold();
    }

    // ========== Getters (可选，用于测试或调试) ==========

    public ShopRefresher getShopRefresher() {
        return shopRefresher;
    }

    public ShopTransactionManager getShopTransactionManager() {
        return shopTransactionManager;
    }

    public CardShop getCardShop() {
        return cardShop;
    }

    public PlayerEconomy getPlayerEconomy() {
        return playerEconomy;
    }

    public PlayerDeck getPlayerDeck() {
        return playerDeck;
    }
}
