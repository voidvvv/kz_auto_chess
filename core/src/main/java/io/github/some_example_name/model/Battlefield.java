package io.github.some_example_name.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 战场类
 * 管理战场上的角色
 */
public class Battlefield {
    private float x, y, width, height;
    private List<BattleCharacter> characters;
    
    public Battlefield(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.characters = new ArrayList<>();
    }
    
    /**
     * 检查点是否在战场范围内
     */
    public boolean contains(float px, float py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
    
    /**
     * 在战场上放置角色
     */
    public boolean placeCharacter(Card card, CharacterStats stats, float px, float py) {
        if (!contains(px, py)) {
            return false;
        }
        
        // 检查位置是否被占用
        for (BattleCharacter character : characters) {
            if (character.contains(px, py)) {
                return false;
            }
        }
        
        BattleCharacter character = new BattleCharacter(card, stats, px, py);
        characters.add(character);
        return true;
    }
    
    /**
     * 移动角色到新位置
     */
    public boolean moveCharacter(BattleCharacter character, float newX, float newY) {
        if (!contains(newX, newY)) {
            return false;
        }
        
        // 检查新位置是否被占用（排除自己）
        for (BattleCharacter other : characters) {
            if (other != character && other.contains(newX, newY)) {
                return false;
            }
        }
        
        character.setPosition(newX, newY);
        return true;
    }
    
    /**
     * 移除指定位置的角色
     */
    public BattleCharacter removeCharacter(float px, float py) {
        for (int i = 0; i < characters.size(); i++) {
            BattleCharacter character = characters.get(i);
            if (character.contains(px, py)) {
                characters.remove(i);
                return character;
            }
        }
        return null;
    }
    
    /**
     * 获取指定位置的角色
     */
    public BattleCharacter getCharacterAt(float px, float py) {
        for (BattleCharacter character : characters) {
            if (character.contains(px, py)) {
                return character;
            }
        }
        return null;
    }
    
    /**
     * 获取所有角色
     */
    public List<BattleCharacter> getCharacters() {
        return new ArrayList<>(characters);
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
}

