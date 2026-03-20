package com.voidvvv.autochess.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.voidvvv.autochess.battle.collision.CollisionContext;
import com.voidvvv.autochess.battle.collision.CollisionDetector;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Battlefield;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.Projectile;
import com.voidvvv.autochess.manage.ProjectileManager;
import com.voidvvv.autochess.model.CharacterStats;
import com.voidvvv.autochess.model.battle.Damage;
import com.voidvvv.autochess.model.event.DamageEvent;
import com.voidvvv.autochess.model.Skill;
import com.voidvvv.autochess.model.SkillType;
import com.voidvvv.autochess.msg.MessageConstants;
import com.voidvvv.autochess.sm.machine.BaseStateMachine;
import com.voidvvv.autochess.sm.machine.StateMachine;
import com.voidvvv.autochess.sm.state.common.AttackState;
import com.voidvvv.autochess.sm.state.common.States;
import com.voidvvv.autochess.model.skill.BasicSkill;
import com.voidvvv.autochess.model.skill.AoeSkill;
import com.voidvvv.autochess.model.skill.HealSkill;
import com.voidvvv.autochess.model.skill.BuffSkill;
import com.voidvvv.autochess.model.skill.DebuffSkill;
import com.voidvvv.autochess.model.skill.SkillContext;
import com.voidvvv.autochess.model.Card.CardType;

/**
 * 行为树黑板：持有当前单位与战场引用，供寻敌、攻击等任务使用
 */
public class BattleUnitBlackboard implements Telegraph {
    private final BattleCharacter self;
    private final Battlefield battlefield;
    private BattleCharacter target;
    public StateMachine<BattleUnitBlackboard> stateMachine;

    /**
     * 当前帧用于攻击冷却判断，由外部每帧更新
     */
    private float currentTime;

    public boolean couldDamage = false;

    /**
     * 魔法值组件（内部类，纯数据）
     * 存储角色的魔法值状态
     */
    private static class ManaComponent {
        float currentMana;
        float maxMana;
        float regenRate;     // 每秒恢复量
        float attackGain;    // 攻击获得量

        ManaComponent(float maxMana, float regenRate, float attackGain) {
            this.currentMana = 0f;
            this.maxMana = maxMana;
            this.regenRate = regenRate;
            this.attackGain = attackGain;
        }

        void gainMana(float amount) {
            this.currentMana += amount;
            this.currentMana = Math.min(this.currentMana, this.maxMana);
        }

        void reset() {
            this.currentMana = 0f;
        }

        boolean isFull() {
            return this.currentMana >= this.maxMana;
        }

        void setCurrentMana(float mana) {
            this.currentMana = mana;
        }

        float getCurrentMana() {
            return currentMana;
        }

        float getMaxMana() {
            return maxMana;
        }

        float getRegenRate() {
            return regenRate;
        }

        float getAttackGain() {
            return attackGain;
        }
    }

    /**
     * 技能实例
     */
    private Skill<BattleUnitBlackboard> skill;

    /**
     * 魔法值组件
     */
    private ManaComponent mana;

    // 默认魔法值配置
    private static final float DEFAULT_MAX_MANA = 100f;
    private static final float DEFAULT_REGEN_RATE = 10f;    // 每秒恢复10点
    private static final float DEFAULT_ATTACK_GAIN = 20f;    // 每次攻击获得20点

    public BattleUnitBlackboard(BattleCharacter self, Battlefield battlefield) {
        this.self = self;
        this.battlefield = battlefield;

        // 初始化技能和魔法值
        this.skill = createSkillForCard(self.getCard());
        this.mana = createManaForCard(self.getCard());

        stateMachine = new BaseStateMachine<>();
        stateMachine.setOwn(this);
        stateMachine.setInitialState(States.NORMAL_STATE);
    }

    /**
     * 根据卡牌类型创建技能实例
     * Factory method that creates appropriate skill implementations based on card configuration.
     *
     * @param card the card containing skill configuration
     * @return the created skill instance
     */
    private Skill<BattleUnitBlackboard> createSkillForCard(Card card) {
        if (card == null) {
            return new BasicSkill(); // 默认基础技能
        }

        SkillType skillType = card.getSkillType();
        if (skillType == null) {
            return new BasicSkill(); // 默认基础技能
        }

        // Create skill context from card parameters
        SkillContext context = SkillContext.of(
                card.getSkillValue(),
                card.getSkillRange(),
                card.getSkillDuration(),
                card.getSkillDamageType()
        );

        switch (skillType) {
            case BASIC:
                return new BasicSkill();
            case HEAL:
                return new HealSkill(context);
            case AOE:
                return new AoeSkill(context);
            case BUFF:
                return new BuffSkill(context);
            case DEBUFF:
                return new DebuffSkill(context);
            default:
                if (Gdx.app != null) {
                    Gdx.app.error("BattleUnitBlackboard",
                            "Unknown skill type: " + skillType + ", using BasicSkill");
                }
                return new BasicSkill();
        }
    }

    /**
     * 根据卡牌类型创建魔法值组件
     */
    private ManaComponent createManaForCard(Card card) {
        float maxMana = DEFAULT_MAX_MANA;
        float regenRate = DEFAULT_REGEN_RATE;
        float attackGain = DEFAULT_ATTACK_GAIN;

        // 优先级：Card配置 > 角色类型默认 > 全局默认
        if (card != null) {
            if (card.getMaxMana() > 0) {
                maxMana = card.getMaxMana();
            } else {
                switch (card.getType()) {
                    case MAGE:
                        maxMana = 150f;      // 法师上限更高
                        break;
                    case WARRIOR:
                        maxMana = 80f;    // 战士较低
                        break;
                    case ARCHER:
                        maxMana = 100f;      // 弓手中等
                        break;
                    case ASSASSIN:
                        maxMana = 90f;    // 刺客中等
                        break;
                    case TANK:
                        maxMana = 70f;       // 坦克较低
                        break;
                    default:
                        maxMana = DEFAULT_MAX_MANA;
                }
            }

        }
        return new ManaComponent(maxMana, regenRate, attackGain);

    }

    /**
     * 更新魔法值
     * @param delta 时间增量（秒）
     */
    public void updateMana(float delta) {
        // 死亡时不更新魔法值
        if (self.isDead()) {
            return;
        }
        if (mana == null) return;
        // 时间恢复
        float timeGain = mana.getRegenRate() * delta;
        mana.gainMana(timeGain);

        // 检查是否满并尝试释放技能
        if (mana.isFull()) {
            tryCastSkill();
        }
    }

    /**
     * 攻击时增加魔法值
     */
    public void onAttackGainMana() {
        if (mana == null) return;
        mana.gainMana(mana.getAttackGain());
    }

    /**
     * 尝试释放技能
     * 只在空闲状态且魔法值满时释放
     * @return 是否成功释放技能
     */
    public boolean tryCastSkill() {
        if (mana == null || skill == null) return false;
        if (!mana.isFull()) return false;

        // 只在空闲状态释放
        if (!stateMachine.getCurrent().isState(States.NORMAL_STATE)) {
            return false;
        }

        try {
            skill.cast(this);
            // 释放成功后清零魔法值
            mana.reset();
            return true;
        } catch (Exception e) {
            Gdx.app.error("SkillSystem", "技能释放失败: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取技能实例
     */
    public Skill<BattleUnitBlackboard> getSkill() {
        return skill;
    }

    public BattleCharacter getSelf() {
        return self;
    }

    public Battlefield getBattlefield() {
        return battlefield;
    }

    public BattleCharacter getTarget() {
        return target;
    }

    public void setTarget(BattleCharacter target) {
        this.target = target;
    }

    public float getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(float t) {
        this.currentTime = t;
    }

    @Override
    public boolean handleMessage(Telegram telegram) {
        int message = telegram.message;
        switch (message) {
            case MessageConstants.attack:
                onMessageAttack(telegram);
                break;
            case MessageConstants.doAttack:
                onMessageDoAttack(telegram);
                break;
            case MessageConstants.endAttack:
                onMessageEndAttackAct(telegram);
                break;
        }
        return false;
    }

    private void onMessageEndAttackAct(Telegram telegram) {
        this.stateMachine.switchState(States.NORMAL_STATE);
    }

    private void onMessageAttack(Telegram telegram) {
        if (this.stateMachine.getCurrent().isState(AttackState.INSTANCE)) return;
        if (this.self.attackCooldown > 0) return;
        this.stateMachine.switchState(States.ATTACK_STATE);
    }

    private void onMessageDoAttack(Telegram telegram) {
        if (!couldDamage) return;

        // 获取攻击者卡牌类型
        Card.CardType attackerType = self.getCard().getType();
        float damage = computeDamage(self, target);

        // 检查是否需要使用投掷物（远程攻击者）
        boolean shouldUseProjectile = false;
        Projectile.ProjectileType projectileType = null;

        if (attackerType == Card.CardType.ARCHER) {
            shouldUseProjectile = true;
            projectileType = Projectile.ProjectileType.ARROW;
        } else if (attackerType == Card.CardType.MAGE) {
            shouldUseProjectile = true;
            projectileType = Projectile.ProjectileType.MAGIC_BALL;
        }

        if (shouldUseProjectile) {
            // 远程攻击者：创建投掷物
            createProjectile(self, target, damage, projectileType);
        } else {
            // 近战攻击者：直接造成伤害
            createDirectDamage(self, target, damage);
        }

        // 攻击成功后增加魔法值
        onAttackGainMana();
    }

    /**
     * 创建投掷物
     */
    private void createProjectile(BattleCharacter source, BattleCharacter target, float damage,
                                  Projectile.ProjectileType projectileType) {
        // 从角色位置发射投掷物（稍微偏移，避免从角色中心发射）
        float offset = source.getSize() / 2f + 5f;
        float startX = source.getX();
        float startY = source.getY() + offset;

        // 获取战场中的ProjectileManager
        Battlefield battlefield = getBattlefield();
        ProjectileManager projectileManager = battlefield.getProjectileManager();
        if (projectileManager != null) {
            projectileManager.createProjectile(startX, startY, source, target, damage, projectileType);
        } else {
            // 如果ProjectileManager不可用，使用直接伤害
            createDirectDamage(source, target, damage);
        }
    }

    /**
     * 创建直接伤害
     */
    private void createDirectDamage(BattleCharacter source, BattleCharacter target, float damage) {
        DamageEvent de = new DamageEvent();
        de.setFrom(source);
        de.setTo(target);

        // 根据攻击者类型确定伤害类型
        Damage.DamageType damageType = Damage.DamageType.PhySic;
        Card.CardType attackerType = source.getCard().getType();
        if (attackerType == Card.CardType.MAGE) {
            damageType = Damage.DamageType.Magic;
        }

        de.setDamage(new Damage(damage, damageType));
        this.getBattlefield().getDamageEventHolder().addModel(de);
    }

    private float computeDamage(BattleCharacter attacker, BattleCharacter defender) {
        CharacterStats as = attacker.getStats();

        return as.getAttack();
    }

    /**
     * 获取角色魔法值（用于渲染）
     */
    public float getCurrentMana() {
        return mana == null ? 0f : mana.getCurrentMana();
    }

    /**
     * 获取魔法值上限（用于渲染）
     */
    public float getMaxMana() {
        return mana == null ? 0f : mana.getMaxMana();
    }

    /**
     * 获取魔法值比例（用于渲染）
     */
    public float getManaRatio() {
        if (mana == null || mana.getMaxMana() == 0f) return 0f;
        return mana.getCurrentMana() / mana.getMaxMana();
    }

    public void update(float delta) {
        // 更新魔法值
        updateMana(delta);

        // 更新临时效果（检查过期)
        this.self.updateTemporaryEffects(this.self.currentTime);

        this.stateMachine.update(delta);
        this.self.attackCooldown -= delta;
        this.self.attackCooldown = Math.max( this.self.attackCooldown, 0);
    }

    /**
     * 重置魔法值（用于角色重置时）
     */
    public void resetMana() {
        if (mana != null) {
            mana.reset();
        }
    }

    // ==================== 碰撞检测方法（委托给 CollisionDetector）====================

    /**
     * 检测自身是否与另一个角色发生 body 碰撞
     *
     * @param other 另一个角色
     * @return 如果碰撞返回 true
     */
    public boolean collidesWithBody(BattleCharacter other) {
        if (other == null) return false;
        CollisionContext selfContext = CollisionDetector.createContext(self);
        CollisionContext otherContext = CollisionDetector.createContext(other);
        return CollisionDetector.checkCharacterBodyCollision(selfContext, otherContext);
    }

    /**
     * 检测自身是否与另一个角色发生 face 碰撞
     *
     * @param other 另一个角色
     * @return 如果碰撞返回 true
     */
    public boolean collidesWithFace(BattleCharacter other) {
        if (other == null) return false;
        CollisionContext selfContext = CollisionDetector.createContext(self);
        CollisionContext otherContext = CollisionDetector.createContext(other);
        return CollisionDetector.checkCharacterFaceCollision(selfContext, otherContext);
    }

    /**
     * 检测自身是否与另一个角色发生碰撞（face 或 body）
     *
     * @param other 另一个角色
     * @return 如果碰撞返回 true
     */
    public boolean collidesWithCharacter(BattleCharacter other) {
        if (other == null) return false;
        CollisionContext selfContext = CollisionDetector.createContext(self);
        CollisionContext otherContext = CollisionDetector.createContext(other);
        return CollisionDetector.checkCharacterCollision(selfContext, otherContext);
    }

    /**
     * 检测自身是否与投射物发生碰撞
     *
     * @param projectile 投射物
     * @return 如果碰撞返回 true
     */
    public boolean collidesWithProjectile(Projectile projectile) {
        if (projectile == null) return false;
        CollisionContext selfContext = CollisionDetector.createContext(self);
        return CollisionDetector.checkCharacterProjectileCollision(selfContext, projectile);
    }

    /**
     * 获取自身的碰撞上下文
     *
     * @return 碰撞上下文
     */
    public CollisionContext getCollisionContext() {
        return CollisionDetector.createContext(self);
    }
}
