package com.voidvvv.autochess.model.skill.exception;

/**
 * Exception thrown when a skill fails to execute properly.
 * This represents runtime errors during skill casting, such as invalid targets,
 * insufficient resources, or state inconsistencies.
 */
public class SkillExecutionException extends SkillException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new skill execution exception with the specified detail message.
     *
     * @param message the detail message
     */
    public SkillExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs a new skill execution exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public SkillExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new skill execution exception for a specific skill and target.
     *
     * @param skillName the name of the skill that failed
     * @param targetDescription description of the target
     * @param reason the reason for failure
     */
    public SkillExecutionException(String skillName, String targetDescription, String reason) {
        super(String.format("Skill '%s' failed on target '%s': %s", skillName, targetDescription, reason));
    }
}
