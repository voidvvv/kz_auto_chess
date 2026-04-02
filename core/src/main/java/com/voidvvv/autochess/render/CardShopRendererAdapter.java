package com.voidvvv.autochess.render;

import com.voidvvv.autochess.input.v2.listener.CardShopInputListener;
import com.voidvvv.autochess.input.v2.state.CardShopInputState;
import com.voidvvv.autochess.manage.CardShopManager;
import com.voidvvv.autochess.model.Card;

import java.util.List;

/**
 * CardShopRendererAdapter - 商店渲染器适配器（改进版）
 *
 * 职责:
 * - 实现 GameRenderer 接口
 * - 协调 CardShopManager、CardShopLayout 和 CardShopInputState
 * - 实现 ShopCardsProvider 接口提供卡牌列表
 * - 实现 CardShopInteractionHandler 接口将 cardIndex 转换为 Card 对象
 *
 * 改进点:
 * - 持有 CardShopLayout 和 CardShopInputState
 * - 实现 ShopCardsProvider 接口
 * - 实现 CardShopInteractionHandler 接口，将 cardIndex 转换为 Card 对象
 */
public class CardShopRendererAdapter implements GameRenderer,
        CardShopInputListener.ShopCardsProvider,
        CardShopInputListener.CardShopInteractionHandler {

    private final CardShopManager cardShopManager;
    private final CardShopLayout layout;
    private final CardShopInputState inputState;
    private final CardShopInputListener inputListener;

    /**
     * 构造函数
     *
     * @param cardShopManager 商店管理器
     */
    public CardShopRendererAdapter(CardShopManager cardShopManager) {
        this.cardShopManager = cardShopManager;
        this.layout = CardShopLayout.createDefault();
        this.inputState = new CardShopInputState();

        // 创建 InputListener，传入 this 作为 Provider 和 Handler
        this.inputListener = new CardShopInputListener(
            inputState, layout, this, this);
    }

    @Override
    public void render(RenderHolder holder) {
        List<Card> shopCards = cardShopManager.getShopCards();
        int currentGold = cardShopManager.getCurrentGold();
        int refreshCost = cardShopManager.getRefreshCost();

        CardShopRenderer.renderShop(holder, shopCards, currentGold, refreshCost,
            inputState.mouseX, inputState.mouseY, layout);
    }

    // ========== ShopCardsProvider 实现 ==========

    @Override
    public List<Card> getShopCards() {
        return cardShopManager.getShopCards();
    }

    // ========== CardShopInteractionHandler 实现 ==========

    @Override
    public void onBuyCard(int cardIndex) {
        List<Card> shopCards = cardShopManager.getShopCards();
        if (cardIndex >= 0 && cardIndex < shopCards.size()) {
            Card card = shopCards.get(cardIndex);
            cardShopManager.buyCard(card);
        }
    }

    @Override
    public void onRefreshShop() {
        cardShopManager.refreshShop();
    }

    // ========== Getters ==========

    /**
     * 获取输入监听器（用于注册到 InputHandlerV2）
     */
    public CardShopInputListener getInputListener() {
        return inputListener;
    }

    /**
     * 获取布局参数（用于动态调整布局）
     */
    public CardShopLayout getLayout() {
        return layout;
    }

    /**
     * 获取输入状态（用于调试或测试）
     */
    public CardShopInputState getInputState() {
        return inputState;
    }

    /**
     * 获取商店管理器（用于其他组件访问）
     */
    public CardShopManager getCardShopManager() {
        return cardShopManager;
    }
}
