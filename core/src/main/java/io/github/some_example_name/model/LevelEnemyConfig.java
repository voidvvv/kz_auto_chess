package io.github.some_example_name.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 关卡敌人配置：每个关卡指定若干敌方单位的 cardId，在战斗开始时在敌人区域生成
 */
public final class LevelEnemyConfig {

    /** 每关敌人 cardId 列表，索引 0 对应关卡 1 */
    private static final List<int[]> LEVEL_ENEMIES = Arrays.asList(
        new int[] { 1, 2, 3 },           // 关卡 1：战士、法师、射手
        new int[] { 1, 2, 3, 4 },        // 关卡 2
        new int[] { 2, 3, 4, 5 },        // 关卡 3
        new int[] { 6, 7, 8, 4, 5 },     // 关卡 4：精英 + 刺客坦克
        new int[] { 6, 7, 8, 9, 10 }     // 关卡 5
    );

    private LevelEnemyConfig() {}

    /** 获取指定关卡（1-based）的敌人 cardId 列表 */
    public static List<Integer> getEnemyCardIdsForLevel(int level) {
        int idx = Math.max(0, Math.min(level - 1, LEVEL_ENEMIES.size() - 1));
        int[] arr = LEVEL_ENEMIES.get(idx);
        List<Integer> list = new ArrayList<>();
        for (int id : arr) list.add(id);
        return list;
    }

    /**
     * 在战场敌人区域内按网格生成敌人
     * @param battlefield 战场
     * @param cardIds 本关敌人 cardId 列表
     * @param cardPool 用于根据 id 取 Card
     * @return 成功放置的数量
     */
    public static int spawnEnemiesInBattlefield(Battlefield battlefield, List<Integer> cardIds,
                                                 CardPool cardPool) {
        if (cardIds == null || cardIds.isEmpty()) return 0;

        float bw = battlefield.getWidth();
        float bh = battlefield.getHeight();
        float zoneHeight = bh * (1f - Battlefield.PLAYER_ZONE_RATIO);
        float zoneBottom = battlefield.getEnemyZoneBottom();
        float left = battlefield.getX();
        int count = cardIds.size();
        int cols = Math.min(count, 4);
        int rows = (count + cols - 1) / cols;
        float cellW = cols > 1 ? (bw - 60) / (cols - 1) : bw / 2;
        float cellH = rows > 1 ? (zoneHeight - 40) / (rows - 1) : zoneHeight / 2;
        float startX = left + 30;
        float startY = zoneBottom + 20;

        int placed = 0;
        for (int i = 0; i < count; i++) {
            int id = cardIds.get(i);
            Card card = cardPool.getCardById(id);
            CharacterStats stats = CharacterStats.Config.getStats(id);
            if (card == null || stats == null) continue;

            int c = i % cols;
            int r = i / cols;
            float px = startX + c * cellW;
            float py = startY + r * cellH;
            if (battlefield.placeEnemyCharacter(card, stats, px, py)) {
                placed++;
            }
        }
        return placed;
    }
}
