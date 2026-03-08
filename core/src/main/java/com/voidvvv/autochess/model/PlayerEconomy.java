package com.voidvvv.autochess.model;

import com.voidvvv.autochess.utils.I18N;
import com.voidvvv.autochess.logic.EconomyCalculator;

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

    // Note: Configuration constants moved to EconomyCalculator

    public PlayerEconomy() {
        this.gold = 10; // 初始金币
        this.interest = 0;
        this.winStreak = 0;
        this.loseStreak = 0;
        this.playerLevel = 1;
        this.experience = 0;
        this.experienceToNextLevel = EconomyCalculator.getExperienceRequirement(1);
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
        this.experienceToNextLevel = EconomyCalculator.getExperienceRequirement(playerLevel);
    }

    /**
     * 增加金币（委托给EconomyCalculator计算利息）
     */
    public void addGold(int amount) {
        if (amount <= 0) return;
        this.gold += amount;
        this.interest = EconomyCalculator.calculateInterest(this.gold);
    }

    /**
     * 花费金币（委托给EconomyCalculator计算利息）
     */
    public boolean spendGold(int amount) {
        if (amount > 0 && gold >= amount) {
            this.gold -= amount;
            this.totalGoldSpent += amount;
            this.interest = EconomyCalculator.calculateInterest(this.gold);
            return true;
        }
        return false;
    }

    /**
     * 重置连胜/连败状态
     */
    public void resetStreaks() {
        this.winStreak = 0;
        this.loseStreak = 0;
    }

    /**
     * 尝试升级玩家等级（需要4金币）
     * 委托给EconomyCalculator计算
     * @return 是否成功升级
     */
    public boolean tryLevelUp() {
        return EconomyCalculator.tryLevelUp(this);
    }

    /**
     * 增加经验值并检查是否可以升级（自动升级不消耗金币）
     * 委托给EconomyCalculator计算
     */
    public void addExperience(int exp) {
        EconomyCalculator.addExperience(this, exp);
    }

    /**
     * 回合结束结算
     * 委托给EconomyCalculator计算
     * @param won 是否胜利
     */
    public void endRound(boolean won) {
        EconomyCalculator.endRound(this, won);
    }

    /**
     * 获取升级所需剩余经验（委托给EconomyCalculator）
     */
    public int getRemainingExperience() {
        return EconomyCalculator.getRemainingExperience(this);
    }

    /**
     * 获取经验进度百分比 (0-100)（委托给EconomyCalculator）
     */
    public float getExperiencePercentage() {
        return EconomyCalculator.getExperiencePercentage(this);
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

    // Setters (for EconomyCalculator to update state)
    public void setGold(int gold) {
        this.gold = gold;
    }

    public void setInterest(int interest) {
        this.interest = interest;
    }

    public void setWinStreak(int winStreak) {
        this.winStreak = winStreak;
    }

    public void setLoseStreak(int loseStreak) {
        this.loseStreak = loseStreak;
    }

    public void setPlayerLevel(int playerLevel) {
        this.playerLevel = playerLevel;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void setExperienceToNextLevel(int experienceToNextLevel) {
        this.experienceToNextLevel = experienceToNextLevel;
    }

    public void setTotalGoldEarned(int totalGoldEarned) {
        this.totalGoldEarned = totalGoldEarned;
    }

    public void setTotalGoldSpent(int totalGoldSpent) {
        this.totalGoldSpent = totalGoldSpent;
    }

    public void setTotalInterestEarned(int totalInterestEarned) {
        this.totalInterestEarned = totalInterestEarned;
    }

    public void setTotalWinStreakBonus(int totalWinStreakBonus) {
        this.totalWinStreakBonus = totalWinStreakBonus;
    }

    public void setTotalLoseStreakBonus(int totalLoseStreakBonus) {
        this.totalLoseStreakBonus = totalLoseStreakBonus;
    }

    /**
     * 获取当前回合总收入预览（基础收入 + 利息 + 连胜/连败奖励）
     * 委托给EconomyCalculator计算
     */
    public int getRoundIncomePreview(boolean assumeWin) {
        return EconomyCalculator.getRoundIncomePreview(this, assumeWin);
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