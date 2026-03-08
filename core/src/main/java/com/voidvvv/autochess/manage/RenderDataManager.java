package com.voidvvv.autochess.manage;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Projectile;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages rendering-specific data separate from model classes.
 * This ensures models remain pure data entities without rendering dependencies.
 */
public class RenderDataManager {
    private final Map<BattleCharacter, TextureRegion> characterTextures = new HashMap<>();
    private final Map<BattleCharacter, com.voidvvv.autochess.model.BaseCollision> characterCollisions = new HashMap<>();
    private final Map<Projectile, Color> projectileColors = new HashMap<>();

    /**
     * Get the texture region for a character
     * @param character the character to get texture for
     * @return the texture region, or null if not set
     */
    public TextureRegion getCharacterTexture(BattleCharacter character) {
        return characterTextures.get(character);
    }

    /**
     * Set the texture region for a character
     * @param character the character to set texture for
     * @param texture the texture region to set
     */
    public void setCharacterTexture(BattleCharacter character, TextureRegion texture) {
        characterTextures.put(character, texture);
    }

    /**
     * Get the collision data for a character
     * @param character the character to get collision for
     * @return the collision data, or null if not set
     */
    public com.voidvvv.autochess.model.BaseCollision getCharacterCollision(BattleCharacter character) {
        return characterCollisions.get(character);
    }

    /**
     * Set the collision data for a character
     * @param character the character to set collision for
     * @param collision the collision data to set
     */
    public void setCharacterCollision(BattleCharacter character, com.voidvvv.autochess.model.BaseCollision collision) {
        characterCollisions.put(character, collision);
    }

    /**
     * Check if a character has a texture
     * @param character the character to check
     * @return true if the character has a texture
     */
    public boolean hasCharacterTexture(BattleCharacter character) {
        return characterTextures.containsKey(character);
    }

    /**
     * Get the color for a projectile
     * @param projectile the projectile to get color for
     * @return the color, or null if not set
     */
    public Color getProjectileColor(Projectile projectile) {
        return projectileColors.get(projectile);
    }

    /**
     * Set the color for a projectile
     * @param projectile the projectile to set color for
     * @param color the color to set
     */
    public void setProjectileColor(Projectile projectile, Color color) {
        projectileColors.put(projectile, color);
    }

    /**
     * Remove a character from the render data manager
     * @param character the character to remove
     */
    public void removeCharacter(BattleCharacter character) {
        characterTextures.remove(character);
        characterCollisions.remove(character);
    }

    /**
     * Remove a projectile from the render data manager
     * @param projectile the projectile to remove
     */
    public void removeProjectile(Projectile projectile) {
        projectileColors.remove(projectile);
    }

    /**
     * Clear all render data
     */
    public void clear() {
        characterTextures.clear();
        characterCollisions.clear();
        projectileColors.clear();
    }

    /**
     * Get the number of managed characters
     * @return the number of managed characters
     */
    public int getManagedCharacterCount() {
        return characterTextures.size();
    }

    /**
     * Get the number of managed projectiles
     * @return the number of managed projectiles
     */
    public int getManagedProjectileCount() {
        return projectileColors.size();
    }
}
