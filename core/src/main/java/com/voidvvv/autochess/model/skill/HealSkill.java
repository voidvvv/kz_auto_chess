package com.voidvvv.autochess.model.skill;

import com.badlogic.gdx.Gdx;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Skill;
import com.voidvvv.autochess.model.skill.exception.InvalidTargetException;
import com.voidvvv.autochess.model.skill.exception.SkillExecutionException;

/**
 * Healing skill implementation.
 * Restores health to the caster (self-only healing per R2).
 *
 * <p>This skill follows the Strategy pattern and implements the Skill interface.
 * It provides self-targeted healing based on skill value from Card configuration.</p>
 */
public class HealSkill implements Skill<BattleUnitBlackboard> {

    private static final String TAG = "HealSkill";
    private static final String SKILL_NAME = "治疗术";
    private static final float HEAL_MULTIPLIER = 1.0f;

    private final SkillContext context;

    /**
     * Creates a heal skill with default parameters.
     */
    public HealSkill() {
        this(SkillContext.DEFAULT);
    }

    /**
     * Creates a heal skill with the specified context.
     *
     * @param context the skill context containing parameters
     */
    public HealSkill(SkillContext context) {
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

        try {
            // R2: 恢复释放者自身的生命值 (Self-only healing)
            float healAmount = calculateHealAmount(caster);
            float actualHealed = healCharacter(caster, healAmount);

            if (Gdx.app != null) {
                Gdx.app.log(TAG, String.format("[%s] cast %s on self, healed: %.1f (requested: %.1f)",
                        caster.getName(), getName(), actualHealed, healAmount));
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
     * Calculates the base heal amount based on skill context.
     *
     * @param caster the casting character
     * @return the calculated heal amount
     */
    private float calculateHealAmount(BattleCharacter caster) {
        return context.skillValue() * HEAL_MULTIPLIER;
    }

    /**
     * Heals a single character, respecting maximum health.
     *
     * @param character the character to heal
     * @param healAmount the amount to heal
     * @return the actual amount healed
     */
    private float healCharacter(BattleCharacter character, float healAmount) {
        if (character == null) {
            return 0f;
        }

        float currentHp = character.getCurrentHp();
        float maxHp = character.getStats().getHealth();
        float missingHp = maxHp - currentHp;

        float actualHealed = Math.min(healAmount, missingHp);
        character.setCurrentHp(currentHp + actualHealed);

        return actualHealed;
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
