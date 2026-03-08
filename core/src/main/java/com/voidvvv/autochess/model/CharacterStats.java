package com.voidvvv.autochess.model;

/**
 * 角色属性配置类
 * 纯数据模型，不包含资源加载逻辑
 */
public class CharacterStats {
    private float cardId;
    private float health;        // 健康值
    private float mana;          // 法力值
    private float attack;        // 攻击力
    private float defense;       // 防御力
    private float magicPower;    // 魔法强度
    private float magicResist;   // 魔法抵抗
    private float agility;       // 敏捷值

    public CharacterStats(float cardId, float health, float mana, float attack, float defense,
                          float magicPower, float magicResist, float agility) {
        this.cardId = cardId;
        this.health = health;
        this.mana = mana;
        this.attack = attack;
        this.defense = defense;
        this.magicPower = magicPower;
        this.magicResist = magicResist;
        this.agility = agility;
    }

    // Getters
    public float getCardId() { return cardId; }
    public float getHealth() { return health; }
    public float getMana() { return mana; }
    public float getAttack() { return attack; }
    public float getDefense() { return defense; }
    public float getMagicPower() { return magicPower; }
    public float getMagicResist() { return magicResist; }
    public float getAgility() { return agility; }
}

