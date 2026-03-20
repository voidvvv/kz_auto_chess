package com.voidvvv.autochess.model.skill.exception;

/**
 * Exception thrown when a skill is used on an invalid target.
 * This includes cases where the target is null, dead, or out of range.
 */
public class InvalidTargetException extends SkillExecutionException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new invalid target exception with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidTargetException(String message) {
        super(message);
    }

    /**
     * Constructs a new invalid target exception for a specific skill and target.
     *
     * @param skillName the name of the skill
     * @param targetDescription description of the invalid target
     */
    public InvalidTargetException(String skillName, String targetDescription) {
        super(skillName, targetDescription, "Invalid target");
    }

    /**
     * Constructs a new invalid target exception with skill name, target description, and reason.
     *
     * @param skillName the name of the skill
     * @param targetDescription description of the invalid target
     * @param reason the reason for the invalid target
     */
    public InvalidTargetException(String skillName, String targetDescription, String reason) {
        super(skillName, targetDescription, reason);
    }
}
