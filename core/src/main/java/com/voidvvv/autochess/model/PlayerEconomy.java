package com.voidvvv.autochess.model;

import com.voidvvv.autochess.utils.I18N;

/**
 * 玩家经济管理系统
 * 管理金币、利息、连胜/连败奖励、玩家等级和经验
 */
public class PlayerEconomy {
    // 经济状态
    private int gold;
    private int interest; // 当前利息
    private int winStreak; // 连胜次数
    private int loseStreak; // 连败次数

    // 玩家等级系统
    private int playerLevel; // 玩家等级 (1-10)
    private int experience; // 当前经验值
    private int experienceToNextLevel; // 升级所需经验

    // 统计信息
    private int totalGoldEarned; // 累计获得金币
    private int totalGoldSpent; // 累计花费金币
    private int totalInterestEarned; // 累计获得利息
    private int totalWinStreakBonus; // 累计连胜奖励
    private int totalLoseStreakBonus; // 累计连败补偿

    // 配置常量
    private static final int BASE_INCOME_PER_ROUND = 5; // 每回合基础收入
    private static final int INTEREST_PER_10_GOLD = 1; // 每10金币利息
    private static final int MAX_INTEREST = 5; // 最大利息
    private static final int[] WIN_STREAK_REWARDS = {0, 0, 1, 1, 2, 2, 3}; // 连胜奖励
    private static final int[] LOSE_STREAK_COMPENSATION = {0, 0, 1, 1, 2}; // 连败补偿
    private static final int[] EXP_TO_NEXT_LEVEL = {4, 6, 8, 10, 12, 14, 16, 18, 20, 22}; // 升级所需经验
    private static final int[] LEVEL_UP_GOLD_REWARD = {0, 1, 1, 1, 1, 1, 1, 1, 1, 1}; // 升级金币奖励

    public PlayerEconomy() {
        this.gold = 10; // 初始金币
        this.interest = 0;
        this.winStreak = 0;
        this.loseStreak = 0;
        this.playerLevel = 1;
        this.experience = 0;
        this.experienceToNextLevel = EXP_TO_NEXT_LEVEL[0];
        this.totalGoldEarned = 0;
        this.totalGoldSpent = 0;
        this.totalInterestEarned = 0;
        this.totalWinStreakBonus = 0;
        this.totalLoseStreakBonus = 0;
    }

    public PlayerEconomy(int startingGold, int startingLevel) {
        this();
        this.gold = Math.max(0, startingGold);
        this.playerLevel = Math.max(1, Math.min(10, startingLevel));
        updateExperienceRequirement();
    }

    /**
     * 计算当前利息
     */
    private void calculateInterest() {
        int baseGold = Math.min(gold, 50); // 最多计算50金币的利息
        this.interest = Math.min(MAX_INTEREST, baseGold / 10 * INTEREST_PER_10_GOLD);
    }

    /**
     * 更新升级所需经验
     */
    private void updateExperienceRequirement() {
        if (playerLevel <= 10) {
            this.experienceToNextLevel = EXP_TO_NEXT_LEVEL[playerLevel - 1];
        } else {
            this.experienceToNextLevel = Integer.MAX_VALUE;
        }
    }

    /**
     * 增加金币
     */
    public void addGold(int amount) {
        if (amount > 0) {
            this.gold += amount;
            this.totalGoldEarned += amount;
            calculateInterest(); // 金币变化时重新计算利息
        }
    }

    /**
     * 花费金币
     */
    public boolean spendGold(int amount) {
        if (amount > 0 && gold >= amount) {
            this.gold -= amount;
            this.totalGoldSpent += amount;
            calculateInterest(); // 金币变化时重新计算利息
            return true;
        }
        return false;
    }

    /**
     * 尝试升级玩家等级
     * @return 是否成功升级
     */
    public boolean tryLevelUp() {
        if (playerLevel >= 10) {
            return false; // 已达最大等级
        }

        if (experience >= experienceToNextLevel) {
            // 升级
            playerLevel++;
            experience -= experienceToNextLevel;
            updateExperienceRequirement();

            // 升级奖励
            int reward = LEVEL_UP_GOLD_REWARD[playerLevel - 1];
            if (reward > 0) {
                addGold(reward);
            }

            return true;
        }
        return false;
    }

    /**
     * 增加经验值
     */
    public void addExperience(int exp) {
        if (exp > 0) {
            this.experience += exp;
            // 检查是否可以升级
            while (experience >= experienceToNextLevel && playerLevel < 10) {
                tryLevelUp();
            }
        }
    }

    /**
     * 回合结束结算
     * @param won 是否胜利
     */
    public void endRound(boolean won) {
        // 处理连胜/连败
        if (won) {
            winStreak++;
            loseStreak = 0;

            // 连胜奖励
            if (winStreak < WIN_STREAK_REWARDS.length) {
                int bonus = WIN_STREAK_REWARDS[winStreak];
                if (bonus > 0) {
                    addGold(bonus);
                    totalWinStreakBonus += bonus;
                }
            } else {
                // 超过数组长度，使用最后一个值
                int bonus = WIN_STREAK_REWARDS[WIN_STREAK_REWARDS.length - 1];
                if (bonus > 0) {
                    addGold(bonus);
                    totalWinStreakBonus += bonus;
                }
            }

            // 胜利经验奖励
            addExperience(2);
        } else {
            loseStreak++;
            winStreak = 0;

            // 连败补偿
            if (loseStreak < LOSE_STREAK_COMPENSATION.length) {
                int compensation = LOSE_STREAK_COMPENSATION[loseStreak];
                if (compensation > 0) {
                    addGold(compensation);
                    totalLoseStreakBonus += compensation;
                }
            }
        }

        // 基础回合奖励
        addGold(BASE_INCOME_PER_ROUND);

        // 利息收入
        if (interest > 0) {
            addGold(interest);
            totalInterestEarned += interest;
        }

        // 每回合基础经验
        addExperience(1);
    }

    /**
     * 重置连胜/连败状态
     */
    public void resetStreaks() {
        this.winStreak = 0;
        this.loseStreak = 0;
    }

    /**
     * 获取升级所需剩余经验
     */
    public int getRemainingExperience() {
        return Math.max(0, experienceToNextLevel - experience);
    }

    /**
     * 获取经验进度百分比 (0-100)
     */
    public float getExperiencePercentage() {
        if (playerLevel >= 10) {
            return 100f;
        }
        return (float) experience / experienceToNextLevel * 100f;
    }

    // Getters
    public int getGold() { return gold; }
    public int getInterest() { return interest; }
    public int getWinStreak() { return winStreak; }
    public int getLoseStreak() { return loseStreak; }
    public int getPlayerLevel() { return playerLevel; }
    public int getExperience() { return experience; }
    public int getExperienceToNextLevel() { return experienceToNextLevel; }
    public int getTotalGoldEarned() { return totalGoldEarned; }
    public int getTotalGoldSpent() { return totalGoldSpent; }
    public int getTotalInterestEarned() { return totalInterestEarned; }
    public int getTotalWinStreakBonus() { return totalWinStreakBonus; }
    public int getTotalLoseStreakBonus() { return totalLoseStreakBonus; }

    /**
     * 获取当前回合总收入预览（基础收入 + 利息 + 连胜/连败奖励）
     */
    public int getRoundIncomePreview(boolean assumeWin) {
        int income = BASE_INCOME_PER_ROUND;

        // 利息
        income += interest;

        // 连胜/连败奖励
        if (assumeWin) {
            int nextWinStreak = winStreak + 1;
            if (nextWinStreak < WIN_STREAK_REWARDS.length) {
                income += WIN_STREAK_REWARDS[nextWinStreak];
            } else if (WIN_STREAK_REWARDS.length > 0) {
                income += WIN_STREAK_REWARDS[WIN_STREAK_REWARDS.length - 1];
            }
        } else {
            int nextLoseStreak = loseStreak + 1;
            if (nextLoseStreak < LOSE_STREAK_COMPENSATION.length) {
                income += LOSE_STREAK_COMPENSATION[nextLoseStreak];
            }
        }

        return income;
    }

    /**
     * 获取经济状态信息字符串
     */
    public String getEconomyInfoString() {
        return I18N.format(
            "economy_info",
            "金币: {0} (+{1}利息) | 等级: {2} ({3}/{4} EXP {5}%) | 连胜: {6} | 连败: {7}",
            gold, interest, playerLevel, experience, experienceToNextLevel,
            getExperiencePercentage(), winStreak, loseStreak
        );
    }

    /**
     * 获取详细统计信息
     */
    public String getDetailedStatsString() {
        return I18N.format(
            "economy_detailed_stats",
            "经济统计:\n" +
            "  累计获得金币: {0}\n" +
            "  累计花费金币: {1}\n" +
            "  累计利息收入: {2}\n" +
            "  累计连胜奖励: {3}\n" +
            "  累计连败补偿: {4}\n" +
            "  当前余额: {5}",
            totalGoldEarned, totalGoldSpent, totalInterestEarned,
            totalWinStreakBonus, totalLoseStreakBonus, gold
        );
    }
}