package com.voidvvv.autochess.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 共享卡池 - 追踪卡牌可用数量
 *
 * 实现卡池耗尽机制:
 * - 每张卡牌有基于等级的最大数量
 * - 购买卡牌时减少可用数量
 * - 出售卡牌时增加可用数量
 * - 可用数量为0的卡牌不会出现在商店刷新中
 */
public class SharedCardPool {
    // 等级对应的最大数量
    private static final int[] TIER_MAX_COPIES = {0, 15, 12, 9, 6, 3}; // index 0 unused, 1-5 used

    // 卡牌ID -> 当前可用数量
    private final Map<Integer, Integer> availableCopies;
    // 卡牌ID -> 最大数量
    private final Map<Integer, Integer> maxCopies;

    public SharedCardPool() {
        this.availableCopies = new HashMap<>();
        this.maxCopies = new HashMap<>();
    }

    /**
     * 使用CardPool中的所有卡牌初始化共享池
     */
    public void initialize(CardPool cardPool) {
        availableCopies.clear();
        maxCopies.clear();

        List<Card> allCards = cardPool.getAllCards();
        for (Card card : allCards) {
            int cardId = card.getId();
            int tier = card.getTier();
            int maxCount = getMaxCopiesForTier(tier);

            maxCopies.put(cardId, maxCount);
            availableCopies.put(cardId, maxCount);
        }
    }

    /**
     * 重置共享池（新游戏时调用）
     */
    public void reset(CardPool cardPool) {
        initialize(cardPool);
    }

    /**
     * 获取指定等级的最大数量
     */
    public static int getMaxCopiesForTier(int tier) {
        if (tier >= 1 && tier <= 5) {
            return TIER_MAX_COPIES[tier];
        }
        return 0;
    }

    /**
     * 获取卡牌的当前可用数量
     */
    public int getRemainingCopies(int cardId) {
        return availableCopies.getOrDefault(cardId, 0);
    }

    /**
     * 获取卡牌的最大数量
     */
    public int getMaxCopies(int cardId) {
        return maxCopies.getOrDefault(cardId, 0);
    }

    /**
     * 购买卡牌时减少可用数量
     * @return true 如果成功减少，false 如果没有可用数量
     */
    public boolean decrementCopies(int cardId) {
        int current = availableCopies.getOrDefault(cardId, 0);
        if (current <= 0) {
            return false;
        }
        availableCopies.put(cardId, current - 1);
        return true;
    }

    /**
     * 出售卡牌时增加可用数量
     * @return true 如果成功增加，false 如果已达到最大数量
     */
    public boolean incrementCopies(int cardId) {
        int current = availableCopies.getOrDefault(cardId, 0);
        int max = maxCopies.getOrDefault(cardId, 0);
        if (current >= max) {
            return false;
        }
        availableCopies.put(cardId, current + 1);
        return true;
    }

    /**
     * 检查卡牌是否可用（至少有1张剩余）
     */
    public boolean isCardAvailable(int cardId) {
        return getRemainingCopies(cardId) > 0;
    }

    /**
     * 获取所有可用卡牌ID
     */
    public Map<Integer, Integer> getAllAvailableCopies() {
        return new HashMap<>(availableCopies);
    }
}
