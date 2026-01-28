package io.github.some_example_name.battle;

import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.model.Battlefield;

/**
 * 行为树黑板：持有当前单位与战场引用，供寻敌、攻击等任务使用
 */
public class BattleUnitBlackboard {
    private final BattleCharacter self;
    private final Battlefield battlefield;
    private BattleCharacter target;
    /** 当前帧用于攻击冷却判断，由外部每帧更新 */
    private float currentTime;

    public BattleUnitBlackboard(BattleCharacter self, Battlefield battlefield) {
        this.self = self;
        this.battlefield = battlefield;
    }

    public BattleCharacter getSelf() { return self; }
    public Battlefield getBattlefield() { return battlefield; }
    public BattleCharacter getTarget() { return target; }
    public void setTarget(BattleCharacter target) { this.target = target; }
    public float getCurrentTime() { return currentTime; }
    public void setCurrentTime(float t) { this.currentTime = t; }
}
