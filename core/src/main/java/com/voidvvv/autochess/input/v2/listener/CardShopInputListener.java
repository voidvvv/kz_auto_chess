package com.voidvvv.autochess.input.v2.listener;

import com.voidvvv.autochess.input.v2.event.InputEvent;
import com.voidvvv.autochess.input.v2.state.CardShopInputState;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.render.CardShopLayout;
import com.voidvvv.autochess.render.CardShopRenderer;

import java.util.List;

/**
 * CardShopInputListener - 商店输入监听器（改进版）
 *
 * 职责:
 * - 监听鼠标/触摸事件
 * - 更新 CardShopInputState
 * - 调用 CardShopRenderer 的静态方法进行点击检测
 * - 通过解耦的回调接口触发业务逻辑
 *
 * 改进点:
 * - 不直接依赖 CardShopManager
 * - 回调接口使用 cardIndex 而非 Card 对象
 * - 点击检测逻辑复用 CardShopRenderer 的静态方法
 */
public class CardShopInputListener implements InputListener {

    /**
     * 商店交互回调接口（解耦版）
     * 只传递索引，不传递 Card 对象，由调用者解析
     */
    public interface CardShopInteractionHandler {
        void onBuyCard(int cardIndex);
        void onRefreshShop();
    }

    /**
     * 卡牌列表提供者接口
     * 用于获取当前商店的卡牌列表
     */
    public interface ShopCardsProvider {
        List<Card> getShopCards();
    }

    private final CardShopInputState inputState;
    private final CardShopLayout layout;
    private final CardShopInteractionHandler interactionHandler;
    private final ShopCardsProvider cardsProvider;

    /**
     * 构造函数
     *
     * @param inputState 输入状态对象
     * @param layout 布局参数
     * @param cardsProvider 卡牌列表提供者
     * @param interactionHandler 交互处理器
     */
    public CardShopInputListener(CardShopInputState inputState,
                                CardShopLayout layout,
                                ShopCardsProvider cardsProvider,
                                CardShopInteractionHandler interactionHandler) {
        this.inputState = inputState;
        this.layout = layout;
        this.cardsProvider = cardsProvider;
        this.interactionHandler = interactionHandler;
    }

    @Override
    public boolean accepts(InputEvent event) {
        return event.isMouseEvent() || event.isTouchEvent();
    }

    @Override
    public boolean handle(InputEvent event) {
        // 对于 UI 界面，使用屏幕坐标更可靠
        // LibGDX 的 Gdx.input.getX() 是从左边开始，getY() 是从顶部开始
        // 我们的布局也是基于屏幕坐标系统的
        InputEvent.InputPosition pos = event.getInputPosition();

        // 将屏幕坐标（原点在左下角）转换为 UI 坐标（原点在左上角）
        float x = pos.getScreenX();
        float y = com.badlogic.gdx.Gdx.graphics.getHeight() - pos.getScreenY();

        // 调试日志
        com.badlogic.gdx.Gdx.app.log("CardShopInputListener",
            String.format("Input: screenRaw=(%.0f,%.0f) converted=(%.0f,%.0f) hasWorld=%s",
                pos.getScreenX(), pos.getScreenY(), x, y, pos.hasWorldCoords()));

        inputState.mouseX = x;
        inputState.mouseY = y;

        // 更新悬停状态
        updateHoverState();

        // 处理点击事件
        InputEvent.InputState state = event.getInputState();
        if (state == InputEvent.InputState.PRESSED &&
            (event.isLeftMouseButton() || event.isTouchEvent())) {
            inputState.isLeftButtonPressed = true;
            // 检查是否在商店区域或刷新按钮上
            boolean inValidArea = CardShopRenderer.isInShopArea(x, y, layout) ||
                                  CardShopRenderer.isOnRefreshButton(x, y, layout);
            if (inValidArea) {
                inputState.wasPressingInShop = true;
                com.badlogic.gdx.Gdx.app.log("CardShopInputListener",
                    "PRESSED in valid area, wasPressingInShop=true");
            }
        } else if (state == InputEvent.InputState.RELEASED &&
                   (event.isLeftMouseButton() || event.isTouchEvent())) {
            com.badlogic.gdx.Gdx.app.log("CardShopInputListener",
                "RELEASED: wasPressingInShop=" + inputState.wasPressingInShop);
            inputState.isLeftButtonPressed = false;
            if (inputState.wasPressingInShop) {
                handleClick();
                inputState.wasPressingInShop = false;
            }
        }

        return false;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    /**
     * 更新悬停状态
     */
    private void updateHoverState() {
        List<Card> shopCards = cardsProvider.getShopCards();
        inputState.isHoveringShop = CardShopRenderer.isInShopArea(inputState.mouseX, inputState.mouseY, layout);

        // 独立检查刷新按钮（不依赖于 isInShopArea）
        inputState.isHoveringRefreshButton = CardShopRenderer.isOnRefreshButton(
            inputState.mouseX, inputState.mouseY, layout);

        if (inputState.isHoveringShop) {
            inputState.hoveredCardIndex = CardShopRenderer.getCardIndexAtPosition(
                inputState.mouseX, inputState.mouseY, shopCards, layout);
        } else {
            inputState.hoveredCardIndex = -1;
        }
    }

    /**
     * 处理点击事件
     */
    private void handleClick() {
        com.badlogic.gdx.Gdx.app.log("CardShopInputListener",
            String.format("Click: hoveringShop=%s, hoverRefresh=%s, hoverCardIndex=%d",
                inputState.isHoveringShop, inputState.isHoveringRefreshButton, inputState.hoveredCardIndex));

        // 优先检查刷新按钮（独立于商店区域）
        if (inputState.isHoveringRefreshButton) {
            com.badlogic.gdx.Gdx.app.log("CardShopInputListener", "Refresh button clicked!");
            interactionHandler.onRefreshShop();
            return;
        }

        // 检查卡牌（需要在商店区域内）
        if (inputState.isHoveringShop && inputState.hoveredCardIndex >= 0) {
            com.badlogic.gdx.Gdx.app.log("CardShopInputListener", "Card clicked at index: " + inputState.hoveredCardIndex);
            interactionHandler.onBuyCard(inputState.hoveredCardIndex);
        }
    }

    /**
     * 获取输入状态对象
     */
    public CardShopInputState getInputState() {
        return inputState;
    }
}
