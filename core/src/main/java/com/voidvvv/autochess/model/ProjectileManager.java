package com.voidvvv.autochess.model;

import com.voidvvv.autochess.model.battle.DamageEventHolder;
import com.voidvvv.autochess.model.battle.Damage;
import com.voidvvv.autochess.model.event.DamageEvent;
import com.voidvvv.autochess.utils.CharacterCamp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 投掷物管理器
 * 管理战场上的所有投掷物，负责更新、碰撞检测和事件生成
 */
public class ProjectileManager {

    private final List<Projectile> projectiles = new ArrayList<>();
    private final DamageEventHolder damageEventHolder;

    public ProjectileManager(DamageEventHolder damageEventHolder) {
        this.damageEventHolder = damageEventHolder;
    }

    /**
     * 创建并添加投掷物
     */
    public void createProjectile(float startX, float startY, BattleCharacter source,
                                 BattleCharacter target, float damage, Projectile.ProjectileType type) {
        Projectile projectile = new Projectile(startX, startY, source, target, damage, type);
        projectiles.add(projectile);
    }

    /**
     * 更新所有投掷物
     * @param deltaTime 时间增量（秒）
     * @param battlefield 战场，用于碰撞检测
     */
    public void update(float deltaTime, Battlefield battlefield) {
        if (projectiles.isEmpty()) {
            return;
        }

        // 遍历所有投掷物进行更新
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();

            // 更新投掷物位置
            boolean shouldRemove = projectile.update(deltaTime);

            // 检查碰撞
            if (!shouldRemove) {
                checkCollision(projectile, battlefield);
                shouldRemove = projectile.hasHit();
            }

            // 如果投掷物需要被移除（命中、超出范围等）
            if (shouldRemove) {
                iterator.remove();
            }
        }
    }

    /**
     * 检查投掷物与战场中所有角色的碰撞
     */
    private void checkCollision(Projectile projectile, Battlefield battlefield) {
        if (projectile.hasHit()) {
            return; // 已经命中，不再检测
        }

        BattleCharacter source = projectile.getSource();
        if (source == null) {
            return;
        }

        // 获取可能的碰撞目标
        List<BattleCharacter> potentialTargets;
        if (source.isEnemy()) {
            // 如果来源是敌人，检查与玩家单位的碰撞
            potentialTargets = battlefield.getPlayerCharacters();
        } else {
            // 如果来源是玩家单位，检查与敌人单位的碰撞
            potentialTargets = battlefield.getEnemyCharacters();
        }

        // 排除来源自身
        potentialTargets.remove(source);

        // 检查与每个目标的碰撞
        for (BattleCharacter target : potentialTargets) {
            if (target.collidesWith(projectile)) {
                // 命中目标，创建伤害事件
                createDamageEvent(projectile, target);
                projectile.hit();
                return; // 命中一个目标后停止检测
            }
        }
    }

    /**
     * 创建伤害事件
     */
    private void createDamageEvent(Projectile projectile, BattleCharacter target) {
        DamageEvent damageEvent = new DamageEvent();

        // 设置伤害来源和目标
        damageEvent.setFrom(projectile.getSource());
        damageEvent.setTo(target);

        // 设置伤害值和类型（根据投掷物类型）
        float damageValue = projectile.getDamage();
        Damage.DamageType damageType;

        if (projectile.getType() == Projectile.ProjectileType.ARROW) {
            damageType = Damage.DamageType.PhySic;
        } else {
            damageType = Damage.DamageType.Magic;
        }

        damageEvent.setDamage(new Damage(damageValue, damageType));

        // 添加到伤害事件持有者
        damageEventHolder.addModel(damageEvent);
    }

    /**
     * 获取所有活跃的投掷物
     */
    public List<Projectile> getProjectiles() {
        return new ArrayList<>(projectiles);
    }

    /**
     * 清除所有投掷物
     */
    public void clear() {
        projectiles.clear();
    }

    /**
     * 获取投掷物数量
     */
    public int getProjectileCount() {
        return projectiles.size();
    }

    /**
     * 批量创建投掷物（用于测试或特殊技能）
     */
    public void createProjectiles(List<Projectile> projectiles) {
        this.projectiles.addAll(projectiles);
    }

    /**
     * 获取指定来源的投掷物
     */
    public List<Projectile> getProjectilesBySource(BattleCharacter source) {
        List<Projectile> result = new ArrayList<>();
        for (Projectile projectile : projectiles) {
            if (projectile.getSource() == source) {
                result.add(projectile);
            }
        }
        return result;
    }
}