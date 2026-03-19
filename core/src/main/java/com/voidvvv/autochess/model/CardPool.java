package com.voidvvv.autochess.model;

import com.voidvvv.autochess.utils.I18N;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 卡池管理类，管理所有可用的卡牌
 */
public class CardPool {
    private List<Card> allCards;
    private Random random;
    private SharedCardPool sharedCardPool;

    public CardPool() {
        this.allCards = new ArrayList<>();
        this.random = new Random();
        initCards();
    }

    /**
     * 设置共享卡池引用
     */
    public void setSharedCardPool(SharedCardPool sharedCardPool) {
        this.sharedCardPool = sharedCardPool;
    }

    /**
     * 获取共享卡池引用
     */
    public SharedCardPool getSharedCardPool() {
        return sharedCardPool;
    }

    /**
     * 初始化卡池，添加所有可用的卡牌
     */
    private void initCards() {
        // Tier 1 卡牌
        allCards.add(new Card(140, I18N.get("card_novice_warrior"), I18N.get("card_desc_novice_warrior"), 1, 1, Card.CardType.WARRIOR, 1, 1, Arrays.asList(SynergyType.WARRIOR), "abc+140"));
        allCards.add(new Card(141, I18N.get("card_novice_mage"), I18N.get("card_desc_novice_mage"), 1, 1, Card.CardType.MAGE, 1, 2, Arrays.asList(SynergyType.MAGE), "abc+141"));
        allCards.add(new Card(142, I18N.get("card_novice_archer"), I18N.get("card_desc_novice_archer"), 1, 1, Card.CardType.ARCHER, 1, 3, Arrays.asList(SynergyType.ARCHER), "abc+142"));
        allCards.add(new Card(143, I18N.get("card_novice_assassin"), I18N.get("card_desc_novice_assassin"), 1, 1, Card.CardType.ASSASSIN, 1, 4, Arrays.asList(SynergyType.ASSASSIN), "abc+143"));
        allCards.add(new Card(144, I18N.get("card_novice_tank"), I18N.get("card_desc_novice_tank"), 1, 1, Card.CardType.TANK, 1, 5, Arrays.asList(SynergyType.TANK), "abc+144"));

        // Tier 2 卡牌
        allCards.add(new Card(145, I18N.get("card_elite_warrior"), I18N.get("card_desc_elite_warrior"), 2, 2, Card.CardType.WARRIOR, 1, 6, Arrays.asList(SynergyType.WARRIOR), "abc+145"));
        allCards.add(new Card(146, I18N.get("card_elite_mage"), I18N.get("card_desc_elite_mage"), 2, 2, Card.CardType.MAGE, 1, 7, Arrays.asList(SynergyType.MAGE), "abc+146"));
        allCards.add(new Card(147, I18N.get("card_elite_archer"), I18N.get("card_desc_elite_archer"), 2, 2, Card.CardType.ARCHER, 1, 8, Arrays.asList(SynergyType.ARCHER), "abc+147"));
        allCards.add(new Card(148, I18N.get("card_elite_assassin"), I18N.get("card_desc_elite_assassin"), 2, 2, Card.CardType.ASSASSIN, 1, 9, Arrays.asList(SynergyType.ASSASSIN), "abc+148"));
        allCards.add(new Card(149, I18N.get("card_elite_tank"), I18N.get("card_desc_elite_tank"), 2, 2, Card.CardType.TANK, 1, 10, Arrays.asList(SynergyType.TANK), "abc+149"));

        // Tier 3 卡牌
        allCards.add(new Card(150, I18N.get("card_advanced_warrior"), I18N.get("card_desc_advanced_warrior"), 3, 3, Card.CardType.WARRIOR, 1, 11, Arrays.asList(SynergyType.WARRIOR), "abc+150"));
        allCards.add(new Card(151, I18N.get("card_advanced_mage"), I18N.get("card_desc_advanced_mage"), 3, 3, Card.CardType.MAGE, 1, 12, Arrays.asList(SynergyType.MAGE), "abc+151"));
        allCards.add(new Card(152, I18N.get("card_advanced_archer"), I18N.get("card_desc_advanced_archer"), 3, 3, Card.CardType.ARCHER, 1, 13, Arrays.asList(SynergyType.ARCHER), "abc+152"));
        allCards.add(new Card(153, I18N.get("card_advanced_assassin"), I18N.get("card_desc_advanced_assassin"), 3, 3, Card.CardType.ASSASSIN, 1, 14, Arrays.asList(SynergyType.ASSASSIN), "abc+153"));
        allCards.add(new Card(154, I18N.get("card_advanced_tank"), I18N.get("card_desc_advanced_tank"), 3, 3, Card.CardType.TANK, 1, 15, Arrays.asList(SynergyType.TANK), "abc+154"));

        // Tier 4 卡牌 - 传奇卡牌
        allCards.add(new Card(155, I18N.get("card_legendary_warrior"), I18N.get("card_desc_legendary_warrior"), 4, 4, Card.CardType.WARRIOR, 1, 16, Arrays.asList(SynergyType.WARRIOR), "abc+155"));
        allCards.add(new Card(156, I18N.get("card_legendary_mage"), I18N.get("card_desc_legendary_mage"), 4, 4, Card.CardType.MAGE, 1, 17, Arrays.asList(SynergyType.MAGE), "abc+156"));
        allCards.add(new Card(157, I18N.get("card_legendary_archer"), I18N.get("card_desc_legendary_archer"), 4, 4, Card.CardType.ARCHER, 1, 18, Arrays.asList(SynergyType.ARCHER), "abc+157"));
        allCards.add(new Card(158, I18N.get("card_legendary_assassin"), I18N.get("card_desc_legendary_assassin"), 4, 4, Card.CardType.ASSASSIN, 1, 19, Arrays.asList(SynergyType.ASSASSIN), "abc+158"));
        allCards.add(new Card(159, I18N.get("card_legendary_tank"), I18N.get("card_desc_legendary_tank"), 4, 4, Card.CardType.TANK, 1, 20, Arrays.asList(SynergyType.TANK), "abc+159"));

        // Tier 5 卡牌 - 神话卡牌
        allCards.add(new Card(160, I18N.get("card_mythical_warrior"), I18N.get("card_desc_mythical_warrior"), 5, 5, Card.CardType.WARRIOR, 1, 21, Arrays.asList(SynergyType.WARRIOR), "abc+160"));
        allCards.add(new Card(161, I18N.get("card_mythical_mage"), I18N.get("card_desc_mythical_mage"), 5, 5, Card.CardType.MAGE, 1, 22, Arrays.asList(SynergyType.MAGE), "abc+161"));
        allCards.add(new Card(162, I18N.get("card_mythical_archer"), I18N.get("card_desc_mythical_archer"), 5, 5, Card.CardType.ARCHER, 1, 23, Arrays.asList(SynergyType.ARCHER), "abc+162"));
        allCards.add(new Card(163, I18N.get("card_mythical_assassin"), I18N.get("card_desc_mythical_assassin"), 5, 5, Card.CardType.ASSASSIN, 1, 24, Arrays.asList(SynergyType.ASSASSIN), "abc+163"));
        allCards.add(new Card(164, I18N.get("card_mythical_tank"), I18N.get("card_desc_mythical_tank"), 5, 5, Card.CardType.TANK, 1, 25, Arrays.asList(SynergyType.TANK), "abc+164"));
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
     * 根据等级获取可用卡牌列表（考虑共享池中的可用数量）
     */
    public List<Card> getAvailableCardsByTier(int tier) {
        List<Card> result = new ArrayList<>();
        for (Card card : allCards) {
            if (card.getTier() == tier) {
                // 如果没有共享池，所有卡都可用
                if (sharedCardPool == null || sharedCardPool.isCardAvailable(card.getId())) {
                    result.add(card);
                }
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
     * 会过滤掉共享池中已耗尽的卡牌
     */
    public List<Card> getRandomCardsByLevel(int count, int playerLevel) {
        List<Card> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // 根据玩家等级计算各等级卡牌的出现概率
            int tier = calculateTierByLevel(playerLevel);
            List<Card> tierCards = getAvailableCardsByTier(tier);
            if (!tierCards.isEmpty()) {
                int index = random.nextInt(tierCards.size());
                result.add(tierCards.get(index));
            } else {
                // 如果当前等级没有可用卡牌，尝试从其他等级获取
                Card fallbackCard = getFallbackCard(tier);
                if (fallbackCard != null) {
                    result.add(fallbackCard);
                }
            }
        }
        return result;
    }

    /**
     * 当指定等级没有可用卡牌时，从其他等级获取替代卡牌
     */
    private Card getFallbackCard(int excludedTier) {
        // 优先从相邻等级获取
        for (int tierOffset = 1; tierOffset <= 4; tierOffset++) {
            // 先检查更低的等级
            int lowerTier = excludedTier - tierOffset;
            if (lowerTier >= 1) {
                List<Card> lowerCards = getAvailableCardsByTier(lowerTier);
                if (!lowerCards.isEmpty()) {
                    return lowerCards.get(random.nextInt(lowerCards.size()));
                }
            }
            // 再检查更高的等级
            int higherTier = excludedTier + tierOffset;
            if (higherTier <= 5) {
                List<Card> higherCards = getAvailableCardsByTier(higherTier);
                if (!higherCards.isEmpty()) {
                    return higherCards.get(random.nextInt(higherCards.size()));
                }
            }
        }
        return null;
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

