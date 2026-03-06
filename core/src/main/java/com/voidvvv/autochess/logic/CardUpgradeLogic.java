package com.voidvvv.autochess.logic;

import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.PlayerDeck;
import java.util.List;

/**
 * 卡牌升级逻辑工具类
 * 提供静态方法处理卡牌升级相关逻辑
 */
public class CardUpgradeLogic {

    /**
     * 检查卡牌是否可以升级
     * @param deck 玩家卡组
     * @param card 要检查的卡牌
     * @return 是否可以升级
     */
    public static boolean canUpgradeCard(PlayerDeck deck, Card card) {
        if (deck == null || card == null || !card.canUpgrade()) {
            return false;
        }
        return deck.canUpgradeCard(card);
    }

    /**
     * 升级卡牌
     * @param deck 玩家卡组
     * @param card 要升级的卡牌
     * @return 升级后的卡牌，如果无法升级则返回null
     */
    public static Card upgradeCard(PlayerDeck deck, Card card) {
        if (deck == null || card == null) {
            return null;
        }
        return deck.upgradeCard(card);
    }

    /**
     * 批量检查卡牌升级
     * @param deck 玩家卡组
     * @param cards 要检查的卡牌列表
     * @return 可升级的卡牌列表
     */
    public static List<Card> findUpgradableCards(PlayerDeck deck, List<Card> cards) {
        if (deck == null || cards == null) {
            return new java.util.ArrayList<>();
        }
        List<Card> upgradable = new java.util.ArrayList<>();
        for (Card card : cards) {
            if (canUpgradeCard(deck, card)) {
                upgradable.add(card);
            }
        }
        return upgradable;
    }

    /**
     * 自动升级所有可升级的卡牌
     * @param deck 玩家卡组
     * @return 升级的卡牌数量
     */
    public static int autoUpgradeAll(PlayerDeck deck) {
        if (deck == null) {
            return 0;
        }
        int upgradeCount = 0;
        List<Card> upgradableCards = deck.getUpgradableCards();
        for (Card card : upgradableCards) {
            Card upgraded = deck.upgradeCard(card);
            if (upgraded != null) {
                upgradeCount++;
                // 递归检查升级后的卡牌是否可以继续升级
                if (upgraded.canUpgrade() && deck.canUpgradeCard(upgraded)) {
                    deck.upgradeCard(upgraded);
                    upgradeCount++;
                }
            }
        }
        return upgradeCount;
    }

    /**
     * 计算升级所需剩余卡牌数量
     * @param deck 玩家卡组
     * @param card 目标卡牌
     * @return 还需要多少张相同基础卡牌才能升级
     */
    public static int getRemainingCardsForUpgrade(PlayerDeck deck, Card card) {
        if (deck == null || card == null || !card.canUpgrade()) {
            return 0;
        }
        int currentCount = deck.getCardCountByBaseId(card.getBaseCardId());
        int needed = 3; // 需要3张相同卡牌
        return Math.max(0, needed - currentCount);
    }

    /**
     * 检查卡组中是否有足够的卡牌进行升级
     * @param deck 玩家卡组
     * @param card 目标卡牌
     * @param count 需要检查的数量
     * @return 是否有足够数量的卡牌
     */
    public static boolean hasEnoughCardsForUpgrade(PlayerDeck deck, Card card, int count) {
        if (deck == null || card == null) {
            return false;
        }
        int currentCount = deck.getCardCountByBaseId(card.getBaseCardId());
        return currentCount >= count;
    }
}