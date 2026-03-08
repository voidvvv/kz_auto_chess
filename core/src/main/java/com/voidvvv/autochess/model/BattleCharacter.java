package com.voidvvv.autochess.model;

import com.voidvvv.autochess.utils.CharacterCamp;
import java.util.HashMap;
import java.util.Map;

/**
 * 战场角色类
 * 表示战场上的一个角色单位，支持生命、攻击、目标与行为树所需状态
 */
public class BattleCharacter {
    public float time = 0f;
    public float currentTime = 0f;
    public float lastStateTime = 0f;

    private Card card;
    private String name;
    private CharacterStats stats;
    private CharacterStats battleStats;
    private float x, y;
    private float initX, initY;
    private float size = 40;
    private CharacterCamp camp;

    /** 当前生命，战斗时扣减 */
    private float currentHp;
    /** 当前目标（可为 null） */
    private BattleCharacter target;
    /** 攻击间隔（秒），下次可攻击时间点 */
    private float nextAttackTime;
    /** 攻击距离（世界单位），根据类型或配置设定 */
    private float attackRange = 80f;

    public final MoveComponent moveComponent = new MoveComponent();

    // 羁绊效果系统
    private Map<String, SynergyEffect> activeSynergyEffects = new HashMap<>();

    public BaseCollision baseCollision = new BaseCollision();

    // 普通攻击
    public float progressCouldDamage = 0.15f;
    public float maxAttackActProgress = 0.25f;
    public float currentAttackProgress = 0f;
    public float attackCooldown = 1f;

    public BattleCharacter(Card card, CharacterStats stats, float x, float y, boolean isEnemy) {
        this.card = card;
        this.stats = stats;
        this.name = card.getName();
        this.initX = x;
        this.initY = y;
        if (isEnemy) {
            this.camp = CharacterCamp.BLACK;
        } else {
            this.camp = CharacterCamp.WHITE;
        }
        this.reset();
    }

    public void reset() {
        battleStats = null; // 确保战斗属性清除
        this.x = initX;
        this.y = initY;
        this.currentHp = stats != null ? stats.getHealth() : 100f;
        this.nextAttackTime = 0;
        moveComponent.speed = 10f;
        moveComponent.canWalk = true;
        setTarget(null);
        // 重置计时器和攻击进度
        this.time = 0f;
        this.currentTime = 0f;
        this.lastStateTime = 0f;
        this.currentAttackProgress = 0f;
        if (stats != null && card != null) {
            inferAttackRange();
        }
    }

    public void enterBattle() {
        // 进入战斗状态，初始化战斗属性
        battleStats = stats; // 目前引用相同的基础属性，未来可以复制或应用buff
    }

    public void exitBattle() {
        // 退出战斗状态，清除战斗属性
        battleStats = null;
    }

    public CharacterCamp getCamp() {
        return camp;
    }

    private void inferAttackRange() {
        switch (card.getType()) {
            case ARCHER:
                attackRange = 150f;
                break;
            case MAGE:
                attackRange = 120f;
                break;
            default:
                attackRange = 70f;
                break;
        }
    }

    public Card getCard() { return card; }
    public CharacterStats getStats() { return battleStats != null ? battleStats : stats; }
    public float getX() { return x; }
    public float getY() { return y; }
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public void setInitPosition (float x, float y) {
        this.x = x;
        this.y = y;
        this.initX = x;
        this.initY = y;
    }
    public float getSize() { return size; }
    public boolean isEnemy() { return this.camp == CharacterCamp.BLACK; }

    public float getCurrentHp() { return currentHp; }
    public void setCurrentHp(float currentHp) { this.currentHp = currentHp; }
    public boolean isDead() { return currentHp <= 0; }

    public BattleCharacter getTarget() { return target; }
    public void setTarget(BattleCharacter target) { this.target = target; }

    public float getNextAttackTime() { return nextAttackTime; }
    public void setNextAttackTime(float t) { this.nextAttackTime = t; }
    public float getAttackRange() { return attackRange; }
    public void setAttackRange(float r) { this.attackRange = r; }

    public String getName() {
        return name;
    }

    /**
     * 检查点是否在角色范围内
     */
    public boolean contains(float px, float py) {
        float halfSize = size / 2;
        return px >= x - halfSize && px <= x + halfSize &&
               py >= y - halfSize && py <= y + halfSize;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    /**
     * 与另一单位的平面距离
     */
    public float distanceTo(BattleCharacter other) {
        if (other == null) return Float.MAX_VALUE;
        float dx = other.getX() - x;
        float dy = other.getY() - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 获取碰撞半径
     */
    public float getCollisionRadius() {
        return size / 2f;
    }

    /**
     * 获取碰撞中心X坐标
     */
    public float getCollisionCenterX() {
        return x;
    }

    /**
     * 获取碰撞中心Y坐标
     */
    public float getCollisionCenterY() {
        return y;
    }

    /**
     * 检测与投掷物的碰撞
     */
    public boolean collidesWith(Projectile projectile) {
        if (projectile == null) return false;
        return collidesWith(projectile.getX(), projectile.getY(), projectile.getCollisionRadius());
    }

    /**
     * 检测与圆形区域的碰撞
     */
    public boolean collidesWith(float x, float y, float radius) {
        float dx = getCollisionCenterX() - x;
        float dy = getCollisionCenterY() - y;
        float distanceSquared = dx * dx + dy * dy;
        float combinedRadius = getCollisionRadius() + radius;
        return distanceSquared <= combinedRadius * combinedRadius;
    }

    // ===================== 羁绊效果系统 =====================

    /**
     * 添加羁绊效果
     */
    public void addSynergyEffect(String synergyName,
                                float attackBonus, float defenseBonus,
                                float magicBonus, float manaRegenBonus) {
        addSynergyEffect(synergyName, attackBonus, defenseBonus, magicBonus, manaRegenBonus,
                        0, 0, 0, 0, 0, 0, 0, 0);
    }

    public void addSynergyEffect(String synergyName,
                                float attackBonus, float defenseBonus,
                                float magicBonus, float manaRegenBonus,
                                float attackSpeedBonus, float critBonus) {
        addSynergyEffect(synergyName, attackBonus, defenseBonus, magicBonus, manaRegenBonus,
                        attackSpeedBonus, critBonus, 0, 0, 0, 0, 0, 0);
    }

    public void addSynergyEffect(String synergyName,
                                float attackBonus, float defenseBonus,
                                float magicBonus, float manaRegenBonus,
                                float attackSpeedBonus, float critBonus,
                                float critDamageBonus, float dodgeBonus) {
        addSynergyEffect(synergyName, attackBonus, defenseBonus, magicBonus, manaRegenBonus,
                        attackSpeedBonus, critBonus, critDamageBonus, dodgeBonus, 0, 0, 0, 0);
    }

    public void addSynergyEffect(String synergyName,
                                float attackBonus, float defenseBonus,
                                float magicBonus, float manaRegenBonus,
                                float attackSpeedBonus, float critBonus,
                                float critDamageBonus, float dodgeBonus,
                                float hpBonus, float damageReductionBonus) {
        addSynergyEffect(synergyName, attackBonus, defenseBonus, magicBonus, manaRegenBonus,
                        attackSpeedBonus, critBonus, critDamageBonus, dodgeBonus,
                        hpBonus, damageReductionBonus, 0, 0);
    }

    public void addSynergyEffect(String synergyName,
                                float attackBonus, float defenseBonus,
                                float magicBonus, float manaRegenBonus,
                                float attackSpeedBonus, float critBonus,
                                float critDamageBonus, float dodgeBonus,
                                float hpBonus, float damageReductionBonus,
                                float lifeStealBonus, float expBonus) {
        SynergyEffect existing = activeSynergyEffects.get(synergyName);
        if (existing != null) {
            // 合并效果
            SynergyEffect newEffect = new SynergyEffect(synergyName,
                    attackBonus, defenseBonus, magicBonus, manaRegenBonus,
                    attackSpeedBonus, critBonus, critDamageBonus, dodgeBonus,
                    hpBonus, damageReductionBonus, lifeStealBonus, expBonus);
            existing.merge(newEffect);
        } else {
            // 创建新效果
            SynergyEffect effect = new SynergyEffect(synergyName,
                    attackBonus, defenseBonus, magicBonus, manaRegenBonus,
                    attackSpeedBonus, critBonus, critDamageBonus, dodgeBonus,
                    hpBonus, damageReductionBonus, lifeStealBonus, expBonus);
            activeSynergyEffects.put(synergyName, effect);
        }
    }

    /**
     * 清除所有羁绊效果
     */
    public void clearSynergyEffects() {
        activeSynergyEffects.clear();
    }

    /**
     * 获取指定羁绊的效果
     */
    public SynergyEffect getSynergyEffect(String synergyName) {
        return activeSynergyEffects.get(synergyName);
    }

    /**
     * 获取所有激活的羁绊效果
     */
    public Map<String, SynergyEffect> getAllSynergyEffects() {
        return new HashMap<>(activeSynergyEffects);
    }

    /**
     * 检查是否有激活的羁绊效果
     */
    public boolean hasActiveSynergyEffects() {
        return !activeSynergyEffects.isEmpty();
    }

    /**
     * 计算总攻击力加成（包括所有羁绊效果）
     */
    public float getTotalAttackBonus() {
        float total = 0;
        for (SynergyEffect effect : activeSynergyEffects.values()) {
            total += effect.getAttackBonus();
        }
        return total;
    }

    /**
     * 计算总防御力加成（包括所有羁绊效果）
     */
    public float getTotalDefenseBonus() {
        float total = 0;
        for (SynergyEffect effect : activeSynergyEffects.values()) {
            total += effect.getDefenseBonus();
        }
        return total;
    }

    /**
     * 计算总魔法强度加成（包括所有羁绊效果）
     */
    public float getTotalMagicBonus() {
        float total = 0;
        for (SynergyEffect effect : activeSynergyEffects.values()) {
            total += effect.getMagicBonus();
        }
        return total;
    }

    /**
     * 计算总生命值加成（包括所有羁绊效果）
     */
    public float getTotalHpBonus() {
        float total = 0;
        for (SynergyEffect effect : activeSynergyEffects.values()) {
            total += effect.getHpBonus();
        }
        return total;
    }

    /**
     * 获取羁绊效果信息字符串
     */
    public String getSynergyEffectsInfo() {
        if (activeSynergyEffects.isEmpty()) {
            return "无羁绊效果";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("羁绊效果:");
        for (SynergyEffect effect : activeSynergyEffects.values()) {
            if (effect.hasAnyBonus()) {
                sb.append("\n  ").append(effect);
            }
        }
        return sb.toString();
    }
}

