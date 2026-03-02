package com.voidvvv.autochess.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 卡池管理类，管理所有可用的卡牌
 */
public class CardPool {
    private List<Card> allCards;
    private Random random;

    public CardPool() {
        this.allCards = new ArrayList<>();
        this.random = new Random();
        initCards();
    }

    /**
     * 初始化卡池，添加所有可用的卡牌
     */
    private void initCards() {
        // Tier 1 卡牌
        allCards.add(new Card(1, "新手战士", "基础的战士单位", 1, 1, Card.CardType.WARRIOR));
        allCards.add(new Card(2, "新手法师", "基础的法师单位", 1, 1, Card.CardType.MAGE));
        allCards.add(new Card(3, "新手射手", "基础的射手单位", 1, 1, Card.CardType.ARCHER));
        allCards.add(new Card(4, "新手刺客", "基础的刺客单位", 1, 1, Card.CardType.ASSASSIN));
        allCards.add(new Card(5, "新手坦克", "基础的坦克单位", 1, 1, Card.CardType.TANK));

        // Tier 2 卡牌
        allCards.add(new Card(6, "精英战士", "强化的战士单位", 2, 2, Card.CardType.WARRIOR));
        allCards.add(new Card(7, "精英法师", "强化的法师单位", 2, 2, Card.CardType.MAGE));
        allCards.add(new Card(8, "精英射手", "强化的射手单位", 2, 2, Card.CardType.ARCHER));
        allCards.add(new Card(9, "精英刺客", "强化的刺客单位", 2, 2, Card.CardType.ASSASSIN));
        allCards.add(new Card(10, "精英坦克", "强化的坦克单位", 2, 2, Card.CardType.TANK));

        // Tier 3 卡牌
        allCards.add(new Card(11, "高级战士", "高级的战士单位", 3, 3, Card.CardType.WARRIOR));
        allCards.add(new Card(12, "高级法师", "高级的法师单位", 3, 3, Card.CardType.MAGE));
        allCards.add(new Card(13, "高级射手", "高级的射手单位", 3, 3, Card.CardType.ARCHER));
        allCards.add(new Card(14, "高级刺客", "高级的刺客单位", 3, 3, Card.CardType.ASSASSIN));
        allCards.add(new Card(15, "高级坦克", "高级的坦克单位", 3, 3, Card.CardType.TANK));

        // Tier 4 卡牌
        allCards.add(new Card(16, "传奇战士", "传奇的战士单位", 4, 4, Card.CardType.WARRIOR));
        allCards.add(new Card(17, "传奇法师", "传奇的法师单位", 4, 4, Card.CardType.MAGE));
        allCards.add(new Card(18, "传奇射手", "传奇的射手单位", 4, 4, Card.CardType.ARCHER));
        allCards.add(new Card(19, "传奇刺客", "传奇的刺客单位", 4, 4, Card.CardType.ASSASSIN));
        allCards.add(new Card(20, "传奇坦克", "传奇的坦克单位", 4, 4, Card.CardType.TANK));

        // Tier 5 卡牌
        allCards.add(new Card(21, "神话战士", "神话级的战士单位", 5, 5, Card.CardType.WARRIOR));
        allCards.add(new Card(22, "神话法师", "神话级的法师单位", 5, 5, Card.CardType.MAGE));
        allCards.add(new Card(23, "神话射手", "神话级的射手单位", 5, 5, Card.CardType.ARCHER));
        allCards.add(new Card(24, "神话刺客", "神话级的刺客单位", 5, 5, Card.CardType.ASSASSIN));
        allCards.add(new Card(25, "神话坦克", "神话级的坦克单位", 5, 5, Card.CardType.TANK));
    }

    /**
     * 根据等级获取卡牌列表
     */
    public List<Card> getCardsByTier(int tier) {
        List<Card> result = new ArrayList<>();
        for (Card card : allCards) {
            if (card.getTier() == tier) {
                result.add(card);
            }
        }
        return result;
    }

    /**
     * 随机获取指定数量的卡牌（用于商店刷新）
     */
    public List<Card> getRandomCards(int count) {
        List<Card> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(allCards.size());
            result.add(allCards.get(index));
        }
        return result;
    }

    /**
     * 根据等级概率随机获取卡牌
     * 等级越高，高等级卡牌出现概率越高
     */
    public List<Card> getRandomCardsByLevel(int count, int playerLevel) {
        List<Card> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // 根据玩家等级计算各等级卡牌的出现概率
            int tier = calculateTierByLevel(playerLevel);
            List<Card> tierCards = getCardsByTier(tier);
            if (!tierCards.isEmpty()) {
                int index = random.nextInt(tierCards.size());
                result.add(tierCards.get(index));
            } else {
                // 如果没有对应等级的卡，随机选择
                int index = random.nextInt(allCards.size());
                result.add(allCards.get(index));
            }
        }
        return result;
    }

    /**
     * 根据玩家等级计算卡牌等级
     */
    private int calculateTierByLevel(int playerLevel) {
        // 简单的概率计算
        float rand = random.nextFloat();
        if (playerLevel <= 3) {
            // 1-3级：主要出1-2级卡
            if (rand < 0.7f) return 1;
            else if (rand < 0.95f) return 2;
            else return 3;
        } else if (playerLevel <= 6) {
            // 4-6级：主要出2-3级卡
            if (rand < 0.3f) return 1;
            else if (rand < 0.7f) return 2;
            else if (rand < 0.95f) return 3;
            else return 4;
        } else {
            // 7级以上：可以出所有等级的卡
            if (rand < 0.2f) return 1;
            else if (rand < 0.4f) return 2;
            else if (rand < 0.6f) return 3;
            else if (rand < 0.85f) return 4;
            else return 5;
        }
    }

    public List<Card> getAllCards() {
        return new ArrayList<>(allCards);
    }

    /** 根据卡牌 id 获取卡牌，用于关卡敌人配置等 */
    public Card getCardById(int id) {
        for (Card c : allCards) {
            if (c.getId() == id) return c;
        }
        return null;
    }
}

