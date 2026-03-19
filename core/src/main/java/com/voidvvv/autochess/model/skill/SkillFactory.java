package com.voidvvv.autochess.model.skill;

import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.Skill;
import com.voidvvv.autochess.model.SkillType;

/**
 * Factory class for creating skill instances based on card configuration.
 *
 * <p>This class follows the Factory pattern to encapsulate skill creation logic.
 * It provides a centralized point for skill instantiation and ensures proper
 * configuration based on card parameters.</p>
 *
 * <p>All factory methods are static and return properly configured skill instances
 * with appropriate validation and error handling.</p>
 */
public final class SkillFactory {

    private static final String TAG = "SkillFactory";

    // Private constructor to prevent instantiation
    private SkillFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates a skill instance based on the card's configuration.
     *
     * @param card the card containing skill configuration
     * @return the created skill instance, or BasicSkill if configuration is invalid
     * @throws IllegalArgumentException if card is null
     */
    public static Skill<BattleUnitBlackboard> createSkill(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }

        SkillType skillType = card.getSkillType();
        if (skillType == null) {
            return new BasicSkill();
        }

        SkillContext context = createSkillContext(card);

        return switch (skillType) {
            case BASIC -> new BasicSkill();
            case HEAL -> new HealSkill(context);
            case AOE -> new AoeSkill(context);
            case BUFF -> new BuffSkill(context);
            case DEBUFF -> new DebuffSkill(context);
        };
    }

    /**
     * Creates a skill context from card parameters.
     *
     * @param card the card containing skill parameters
     * @return the created skill context
     * @throws IllegalArgumentException if card is null
     */
    public static SkillContext createSkillContext(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }

        return SkillContext.of(
                card.getSkillValue(),
                card.getSkillRange(),
                card.getSkillDuration(),
                card.getSkillDamageType()
        );
    }

    /**
     * Creates a skill with explicit parameters.
     *
     * @param skillType the type of skill to create
     * @param skillValue the skill value parameter
     * @param skillRange the skill range parameter
     * @param skillDuration the skill duration parameter
     * @return the created skill instance
     * @throws IllegalArgumentException if skillType is null
     */
    public static Skill<BattleUnitBlackboard> createSkill(
            SkillType skillType,
            float skillValue,
            float skillRange,
            float skillDuration) {

        if (skillType == null) {
            throw new IllegalArgumentException("SkillType cannot be null");
        }

        SkillContext context = SkillContext.of(skillValue, skillRange, skillDuration);

        return switch (skillType) {
            case BASIC -> new BasicSkill();
            case HEAL -> new HealSkill(context);
            case AOE -> new AoeSkill(context);
            case BUFF -> new BuffSkill(context);
            case DEBUFF -> new DebuffSkill(context);
        };
    }

    /**
     * Checks if a skill type is valid.
     *
     * @param skillType the skill type to check
     * @return true if the skill type is valid (not null)
     */
    public static boolean isValidSkillType(SkillType skillType) {
        return skillType != null;
    }

    /**
     * Gets the display name for a skill type.
     *
     * @param skillType the skill type
     * @return the display name, or "Unknown" if skillType is null
     */
    public static String getSkillTypeName(SkillType skillType) {
        if (skillType == null) {
            return "Unknown";
        }

        return switch (skillType) {
            case BASIC -> "基础技能";
            case HEAL -> "治疗术";
            case AOE -> "范围伤害";
            case BUFF -> "增益";
            case DEBUFF -> "减益";
        };
    }
}
