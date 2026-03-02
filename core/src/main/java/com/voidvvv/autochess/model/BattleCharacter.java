package com.voidvvv.autochess.model;

import com.voidvvv.autochess.utils.CharacterCamp;

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



    // 普通攻击
    public float progressCouldDamage = 0.15f;
    public float maxAttackActProgress = 0.25f;
    public float currentAttackProgress = 0f;


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
        this.x = initX;
        this.y = initY;
        this.currentHp = stats != null ? stats.getHealth() : 100f;
        this.nextAttackTime = 0;
        moveComponent.speed = 10f;
        moveComponent.canWalk = true;
        setTarget(null);
        if (stats != null && card != null) {
            inferAttackRange();
        }
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
    public CharacterStats getStats() { return stats; }
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

}

