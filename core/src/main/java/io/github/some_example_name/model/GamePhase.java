package io.github.some_example_name.model;

/**
 * 游戏阶段枚举
 * 放置阶段：玩家在己方区域放置/移动棋子
 * 战斗阶段：双方棋子自动战斗，玩家不可操作
 */
public enum GamePhase {
    /** 放置阶段 */
    PLACEMENT,
    /** 战斗阶段 */
    BATTLE
}
