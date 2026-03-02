package com.voidvvv.autochess.model;

/**
 * 卡牌数据类
 */
public class Card {
    private int id;
    private String name;
    private String description;
    private int cost; // 购买费用
    private int tier; // 卡牌等级/稀有度 (1-5)
    private CardType type; // 卡牌类型

    public Card(int id, String name, String description, int cost, int tier, CardType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.tier = tier;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }

    public int getTier() {
        return tier;
    }

    public CardType getType() {
        return type;
    }

    /**
     * 卡牌类型枚举
     */
    public enum CardType {
        WARRIOR("战士"),
        MAGE("法师"),
        ARCHER("射手"),
        ASSASSIN("刺客"),
        TANK("坦克");

        private final String displayName;

        CardType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

