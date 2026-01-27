package io.github.some_example_name.model;

/**
 * 战场角色类
 * 表示战场上的一个角色单位
 */
public class BattleCharacter {
    private Card card;
    private CharacterStats stats;
    private float x, y;  // 战场上的位置
    private float size = 40; // 角色大小
    
    public BattleCharacter(Card card, CharacterStats stats, float x, float y) {
        this.card = card;
        this.stats = stats;
        this.x = x;
        this.y = y;
    }
    
    public Card getCard() {
        return card;
    }
    
    public CharacterStats getStats() {
        return stats;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public float getSize() {
        return size;
    }
    
    /**
     * 检查点是否在角色范围内
     */
    public boolean contains(float px, float py) {
        float halfSize = size / 2;
        return px >= x - halfSize && px <= x + halfSize &&
               py >= y - halfSize && py <= y + halfSize;
    }
}

