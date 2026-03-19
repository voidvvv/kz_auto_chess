package com.voidvvv.autochess.model.skill.exception;

/**
 * Base exception for skill-related errors.
 * All skill-specific exceptions should extend this class.
 */
public class SkillException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new skill exception with the specified detail message.
     *
     * @param message the detail message
     */
    public SkillException(String message) {
        super(message);
    }

    /**
     * Constructs a new skill exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public SkillException(String message, Throwable cause) {
        super(message, cause);
    }
}
