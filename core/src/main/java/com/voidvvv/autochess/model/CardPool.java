package com.voidvvv.autochess.model;

import com.voidvvv.autochess.utils.I18N;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 卡池管理类，管理所有可用的卡牌
 */
public class CardPool {
    private List<Card> allCards;
    private Random random;

    public CardPool() {
        this.allCards = new ArrayList<>();
        this.random = new Random();
        initCards();
    }

    /**
     * 初始化卡池，添加所有可用的卡牌
     */
    private void initCards() {
        // Tier 1 卡牌 - 技能配置
        // 战士: BUFF技能（增加攻击力），法师: AOE技能（范围伤害），弓手: DEBUFF技能（降低防御），刺客: DEBUFF技能（降低攻击），坦克: HEAL技能（自身治疗）
        allCards.add(createCardWithSkill(140, I18N.get("card_novice_warrior"), I18N.get("card_desc_novice_warrior"), 1, 1, Card.CardType.WARRIOR, 1, 1, Arrays.asList(SynergyType.WARRIOR), "abc+140", SkillType.BUFF, 30f, 100f, 5f));
        allCards.add(createCardWithSkill(141, I18N.get("card_novice_mage"), I18N.get("card_desc_novice_mage"), 1, 1, Card.CardType.MAGE, 1, 2, Arrays.asList(SynergyType.MAGE), "abc+141", SkillType.AOE, 40f, 80f, 0f));
        allCards.add(createCardWithSkill(142, I18N.get("card_novice_archer"), I18N.get("card_desc_novice_archer"), 1, 1, Card.CardType.ARCHER, 1, 3, Arrays.asList(SynergyType.ARCHER), "abc+142", SkillType.DEBUFF, 20f, 100f, 4f));
        allCards.add(createCardWithSkill(143, I18N.get("card_novice_assassin"), I18N.get("card_desc_novice_assassin"), 1, 1, Card.CardType.ASSASSIN, 1, 4, Arrays.asList(SynergyType.ASSASSIN), "abc+143", SkillType.DEBUFF, 25f, 100f, 5f));
        allCards.add(createCardWithSkill(144, I18N.get("card_novice_tank"), I18N.get("card_desc_novice_tank"), 1, 1, Card.CardType.TANK, 1, 5, Arrays.asList(SynergyType.TANK), "abc+144", SkillType.HEAL, 50f, 0f, 0f));

        // Tier 2 卡牌
        allCards.add(createCardWithSkill(145, I18N.get("card_elite_warrior"), I18N.get("card_desc_elite_warrior"), 2, 2, Card.CardType.WARRIOR, 1, 6, Arrays.asList(SynergyType.WARRIOR), "abc+145", SkillType.BUFF, 35f, 100f, 6f));
        allCards.add(createCardWithSkill(146, I18N.get("card_elite_mage"), I18N.get("card_desc_elite_mage"), 2, 2, Card.CardType.MAGE, 1, 7, Arrays.asList(SynergyType.MAGE), "abc+146", SkillType.AOE, 50f, 100f, 0f));
        allCards.add(createCardWithSkill(147, I18N.get("card_elite_archer"), I18N.get("card_desc_elite_archer"), 2, 2, Card.CardType.ARCHER, 1, 8, Arrays.asList(SynergyType.ARCHER), "abc+147", SkillType.DEBUFF, 25f, 120f, 5f));
        allCards.add(createCardWithSkill(148, I18N.get("card_elite_assassin"), I18N.get("card_desc_elite_assassin"), 2, 2, Card.CardType.ASSASSIN, 1, 9, Arrays.asList(SynergyType.ASSASSIN), "abc+148", SkillType.DEBUFF, 30f, 100f, 6f));
        allCards.add(createCardWithSkill(149, I18N.get("card_elite_tank"), I18N.get("card_desc_elite_tank"), 2, 2, Card.CardType.TANK, 1, 10, Arrays.asList(SynergyType.TANK), "abc+149", SkillType.HEAL, 60f, 0f, 0f));

        // Tier 3 卡牌
        allCards.add(createCardWithSkill(150, I18N.get("card_advanced_warrior"), I18N.get("card_desc_advanced_warrior"), 3, 3, Card.CardType.WARRIOR, 1, 11, Arrays.asList(SynergyType.WARRIOR), "abc+150", SkillType.BUFF, 40f, 120f, 7f));
        allCards.add(createCardWithSkill(151, I18N.get("card_advanced_mage"), I18N.get("card_desc_advanced_mage"), 3, 3, Card.CardType.MAGE, 1, 12, Arrays.asList(SynergyType.MAGE), "abc+151", SkillType.AOE, 70f, 120f, 0f));
        allCards.add(createCardWithSkill(152, I18N.get("card_advanced_archer"), I18N.get("card_desc_advanced_archer"), 3, 3, Card.CardType.ARCHER, 1, 13, Arrays.asList(SynergyType.ARCHER), "abc+152", SkillType.DEBUFF, 30f, 150f, 6f));
        allCards.add(createCardWithSkill(153, I18N.get("card_advanced_assassin"), I18N.get("card_desc_advanced_assassin"), 3, 3, Card.CardType.ASSASSIN, 1, 14, Arrays.asList(SynergyType.ASSASSIN), "abc+153", SkillType.DEBUFF, 35f, 120f, 7f));
        allCards.add(createCardWithSkill(154, I18N.get("card_advanced_tank"), I18N.get("card_desc_advanced_tank"), 3, 3, Card.CardType.TANK, 1, 15, Arrays.asList(SynergyType.TANK), "abc+154", SkillType.HEAL, 80f, 0f, 0f));

        // Tier 4 卡牌 - 传奇卡牌
        allCards.add(createCardWithSkill(155, I18N.get("card_legendary_warrior"), I18N.get("card_desc_legendary_warrior"), 4, 4, Card.CardType.WARRIOR, 1, 16, Arrays.asList(SynergyType.WARRIOR), "abc+155", SkillType.BUFF, 50f, 150f, 8f));
        allCards.add(createCardWithSkill(156, I18N.get("card_legendary_mage"), I18N.get("card_desc_legendary_mage"), 4, 4, Card.CardType.MAGE, 1, 17, Arrays.asList(SynergyType.MAGE), "abc+156", SkillType.AOE, 100f, 150f, 0f));
        allCards.add(createCardWithSkill(157, I18N.get("card_legendary_archer"), I18N.get("card_desc_legendary_archer"), 4, 4, Card.CardType.ARCHER, 1, 18, Arrays.asList(SynergyType.ARCHER), "abc+157", SkillType.DEBUFF, 40f, 180f, 8f));
        allCards.add(createCardWithSkill(158, I18N.get("card_legendary_assassin"), I18N.get("card_desc_legendary_assassin"), 4, 4, Card.CardType.ASSASSIN, 1, 19, Arrays.asList(SynergyType.ASSASSIN), "abc+158", SkillType.DEBUFF, 45f, 150f, 8f));
        allCards.add(createCardWithSkill(159, I18N.get("card_legendary_tank"), I18N.get("card_desc_legendary_tank"), 4, 4, Card.CardType.TANK, 1, 20, Arrays.asList(SynergyType.TANK), "abc+159", SkillType.HEAL, 120f, 0f, 0f));

        // Tier 5 卡牌 - 神话卡牌
        allCards.add(createCardWithSkill(160, I18N.get("card_mythical_warrior"), I18N.get("card_desc_mythical_warrior"), 5, 5, Card.CardType.WARRIOR, 1, 21, Arrays.asList(SynergyType.WARRIOR), "abc+160", SkillType.BUFF, 60f, 200f, 10f));
        allCards.add(createCardWithSkill(161, I18N.get("card_mythical_mage"), I18N.get("card_desc_mythical_mage"), 5, 5, Card.CardType.MAGE, 1, 22, Arrays.asList(SynergyType.MAGE), "abc+161", SkillType.AOE, 150f, 200f, 0f));
        allCards.add(createCardWithSkill(162, I18N.get("card_mythical_archer"), I18N.get("card_desc_mythical_archer"), 5, 5, Card.CardType.ARCHER, 1, 23, Arrays.asList(SynergyType.ARCHER), "abc+162", SkillType.DEBUFF, 50f, 200f, 10f));
        allCards.add(createCardWithSkill(163, I18N.get("card_mythical_assassin"), I18N.get("card_desc_mythical_assassin"), 5, 5, Card.CardType.ASSASSIN, 1, 24, Arrays.asList(SynergyType.ASSASSIN), "abc+163", SkillType.DEBUFF, 55f, 200f, 10f));
        allCards.add(createCardWithSkill(164, I18N.get("card_mythical_tank"), I18N.get("card_desc_mythical_tank"), 5, 5, Card.CardType.TANK, 1, 25, Arrays.asList(SynergyType.TANK), "abc+164", SkillType.HEAL, 200f, 0f, 0f));
    }

    /**
     * 创建带有技能配置的卡牌
     */
    private Card createCardWithSkill(int id, String name, String description, int cost, int tier,
                                     Card.CardType type, int starLevel, int baseCardId,
                                     List<SynergyType> synergies, String tiledResourceKey,
                                     SkillType skillType, float skillValue, float skillRange, float skillDuration) {
        Card card = new Card(id, name, description, cost, tier, type, starLevel, baseCardId, synergies, tiledResourceKey);
        card.setSkillType(skillType);
        card.setSkillValue(skillValue);
        card.setSkillRange(skillRange);
        card.setSkillDuration(skillDuration);
        return card;
    }

    /**
     * 根据等级获取卡牌列表
     */
    public List<Card> getCardsByTier(int tier) {
        List<Card> result = new ArrayList<>();
        for (Card card : allCards) {
            if (card.getTier() == tier) {
                result.add(card);
            }
        }
        return result;
    }

    /**
     * 随机获取指定数量的卡牌（用于商店刷新）
     */
    public List<Card> getRandomCards(int count) {
        List<Card> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(allCards.size());
            result.add(allCards.get(index));
        }
        return result;
    }

    /**
     * 根据等级概率随机获取卡牌
     * 等级越高，高等级卡牌出现概率越高
     */
    public List<Card> getRandomCardsByLevel(int count, int playerLevel) {
        List<Card> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // 根据玩家等级计算各等级卡牌的出现概率
            int tier = calculateTierByLevel(playerLevel);
            List<Card> tierCards = getCardsByTier(tier);
            if (!tierCards.isEmpty()) {
                int index = random.nextInt(tierCards.size());
                result.add(tierCards.get(index));
            } else {
                // 如果没有对应等级的卡，随机选择
                int index = random.nextInt(allCards.size());
                result.add(allCards.get(index));
            }
        }
        return result;
    }

    /**
     * 根据玩家等级计算卡牌等级
     */
    private int calculateTierByLevel(int playerLevel) {
        // 简单的概率计算
        float rand = random.nextFloat();
        if (playerLevel <= 3) {
            // 1-3级：主要出1-2级卡
            if (rand < 0.7f) return 1;
            else if (rand < 0.95f) return 2;
            else return 3;
        } else if (playerLevel <= 6) {
            // 4-6级：主要出2-3级卡
            if (rand < 0.3f) return 1;
            else if (rand < 0.7f) return 2;
            else if (rand < 0.95f) return 3;
            else return 4;
        } else {
            // 7级以上：可以出所有等级的卡
            if (rand < 0.2f) return 1;
            else if (rand < 0.4f) return 2;
            else if (rand < 0.6f) return 3;
            else if (rand < 0.85f) return 4;
            else return 5;
        }
    }

    public List<Card> getAllCards() {
        return new ArrayList<>(allCards);
    }

    /** 根据卡牌 id 获取卡牌，用于关卡敌人配置等 */
    public Card getCardById(int id) {
        for (Card c : allCards) {
            if (c.getId() == id) return c;
        }
        return null;
    }
}

