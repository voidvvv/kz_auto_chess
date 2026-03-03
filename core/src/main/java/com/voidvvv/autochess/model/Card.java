package com.voidvvv.autochess.model;

import com.voidvvv.autochess.utils.I18N;

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
        WARRIOR("warrior"),
        MAGE("mage"),
        ARCHER("archer"),
        ASSASSIN("assassin"),
        TANK("tank");

        private final String i18nKey;

        CardType(String i18nKey) {
            this.i18nKey = i18nKey;
        }

        public String getDisplayName() {
            String key = "card_type_" + i18nKey;
            return I18N.get(key, getDefaultDisplayName());
        }

        private String getDefaultDisplayName() {
            switch (this) {
                case WARRIOR: return "战士";
                case MAGE: return "法师";
                case ARCHER: return "射手";
                case ASSASSIN: return "刺客";
                case TANK: return "坦克";
                default: return name();
            }
        }
    }
}

