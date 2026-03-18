package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.BattleCharacter;

/**
 * 生命值条渲染器
 * 在角色下方渲染生命值进度条（位于魔法条上方）
 */
public class HealthBarRenderer {

    private static final float DEFAULT_BAR_HEIGHT = 6f;
    private static final float DEFAULT_BAR_WIDTH = 40f;
    private static final float DEFAULT_Y_OFFSET = 8f; // 放在魔法条上方（屏幕Y坐标越大越高，所以offset越小越靠上）

    /**
     * 渲染生命值条（默认样式）
     * @param shapeRenderer ShapeRenderer实例
     * @param blackboard BattleUnitBlackboard实例
     */
    public static void render(ShapeRenderer shapeRenderer, BattleUnitBlackboard blackboard) {
        render(shapeRenderer, blackboard,
               DEFAULT_BAR_WIDTH, DEFAULT_BAR_HEIGHT, DEFAULT_Y_OFFSET,
               Color.RED, Color.DARK_GRAY, Color.WHITE);
    }

    /**
     * 渲染生命值条（可配置样式）
     * @param shapeRenderer ShapeRenderer实例
     * @param blackboard BattleUnitBlackboard实例
     * @param barWidth 条宽度
     * @param barHeight 条高度
     * @param yOffset Y偏移量
     * @param fillColor 填充颜色
     * @param bgColor 背景颜色
     * @param borderColor 边框颜色
     */
    public static void render(ShapeRenderer shapeRenderer,
                             BattleUnitBlackboard blackboard,
                             float barWidth, float barHeight, float yOffset,
                             Color fillColor, Color bgColor, Color borderColor) {
        BattleCharacter character = blackboard.getSelf();

        // 计算生命值
        float currentHp = character.getCurrentHp();
        float maxHp = character.getStats().getHealth();

        // 死亡或无生命值时不渲染
        if (character.isDead() || maxHp <= 0f) {
            return;
        }

        // 计算位置
        float x = character.getX() - barWidth / 2f;
        float y = character.getY() - character.getSize() - yOffset;

        // 计算进度比例
        float ratio = Math.min(1f, Math.max(0f, currentHp / maxHp));
        float filledWidth = barWidth * ratio;

        // 渲染背景
        shapeRenderer.setColor(bgColor);
        shapeRenderer.rect(x, y, barWidth, barHeight);

        // 渲染填充部分
        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x, y, filledWidth, barHeight);

        // 渲染边框（使用rectLine避免覆盖）
        shapeRenderer.setColor(borderColor);
        float borderThickness = 1f;
        shapeRenderer.rectLine(x, y, x + barWidth, y, borderThickness);              // 上边
        shapeRenderer.rectLine(x, y, x, y + barHeight, borderThickness);             // 左边
        shapeRenderer.rectLine(x + barWidth, y, x + barWidth, y + barHeight, borderThickness);  // 右边
        shapeRenderer.rectLine(x, y + barHeight, x + barWidth, y + barHeight, borderThickness);  // 下边
    }
}
