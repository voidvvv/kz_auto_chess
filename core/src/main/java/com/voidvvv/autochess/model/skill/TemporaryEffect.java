package com.voidvvv.autochess.model.skill;

import com.voidvvv.autochess.model.battle.Damage;

/**
 * Immutable record representing a temporary effect applied to a character.
 * Used for BUFF and DEBUFF effects with duration tracking.
 *
 * <p>This class follows the immutability principle with final fields.
 * All modifications create new instances rather than modifying existing ones.</p>
 *
 * @param effectType the type of effect (BUFF or DEBUFF)
 * @param value the effect value (e.g., damage boost, healing amount)
 * @param duration the duration in seconds
 * @param remainingTime the remaining time in seconds
 * @param damageType the damage type for damage-related effects
 */
public record TemporaryEffect(
        EffectType effectType,
        float value,
        float duration,
        float remainingTime,
        Damage.DamageType damageType
) {
    /**
     * Creates a new temporary effect with full duration.
     *
     * @param effectType the type of effect
     * @param value the effect value
     * @param duration the duration in seconds
     * @return a new TemporaryEffect instance
     */
    public static TemporaryEffect create(EffectType effectType, float value, float duration) {
        return new TemporaryEffect(effectType, value, duration, duration, Damage.DamageType.PhySic);
    }

    /**
     * Creates a new temporary effect with specified damage type.
     *
     * @param effectType the type of effect
     * @param value the effect value
     * @param duration the duration in seconds
     * @param damageType the damage type
     * @return a new TemporaryEffect instance
     */
    public static TemporaryEffect create(EffectType effectType, float value, float duration,
                                         Damage.DamageType damageType) {
        return new TemporaryEffect(effectType, value, duration, duration, damageType);
    }

    /**
     * Creates a new instance with decreased remaining time.
     * This preserves immutability by returning a new object.
     *
     * @param deltaTime the time to decrease in seconds
     * @return a new TemporaryEffect with updated remaining time
     */
    public TemporaryEffect decreaseRemainingTime(float deltaTime) {
        float newRemainingTime = Math.max(0f, this.remainingTime - deltaTime);
        return new TemporaryEffect(this.effectType, this.value, this.duration, newRemainingTime, this.damageType);
    }

    /**
     * Checks if the effect has expired.
     *
     * @return true if remaining time is zero or negative
     */
    public boolean isExpired() {
        return this.remainingTime <= 0f;
    }

    /**
     * Gets the effect progress as a ratio (0.0 to 1.0).
     *
     * @return the progress ratio
     */
    public float getProgress() {
        if (this.duration <= 0f) {
            return 0f;
        }
        return 1f - (this.remainingTime / this.duration);
    }

    /**
     * Enum representing the type of temporary effect.
     */
    public enum EffectType {
        /**
         * Buff effect - increases character stats
         */
        BUFF,

        /**
         * Debuff effect - decreases character stats
         */
        DEBUFF
    }
}
