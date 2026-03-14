package com.voidvvv.autochess.manage;

import com.voidvvv.autochess.battle.collision.CollisionContext;
import com.voidvvv.autochess.battle.collision.CollisionDetector;
import com.voidvvv.autochess.event.CollisionEvent;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Battlefield;
import com.voidvvv.autochess.model.Projectile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 碰撞管理器
 * 遵循 Manager 模式，协调所有碰撞检测并通过事件系统发送碰撞事件
 *
 * 职责：
 * - 管理碰撞上下文缓存
 * - 执行碰撞检测（角色-角色、角色-投射物、投射物-投射物）
 * - 发送 CollisionEvent 通知其他系统
 */
public class CollisionManager {

    /** 碰撞上下文缓存，避免每帧重复计算 */
    private final Map<BattleCharacter, CollisionContext> contextCache = new HashMap<>();

    /** 事件系统引用 */
    private final GameEventSystem eventSystem;

    /** 是否启用角色间碰撞检测 */
    private boolean characterCollisionEnabled = true;

    /** 是否启用投射物碰撞检测 */
    private boolean projectileCollisionEnabled = true;

    /** 是否启用投射物间碰撞检测 */
    private boolean projectileProjectileCollisionEnabled = false;

    public CollisionManager(GameEventSystem eventSystem) {
        this.eventSystem = eventSystem;
    }

    // ==================== 缓存管理 ====================

    /**
     * 更新所有角色的碰撞上下文缓存
     * 在每帧碰撞检测前调用
     *
     * @param characters 所有角色列表
     */
    public void updateCache(List<BattleCharacter> characters) {
        contextCache.clear();
        if (characters == null) return;

        for (BattleCharacter character : characters) {
            if (character != null && !character.isDead()) {
                CollisionContext context = CollisionDetector.createContext(character);
                contextCache.put(character, context);
            }
        }
    }

    /**
     * 获取角色的碰撞上下文
     *
     * @param character 角色
     * @return 碰撞上下文，如果不存在返回 null
     */
    public CollisionContext getContext(BattleCharacter character) {
        return contextCache.get(character);
    }

    /**
     * 清除缓存（战斗结束时调用）
     */
    public void clear() {
        contextCache.clear();
    }

    // ==================== 碰撞检测 ====================

    /**
     * 检测所有角色间的碰撞
     * 发送 CHARACTER_CHARACTER 类型的 CollisionEvent
     *
     * @param battlefield 战场
     */
    public void checkCharacterCollisions(Battlefield battlefield) {
        if (!characterCollisionEnabled || battlefield == null) return;

        List<BattleCharacter> playerChars = battlefield.getPlayerCharacters();
        List<BattleCharacter> enemyChars = battlefield.getEnemyCharacters();

        // 检测玩家与敌人角色的碰撞
        for (BattleCharacter player : playerChars) {
            CollisionContext playerCtx = contextCache.get(player);
            if (playerCtx == null) continue;

            for (BattleCharacter enemy : enemyChars) {
                CollisionContext enemyCtx = contextCache.get(enemy);
                if (enemyCtx == null) continue;

                // 检测 face 碰撞（攻击区域）
                if (CollisionDetector.checkCharacterFaceCollision(playerCtx, enemyCtx)) {
                    emitCollisionEvent(CollisionEvent.CollisionType.CHARACTER_CHARACTER,
                            player, enemy, "face");
                }
            }
        }
    }

    /**
     * 检测所有角色与投射物的碰撞
     * 发送 CHARACTER_PROJECTILE 类型的 CollisionEvent
     *
     * @param battlefield   战场
     * @param projectiles   投射物列表
     */
    public void checkProjectileCollisions(Battlefield battlefield, List<Projectile> projectiles) {
        if (!projectileCollisionEnabled || battlefield == null || projectiles == null) return;

        List<BattleCharacter> allChars = battlefield.getCharacters();

        for (Projectile projectile : projectiles) {
            if (projectile == null || projectile.hasHit()) continue;

            BattleCharacter source = projectile.getSource();

            for (BattleCharacter character : allChars) {
                if (character == null || character.isDead()) continue;

                // 跳过来源角色（不能打自己）
                if (character == source) continue;

                // 跳过同阵营（默认关闭友军伤害）
                if (source != null && source.isEnemy() == character.isEnemy()) continue;

                CollisionContext charCtx = contextCache.get(character);
                if (charCtx == null) continue;

                // 检测碰撞
                if (CollisionDetector.checkCharacterProjectileCollision(charCtx, projectile)) {
                    emitCollisionEvent(CollisionEvent.CollisionType.CHARACTER_PROJECTILE,
                            projectile, character, "body");

                    // 标记投射物已命中
                    projectile.hit();
                    break; // 投射物命中后不再检测其他目标
                }
            }
        }
    }

    /**
     * 检测所有投射物间的碰撞
     * 发送 PROJECTILE_PROJECTILE 类型的 CollisionEvent
     *
     * @param projectiles 投射物列表
     */
    public void checkProjectileProjectileCollisions(List<Projectile> projectiles) {
        if (!projectileProjectileCollisionEnabled || projectiles == null) return;

        for (int i = 0; i < projectiles.size(); i++) {
            Projectile p1 = projectiles.get(i);
            if (p1 == null || p1.hasHit()) continue;

            for (int j = i + 1; j < projectiles.size(); j++) {
                Projectile p2 = projectiles.get(j);
                if (p2 == null || p2.hasHit()) continue;

                if (CollisionDetector.checkProjectileCollision(p1, p2)) {
                    emitCollisionEvent(CollisionEvent.CollisionType.PROJECTILE_PROJECTILE,
                            p1, p2, null);
                }
            }
        }
    }

    /**
     * 执行所有碰撞检测（便捷方法）
     *
     * @param battlefield 战场
     */
    public void checkAllCollisions(Battlefield battlefield) {
        if (battlefield == null) return;

        // 更新缓存
        updateCache(battlefield.getCharacters());

        // 检测角色间碰撞
        checkCharacterCollisions(battlefield);

        // 检测投射物碰撞
        ProjectileManager projectileManager = battlefield.getProjectileManager();
        if (projectileManager != null) {
            List<Projectile> projectiles = projectileManager.getProjectiles();
            checkProjectileCollisions(battlefield, projectiles);
            checkProjectileProjectileCollisions(projectiles);
        }
    }

    // ==================== 事件发送 ====================

    /**
     * 发送碰撞事件
     */
    private void emitCollisionEvent(CollisionEvent.CollisionType type,
                                    Object source, Object target, Object extra) {
        if (eventSystem == null) return;

        CollisionEvent event = new CollisionEvent();
        event.setTimestamp(System.currentTimeMillis());
        event.setCollisionType(type);
        event.setSource(source);
        event.setTarget(target);
        if (extra != null) {
            event.setCollisionArea(extra.toString());
        }

        eventSystem.postEvent(event);
    }

    // ==================== 配置方法 ====================

    public void setCharacterCollisionEnabled(boolean enabled) {
        this.characterCollisionEnabled = enabled;
    }

    public void setProjectileCollisionEnabled(boolean enabled) {
        this.projectileCollisionEnabled = enabled;
    }

    public void setProjectileProjectileCollisionEnabled(boolean enabled) {
        this.projectileProjectileCollisionEnabled = enabled;
    }

    public boolean isCharacterCollisionEnabled() {
        return characterCollisionEnabled;
    }

    public boolean isProjectileCollisionEnabled() {
        return projectileCollisionEnabled;
    }

    public boolean isProjectileProjectileCollisionEnabled() {
        return projectileProjectileCollisionEnabled;
    }
}
