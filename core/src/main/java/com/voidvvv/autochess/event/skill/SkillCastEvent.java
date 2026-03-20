package com.voidvvv.autochess.event.skill;

import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Skill;
import com.voidvvv.autochess.model.SkillType;
import com.voidvvv.autochess.model.skill.SkillContext;

/**
 * 技能释放事件
 * 当角色释放技能时触发，用于通知渲染系统显示技能效果
 */
public class SkillCastEvent implements GameEvent {
    private long timestamp;
    private final BattleCharacter caster;
    private final BattleCharacter target;
    private final Skill<?> skill;
    private final SkillType skillType;
    private final SkillContext context;
    private final float worldX;
    private final float worldY;

    /**
     * 构造函数
     *
     * @param caster  释放者
     * @param target  目标（可为 null，如 AOE 技能）
     * @param skill   技能实例
     * @param skillType 技能类型（用于确定渲染效果类型）
     * @param context 技能上下文（包含范围、持续时间等参数）
     * @param worldX  释放位置 X 坐标
     * @param worldY  释放位置 Y 坐标
     */
    public SkillCastEvent(BattleCharacter caster, BattleCharacter target,
                          Skill<?> skill, SkillType skillType, SkillContext context,
                          float worldX, float worldY) {
        this.timestamp = System.currentTimeMillis();
        this.caster = caster;
        this.target = target;
        this.skill = skill;
        this.skillType = skillType != null ? skillType : SkillType.BASIC;
        this.context = context;
        this.worldX = worldX;
        this.worldY = worldY;
    }

    // ========== Getters ==========

    public BattleCharacter getCaster() {
        return caster;
    }

    public BattleCharacter getTarget() {
        return target;
    }

    public Skill<?> getSkill() {
        return skill;
    }

    public SkillType getSkillType() {
        return skillType;
    }

    public SkillContext getContext() {
        return context;
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldY() {
        return worldY;
    }

    // ========== GameEvent 接口实现 ==========

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SkillCastEvent{" +
                "caster=" + (caster != null ? caster.getCard().getName() : "null") +
                ", target=" + (target != null ? target.getCard().getName() : "null") +
                ", skillName=" + (skill != null ? skill.getName() : "null") +
                ", worldX=" + worldX +
                ", worldY=" + worldY +
                '}';
    }
}
