package com.voidvvv.autochess.model;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * 基础碰撞数据模型
 * 包含碰撞框的偏移和尺寸信息
 *
 * realface 和 realbottom 的计算公式：
 * realface.x = 角色位置.x - base.x + face.x
 * realface.y = 角色位置.y - base.y + face.y
 * realbottom.x = 角色位置.x - base.x + bottom.x
 * realbottom.y = 角色位置.y - base.y + bottom.y
 */
public class BaseCollision {
    /** 底部碰撞框（身体碰撞区域）- 存储偏移和尺寸 */
    public Rectangle bottom = new Rectangle();
    /** 正面碰撞框（攻击判定区域）- 存储偏移和尺寸 */
    public Rectangle face = new Rectangle();
    /** 基准点偏移（用于计算实际碰撞框位置） */
    public Vector2 base = new Vector2();

    /**
     * 默认构造函数
     * 初始化合理的默认碰撞框尺寸
     */
    public BaseCollision() {
        // 默认碰撞框尺寸（适用于标准角色）
        this.bottom.set(-20, -20, 40, 40);  // 身体碰撞框：40x40，以角色为中心
        this.face.set(-30, 0, 60, 40);       // 正面碰撞框：60x40，略微偏前
        this.base.set(0, 0);                 // 基准点：角色中心
    }

    /**
     * 设置底部碰撞框
     * @param x 相对于 base 的 X 偏移
     * @param y 相对于 base 的 Y 偏移
     * @param width 碰撞框宽度
     * @param height 碰撞框高度
     * @return this（支持链式调用）
     */
    public BaseCollision setBottom(float x, float y, float width, float height) {
        this.bottom.set(x, y, width, height);
        return this;
    }

    /**
     * 设置正面碰撞框
     * @param x 相对于 base 的 X 偏移
     * @param y 相对于 base 的 Y 偏移
     * @param width 碰撞框宽度
     * @param height 碰撞框高度
     * @return this（支持链式调用）
     */
    public BaseCollision setFace(float x, float y, float width, float height) {
        this.face.set(x, y, width, height);
        return this;
    }

    /**
     * 设置基准点偏移
     * @param x X 偏移
     * @param y Y 偏移
     * @return this（支持链式调用）
     */
    public BaseCollision setBase(float x, float y) {
        this.base.set(x, y);
        return this;
    }

    /**
     * 重置为默认值
     */
    public void reset() {
        this.bottom.set(-20, -20, 40, 40);
        this.face.set(-30, 0, 60, 40);
        this.base.set(0, 0);
    }

    /**
     * 从另一个 BaseCollision 复制数据
     * @param other 源碰撞数据
     */
    public void copyFrom(BaseCollision other) {
        if (other == null) return;
        this.bottom.set(other.bottom);
        this.face.set(other.face);
        this.base.set(other.base);
    }

    @Override
    public String toString() {
        return "BaseCollision{" +
                "bottom=" + bottom +
                ", face=" + face +
                ", base=" + base +
                '}';
    }
}
