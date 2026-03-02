package com.voidvvv.autochess.model;

import com.voidvvv.autochess.model.battle.DamageEventHolder;
import com.voidvvv.autochess.model.battle.DamageEventListenerHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 战场类
 * 管理战场上的角色，分为玩家可放置区域与敌人区域
 */
public class Battlefield {
    private float x, y, width, height;
    private List<BattleCharacter> characters;

    private DamageEventHolder damageEventHolder;
    private DamageEventListenerHolder damageEventListenerHolder;
    /** 玩家区域占战场高度的比例（靠己方一侧） */
    public static final float PLAYER_ZONE_RATIO = 0.5f;

    public Battlefield(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.characters = new ArrayList<>();
        this.damageEventHolder = new DamageEventHolder();
        this.damageEventListenerHolder = new DamageEventListenerHolder();
    }

    /** 玩家可放置区域：Y 从底边到中线 */
    public boolean isInPlayerZone(float px, float py) {
        if (!contains(px, py)) return false;
        float splitY = y + height * PLAYER_ZONE_RATIO;
        return py >= y && py <= splitY;
    }

    /** 敌人区域：Y 从中线到顶边 */
    public boolean isInEnemyZone(float px, float py) {
        if (!contains(px, py)) return false;
        float splitY = y + height * PLAYER_ZONE_RATIO;
        return py > splitY && py <= y + height;
    }

    /** 玩家区域上边界（世界坐标） */
    public float getPlayerZoneTop() {
        return y + height * PLAYER_ZONE_RATIO;
    }

    /** 敌人区域下边界（世界坐标） */
    public float getEnemyZoneBottom() {
        return y + height * PLAYER_ZONE_RATIO;
    }

    /**
     * 检查点是否在战场范围内
     */
    public boolean contains(float px, float py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    /**
     * 在战场上放置玩家角色（仅允许在玩家区域）
     */
    public boolean placeCharacter(Card card, CharacterStats stats, float px, float py) {
        if (!isInPlayerZone(px, py)) {
            return false;
        }

        for (BattleCharacter c : characters) {
            if (c.contains(px, py)) return false;
        }

        BattleCharacter character = new BattleCharacter(card, stats, px, py, false);
        characters.add(character);
        return true;
    }

    /**
     * 在敌人区域放置敌方单位（仅用于战斗开始生成敌人）
     */
    public boolean placeEnemyCharacter(Card card, CharacterStats stats, float px, float py) {
        if (!isInEnemyZone(px, py)) {
            return false;
        }
        for (BattleCharacter c : characters) {
            if (c.contains(px, py)) return false;
        }
        BattleCharacter character = new BattleCharacter(card, stats, px, py, true);
        characters.add(character);
        return true;
    }

    /**
     * 移动角色到新位置（放置阶段仅允许在玩家区域内移动）
     */
    public boolean moveCharacter(BattleCharacter character, float newX, float newY) {
        if (!contains(newX, newY)) return false;
        if (!character.isEnemy() && !isInPlayerZone(newX, newY)) return false;

        for (BattleCharacter other : characters) {
            if (other != character && other.contains(newX, newY)) return false;
        }
        character.setInitPosition(newX, newY);
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
     * 移除指定角色（用于战斗死亡等）
     */
    public boolean removeCharacter(BattleCharacter character) {
        return characters.remove(character);
    }

    /**
     * 获取指定位置的角色
     */
    public BattleCharacter getCharacterAt(float px, float py) {
        for (BattleCharacter character : characters) {
            if (character.contains(px, py)) return character;
        }
        return null;
    }

    /**
     * 获取所有角色
     */
    public List<BattleCharacter> getCharacters() {
        return new ArrayList<>(characters);
    }

    /**
     * 获取己方单位（玩家放置的）
     */
    public List<BattleCharacter> getPlayerCharacters() {
        List<BattleCharacter> list = new ArrayList<>();
        for (BattleCharacter c : characters) {
            if (!c.isEnemy() && !c.isDead()) list.add(c);
        }
        return list;
    }

    /**
     * 获取敌方单位（与玩家敌对的一方）
     */
    public List<BattleCharacter> getEnemyCharacters() {
        List<BattleCharacter> list = new ArrayList<>();
        for (BattleCharacter c : characters) {
            if (c.isEnemy() && !c.isDead()) list.add(c);
        }
        return list;
    }

    /** 获取指定单位的敌对单位列表（己方单位看敌方，敌方单位看己方） */
    public List<BattleCharacter> getOpponents(BattleCharacter forUnit) {
        if (forUnit == null) return new ArrayList<>();
        return forUnit.isEnemy() ? getPlayerCharacters() : getEnemyCharacters();
    }

    public DamageEventHolder getDamageEventHolder() {
        return damageEventHolder;
    }

    public DamageEventListenerHolder getDamageEventListenerHolder() {
        return damageEventListenerHolder;
    }

    public void reset () {
        this.characters.clear();
        this.damageEventHolder.clear();
        this.getDamageEventListenerHolder().clear();
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
}

