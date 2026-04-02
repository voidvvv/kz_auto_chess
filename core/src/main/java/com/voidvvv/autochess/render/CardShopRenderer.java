package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.ui.CardRenderer;
import com.voidvvv.autochess.utils.FontUtils;
import com.voidvvv.autochess.utils.I18N;

import java.util.List;

/**
 * CardShopRenderer - 卡牌商店渲染器（静态工具类）
 *
 * 职责:
 * - 渲染商店背景、标题
 * - 渲染商店中的卡牌列表（复用 CardRenderer）
 * - 渲染刷新按钮
 * - 提供与渲染布局完全同步的点击检测方法
 *
 * 设计原则:
 * - 静态方法，无状态设计
 * - 接收 CardShopLayout 参数，避免静态变量
 * - 批处理优化渲染性能
 * - 点击检测与渲染使用相同的布局参数
 */
public class CardShopRenderer {

    // ========== 颜色常量 ==========
    private static final Color SHOP_BG_COLOR = new Color(0.15f, 0.15f, 0.2f, 0.95f);
    private static final Color SHOP_BORDER_COLOR = new Color(0.4f, 0.4f, 0.5f, 1);
    private static final Color REFRESH_BUTTON_NORMAL = new Color(0.3f, 0.5f, 0.8f, 1);
    private static final Color REFRESH_BUTTON_HOVER = new Color(0.4f, 0.6f, 0.9f, 1);
    private static final Color REFRESH_BUTTON_DISABLED = new Color(0.3f, 0.3f, 0.3f, 1);
    private static final Color TITLE_COLOR = new Color(1.0f, 0.85f, 0.2f, 1);
    private static final Color GOLD_COLOR = new Color(1.0f, 0.8f, 0.2f, 1);

    /**
     * 渲染商店区域（主入口方法）
     *
     * @param holder 渲染上下文
     * @param shopCards 商店卡牌列表
     * @param currentGold 当前金币
     * @param refreshCost 刷新费用
     * @param mouseX 鼠标 X 坐标（UI 坐标系）
     * @param mouseY 鼠标 Y 坐标（UI 坐标系）
     * @param layout 布局参数
     */
    public static void renderShop(RenderHolder holder,
                                  List<Card> shopCards,
                                  int currentGold,
                                  int refreshCost,
                                  float mouseX,
                                  float mouseY,
                                  CardShopLayout layout) {
        SpriteBatch batch = holder.getSpriteBatch();
        ShapeRenderer shapeRenderer = holder.getShapeRenderer();
        BitmapFont font = FontUtils.getSmallFont();

        // 1. 渲染商店背景
        renderShopBackground(shapeRenderer, layout);

        // 2. 渲染标题和金币信息
        renderTitleAndGold(batch, font, currentGold, layout);

        // 3. 渲染卡牌列表
        renderCards(shapeRenderer, batch, shopCards, mouseX, mouseY, layout);

        // 4. 渲染刷新按钮
        boolean canAffordRefresh = currentGold >= refreshCost;
        renderRefreshButton(shapeRenderer, batch, font, refreshCost, canAffordRefresh, mouseX, mouseY, layout);
    }

    /**
     * 渲染商店背景
     */
    private static void renderShopBackground(ShapeRenderer shapeRenderer, CardShopLayout layout) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(SHOP_BG_COLOR);
        shapeRenderer.rect(layout.shopX, layout.shopY, layout.shopWidth, layout.shopHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(SHOP_BORDER_COLOR);
        shapeRenderer.rect(layout.shopX, layout.shopY, layout.shopWidth, layout.shopHeight);
        shapeRenderer.end();
    }

    /**
     * 渲染标题和金币信息
     */
    private static void renderTitleAndGold(SpriteBatch batch, BitmapFont font,
                                          int currentGold, CardShopLayout layout) {
        batch.begin();
        try {
            // 标题
            font.setColor(TITLE_COLOR);
            font.getData().setScale(1.0f);
            String title = I18N.get("shop_title");
            GlyphLayout titleLayout = new GlyphLayout(font, title);
            float titleX = layout.shopX + (layout.shopWidth - titleLayout.width) / 2;
            float titleY = layout.shopY + layout.shopHeight - layout.titleOffsetFromTop;
            font.draw(batch, titleLayout, titleX, titleY);

            // 金币信息
            font.setColor(GOLD_COLOR);
            font.getData().setScale(0.8f);
            String goldText = I18N.format("gold_text", currentGold);
            GlyphLayout goldLayout = new GlyphLayout(font, goldText);
            float goldX = layout.shopX + layout.shopWidth - goldLayout.width - 10;
            float goldY = layout.shopY + layout.shopHeight - layout.titleOffsetFromTop;
            font.draw(batch, goldLayout, goldX, goldY);
        } finally {
            batch.end();
        }
    }

    /**
     * 渲染卡牌列表
     */
    private static void renderCards(ShapeRenderer shapeRenderer, SpriteBatch batch,
                                   List<Card> shopCards, float mouseX, float mouseY,
                                   CardShopLayout layout) {
        if (shopCards == null || shopCards.isEmpty()) {
            return;
        }

        for (int i = 0; i < shopCards.size(); i++) {
            Card card = shopCards.get(i);
            float cardX = layout.cardStartX + i * (layout.cardWidth + layout.cardSpacing);
            float cardY = layout.cardStartY;

            boolean isHovering = mouseX >= cardX && mouseX <= cardX + layout.cardWidth &&
                               mouseY >= cardY && mouseY <= cardY + layout.cardHeight;

            // 复用现有 CardRenderer（商店卡牌不显示数量，不显示池数量）
            CardRenderer.render(shapeRenderer, batch, card,
                cardX, cardY, layout.cardWidth, layout.cardHeight,
                isHovering, false, 0, false, -1, -1);
        }
    }

    /**
     * 渲染刷新按钮
     */
    private static void renderRefreshButton(ShapeRenderer shapeRenderer, SpriteBatch batch,
                                           BitmapFont font, int refreshCost, boolean canAfford,
                                           float mouseX, float mouseY, CardShopLayout layout) {
        boolean isHovering = mouseX >= layout.refreshButtonX &&
                            mouseX <= layout.refreshButtonX + layout.refreshButtonWidth &&
                            mouseY >= layout.refreshButtonY &&
                            mouseY <= layout.refreshButtonY + layout.refreshButtonHeight;

        Color buttonColor;
        if (!canAfford) {
            buttonColor = REFRESH_BUTTON_DISABLED;
        } else if (isHovering) {
            buttonColor = REFRESH_BUTTON_HOVER;
        } else {
            buttonColor = REFRESH_BUTTON_NORMAL;
        }

        // 按钮背景
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(buttonColor);
        shapeRenderer.rect(layout.refreshButtonX, layout.refreshButtonY,
                          layout.refreshButtonWidth, layout.refreshButtonHeight);
        shapeRenderer.end();

        // 按钮边框
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(layout.refreshButtonX, layout.refreshButtonY,
                          layout.refreshButtonWidth, layout.refreshButtonHeight);
        shapeRenderer.end();

        // 按钮文字
        batch.begin();
        try {
            font.setColor(canAfford ? Color.WHITE : Color.GRAY);
            font.getData().setScale(0.8f);
            String buttonText = I18N.format("refresh_cost", refreshCost);
            GlyphLayout layoutGlyph = new GlyphLayout(font, buttonText);
            float textX = layout.refreshButtonX + (layout.refreshButtonWidth - layoutGlyph.width) / 2;
            float textY = layout.refreshButtonY + (layout.refreshButtonHeight + layoutGlyph.height) / 2;
            font.draw(batch, layoutGlyph, textX, textY);
        } finally {
            batch.end();
        }
    }

    // ========== 点击检测方法 ==========

    /**
     * 获取指定位置的卡牌索引
     * @param x X 坐标
     * @param y Y 坐标
     * @param shopCards 商店卡牌列表
     * @param layout 布局参数
     * @return 卡牌索引，未点击到卡牌返回 -1
     */
    public static int getCardIndexAtPosition(float x, float y, List<Card> shopCards, CardShopLayout layout) {
        if (shopCards == null || shopCards.isEmpty()) {
            return -1;
        }

        for (int i = 0; i < shopCards.size(); i++) {
            float cardX = layout.cardStartX + i * (layout.cardWidth + layout.cardSpacing);
            float cardY = layout.cardStartY;

            if (x >= cardX && x <= cardX + layout.cardWidth &&
                y >= cardY && y <= cardY + layout.cardHeight) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 判断是否点击刷新按钮
     * @param x X 坐标
     * @param y Y 坐标
     * @param layout 布局参数
     * @return 是否在刷新按钮区域
     */
    public static boolean isOnRefreshButton(float x, float y, CardShopLayout layout) {
        return x >= layout.refreshButtonX && x <= layout.refreshButtonX + layout.refreshButtonWidth &&
               y >= layout.refreshButtonY && y <= layout.refreshButtonY + layout.refreshButtonHeight;
    }

    /**
     * 判断是否在商店区域内
     * @param x X 坐标
     * @param y Y 坐标
     * @param layout 布局参数
     * @return 是否在商店区域
     */
    public static boolean isInShopArea(float x, float y, CardShopLayout layout) {
        return x >= layout.shopX && x <= layout.shopX + layout.shopWidth &&
               y >= layout.shopY && y <= layout.shopY + layout.shopHeight;
    }
}
