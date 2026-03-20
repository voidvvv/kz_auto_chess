package com.voidvvv.autochess.model.skill;

import com.badlogic.gdx.Gdx;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Skill;
import com.voidvvv.autochess.model.SynergyEffect;
import com.voidvvv.autochess.model.skill.exception.InvalidTargetException;
import com.voidvvv.autochess.model.skill.exception.SkillExecutionException;

/**
 * DEBUFF skill implementation.
 * Applies temporary stat reduction to the caster's current target only.
 *
 * <p>This skill follows the Strategy pattern and implements the Skill interface.
 * It uses BattleCharacter.addTemporaryEffect() for proper duration tracking.</p>
 *
 * <p>Per R4: 对释放者当前的攻击目标施加属性减益 (Target only current attack target)</p>
 */
public class DebuffSkill implements Skill<BattleUnitBlackboard> {

    private static final String TAG = "DebuffSkill";
    private static final String SKILL_NAME = "减益";

    private final SkillContext context;

    /**
     * Creates a debuff skill with default parameters.
     */
    public DebuffSkill() {
        this(SkillContext.DEFAULT);
    }

    /**
     * Creates a debuff skill with the specified context.
     *
     * @param context the skill context containing parameters
     */
    public DebuffSkill(SkillContext context) {
        this.context = context != null ? context : SkillContext.DEFAULT;
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

        BattleCharacter caster = blackboard.getSelf();
        if (caster == null) {
            throw new InvalidTargetException(getName(), "null", "Caster cannot be null");
        }

        if (caster.isDead()) {
            throw new InvalidTargetException(getName(), caster.getName(), "Caster is dead");
        }

        // R4: 对释放者当前的攻击目标施加属性减益 (Current target only)
        BattleCharacter target = caster.getTarget();
        if (target == null) {
            if (Gdx.app != null) {
                Gdx.app.log(TAG, String.format("[%s] has no target, %s has no effect",
                        caster.getName(), getName()));
            }
            return; // No target, skill has no effect
        }

        if (target.isDead()) {
            if (Gdx.app != null) {
                Gdx.app.log(TAG, String.format("[%s] target %s is dead, %s has no effect",
                        caster.getName(), target.getName(), getName()));
            }
            return; // Target is dead, skill has no effect
        }

        try {
            applyDebuffToTarget(caster, target);

            if (Gdx.app != null) {
                Gdx.app.log(TAG, String.format("[%s] cast %s on %s, duration: %.1f, value: %.1f",
                        caster.getName(), getName(), target.getName(),
                        context.skillDuration(), context.skillValue()));
            }
        } catch (Exception e) {
            String errorMessage = String.format("Failed to cast %s for %s: %s",
                    getName(), caster.getName(), e.getMessage());
            if (Gdx.app != null) {
                Gdx.app.error(TAG, errorMessage, e);
            }
            throw new SkillExecutionException(errorMessage, e);
        }
    }

    /**
     * Applies debuff effect to the target using temporary effect system.
     *
     * @param caster the casting character
     * @param target the target character to debuff
     */
    private void applyDebuffToTarget(BattleCharacter caster, BattleCharacter target) {
        float currentTime = caster.currentTime;
        float duration = context.skillDuration();
        float debuffValue = -context.skillValue() / 100f; // Negative value for debuff

        // Create SynergyEffect with attack reduction (negative value)
        SynergyEffect debuffEffect = new SynergyEffect(
                "DEBUFF_" + caster.getName() + "_" + System.currentTimeMillis(),
                debuffValue,  // attackBonus (negative for debuff)
                0f,           // defenseBonus
                0f,           // magicBonus
                0f,           // manaRegenBonus
                0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f  // other bonuses
        );

        // Use new temporary effect system with expiration tracking
        target.addTemporaryEffect(debuffEffect, duration, currentTime);

        if (Gdx.app != null) {
            Gdx.app.log(TAG, String.format("Applied debuff to %s: %.1f%% attack for %.1f seconds",
                    target.getName(), debuffValue * 100, duration));
        }
    }

    /**
     * Gets the skill context.
     *
     * @return the skill context
     */
    public SkillContext getContext() {
        return context;
    }
}
