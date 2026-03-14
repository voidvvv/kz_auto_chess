package com.voidvvv.autochess.battle.collision;

import com.badlogic.gdx.math.Rectangle;
import com.voidvvv.autochess.model.BaseCollision;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Projectile;

/**
 * 碰撞检测器
 * 纯静态方法工具类，包含所有碰撞计算逻辑
 * 无状态，无副作用 - 纯函数
 */
public final class CollisionDetector {

    // 静态临时矩形，避免 GC
    private static final Rectangle TEMP_RECT_1 = new Rectangle();
    private static final Rectangle TEMP_RECT_2 = new Rectangle();

    private CollisionDetector() {
        // 防止实例化
    }

    // ==================== 碰撞框计算 ====================

    /**
     * 计算 realface 矩形
     * 公式: realface.x = characterX - base.x + face.x
     *
     * @param collision   BaseCollision 对象
     * @param characterX  角色位置 X
     * @param characterY  角色位置 Y
     * @param output      输出矩形（复用避免 GC）
     * @return 计算后的 realface 矩形
     */
    public static Rectangle calculateRealFace(BaseCollision collision,
                                              float characterX, float characterY,
                                              Rectangle output) {
        if (collision == null) {
            return output.set(0, 0, 0, 0);
        }
        output.x = characterX - collision.base.x + collision.face.x;
        output.y = characterY - collision.base.y + collision.face.y;
        output.width = collision.face.width;
        output.height = collision.face.height;
        return output;
    }

    /**
     * 计算 realbottom 矩形
     * 公式: realbottom.x = characterX - base.x + bottom.x
     *
     * @param collision   BaseCollision 对象
     * @param characterX  角色位置 X
     * @param characterY  角色位置 Y
     * @param output      输出矩形（复用避免 GC）
     * @return 计算后的 realbottom 矩形
     */
    public static Rectangle calculateRealBottom(BaseCollision collision,
                                                float characterX, float characterY,
                                                Rectangle output) {
        if (collision == null) {
            return output.set(0, 0, 0, 0);
        }
        output.x = characterX - collision.base.x + collision.bottom.x;
        output.y = characterY - collision.base.y + collision.bottom.y;
        output.width = collision.bottom.width;
        output.height = collision.bottom.height;
        return output;
    }

    /**
     * 为 BattleCharacter 创建 CollisionContext
     *
     * @param character 角色对象
     * @return 碰撞上下文（不可变）
     */
    public static CollisionContext createContext(BattleCharacter character) {
        if (character == null) {
            return new CollisionContext(new Rectangle(), new Rectangle(), 0, 0);
        }

        BaseCollision bc = character.baseCollision;
        Rectangle realFace = new Rectangle();
        Rectangle realBottom = new Rectangle();

        calculateRealFace(bc, character.getX(), character.getY(), realFace);
        calculateRealBottom(bc, character.getX(), character.getY(), realBottom);

        return new CollisionContext(realFace, realBottom, character.getX(), character.getY());
    }

    // ==================== 基础碰撞检测 ====================

    /**
     * 检测两个矩形是否重叠
     *
     * @param a 矩形 A
     * @param b 矩形 B
     * @return 如果重叠返回 true
     */
    public static boolean rectanglesOverlap(Rectangle a, Rectangle b) {
        if (a == null || b == null) return false;
        return a.overlaps(b);
    }

    /**
     * 检测矩形与圆形是否碰撞
     *
     * @param rect     矩形
     * @param circleX  圆心 X
     * @param circleY  圆心 Y
     * @param radius   圆半径
     * @return 如果碰撞返回 true
     */
    public static boolean rectangleCircleCollision(Rectangle rect,
                                                   float circleX, float circleY,
                                                   float radius) {
        if (rect == null) return false;

        // 找到矩形上距离圆心最近的点
        float closestX = Math.max(rect.x, Math.min(circleX, rect.x + rect.width));
        float closestY = Math.max(rect.y, Math.min(circleY, rect.y + rect.height));

        // 计算圆心到最近点的距离
        float dx = circleX - closestX;
        float dy = circleY - closestY;

        return (dx * dx + dy * dy) < (radius * radius);
    }

    /**
     * 检测两个圆形是否碰撞
     *
     * @param x1 圆1 X
     * @param y1 圆1 Y
     * @param r1 圆1 半径
     * @param x2 圆2 X
     * @param y2 圆2 Y
     * @param r2 圆2 半径
     * @return 如果碰撞返回 true
     */
    public static boolean circleCircleCollision(float x1, float y1, float r1,
                                                float x2, float y2, float r2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float distSq = dx * dx + dy * dy;
        float combinedR = r1 + r2;
        return distSq < combinedR * combinedR;
    }

    // ==================== 角色碰撞检测 ====================

    /**
     * 检测两个角色的 face 碰撞（攻击区域碰撞）
     *
     * @param contextA 角色 A 的碰撞上下文
     * @param contextB 角色 B 的碰撞上下文
     * @return 如果 face 碰撞返回 true
     */
    public static boolean checkCharacterFaceCollision(CollisionContext contextA,
                                                      CollisionContext contextB) {
        if (contextA == null || contextB == null) return false;
        return rectanglesOverlap(contextA.getRealFace(), contextB.getRealFace());
    }

    /**
     * 检测两个角色的 body 碰撞（身体碰撞）
     *
     * @param contextA 角色 A 的碰撞上下文
     * @param contextB 角色 B 的碰撞上下文
     * @return 如果 body 碰撞返回 true
     */
    public static boolean checkCharacterBodyCollision(CollisionContext contextA,
                                                      CollisionContext contextB) {
        if (contextA == null || contextB == null) return false;
        return rectanglesOverlap(contextA.getRealBottom(), contextB.getRealBottom());
    }

    /**
     * 检测两个角色是否碰撞（face 或 body 任一碰撞即返回 true）
     *
     * @param contextA 角色 A 的碰撞上下文
     * @param contextB 角色 B 的碰撞上下文
     * @return 如果碰撞返回 true
     */
    public static boolean checkCharacterCollision(CollisionContext contextA,
                                                  CollisionContext contextB) {
        return checkCharacterFaceCollision(contextA, contextB) ||
               checkCharacterBodyCollision(contextA, contextB);
    }

    // ==================== 投射物碰撞检测 ====================

    /**
     * 检测角色与投射物碰撞（使用角色的 realbottom）
     *
     * @param characterContext 角色碰撞上下文
     * @param projectileX      投射物 X
     * @param projectileY      投射物 Y
     * @param projectileRadius 投射物半径
     * @return 如果碰撞返回 true
     */
    public static boolean checkCharacterProjectileCollision(CollisionContext characterContext,
                                                            float projectileX, float projectileY,
                                                            float projectileRadius) {
        if (characterContext == null) return false;
        return rectangleCircleCollision(characterContext.getRealBottom(),
                                        projectileX, projectileY, projectileRadius);
    }

    /**
     * 检测角色与投射物碰撞（使用 Projectile 对象）
     *
     * @param characterContext 角色碰撞上下文
     * @param projectile       投射物对象
     * @return 如果碰撞返回 true
     */
    public static boolean checkCharacterProjectileCollision(CollisionContext characterContext,
                                                            Projectile projectile) {
        if (characterContext == null || projectile == null) return false;
        return checkCharacterProjectileCollision(characterContext,
                                                 projectile.getX(), projectile.getY(),
                                                 projectile.getCollisionRadius());
    }

    /**
     * 检测两个投射物是否碰撞
     *
     * @param x1 投射物1 X
     * @param y1 投射物1 Y
     * @param r1 投射物1 半径
     * @param x2 投射物2 X
     * @param y2 投射物2 Y
     * @param r2 投射物2 半径
     * @return 如果碰撞返回 true
     */
    public static boolean checkProjectileCollision(float x1, float y1, float r1,
                                                   float x2, float y2, float r2) {
        return circleCircleCollision(x1, y1, r1, x2, y2, r2);
    }

    /**
     * 检测两个投射物是否碰撞（使用 Projectile 对象）
     *
     * @param p1 投射物1
     * @param p2 投射物2
     * @return 如果碰撞返回 true
     */
    public static boolean checkProjectileCollision(Projectile p1, Projectile p2) {
        if (p1 == null || p2 == null) return false;
        return checkProjectileCollision(p1.getX(), p1.getY(), p1.getCollisionRadius(),
                                        p2.getX(), p2.getY(), p2.getCollisionRadius());
    }

    // ==================== 便捷方法 ====================

    /**
     * 使用临时矩形计算 realface（用于快速检测，非线程安全）
     */
    public static Rectangle calculateRealFaceTemp(BaseCollision collision,
                                                  float characterX, float characterY) {
        return calculateRealFace(collision, characterX, characterY, TEMP_RECT_1);
    }

    /**
     * 使用临时矩形计算 realbottom（用于快速检测，非线程安全）
     */
    public static Rectangle calculateRealBottomTemp(BaseCollision collision,
                                                    float characterX, float characterY) {
        return calculateRealBottom(collision, characterX, characterY, TEMP_RECT_2);
    }
}
