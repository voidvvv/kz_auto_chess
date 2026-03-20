package com.voidvvv.autochess.model.skill;

import com.badlogic.gdx.Gdx;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Battlefield;
import com.voidvvv.autochess.model.Skill;
import com.voidvvv.autochess.model.SynergyEffect;
import com.voidvvv.autochess.model.skill.exception.InvalidTargetException;
import com.voidvvv.autochess.model.skill.exception.SkillExecutionException;

import java.util.ArrayList;
import java.util.List;

/**
 * BUFF skill implementation.
 * Applies temporary stat boosts to ALL allied characters on the battlefield.
 *
 * <p>This skill follows the Strategy pattern and implements the Skill interface.
 * It uses BattleCharacter.addTemporaryEffect() for proper duration tracking.</p>
 *
 * <p>Per R3: 对场上所有友方单位施加属性增益</p>
 */
public class BuffSkill implements Skill<BattleUnitBlackboard> {

    private static final String TAG = "BuffSkill";
    private static final String SKILL_NAME = "增益";

    private final SkillContext context;

    /**
     * Creates a buff skill with default parameters.
     */
    public BuffSkill() {
        this(SkillContext.DEFAULT);
    }

    /**
     * Creates a buff skill with the specified context.
     *
     * @param context the skill context containing parameters
     */
    public BuffSkill(SkillContext context) {
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

        Battlefield battlefield = blackboard.getBattlefield();
        if (battlefield == null) {
            throw new SkillExecutionException("Battlefield cannot be null");
        }

        try {
            // R3: 对场上所有友方单位施加属性增益 (All allies on battlefield)
            List<BattleCharacter> allies = findAllies(caster, battlefield);
            applyBuffToAllies(caster, allies, blackboard);

            if (Gdx.app != null) {
                Gdx.app.log(TAG, String.format("[%s] cast %s on %d allies, duration: %.1f, value: %.1f",
                        caster.getName(), getName(), allies.size(), context.skillDuration(), context.skillValue()));
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
     * Finds all ally characters on the battlefield (not range-limited).
     *
     * @param caster the casting character
     * @param battlefield the battlefield containing all characters
     * @return list of all allies
     */
    private List<BattleCharacter> findAllies(BattleCharacter caster, Battlefield battlefield) {
        List<BattleCharacter> allies = new ArrayList<>();
        List<BattleCharacter> allCharacters = battlefield.getCharacters();

        for (BattleCharacter character : allCharacters) {
            if (isValidAlly(caster, character)) {
                allies.add(character);
            }
        }

        return allies;
    }

    /**
     * Checks if a character is a valid ally (alive and same camp).
     *
     * @param caster the casting character
     * @param potentialAlly the potential ally
     * @return true if the ally is valid
     */
    private boolean isValidAlly(BattleCharacter caster, BattleCharacter potentialAlly) {
        if (potentialAlly == null) {
            return false;
        }
        if (potentialAlly.isDead()) {
            return false;
        }
        return caster.getCamp() == potentialAlly.getCamp();
    }

    /**
     * Applies buff effects to all allies using temporary effect system.
     *
     * @param caster the casting character
     * @param allies the list of allies to buff
     * @param blackboard the blackboard for current time
     */
    private void applyBuffToAllies(BattleCharacter caster, List<BattleCharacter> allies,
                                   BattleUnitBlackboard blackboard) {
        float currentTime = caster.currentTime;
        float duration = context.skillDuration();
        float bonusValue = context.skillValue() / 100f; // Convert percentage to decimal

        for (BattleCharacter ally : allies) {
            // Create SynergyEffect with attack bonus (can be extended for other stats)
            SynergyEffect buffEffect = new SynergyEffect(
                    "BUFF_" + caster.getName() + "_" + System.currentTimeMillis(),
                    bonusValue,  // attackBonus
                    0f,          // defenseBonus
                    0f,          // magicBonus
                    0f,          // manaRegenBonus
                    0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f  // other bonuses
            );

            // Use new temporary effect system with expiration tracking
            ally.addTemporaryEffect(buffEffect, duration, currentTime);

            if (Gdx.app != null) {
                Gdx.app.log(TAG, String.format("Applied buff to %s: +%.1f%% attack for %.1f seconds",
                        ally.getName(), context.skillValue(), duration));
            }
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
