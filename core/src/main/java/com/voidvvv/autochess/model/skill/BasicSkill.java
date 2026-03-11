package com.voidvvv.autochess.model.skill;

import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.Skill;

/**
 * 基础技能实现
 * MVP阶段：控制台打印技能释放信息
 */
public class BasicSkill implements Skill<BattleUnitBlackboard> {

    @Override
    public String getName() {
        return "基础技能";
    }

    @Override
    public void cast(BattleUnitBlackboard blackboard) {
        System.out.println("[" + blackboard.getSelf().getName() + "] 释放了技能: " + getName());
    }
}
