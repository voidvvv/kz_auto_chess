package com.voidvvv.autochess.manage;

import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.CardShop;
import com.voidvvv.autochess.model.CardPool;
import com.voidvvv.autochess.model.PlayerDeck;
import com.voidvvv.autochess.model.PlayerEconomy;
import com.voidvvv.autochess.model.SharedCardPool;
import com.voidvvv.autochess.model.SynergyType;
import com.voidvvv.autochess.model.SkillType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CardShopManager 单元测试
 *
 * 测试覆盖场景：
 * - 购买卡牌：金币足够/金币不足/商店无该卡
 * - 刷新商店：金币足够/金币不足
 * - 出售卡牌：拥有卡牌/没有卡牌
 * - 事务性：购买失败时金币应被返还
 *
 * 注意：本测试使用 Mock Card 对象，不依赖 CardPool（CardPool 需要 I18N/Gdx.app）
 */
class CardShopManagerTest {

    private CardShopManager cardShopManager;
    private CardShop cardShop;
    private PlayerEconomy playerEconomy;
    private PlayerDeck playerDeck;
    private SharedCardPool sharedCardPool;
    private GameEventSystem eventSystem;

    // 测试用的卡牌对象（不依赖 CardPool）
    private Card testCard1;
    private Card testCard2;
    private Card testCard3;
    private Card expensiveCard;
    private Card cheapCard;

    @BeforeEach
    void setUp() {
        // 初始化基础组件
        eventSystem = new GameEventSystem();
        sharedCardPool = new SharedCardPool();
        playerEconomy = new PlayerEconomy(100, 1);
        playerDeck = new PlayerDeck();

        // 创建测试用卡牌（不使用 CardPool，避免 I18N 依赖）
        testCard1 = createTestCard(1, "测试战士", 3, 1, Card.CardType.WARRIOR);
        testCard2 = createTestCard(2, "测试法师", 4, 1, Card.CardType.MAGE);
        testCard3 = createTestCard(3, "测试弓手", 2, 1, Card.CardType.ARCHER);
        expensiveCard = createTestCard(10, "昂贵卡牌", 5, 1, Card.CardType.TANK);
        cheapCard = createTestCard(11, "便宜卡牌", 1, 1, Card.CardType.ASSASSIN);

        // 创建 Mock CardShop（使用简单的 ArrayList 而不是 CardPool）
        cardShop = new SimpleMockCardShop(Arrays.asList(testCard1, testCard2, testCard3));

        cardShopManager = new CardShopManager(
            cardShop,
            playerEconomy,
            playerDeck,
            sharedCardPool,
            eventSystem
        );
    }

    // ========== 购买卡牌测试 ==========

    @Test
    void testBuyCard_Success() {
        // Given: 商店有卡牌，玩家金币足够
        List<Card> shopCards = cardShop.getCurrentShopCards();
        assertFalse(shopCards.isEmpty(), "商店应该有卡牌");

        Card cardToBuy = shopCards.get(0);
        int initialGold = playerEconomy.getGold();
        int cardCost = cardToBuy.getCost();

        // When: 购买卡牌
        boolean result = cardShopManager.buyCard(cardToBuy);

        // Then: 购买成功，状态正确更新
        assertTrue(result, "购买应该成功");
        assertEquals(initialGold - cardCost, playerEconomy.getGold(), "金币应该减少");
        assertEquals(1, playerDeck.getCardCount(cardToBuy), "玩家应该拥有该卡");
        assertFalse(cardShop.getCurrentShopCards().contains(cardToBuy), "商店不应再有该卡");
    }

    @Test
    void testBuyCard_InsufficientGold() {
        // Given: 玩家金币为 0
        playerEconomy = new PlayerEconomy(0, 1);
        cardShopManager = new CardShopManager(
            cardShop, playerEconomy, playerDeck, sharedCardPool, eventSystem
        );

        List<Card> shopCards = cardShop.getCurrentShopCards();
        Card cardToBuy = shopCards.get(0);
        int initialGold = playerEconomy.getGold();

        // When: 购买卡牌
        boolean result = cardShopManager.buyCard(cardToBuy);

        // Then: 购买失败，状态不变
        assertFalse(result, "购买应该失败（金币不足）");
        assertEquals(initialGold, playerEconomy.getGold(), "金币不应变化");
        assertEquals(0, playerDeck.getCardCount(cardToBuy), "玩家不应拥有该卡");
    }

    @Test
    void testBuyCard_NotInShop() {
        // Given: 一张不在商店中的卡牌
        Card cardNotInShop = createTestCard(999, "不存在", 3, 1, Card.CardType.WARRIOR);

        // 确保该卡不在商店中
        List<Card> shopCards = cardShop.getCurrentShopCards();
        assertFalse(shopCards.contains(cardNotInShop), "该卡不应在商店中");

        int initialGold = playerEconomy.getGold();

        // When: 购买卡牌
        boolean result = cardShopManager.buyCard(cardNotInShop);

        // Then: 购买失败，状态不变
        assertFalse(result, "购买应该失败（商店没有该卡）");
        assertEquals(initialGold, playerEconomy.getGold(), "金币不应变化");
        assertEquals(0, playerDeck.getCardCount(cardNotInShop), "玩家不应拥有该卡");
    }

    @Test
    void testBuyCard_TransactionRollback() {
        // 测试事务回滚：购买失败时应返还金币
        // 设置金币比卡牌费用少 1
        Card targetCard = expensiveCard; // 费用 5
        playerEconomy = new PlayerEconomy(4, 1); // 只有 4 金币
        cardShop = new SimpleMockCardShop(Arrays.asList(expensiveCard));
        cardShopManager = new CardShopManager(
            cardShop, playerEconomy, playerDeck, sharedCardPool, eventSystem
        );

        int initialGold = playerEconomy.getGold();

        // When: 尝试购买
        boolean result = cardShopManager.buyCard(targetCard);

        // Then: 购买失败，金币未扣除
        assertFalse(result, "购买应该失败");
        assertEquals(initialGold, playerEconomy.getGold(), "金币不应被扣除（事务性保证）");
        assertEquals(0, playerDeck.getCardCount(targetCard), "玩家不应拥有该卡");
    }

    // ========== 刷新商店测试 ==========

    @Test
    void testRefreshShop_Success() {
        // Given: 玩家金币足够
        List<Card> beforeRefresh = cardShop.getCurrentShopCards();
        int initialGold = playerEconomy.getGold();
        int refreshCost = cardShop.getRefreshCost();

        assertTrue(playerEconomy.getGold() >= refreshCost, "玩家金币应该足够");

        // When: 刷新商店
        boolean result = cardShopManager.refreshShop();

        // Then: 刷新成功，金币扣除
        assertTrue(result, "刷新应该成功");
        assertEquals(initialGold - refreshCost, playerEconomy.getGold(), "金币应该减少");
    }

    @Test
    void testRefreshShop_InsufficientGold() {
        // Given: 玩家金币为 0
        playerEconomy = new PlayerEconomy(0, 1);
        cardShopManager = new CardShopManager(
            cardShop, playerEconomy, playerDeck, sharedCardPool, eventSystem
        );

        List<Card> beforeRefresh = cardShop.getCurrentShopCards();

        // When: 尝试刷新
        boolean result = cardShopManager.refreshShop();

        // Then: 刷新失败，状态不变
        assertFalse(result, "刷新应该失败（金币不足）");
        assertEquals(0, playerEconomy.getGold(), "金币不应变化");
    }

    // ========== 出售卡牌测试 ==========

    @Test
    void testSellCard_Success() {
        // Given: 玩家拥有一张卡牌
        List<Card> shopCards = cardShop.getCurrentShopCards();
        Card cardToSell = shopCards.get(0);

        // 先购买一张卡
        assertTrue(cardShopManager.buyCard(cardToSell), "购买应该成功");
        assertEquals(1, playerDeck.getCardCount(cardToSell), "玩家应该拥有该卡");

        int initialGold = playerEconomy.getGold();
        int expectedSellPrice = cardToSell.getCost() / 2;

        // When: 出售卡牌
        boolean result = cardShopManager.sellCard(cardToSell);

        // Then: 出售成功，金币增加，卡牌移除
        assertTrue(result, "出售应该成功");
        assertEquals(initialGold + expectedSellPrice, playerEconomy.getGold(), "金币应该增加");
        assertEquals(0, playerDeck.getCardCount(cardToSell), "玩家不应再拥有该卡");
    }

    @Test
    void testSellCard_NotOwned() {
        // Given: 玩家不拥有该卡牌
        List<Card> shopCards = cardShop.getCurrentShopCards();
        Card cardNotOwned = shopCards.get(0);

        assertEquals(0, playerDeck.getCardCount(cardNotOwned), "玩家不应拥有该卡");

        int initialGold = playerEconomy.getGold();

        // When: 尝试出售
        boolean result = cardShopManager.sellCard(cardNotOwned);

        // Then: 出售失败，状态不变
        assertFalse(result, "出售应该失败（玩家没有该卡）");
        assertEquals(initialGold, playerEconomy.getGold(), "金币不应变化");
    }

    @Test
    void testSellCard_MultipleCopies() {
        // Given: 玩家拥有多张相同卡牌
        Card targetCard = cheapCard; // 费用 1
        cardShop = new SimpleMockCardShop(Arrays.asList(cheapCard, cheapCard, cheapCard));

        // 购买 3 张相同的卡
        for (int i = 0; i < 3; i++) {
            cardShop = new SimpleMockCardShop(Arrays.asList(cheapCard));
            cardShopManager = new CardShopManager(
                cardShop, playerEconomy, playerDeck, sharedCardPool, eventSystem
            );
            cardShopManager.buyCard(cheapCard);
        }

        int countBeforeSell = playerDeck.getCardCount(targetCard);
        assertTrue(countBeforeSell > 0, "玩家应该拥有至少一张该卡");

        int initialGold = playerEconomy.getGold();
        int expectedSellPrice = targetCard.getCost() / 2;

        // When: 出售一张
        boolean result = cardShopManager.sellCard(targetCard);

        // Then: 出售成功，数量减1
        assertTrue(result, "出售应该成功");
        assertEquals(countBeforeSell - 1, playerDeck.getCardCount(targetCard), "卡牌数量应该减1");
        assertEquals(initialGold + expectedSellPrice, playerEconomy.getGold(), "金币应该增加");
    }

    // ========== 辅助方法测试 ==========

    @Test
    void testCanAfford() {
        List<Card> shopCards = cardShop.getCurrentShopCards();
        Card card = shopCards.get(0);

        // 金币足够
        if (playerEconomy.getGold() >= card.getCost()) {
            assertTrue(cardShopManager.canAfford(card), "应该买得起");
        }

        // 金币不足
        playerEconomy = new PlayerEconomy(card.getCost() - 1, 1);
        cardShopManager = new CardShopManager(
            cardShop, playerEconomy, playerDeck, sharedCardPool, eventSystem
        );
        assertFalse(cardShopManager.canAfford(card), "应该买不起");
    }

    @Test
    void testGetSellPrice() {
        // 测试向下取整
        Card oddCostCard = createTestCard(100, "奇数费用卡", 3, 1, Card.CardType.WARRIOR);
        assertEquals(1, cardShopManager.getSellPrice(oddCostCard), "3金币卡牌出售价格应为1");

        Card evenCostCard = createTestCard(101, "偶数费用卡", 4, 1, Card.CardType.MAGE);
        assertEquals(2, cardShopManager.getSellPrice(evenCostCard), "4金币卡牌出售价格应为2");
    }

    // ========== 状态查询测试 ==========

    @Test
    void testIsShopEmpty() {
        // 刷新后应该不为空
        assertFalse(cardShopManager.isShopEmpty(), "刷新后商店不应为空");

        // 购买所有卡牌后
        List<Card> shopCards = new ArrayList<>(cardShop.getCurrentShopCards());
        for (Card card : shopCards) {
            cardShopManager.buyCard(card);
        }

        assertTrue(cardShopManager.isShopEmpty(), "购买所有卡牌后商店应为空");
    }

    @Test
    void testGetCurrentGold() {
        assertEquals(playerEconomy.getGold(), cardShopManager.getCurrentGold(), "应返回当前金币");

        // 刷新后金币应减少
        int beforeRefresh = cardShopManager.getCurrentGold();
        cardShopManager.refreshShop();
        int afterRefresh = cardShopManager.getCurrentGold();

        assertEquals(beforeRefresh - cardShop.getRefreshCost(), afterRefresh, "刷新后金币应减少");
    }

    @Test
    void testUpdateForPlayerLevel() {
        // 更新玩家等级
        cardShopManager.updateForPlayerLevel(5);
        assertEquals(5, cardShop.getPlayerLevel(), "玩家等级应更新为5");
    }

    // ========== 生命周期测试 ==========

    @Test
    void testOnEnterAndOnExit() {
        // 测试生命周期方法不抛异常
        assertDoesNotThrow(() -> {
            cardShopManager.onEnter();
            cardShopManager.onExit();
        });
    }

    @Test
    void testPauseResumeDispose() {
        // 测试暂停/恢复/释放不抛异常
        assertDoesNotThrow(() -> {
            cardShopManager.pause();
            cardShopManager.resume();
            cardShopManager.dispose();
        });
    }

    // ========== 辅助方法 ==========

    /**
     * 创建测试用的卡牌对象
     */
    private Card createTestCard(int id, String name, int cost, int tier, Card.CardType type) {
        Card card = new Card(id, name, "测试描述", cost, tier, type);
        card.setStarLevel(1);
        card.setBaseCardId(id);
        card.setSynergies(Arrays.asList(SynergyType.WARRIOR));
        card.setSkillType(SkillType.BUFF);
        card.setSkillValue(30f);
        card.setSkillRange(100f);
        card.setSkillDuration(5f);
        return card;
    }

    /**
     * 简单的 Mock CardShop，用于测试
     * 不依赖 CardPool 和 I18N
     */
    private static class SimpleMockCardShop extends CardShop {
        private final List<Card> mockCards;

        public SimpleMockCardShop(List<Card> cards) {
            super(null); // 不使用 CardPool
            this.mockCards = new ArrayList<>(cards);
        }

        @Override
        public void refresh() {
            // Mock 刷新：不改变卡牌列表
        }

        @Override
        public List<Card> getCurrentShopCards() {
            return new ArrayList<>(mockCards);
        }

        @Override
        public boolean buyCard(Card card) {
            if (mockCards.contains(card)) {
                mockCards.remove(card);
                return true;
            }
            return false;
        }
    }
}
