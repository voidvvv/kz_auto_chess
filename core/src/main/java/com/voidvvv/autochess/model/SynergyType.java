package com.voidvvv.autochess.model;

import com.voidvvv.autochess.utils.I18N;
import java.util.Arrays;
import java.util.List;

/**
 * 羁绊类型枚举
 * 定义职业和种族羁绊，包含激活阈值
 */
public enum SynergyType {
    // 职业羁绊
    WARRIOR("warrior", "战士", Arrays.asList(2, 4, 6)),
    MAGE("mage", "法师", Arrays.asList(3, 6, 9)),
    ARCHER("archer", "射手", Arrays.asList(2, 4, 6)),
    ASSASSIN("assassin", "刺客", Arrays.asList(3, 6)),
    TANK("tank", "坦克", Arrays.asList(2, 4, 6)),

    // 种族羁绊（未来扩展）
    DRAGON("dragon", "龙族", Arrays.asList(2)),
    BEAST("beast", "野兽", Arrays.asList(2, 4)),
    HUMAN("human", "人族", Arrays.asList(2, 4, 6));

    private final String key;
    private final String displayName;
    private final List<Integer> activationThresholds;

    SynergyType(String key, String displayName, List<Integer> activationThresholds) {
        this.key = key;
        this.displayName = displayName;
        this.activationThresholds = activationThresholds;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        // 使用国际化键：synergy_ + key，后备值为硬编码的displayName
        return I18N.get("synergy_" + key, displayName);
    }

    public List<Integer> getActivationThresholds() {
        return activationThresholds;
    }

    /**
     * 获取指定数量下的羁绊等级
     * @param count 相同羁绊类型的角色数量
     * @return 羁绊等级 (0表示未激活，1表示第一级，2表示第二级，以此类推)
     */
    public int getSynergyLevel(int count) {
        int level = 0;
        for (int threshold : activationThresholds) {
            if (count >= threshold) {
                level++;
            } else {
                break;
            }
        }
        return level;
    }

    /**
     * 获取下一级需要的数量
     * @param currentLevel 当前羁绊等级
     * @return 下一级需要的数量，如果已经是最高级则返回-1
     */
    public int getNextThreshold(int currentLevel) {
        if (currentLevel < 0 || currentLevel >= activationThresholds.size()) {
            return -1;
        }
        return activationThresholds.get(currentLevel);
    }

    /**
     * 检查是否激活了指定等级的羁绊
     * @param count 相同羁绊类型的角色数量
     * @param targetLevel 目标等级（1-based）
     * @return 如果达到指定等级的阈值返回 true，否则返回 false
     */
    public boolean isActivated(int count, int targetLevel) {
        // 边界检查：targetLevel 必须在有效范围内
        if (targetLevel <= 0 || targetLevel > activationThresholds.size()) {
            return false;
        }
        return count >= activationThresholds.get(targetLevel - 1);
    }

    /**
     * 获取最大可能的羁绊等级
     */
    public int getMaxLevel() {
        return activationThresholds.size();
    }

    @Override
    public String toString() {
        return displayName;
    }
}