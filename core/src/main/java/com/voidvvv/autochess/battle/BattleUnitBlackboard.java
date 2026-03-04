package com.voidvvv.autochess.battle;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Battlefield;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.Projectile;
import com.voidvvv.autochess.model.ProjectileManager;
import com.voidvvv.autochess.model.CharacterStats;
import com.voidvvv.autochess.model.battle.Damage;
import com.voidvvv.autochess.model.event.DamageEvent;
import com.voidvvv.autochess.msg.MessageConstants;
import com.voidvvv.autochess.sm.machine.BaseStateMachine;
import com.voidvvv.autochess.sm.machine.StateMachine;
import com.voidvvv.autochess.sm.state.common.AttackState;
import com.voidvvv.autochess.sm.state.common.States;

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

    public BattleUnitBlackboard(BattleCharacter self, Battlefield battlefield) {
        this.self = self;
        this.battlefield = battlefield;

        stateMachine = new BaseStateMachine<>();
        stateMachine.setOwn(this);
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

    private void doSomething() {

    }

    public void update(float delta) {
        this.stateMachine.update(delta);
        this.self.attackCooldown -= delta;
        this.self.attackCooldown = Math.max( this.self.attackCooldown, 0);
    }
}
