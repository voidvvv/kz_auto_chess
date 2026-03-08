package com.voidvvv.autochess.logic;

import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.PlayerDeck;
import java.util.ArrayList;
import java.util.List;

/**
 * 卡牌升级逻辑工具类
 * 提供静态方法处理卡牌升级相关逻辑
 * 从模型中分离业务逻辑，使模型保持纯数据实体
 */
public class CardUpgradeLogic {

    private CardUpgradeLogic() {
        // 工具类，不允许实例化
        throw new UnsupportedOperationException("CardUpgradeLogic is a utility class and cannot be instantiated");
    }

    /**
     * 检查卡牌是否可以升级
     * @param card 要检查的卡牌
     * @return 是否可以升级
     */
    public static boolean canUpgrade(Card card) {
        if (card == null) {
            return false;
        }
        return card.getStarLevel() < 3;
    }

    /**
     * 检查卡组中的卡牌是否可以升级
     * @param deck 玩家卡组
     * @param card 要检查的卡牌
     * @return 是否可以升级
     */
    public static boolean canUpgradeCard(PlayerDeck deck, Card card) {
        if (deck == null || card == null) {
            return false;
        }
        if (!canUpgrade(card)) {
            return false;
        }
        int baseCardId = card.getBaseCardId();
        int count = deck.getCardCountByBaseId(baseCardId);
        return count >= 3;
    }

    /**
     * 创建升级后的卡牌
     * @param original 原始卡牌
     * @return 升级后的卡牌，如果无法升级则返回null
     */
    public static Card createUpgradedCard(Card original) {
        if (original == null || !canUpgrade(original)) {
            return null;
        }

        int newId = original.getId() + 1000; // 简单ID生成策略
        String newName = original.getName() + "★";
        int newCost = original.getCost() + 1;
        int newStarLevel = original.getStarLevel() + 1;

        return new Card(newId, newName, original.getDescription(), newCost, original.getTier(),
                       original.getType(), newStarLevel, original.getBaseCardId(),
                       new ArrayList<>(original.getSynergies()), original.getTiledResourceKey());
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
        if (!canUpgradeCard(deck, card)) {
            return null;
        }

        // 移除3张基础卡牌
        deck.removeCardsByBaseId(card.getBaseCardId(), 3);

        // 创建并添加升级后的卡牌
        Card upgradedCard = createUpgradedCard(card);
        if (upgradedCard != null) {
            deck.addCard(upgradedCard);
        }

        return upgradedCard;
    }

    /**
     * 获取所有可升级的卡牌列表
     * @param deck 玩家卡组
     * @return 可升级的卡牌列表
     */
    public static List<Card> getUpgradableCards(PlayerDeck deck) {
        if (deck == null) {
            return new ArrayList<>();
        }

        List<Card> upgradableCards = new ArrayList<>();
        List<Card> allCards = deck.getAllUniqueCards();

        for (Card card : allCards) {
            if (canUpgradeCard(deck, card)) {
                upgradableCards.add(card);
            }
        }
        return upgradableCards;
    }

    /**
     * 批量检查卡牌升级
     * @param deck 玩家卡组
     * @param cards 要检查的卡牌列表
     * @return 可升级的卡牌列表
     */
    public static List<Card> findUpgradableCards(PlayerDeck deck, List<Card> cards) {
        if (deck == null || cards == null) {
            return new ArrayList<>();
        }

        List<Card> upgradable = new ArrayList<>();
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
        List<Card> upgradableCards = getUpgradableCards(deck);

        for (Card card : upgradableCards) {
            Card upgraded = upgradeCard(deck, card);
            if (upgraded != null) {
                upgradeCount++;
                // 递归检查升级后的卡牌是否可以继续升级
                if (canUpgrade(upgraded) && canUpgradeCard(deck, upgraded)) {
                    upgraded = upgradeCard(deck, upgraded);
                    if (upgraded != null) {
                        upgradeCount++;
                    }
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
        if (deck == null || card == null || !canUpgrade(card)) {
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
