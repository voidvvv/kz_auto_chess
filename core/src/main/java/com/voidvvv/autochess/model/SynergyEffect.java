package com.voidvvv.autochess.model;

/**
 * 羁绊效果数据类
 * 存储单个羁绊效果提供的属性加成
 */
public class SynergyEffect {
    private String synergyName;
    private float attackBonus;          // 攻击力加成
    private float defenseBonus;         // 防御力加成
    private float magicBonus;           // 魔法强度加成
    private float manaRegenBonus;       // 法力回复加成
    private float attackSpeedBonus;     // 攻击速度加成
    private float critBonus;            // 暴击率加成
    private float critDamageBonus;      // 暴击伤害加成
    private float dodgeBonus;           // 闪避率加成
    private float hpBonus;              // 生命值加成
    private float damageReductionBonus; // 伤害减免加成
    private float lifeStealBonus;       // 生命偷取加成
    private float expBonus;             // 经验获取加成

    public SynergyEffect(String synergyName) {
        this.synergyName = synergyName;
    }

    // 全参数构造函数
    public SynergyEffect(String synergyName,
                        float attackBonus, float defenseBonus,
                        float magicBonus, float manaRegenBonus,
                        float attackSpeedBonus, float critBonus,
                        float critDamageBonus, float dodgeBonus,
                        float hpBonus, float damageReductionBonus,
                        float lifeStealBonus, float expBonus) {
        this.synergyName = synergyName;
        this.attackBonus = attackBonus;
        this.defenseBonus = defenseBonus;
        this.magicBonus = magicBonus;
        this.manaRegenBonus = manaRegenBonus;
        this.attackSpeedBonus = attackSpeedBonus;
        this.critBonus = critBonus;
        this.critDamageBonus = critDamageBonus;
        this.dodgeBonus = dodgeBonus;
        this.hpBonus = hpBonus;
        this.damageReductionBonus = damageReductionBonus;
        this.lifeStealBonus = lifeStealBonus;
        this.expBonus = expBonus;
    }

    // 简化构造函数
    public SynergyEffect(String synergyName,
                        float attackBonus, float defenseBonus,
                        float magicBonus, float manaRegenBonus) {
        this(synergyName, attackBonus, defenseBonus, magicBonus, manaRegenBonus,
             0, 0, 0, 0, 0, 0, 0, 0);
    }

    // Getters
    public String getSynergyName() { return synergyName; }
    public float getAttackBonus() { return attackBonus; }
    public float getDefenseBonus() { return defenseBonus; }
    public float getMagicBonus() { return magicBonus; }
    public float getManaRegenBonus() { return manaRegenBonus; }
    public float getAttackSpeedBonus() { return attackSpeedBonus; }
    public float getCritBonus() { return critBonus; }
    public float getCritDamageBonus() { return critDamageBonus; }
    public float getDodgeBonus() { return dodgeBonus; }
    public float getHpBonus() { return hpBonus; }
    public float getDamageReductionBonus() { return damageReductionBonus; }
    public float getLifeStealBonus() { return lifeStealBonus; }
    public float getExpBonus() { return expBonus; }

    // 合并两个效果（用于多个羁绊叠加）
    public void merge(SynergyEffect other) {
        if (other == null) return;
        this.attackBonus += other.attackBonus;
        this.defenseBonus += other.defenseBonus;
        this.magicBonus += other.magicBonus;
        this.manaRegenBonus += other.manaRegenBonus;
        this.attackSpeedBonus += other.attackSpeedBonus;
        this.critBonus += other.critBonus;
        this.critDamageBonus += other.critDamageBonus;
        this.dodgeBonus += other.dodgeBonus;
        this.hpBonus += other.hpBonus;
        this.damageReductionBonus += other.damageReductionBonus;
        this.lifeStealBonus += other.lifeStealBonus;
        this.expBonus += other.expBonus;
    }

    // 应用加成到基础值
    public float applyAttackBonus(float baseAttack) {
        return baseAttack * (1 + attackBonus);
    }

    public float applyDefenseBonus(float baseDefense) {
        return baseDefense * (1 + defenseBonus);
    }

    public float applyMagicBonus(float baseMagic) {
        return baseMagic * (1 + magicBonus);
    }

    public float applyManaRegenBonus(float baseManaRegen) {
        return baseManaRegen * (1 + manaRegenBonus);
    }

    public float applyAttackSpeedBonus(float baseAttackSpeed) {
        return baseAttackSpeed * (1 + attackSpeedBonus);
    }

    public float applyHpBonus(float baseHp) {
        return baseHp * (1 + hpBonus);
    }

    // 检查是否有任何加成
    public boolean hasAnyBonus() {
        return attackBonus != 0 || defenseBonus != 0 || magicBonus != 0 ||
               manaRegenBonus != 0 || attackSpeedBonus != 0 || critBonus != 0 ||
               critDamageBonus != 0 || dodgeBonus != 0 || hpBonus != 0 ||
               damageReductionBonus != 0 || lifeStealBonus != 0 || expBonus != 0;
    }

    @Override
    public String toString() {
        return String.format("SynergyEffect{%s, att:%.1f%%, def:%.1f%%, mag:%.1f%%, hp:%.1f%%}",
            synergyName, attackBonus * 100, defenseBonus * 100,
            magicBonus * 100, hpBonus * 100);
    }
}