package com.voidvvv.autochess.updater;

import com.badlogic.gdx.math.Vector2;
import com.voidvvv.autochess.manage.MovementEffectManager;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.logic.MovementCalculator;

public class BattleCharacterUpdater {

    private final MovementCalculator movementCalculator = new MovementCalculator();
    private final MovementEffectManager effectManager = new MovementEffectManager();

    public void update(BattleCharacter character, float delta) {
        // 1. 更新效果生命周期
        effectManager.updateEffects(character.moveComponent, delta);

        // 2. 计算总移动向量
        Vector2 totalMoveVal = movementCalculator.calculateTotalMove(character.moveComponent);

        // 3. 应用到位置
        character.setX(character.getX() + totalMoveVal.x * delta);
        character.setY(character.getY() + totalMoveVal.y * delta);
    }

    /**
     * 获取效果管理器（供外部使用）
     * @return 效果管理器实例
     */
    public MovementEffectManager getEffectManager() {
        return effectManager;
    }
}
