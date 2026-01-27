package io.github.some_example_name.model;

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

