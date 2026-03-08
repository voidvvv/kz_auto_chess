package com.voidvvv.autochess.logic;

import com.voidvvv.autochess.model.PlayerEconomy;

/**
 * 经济计算逻辑类
 * 处理玩家经济相关的计算逻辑
 * 从模型中分离业务逻辑，使模型保持纯数据实体
 */
public class EconomyCalculator {

    // 配置常量
    private static final int BASE_INCOME_PER_ROUND = 5; // 每回合基础收入
    private static final int INTEREST_PER_10_GOLD = 1; // 每10金币利息
    private static final int MAX_INTEREST = 5; // 最大利息
    private static final int[] WIN_STREAK_REWARDS = {0, 0, 1, 1, 2, 2, 3}; // 连胜奖励
    private static final int[] LOSE_STREAK_COMPENSATION = {0, 0, 1, 1, 2}; // 连败补偿
    private static final int[] EXP_TO_NEXT_LEVEL = {4, 6, 8, 10, 12, 14, 16, 18, 20, 22}; // 升级所需经验
    private static final int[] LEVEL_UP_GOLD_REWARD = {0, 1, 1, 1, 1, 1, 1, 1, 1, 1}; // 升级金币奖励
    private static final int LEVEL_UP_COST = 4; // 升级消耗金币

    private EconomyCalculator() {
        // 工具类，不允许实例化
        throw new UnsupportedOperationException("EconomyCalculator is a utility class and cannot be instantiated");
    }

    /**
     * 计算当前利息
     * @param gold 当前金币数量
     * @return 利息值
     */
    public static int calculateInterest(int gold) {
        int baseGold = Math.min(gold, 50); // 最多计算50金币的利息
        return Math.min(MAX_INTEREST, baseGold / 10 * INTEREST_PER_10_GOLD);
    }

    /**
     * 计算回合总收入
     * @param economy 玩家经济
     * @param win 是否胜利
     * @return 总收入（基础收入 + 利息 + 连胜/连败奖励）
     */
    public static int calculateRoundIncome(PlayerEconomy economy, boolean win) {
        int income = BASE_INCOME_PER_ROUND;

        // 利息
        income += economy.getInterest();

        // 连胜/连败奖励
        if (win) {
            int nextWinStreak = economy.getWinStreak() + 1;
            if (nextWinStreak < WIN_STREAK_REWARDS.length) {
                income += WIN_STREAK_REWARDS[nextWinStreak];
            } else if (WIN_STREAK_REWARDS.length > 0) {
                income += WIN_STREAK_REWARDS[WIN_STREAK_REWARDS.length - 1];
            }
        } else {
            int nextLoseStreak = economy.getLoseStreak() + 1;
            if (nextLoseStreak < LOSE_STREAK_COMPENSATION.length) {
                income += LOSE_STREAK_COMPENSATION[nextLoseStreak];
            }
        }

        return income;
    }

    /**
     * 获取升级所需经验值
     * @param level 当前等级
     * @return 升级所需经验
     */
    public static int getExperienceRequirement(int level) {
        if (level <= 10) {
            return EXP_TO_NEXT_LEVEL[level - 1];
        }
        return Integer.MAX_VALUE;
    }

    /**
     * 尝试升级玩家等级
     * @param economy 玩家经济
     * @return 是否成功升级
     */
    public static boolean tryLevelUp(PlayerEconomy economy) {
        if (economy.getPlayerLevel() >= 10) {
            return false; // 已达最大等级
        }

        int requiredXp = getExperienceRequirement(economy.getPlayerLevel());
        if (economy.getExperience() >= requiredXp && economy.getGold() >= LEVEL_UP_COST) {
            // 升级
            int newLevel = economy.getPlayerLevel() + 1;
            int newExp = economy.getExperience() - requiredXp;
            int newGold = economy.getGold() - LEVEL_UP_COST;

            // 更新经济状态
            updateLevel(economy, newLevel, newExp, newGold);

            // 升级奖励
            int reward = LEVEL_UP_GOLD_REWARD[newLevel - 1];
            if (reward > 0) {
                addGold(economy, reward);
            }

            return true;
        }
        return false;
    }

    /**
     * 添加经验值并检查是否可以升级（自动升级不需要金币）
     * @param economy 玩家经济
     * @param exp 经验值
     */
    public static void addExperience(PlayerEconomy economy, int exp) {
        if (exp <= 0) {
            return;
        }

        int newExp = economy.getExperience() + exp;
        int currentLevel = economy.getPlayerLevel();

        // 检查是否可以升级（自动升级不消耗金币，只检查经验）
        while (currentLevel < 10 && newExp >= getExperienceRequirement(currentLevel)) {
            int requiredXp = getExperienceRequirement(currentLevel);
            newExp -= requiredXp;
            currentLevel++;

            // 升级奖励
            int reward = LEVEL_UP_GOLD_REWARD[currentLevel - 1];
            if (reward > 0) {
                addGold(economy, reward);
            }
        }

        // 更新最终状态
        economy.setPlayerLevel(currentLevel);
        economy.setExperience(newExp);
        economy.setExperienceToNextLevel(getExperienceRequirement(currentLevel));
    }

    /**
     * 处理回合结束
     * @param economy 玩家经济
     * @param win 是否胜利
     */
    public static void endRound(PlayerEconomy economy, boolean win) {
        int initialGold = economy.getGold();
        int initialInterest = economy.getInterest();
        int newGold = economy.getGold();
        int newWinStreak = economy.getWinStreak();
        int newLoseStreak = economy.getLoseStreak();
        int newExp = economy.getExperience();
        int winStreakBonus = 0;
        int loseStreakBonus = 0;

        // 处理连胜/连败
        if (win) {
            newWinStreak++;
            newLoseStreak = 0;

            // 连胜奖励
            if (newWinStreak < WIN_STREAK_REWARDS.length) {
                int bonus = WIN_STREAK_REWARDS[newWinStreak];
                if (bonus > 0) {
                    newGold += bonus;
                    winStreakBonus += bonus;
                }
            } else {
                // 超过数组长度，使用最后一个值
                int bonus = WIN_STREAK_REWARDS[WIN_STREAK_REWARDS.length - 1];
                if (bonus > 0) {
                    newGold += bonus;
                    winStreakBonus += bonus;
                }
            }

            // 胜利经验奖励
            newExp += 2;
        } else {
            newLoseStreak++;
            newWinStreak = 0;

            // 连败补偿
            if (newLoseStreak < LOSE_STREAK_COMPENSATION.length) {
                int compensation = LOSE_STREAK_COMPENSATION[newLoseStreak];
                if (compensation > 0) {
                    newGold += compensation;
                    loseStreakBonus += compensation;
                }
            }
        }

        // 基础回合奖励
        newGold += BASE_INCOME_PER_ROUND;

        // 利息收入（初始利息）
        newGold += initialInterest;

        // 每回合基础经验
        newExp += 1;

        // 更新经济状态（包括重新计算利息）
        updateFull(economy, newGold, newWinStreak, newLoseStreak, newExp);

        // 计算新增利息（final interest - initial interest）
        int interestEarned = economy.getInterest() - initialInterest;

        // 更新统计信息
        updateStats(economy, BASE_INCOME_PER_ROUND + winStreakBonus + loseStreakBonus + initialInterest, 0,
                    interestEarned, winStreakBonus, loseStreakBonus);
    }

    /**
     * 获取回合总收入预览
     * @param economy 玩家经济
     * @param assumeWin 假设胜利
     * @return 预期收入
     */
    public static int getRoundIncomePreview(PlayerEconomy economy, boolean assumeWin) {
        return calculateRoundIncome(economy, assumeWin);
    }

    /**
     * 获取升级所需剩余经验
     * @param economy 玩家经济
     * @return 剩余经验
     */
    public static int getRemainingExperience(PlayerEconomy economy) {
        return Math.max(0, getExperienceRequirement(economy.getPlayerLevel()) - economy.getExperience());
    }

    /**
     * 获取经验进度百分比
     * @param economy 玩家经济
     * @return 0-100的进度百分比
     */
    public static float getExperiencePercentage(PlayerEconomy economy) {
        if (economy.getPlayerLevel() >= 10) {
            return 100f;
        }
        return (float) economy.getExperience() / getExperienceRequirement(economy.getPlayerLevel()) * 100f;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 添加金币
     */
    private static void addGold(PlayerEconomy economy, int amount) {
        if (amount > 0) {
            int newGold = economy.getGold() + amount;
            updateGoldAndInterest(economy, newGold);
            updateStats(economy, amount, 0, economy.getInterest(), 0, 0);
        }
    }

    /**
     * 花费金币
     */
    private static boolean spendGold(PlayerEconomy economy, int amount) {
        if (amount > 0 && economy.getGold() >= amount) {
            int newGold = economy.getGold() - amount;
            updateGoldAndInterest(economy, newGold);
            updateStats(economy, 0, amount, economy.getInterest(), 0, 0);
            return true;
        }
        return false;
    }

    /**
     * 更新金币和利息
     */
    private static void updateGoldAndInterest(PlayerEconomy economy, int newGold) {
        int interest = calculateInterest(newGold);
        economy.setGold(newGold);
        economy.setInterest(interest);
    }

    /**
     * 更新等级和经验
     */
    private static void updateLevel(PlayerEconomy economy, int newLevel, int newExp, int newGold) {
        economy.setPlayerLevel(newLevel);
        economy.setExperience(newExp);
        economy.setExperienceToNextLevel(getExperienceRequirement(newLevel));
        updateFull(economy, newGold, economy.getWinStreak(), economy.getLoseStreak(), newExp);
    }

    /**
     * 更新完整的经济状态
     */
    private static void updateFull(PlayerEconomy economy, int newGold, int newWinStreak, int newLoseStreak, int newExp) {
        int interest = calculateInterest(newGold);
        economy.setGold(newGold);
        economy.setInterest(interest);
        economy.setWinStreak(newWinStreak);
        economy.setLoseStreak(newLoseStreak);
        economy.setExperience(newExp);
        economy.setExperienceToNextLevel(getExperienceRequirement(economy.getPlayerLevel()));
    }

    /**
     * 更新统计信息（累计值）
     */
    private static void updateStats(PlayerEconomy economy, int goldEarned, int goldSpent, int interestEarned,
                                   int winStreakBonus, int loseStreakBonus) {
        economy.setTotalGoldEarned(economy.getTotalGoldEarned() + goldEarned);
        economy.setTotalGoldSpent(economy.getTotalGoldSpent() + goldSpent);
        economy.setTotalInterestEarned(economy.getTotalInterestEarned() + interestEarned);
        economy.setTotalWinStreakBonus(economy.getTotalWinStreakBonus() + winStreakBonus);
        economy.setTotalLoseStreakBonus(economy.getTotalLoseStreakBonus() + loseStreakBonus);
    }
}
