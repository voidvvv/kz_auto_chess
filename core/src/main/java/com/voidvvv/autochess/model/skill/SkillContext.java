package com.voidvvv.autochess.model.skill;

import com.voidvvv.autochess.model.battle.Damage;

/**
 * Value object containing skill execution parameters.
 * This class encapsulates all configurable parameters for skill execution.
 *
 * <p>Following the Value Object pattern, this class is immutable and
 * provides type-safe access to skill parameters.</p>
 *
 * @param skillValue the primary value of the skill (e.g., damage amount, heal amount)
 * @param skillRange the range of the skill in world units
 * @param skillDuration the duration of the skill effect in seconds (for BUFF/DEBUFF)
 * @param damageType the damage type for damage-dealing skills
 */
public record SkillContext(
        float skillValue,
        float skillRange,
        float skillDuration,
        Damage.DamageType damageType
) {
    /**
     * Default skill value constant.
     */
    public static final float DEFAULT_SKILL_VALUE = 50f;

    /**
     * Default skill range constant.
     */
    public static final float DEFAULT_SKILL_RANGE = 100f;

    /**
     * Default skill duration constant.
     */
    public static final float DEFAULT_SKILL_DURATION = 5f;

    /**
     * Default skill context with standard values.
     */
    public static final SkillContext DEFAULT = new SkillContext(
            DEFAULT_SKILL_VALUE,
            DEFAULT_SKILL_RANGE,
            DEFAULT_SKILL_DURATION,
            Damage.DamageType.PhySic
    );

    /**
     * Creates a new skill context with the specified values.
     *
     * @param skillValue the skill value
     * @param skillRange the skill range
     * @param skillDuration the skill duration
     * @return a new SkillContext instance
     */
    public static SkillContext of(float skillValue, float skillRange, float skillDuration) {
        return new SkillContext(skillValue, skillRange, skillDuration, Damage.DamageType.PhySic);
    }

    /**
     * Creates a new skill context with the specified damage type.
     *
     * @param skillValue the skill value
     * @param skillRange the skill range
     * @param skillDuration the skill duration
     * @param damageType the damage type
     * @return a new SkillContext instance
     */
    public static SkillContext of(float skillValue, float skillRange, float skillDuration,
                                   Damage.DamageType damageType) {
        return new SkillContext(skillValue, skillRange, skillDuration, damageType);
    }

    /**
     * Validates the skill context parameters.
     *
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public SkillContext {
        if (skillValue < 0f) {
            throw new IllegalArgumentException("Skill value cannot be negative: " + skillValue);
        }
        if (skillRange < 0f) {
            throw new IllegalArgumentException("Skill range cannot be negative: " + skillRange);
        }
        if (skillDuration < 0f) {
            throw new IllegalArgumentException("Skill duration cannot be negative: " + skillDuration);
        }
        if (damageType == null) {
            throw new IllegalArgumentException("Damage type cannot be null");
        }
    }

    /**
     * Checks if this skill context has a duration effect.
     *
     * @return true if duration is greater than zero
     */
    public boolean hasDuration() {
        return this.skillDuration > 0f;
    }

    /**
     * Checks if the target is within skill range.
     *
     * @param distance the distance to the target
     * @return true if the target is within range
     */
    public boolean isInRange(float distance) {
        return distance <= this.skillRange;
    }
}
