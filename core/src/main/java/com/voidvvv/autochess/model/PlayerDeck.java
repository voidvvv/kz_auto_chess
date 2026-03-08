package com.voidvvv.autochess.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 玩家卡组类，管理玩家拥有的卡牌
 */
public class PlayerDeck {
    // 使用Map存储卡牌和数量，key是卡牌ID，value是数量
    private Map<Integer, Integer> cardCounts;
    // 存储所有卡牌实例（用于快速访问）
    private List<Card> allCards;

    public PlayerDeck() {
        this.cardCounts = new HashMap<>();
        this.allCards = new ArrayList<>();
    }

    /**
     * 添加卡牌到卡组
     */
    public void addCard(Card card) {
        int cardId = card.getId();
        if (cardCounts.containsKey(cardId)) {
            cardCounts.put(cardId, cardCounts.get(cardId) + 1);
        } else {
            cardCounts.put(cardId, 1);
            allCards.add(card);
        }
    }

    /**
     * 移除一张卡牌
     */
    public boolean removeCard(Card card) {
        int cardId = card.getId();
        if (cardCounts.containsKey(cardId)) {
            int count = cardCounts.get(cardId);
            if (count > 1) {
                cardCounts.put(cardId, count - 1);
            } else {
                cardCounts.remove(cardId);
                allCards.removeIf(c -> c.getId() == cardId);
            }
            return true;
        }
        return false;
    }

    /**
     * 获取指定卡牌的数量
     */
    public int getCardCount(Card card) {
        return cardCounts.getOrDefault(card.getId(), 0);
    }

    /**
     * 获取指定ID的卡牌数量
     */
    public int getCardCount(int cardId) {
        return cardCounts.getOrDefault(cardId, 0);
    }

    /**
     * 获取指定基础ID的卡牌数量（用于升级检查）
     */
    public int getCardCountByBaseId(int baseCardId) {
        int total = 0;
        for (Card card : allCards) {
            if (card.getBaseCardId() == baseCardId) {
                total += getCardCount(card.getId());
            }
        }
        return total;
    }

    /**
     * 移除指定基础ID的一定数量的卡牌
     * @param baseCardId 基础卡牌ID
     * @param count 要移除的数量
     * @return 实际移除的数量
     */
    public int removeCardsByBaseId(int baseCardId, int count) {
        int removed = 0;
        List<Card> cardsToRemove = new ArrayList<>();

        // 收集需要移除的卡牌
        for (Card card : allCards) {
            if (card.getBaseCardId() == baseCardId) {
                int cardId = card.getId();
                int available = cardCounts.getOrDefault(cardId, 0);
                int toRemoveFromThisCard = Math.min(available, count - removed);

                if (toRemoveFromThisCard > 0) {
                    // 更新数量
                    if (available > toRemoveFromThisCard) {
                        cardCounts.put(cardId, available - toRemoveFromThisCard);
                    } else {
                        cardCounts.remove(cardId);
                        cardsToRemove.add(card);
                    }
                    removed += toRemoveFromThisCard;

                    if (removed >= count) {
                        break;
                    }
                }
            }
        }

        // 从allCards中移除数量为0的卡牌
        allCards.removeAll(cardsToRemove);
        return removed;
    }

    /**
     * 获取所有拥有的卡牌（去重后的列表）
     */
    public List<Card> getAllUniqueCards() {
        return new ArrayList<>(allCards);
    }

    /**
     * 获取卡组中卡牌的总数量
     */
    public int getTotalCardCount() {
        int total = 0;
        for (int count : cardCounts.values()) {
            total += count;
        }
        return total;
    }

    /**
     * 获取卡组中不同卡牌的数量
     */
    public int getUniqueCardCount() {
        return allCards.size();
    }

    /**
     * 检查是否拥有指定卡牌
     */
    public boolean hasCard(Card card) {
        return cardCounts.containsKey(card.getId());
    }

    /**
     * 清空卡组
     */
    public void clear() {
        cardCounts.clear();
        allCards.clear();
    }
}

