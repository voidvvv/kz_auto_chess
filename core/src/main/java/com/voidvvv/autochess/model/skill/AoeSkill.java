package com.voidvvv.autochess.model.skill;

import com.badlogic.gdx.Gdx;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Battlefield;
import com.voidvvv.autochess.model.Skill;
import com.voidvvv.autochess.model.battle.Damage;
import com.voidvvv.autochess.model.event.DamageEvent;
import com.voidvvv.autochess.model.skill.exception.InvalidTargetException;
import com.voidvvv.autochess.model.skill.exception.SkillExecutionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Area of Effect (AOE) skill implementation.
 * Deals damage to all enemies within a specified range of the caster.
 *
 * <p>This skill follows the Strategy pattern and implements the Skill interface.
 * It uses proper logging, error handling, and immutable data structures.</p>
 */
public class AoeSkill implements Skill<BattleUnitBlackboard> {

    private static final String TAG = "AoeSkill";
    private static final String SKILL_NAME = "AOE伤害";

    private final SkillContext context;

    /**
     * Creates an AOE skill with default parameters.
     */
    public AoeSkill() {
        this(SkillContext.DEFAULT);
    }

    /**
     * Creates an AOE skill with the specified context.
     *
     * @param context the skill context containing parameters
     */
    public AoeSkill(SkillContext context) {
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

        Battlefield battlefield = blackboard.getBattlefield();
        if (battlefield == null) {
            throw new SkillExecutionException("Battlefield cannot be null");
        }

        try {
            List<BattleCharacter> targets = findTargetsInRange(caster, battlefield);
            applyDamageToTargets(caster, targets, battlefield);

            if (Gdx.app != null) {
                Gdx.app.log(TAG, String.format("[%s] cast %s on %d targets, range: %.1f, damage: %.1f",
                        caster.getName(), getName(), targets.size(), context.skillRange(), context.skillValue()));
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
     * Finds all enemy targets within skill range.
     *
     * @param caster the casting character
     * @param battlefield the battlefield containing all characters
     * @return list of targets in range
     */
    private List<BattleCharacter> findTargetsInRange(BattleCharacter caster, Battlefield battlefield) {
        List<BattleCharacter> targets = new ArrayList<>();
        List<BattleCharacter> allCharacters = battlefield.getCharacters();

        for (BattleCharacter character : allCharacters) {
            if (isValidTarget(caster, character)) {
                float distance = calculateDistance(caster, character);
                if (context.isInRange(distance)) {
                    targets.add(character);
                }
            }
        }

        return targets;
    }

    /**
     * Checks if a character is a valid target (alive and enemy).
     *
     * @param caster the casting character
     * @param potentialTarget the potential target
     * @return true if the target is valid
     */
    private boolean isValidTarget(BattleCharacter caster, BattleCharacter potentialTarget) {
        if (potentialTarget == null) {
            return false;
        }
        if (potentialTarget.isDead()) {
            return false;
        }
        return caster.getCamp() != potentialTarget.getCamp();
    }

    /**
     * Calculates the distance between two characters.
     *
     * @param from the source character
     * @param to the target character
     * @return the distance in world units
     */
    private float calculateDistance(BattleCharacter from, BattleCharacter to) {
        float dx = from.getX() - to.getX();
        float dy = from.getY() - to.getY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Applies damage to all target characters.
     *
     * @param caster the casting character
     * @param targets the list of targets
     * @param battlefield the battlefield for event dispatching
     */
    private void applyDamageToTargets(BattleCharacter caster, List<BattleCharacter> targets,
                                      Battlefield battlefield) {
        for (BattleCharacter target : targets) {
            createDamageEvent(caster, target, battlefield);
        }
    }

    /**
     * Creates and dispatches a damage event for a single target.
     *
     * @param caster the casting character
     * @param target the target character
     * @param battlefield the battlefield
     */
    private void createDamageEvent(BattleCharacter caster, BattleCharacter target,
                                   Battlefield battlefield) {
        DamageEvent damageEvent = new DamageEvent();
        damageEvent.setFrom(caster);
        damageEvent.setTo(target);
        damageEvent.setDamage(new Damage(context.skillValue(), context.damageType()));

        battlefield.getDamageEventHolder().addModel(damageEvent);
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
