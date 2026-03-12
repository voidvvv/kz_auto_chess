package com.voidvvv.autochess.manage;

import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.event.GameEventListener;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.event.card.CardBuyEvent;
import com.voidvvv.autochess.event.card.CardSellEvent;
import com.voidvvv.autochess.event.card.CardUpgradeEvent;
import com.voidvvv.autochess.logic.CardUpgradeLogic;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.CardPool;
import com.voidvvv.autochess.model.CardShop;
import com.voidvvv.autochess.model.PlayerDeck;
import com.voidvvv.autochess.render.GameRenderer;
import com.voidvvv.autochess.render.RenderHolder;

import java.util.List;

/**
 * CardManager - 管理所有卡牌相关操作
 *
 * 职责:
 * - 卡池管理
 * - 商店刷新和卡牌购买 (纯卡牌操作，不涉及金币)
 * - 卡牌升级 (纯卡牌操作，不涉及金币)
 * - 卡组管理
 *
 * 设计决策:
 * - 金币操作由 EconomyManager 处理，协调由 AutoChessGameMode 完成
 * - 通过事件系统通信，不直接依赖其他 Manager
 */
public class CardManager implements GameRenderer, GameEventListener {

    // ========== Inner Classes ==========

    public class CardPoolManager {
        private final CardPool cardPool;
        private final CardShop cardShop;

        public CardPoolManager(CardPool cardPool, CardShop cardShop) {
            this.cardPool = cardPool;
            this.cardShop = cardShop;
        }

        public void refreshShop() {
            cardShop.refresh();
        }

        public List<Card> getShopCards() {
            return cardShop.getCurrentShopCards();
        }

        public int getRefreshCost() {
            return cardShop.getRefreshCost();
        }

        public void updateForPlayerLevel(int level) {
            cardShop.setPlayerLevel(level);
        }
    }

    public class CardTransactionManager {
        private final PlayerDeck playerDeck;
        private final CardShop cardShop;
        private final GameEventSystem eventSystem;

        public CardTransactionManager(PlayerDeck playerDeck, CardShop cardShop,
                                      GameEventSystem eventSystem) {
            this.playerDeck = playerDeck;
            this.cardShop = cardShop;
            this.eventSystem = eventSystem;
        }

        /**
         * 从商店购买一张卡牌（纯卡牌操作，金币由 EconomyManager 处理）
         */
        public boolean buyCard(Card card) {
            if (cardShop.buyCard(card)) {
                playerDeck.addCard(card);
                eventSystem.postEvent(new CardBuyEvent(card, card.getCost()));
                return true;
            }
            return false;
        }

        public boolean sellCard(Card card, int goldReceived) {
            if (playerDeck.getCardCount(card) > 0) {
                playerDeck.removeCard(card);
                eventSystem.postEvent(new CardSellEvent(card, goldReceived));
                return true;
            }
            return false;
        }

        public boolean canUpgrade(Card card) {
            return CardUpgradeLogic.canUpgradeCard(playerDeck, card);
        }

        /**
         * 升级一张卡牌（纯卡牌操作，金币由 EconomyManager 处理）
         */
        public boolean upgradeCard(Card card) {
            if (!canUpgrade(card)) return false;

            Card upgradedCard = CardUpgradeLogic.upgradeCard(playerDeck, card);
            if (upgradedCard != null) {
                eventSystem.postEvent(new CardUpgradeEvent(card, upgradedCard, 0));
                return true;
            }
            return false;
        }

        public List<Card> getUpgradableCards() {
            return CardUpgradeLogic.getUpgradableCards(playerDeck);
        }

        public int getCardCount(Card card) {
            return playerDeck.getCardCount(card);
        }

        public int getTotalCardCount() {
            return playerDeck.getTotalCardCount();
        }

        public List<Card> getAllUniqueCards() {
            return playerDeck.getAllUniqueCards();
        }
    }

    // ========== Main Class ==========

    private final GameEventSystem eventSystem;
    private final CardPool cardPool;
    private final CardShop cardShop;
    private final PlayerDeck playerDeck;

    private final CardPoolManager cardPoolManager;
    private final CardTransactionManager cardTransactionManager;

    public CardManager(GameEventSystem eventSystem,
                       CardPool cardPool,
                       CardShop cardShop,
                       PlayerDeck playerDeck) {
        this.eventSystem = eventSystem;
        this.cardPool = cardPool;
        this.cardShop = cardShop;
        this.playerDeck = playerDeck;

        this.cardPoolManager = new CardPoolManager(cardPool, cardShop);
        this.cardTransactionManager = new CardTransactionManager(playerDeck, cardShop, eventSystem);
    }

    // ========== Lifecycle ==========

    public void onEnter() {
        eventSystem.registerListener(this);
    }

    public void update(float delta) {}
    public void pause() {}
    public void resume() {}

    public void onExit() {
        eventSystem.unregisterListener(this);
    }

    public void dispose() {}

    // ========== Event Handling ==========

    @Override
    public void onGameEvent(GameEvent event) {
        // 未来可处理卡牌相关事件
    }

    // ========== Render (卡牌渲染由 GameUIManager 负责) ==========

    @Override
    public void render(RenderHolder holder) {}

    // ========== Public API ==========

    public void refreshShop() {
        cardPoolManager.refreshShop();
    }

    public boolean buyCard(Card card) {
        return cardTransactionManager.buyCard(card);
    }

    public boolean sellCard(Card card, int goldReceived) {
        return cardTransactionManager.sellCard(card, goldReceived);
    }

    public boolean canUpgrade(Card card) {
        return cardTransactionManager.canUpgrade(card);
    }

    public boolean upgradeCard(Card card) {
        return cardTransactionManager.upgradeCard(card);
    }

    public List<Card> getShopCards() {
        return cardPoolManager.getShopCards();
    }

    public List<Card> getAllUniqueCards() {
        return cardTransactionManager.getAllUniqueCards();
    }

    public int getCardCount(Card card) {
        return cardTransactionManager.getCardCount(card);
    }

    public int getTotalCardCount() {
        return cardTransactionManager.getTotalCardCount();
    }

    public List<Card> getUpgradableCards() {
        return cardTransactionManager.getUpgradableCards();
    }

    public int getRefreshCost() {
        return cardPoolManager.getRefreshCost();
    }

    public void updateForPlayerLevel(int level) {
        cardPoolManager.updateForPlayerLevel(level);
    }

    public CardShop getCardShop() {
        return cardShop;
    }

    public PlayerDeck getPlayerDeck() {
        return playerDeck;
    }
}
