package com.voidvvv.autochess.model.skill;

import com.badlogic.gdx.Gdx;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.Skill;
import com.voidvvv.autochess.model.skill.exception.InvalidTargetException;
import com.voidvvv.autochess.model.skill.exception.SkillExecutionException;

/**
 * 基础技能实现
 * MVP阶段：记录技能释放信息
 *
 * <p>This class follows the Strategy pattern and implements the Skill interface.
 * It uses proper logging and error handling according to Java coding standards.</p>
 */
public class BasicSkill implements Skill<BattleUnitBlackboard> {

    private static final String TAG = "BasicSkill";
    private static final String SKILL_NAME = "基础技能";

    /**
     * Default constructor for BasicSkill.
     */
    public BasicSkill() {
        // Default constructor
    }

    @Override
    public String getName() {
        return SKILL_NAME;
    }

    @Override
    public void cast(BattleUnitBlackboard blackboard) {
        if (blackboard == null) {
            throw new InvalidTargetException(getName(), "null", "Blackboard cannot be null");
        }

        try {
            if (blackboard.getSelf() != null && Gdx.app != null) {
                Gdx.app.log(TAG, String.format("[%s] 释放了技能: %s",
                        blackboard.getSelf().getName(), getName()));
            }
        } catch (Exception e) {
            String errorMessage = String.format("Failed to cast %s: %s", getName(), e.getMessage());
            if (Gdx.app != null) {
                Gdx.app.error(TAG, errorMessage, e);
            }
            throw new SkillExecutionException(errorMessage, e);
        }
    }
}

