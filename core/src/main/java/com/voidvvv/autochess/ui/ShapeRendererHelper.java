package com.voidvvv.autochess.ui;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * ShapeRenderer 辅助类
 * 管理 ShapeRenderer 的状态，减少批次数
 */
public class ShapeRendererHelper {
    private final ShapeRenderer shapeRenderer;
    private ShapeRenderer.ShapeType currentShapeType;

    public ShapeRendererHelper(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        this.currentShapeType = null;
    }

    /**
     * 设置 ShapeType，只在需要改变时调用
     */
    public void setShapeType(ShapeRenderer.ShapeType shapeType) {
        if (currentShapeType != shapeType) {
            shapeRenderer.set(shapeType);
            currentShapeType = shapeType;
        }
    }

    /**
     * 获取当前的 ShapeRenderer
     */
    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    /**
     * 重置当前状态
     */
    public void reset() {
        currentShapeType = null;
    }
}
