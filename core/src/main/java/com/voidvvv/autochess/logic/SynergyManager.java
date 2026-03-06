package com.voidvvv.autochess.logic;

import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.SynergyType;
import com.voidvvv.autochess.utils.I18N;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 羁绊管理器
 * 负责计算场上角色的羁绊，管理激活的羁绊效果，并将效果应用到角色
 */
public class SynergyManager {
    private Map<SynergyType, Integer> synergyCounts;
    private Map<SynergyType, Integer> activeSynergyLevels;
    private List<SynergyType> lastActiveSynergies;
    private boolean needsUpdate;

    public SynergyManager() {
        this.synergyCounts = new HashMap<>();
        this.activeSynergyLevels = new HashMap<>();
        this.lastActiveSynergies = new ArrayList<>();
        this.needsUpdate = true;
    }

    /**
     * 更新羁绊计算
     * @param characters 场上所有角色列表
     */
    public void updateSynergies(List<BattleCharacter> characters) {
        if (characters == null || characters.isEmpty()) {
            clearSynergies();
            return;
        }

        // 重置计数
        synergyCounts.clear();

        // 计算每种羁绊的数量
        for (BattleCharacter character : characters) {
            Card card = character.getCard();
            if (card != null) {
                List<SynergyType> synergies = card.getSynergies();
                if (synergies != null) {
                    for (SynergyType synergy : synergies) {
                        synergyCounts.merge(synergy, 1, Integer::sum);
                    }
                }
            }
        }

        // 更新激活的羁绊等级
        updateActiveSynergyLevels();
        needsUpdate = false;
    }

    /**
     * 更新激活的羁绊等级
     */
    private void updateActiveSynergyLevels() {
        activeSynergyLevels.clear();
        lastActiveSynergies.clear();

        for (Map.Entry<SynergyType, Integer> entry : synergyCounts.entrySet()) {
            SynergyType synergy = entry.getKey();
            int count = entry.getValue();
            int level = synergy.getSynergyLevel(count);

            if (level > 0) {
                activeSynergyLevels.put(synergy, level);
                lastActiveSynergies.add(synergy);
            }
        }
    }

    /**
     * 应用羁绊效果到角色
     * @param characters 场上角色列表
     */
    public void applySynergyEffects(List<BattleCharacter> characters) {
        if (needsUpdate) {
            updateSynergies(characters);
        }

        if (characters == null || characters.isEmpty()) {
            return;
        }

        // 首先清除所有角色的羁绊效果
        for (BattleCharacter character : characters) {
            character.clearSynergyEffects();
        }

        // 应用每个激活的羁绊效果
        for (Map.Entry<SynergyType, Integer> entry : activeSynergyLevels.entrySet()) {
            SynergyType synergy = entry.getKey();
            int level = entry.getValue();
            applySynergyEffect(characters, synergy, level);
        }
    }

    /**
     * 应用单个羁绊效果
     */
    private void applySynergyEffect(List<BattleCharacter> characters, SynergyType synergy, int level) {
        if (characters == null || level <= 0) {
            return;
        }

        for (BattleCharacter character : characters) {
            Card card = character.getCard();
            if (card != null && card.hasSynergy(synergy)) {
                // 根据羁绊类型和等级应用效果
                switch (synergy) {
                    case WARRIOR:
                        applyWarriorSynergy(character, level);
                        break;
                    case MAGE:
                        applyMageSynergy(character, level);
                        break;
                    case ARCHER:
                        applyArcherSynergy(character, level);
                        break;
                    case ASSASSIN:
                        applyAssassinSynergy(character, level);
                        break;
                    case TANK:
                        applyTankSynergy(character, level);
                        break;
                    case DRAGON:
                        applyDragonSynergy(character, level);
                        break;
                    case BEAST:
                        applyBeastSynergy(character, level);
                        break;
                    case HUMAN:
                        applyHumanSynergy(character, level);
                        break;
                }
            }
        }
    }

    // 各种羁绊效果的具体实现
    private void applyWarriorSynergy(BattleCharacter character, int level) {
        // 战士羁绊：增加攻击力和防御力
        float attackBonus = 0.05f * level; // 每级增加5%攻击力
        float defenseBonus = 0.10f * level; // 每级增加10%防御力
        character.addSynergyEffect("warrior", attackBonus, defenseBonus, 0, 0);
    }

    private void applyMageSynergy(BattleCharacter character, int level) {
        // 法师羁绊：增加魔法强度和法力回复
        float magicBonus = 0.08f * level; // 每级增加8%魔法强度
        float manaRegenBonus = 0.15f * level; // 每级增加15%法力回复
        character.addSynergyEffect("mage", 0, 0, magicBonus, manaRegenBonus);
    }

    private void applyArcherSynergy(BattleCharacter character, int level) {
        // 射手羁绊：增加攻击速度和暴击率
        float attackSpeedBonus = 0.10f * level; // 每级增加10%攻击速度
        float critBonus = 0.03f * level; // 每级增加3%暴击率
        character.addSynergyEffect("archer", 0.05f * level, 0, 0, 0, attackSpeedBonus, critBonus);
    }

    private void applyAssassinSynergy(BattleCharacter character, int level) {
        // 刺客羁绊：增加暴击伤害和闪避率
        float critDamageBonus = 0.15f * level; // 每级增加15%暴击伤害
        float dodgeBonus = 0.05f * level; // 每级增加5%闪避率
        character.addSynergyEffect("assassin", 0.08f * level, 0, 0, 0, 0, 0, critDamageBonus, dodgeBonus);
    }

    private void applyTankSynergy(BattleCharacter character, int level) {
        // 坦克羁绊：增加生命值和伤害减免
        float hpBonus = 0.10f * level; // 每级增加10%生命值
        float damageReduction = 0.03f * level; // 每级增加3%伤害减免
        character.addSynergyEffect("tank", 0, 0.08f * level, 0, 0, 0, 0, 0, 0, hpBonus, damageReduction);
    }

    private void applyDragonSynergy(BattleCharacter character, int level) {
        // 龙族羁绊：增加全属性和技能伤害
        float allStatBonus = 0.05f * level; // 每级增加5%全属性
        character.addSynergyEffect("dragon", allStatBonus, allStatBonus, allStatBonus, allStatBonus);
    }

    private void applyBeastSynergy(BattleCharacter character, int level) {
        // 野兽羁绊：增加攻击速度和生命偷取
        float attackSpeedBonus = 0.08f * level; // 每级增加8%攻击速度
        float lifeStealBonus = 0.04f * level; // 每级增加4%生命偷取
        character.addSynergyEffect("beast", 0.05f * level, 0, 0, 0, attackSpeedBonus, 0, 0, 0, 0, 0, lifeStealBonus, 0);
    }

    private void applyHumanSynergy(BattleCharacter character, int level) {
        // 人族羁绊：增加全属性和经验获取
        float allStatBonus = 0.03f * level; // 每级增加3%全属性
        float expBonus = 0.10f * level; // 每级增加10%经验获取
        character.addSynergyEffect("human", allStatBonus, allStatBonus, allStatBonus, allStatBonus, 0, 0, 0, 0, 0, 0, 0, expBonus);
    }

    /**
     * 清除所有羁绊计算
     */
    public void clearSynergies() {
        synergyCounts.clear();
        activeSynergyLevels.clear();
        lastActiveSynergies.clear();
        needsUpdate = false;
    }

    /**
     * 标记需要更新
     */
    public void markNeedsUpdate() {
        this.needsUpdate = true;
    }

    /**
     * 获取指定羁绊类型的数量
     */
    public int getSynergyCount(SynergyType synergy) {
        return synergyCounts.getOrDefault(synergy, 0);
    }

    /**
     * 获取指定羁绊类型的激活等级
     */
    public int getSynergyLevel(SynergyType synergy) {
        return activeSynergyLevels.getOrDefault(synergy, 0);
    }

    /**
     * 检查指定羁绊类型是否激活
     */
    public boolean isSynergyActive(SynergyType synergy) {
        return getSynergyLevel(synergy) > 0;
    }

    /**
     * 获取所有激活的羁绊类型
     */
    public List<SynergyType> getActiveSynergies() {
        return new ArrayList<>(lastActiveSynergies);
    }

    /**
     * 获取所有羁绊类型的计数
     */
    public Map<SynergyType, Integer> getAllSynergyCounts() {
        return new HashMap<>(synergyCounts);
    }

    /**
     * 获取所有激活的羁绊等级
     */
    public Map<SynergyType, Integer> getAllActiveSynergyLevels() {
        return new HashMap<>(activeSynergyLevels);
    }

    /**
     * 获取羁绊信息字符串（用于调试和显示）
     */
    public String getSynergyInfoString() {
        StringBuilder sb = new StringBuilder();
        sb.append(I18N.get("synergy_status", "当前羁绊状态:")).append("\n");
        for (SynergyType synergy : lastActiveSynergies) {
            int count = synergyCounts.getOrDefault(synergy, 0);
            int level = activeSynergyLevels.getOrDefault(synergy, 0);
            sb.append(String.format("  %s: %d/%d (%s%d)\n",
                synergy.getDisplayName(), count,
                synergy.getNextThreshold(level - 1),
                I18N.get("level_prefix", "等级"), level));
        }
        if (lastActiveSynergies.isEmpty()) {
            sb.append("  ").append(I18N.get("no_active_synergy", "无激活羁绊")).append("\n");
        }
        return sb.toString();
    }
}