package com.voidvvv.autochess.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡牌商店类，管理商店中的卡牌和刷新功能
 */
public class CardShop {
    private CardPool cardPool;
    private List<Card> currentShopCards;
    private int refreshCost;
    private int playerLevel;

    public CardShop(CardPool cardPool) {
        this.cardPool = cardPool;
        this.currentShopCards = new ArrayList<>();
        this.refreshCost = 2; // 刷新费用
        this.playerLevel = 1;
    }

    /**
     * 刷新商店卡牌
     */
    public void refresh() {
        // 根据玩家等级刷新卡牌
        currentShopCards = cardPool.getRandomCardsByLevel(5, playerLevel);
    }

    /**
     * 购买卡牌
     */
    public boolean buyCard(Card card) {
        if (currentShopCards.contains(card)) {
            currentShopCards.remove(card);
            return true;
        }
        return false;
    }

    /**
     * 获取当前商店中的卡牌
     */
    public List<Card> getCurrentShopCards() {
        return new ArrayList<>(currentShopCards);
    }

    /**
     * 获取刷新费用
     */
    public int getRefreshCost() {
        return refreshCost;
    }

    /**
     * 设置玩家等级
     */
    public void setPlayerLevel(int level) {
        this.playerLevel = level;
    }

    /**
     * 获取玩家等级
     */
    public int getPlayerLevel() {
        return playerLevel;
    }
}

