package com.voidvvv.autochess.battle.collision;

import com.badlogic.gdx.math.Rectangle;

/**
 * 碰撞上下文（不可变）
 * 包含计算后的 realface 和 realbottom 矩形
 * 用于碰撞检测时避免重复计算
 */
public class CollisionContext {

    private final Rectangle realFace;
    private final Rectangle realBottom;
    private final float centerX;
    private final float centerY;

    /**
     * 创建碰撞上下文
     * @param realFace 计算后的正面碰撞框（攻击区域）
     * @param realBottom 计算后的底部碰撞框（身体区域）
     * @param centerX 角色中心X坐标
     * @param centerY 角色中心Y坐标
     */
    public CollisionContext(Rectangle realFace, Rectangle realBottom,
                           float centerX, float centerY) {
        // 创建副本确保不可变性
        this.realFace = new Rectangle(realFace);
        this.realBottom = new Rectangle(realBottom);
        this.centerX = centerX;
        this.centerY = centerY;
    }

    /**
     * 获取正面碰撞框（用于攻击判定）
     */
    public Rectangle getRealFace() {
        return realFace;
    }

    /**
     * 获取底部碰撞框（用于身体碰撞）
     */
    public Rectangle getRealBottom() {
        return realBottom;
    }

    /**
     * 获取角色中心X坐标
     */
    public float getCenterX() {
        return centerX;
    }

    /**
     * 获取角色中心Y坐标
     */
    public float getCenterY() {
        return centerY;
    }

    @Override
    public String toString() {
        return "CollisionContext{" +
                "realFace=" + realFace +
                ", realBottom=" + realBottom +
                ", centerX=" + centerX +
                ", centerY=" + centerY +
                '}';
    }
}
