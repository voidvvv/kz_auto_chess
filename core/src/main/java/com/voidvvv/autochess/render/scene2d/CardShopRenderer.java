package com.voidvvv.autochess.render.scene2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.CardShop;
import com.voidvvv.autochess.model.PlayerEconomy;
import com.voidvvv.autochess.model.SharedCardPool;
import com.voidvvv.autochess.ui.CardRenderer;
import com.voidvvv.autochess.utils.FontUtils;
import com.voidvvv.autochess.utils.I18N;

import java.util.List;

/**
 * 卡牌商店渲染器
 * Scene2D Actor 组件，负责渲染和交互卡牌商店
 */
public class CardShopRenderer extends Actor {

    /**
     * 商店交互回调接口
     */
    public interface ShopCallback {
        /**
         * 购买卡牌回调
         * @param card 要购买的卡牌
         * @return 是否购买成功
         */
        boolean onBuyCard(Card card);

        /**
         * 刷新商店回调
         */
        void onRefreshShop();
    }

    private final CardShop cardShop;
    private final SharedCardPool sharedCardPool;
    private final PlayerEconomy playerEconomy;
    private final ShopCallback shopCallback;

    private final ShapeRenderer shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont smallFont;
    private final GlyphLayout tempGlyphLayout;

    // 布局参数
    private float cardWidth = 120;
    private float cardHeight = 160;
    private float cardSpacing = 10;
    private float headerHeight = 40;
    private float footerHeight = 50;

    // 点击区域
    private Rectangle refreshButtonArea;

    // 悬停状态
    private int hoveredCardIndex = -1;
    private boolean isRefreshHovered = false;

    /**
     * 构造函数
     * @param cardShop 商店数据对象
     * @param sharedCardPool 共享卡池（用于显示池数量，可为null）
     * @param playerEconomy 玩家经济（用于检查金币，可为null）
     * @param shopCallback 交互回调接口
     */
    public CardShopRenderer(CardShop cardShop,
                           SharedCardPool sharedCardPool,
                           PlayerEconomy playerEconomy,
                           ShopCallback shopCallback) {
        if (cardShop == null) {
            throw new IllegalArgumentException("cardShop cannot be null");
        }
        if (shopCallback == null) {
            throw new IllegalArgumentException("shopCallback cannot be null");
        }

        this.cardShop = cardShop;
        this.sharedCardPool = sharedCardPool;
        this.playerEconomy = playerEconomy;
        this.shopCallback = shopCallback;

        this.shapeRenderer = new ShapeRenderer();
        this.titleFont = FontUtils.getDefaultFont();
        this.smallFont = FontUtils.getSmallFont();
        this.tempGlyphLayout = new GlyphLayout();

        // 初始化刷新按钮区域
        this.refreshButtonArea = new Rectangle();

        // 添加点击监听器
        addClickListener();
    }

    /**
     * 设置卡牌尺寸
     */
    public void setCardSize(float width, float height) {
        this.cardWidth = width;
        this.cardHeight = height;
    }

    /**
     * 设置卡牌间距
     */
    public void setCardSpacing(float spacing) {
        this.cardSpacing = spacing;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // 获取 Actor 在舞台中的绝对位置
        float stageX = getX();
        float stageY = getY();

        // 渲染商店背景
        batch.end();
        renderShopBackground(stageX, stageY);

        // 渲染商店标题
        renderShopHeader(batch, stageX, stageY);

        // 渲染卡牌列表
        renderShopCards(batch, stageX, stageY);

        // 渲染刷新按钮
        renderRefreshButton(batch, stageX, stageY);
        batch.begin();
    }

    /**
     * 渲染商店背景
     */
    private void renderShopBackground(float x, float y) {
        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 背景色
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1);
        shapeRenderer.rect(x, y, getWidth(), getHeight());

        shapeRenderer.end();

        // 边框
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x, y, getWidth(), getHeight());
        shapeRenderer.end();
    }

    /**
     * 渲染商店标题和信息
     */
    private void renderShopHeader(Batch batch, float x, float y) {
        batch.begin();

        // 商店标题
        titleFont.setColor(Color.YELLOW);
        titleFont.getData().setScale(1.0f);
        String shopTitle = I18N.get("shop");
        tempGlyphLayout.setText(titleFont, shopTitle);
        titleFont.draw(batch, tempGlyphLayout, x + 10, y + getHeight() - 10);

        // 玩家等级
        titleFont.setColor(Color.CYAN);
        titleFont.getData().setScale(0.8f);
        String levelText = I18N.format("player_level", cardShop.getPlayerLevel());
        tempGlyphLayout.setText(titleFont, levelText);
        titleFont.draw(batch, tempGlyphLayout, x + 10, y + getHeight() - 30);

        // 金币信息（如果有玩家经济数据）
        if (playerEconomy != null) {
            titleFont.setColor(Color.GOLD);
            String goldText = I18N.format("gold_text", playerEconomy.getGold());
            tempGlyphLayout.setText(titleFont, goldText);
            titleFont.draw(batch, tempGlyphLayout, x + getWidth() - tempGlyphLayout.width - 10, y + getHeight() - 10);
        }

        batch.end();
    }

    /**
     * 渲染商店卡牌
     */
    private void renderShopCards(Batch batch, float x, float y) {
        List<Card> shopCards = cardShop.getCurrentShopCards();
        if (shopCards.isEmpty()) {
            renderEmptyShop(batch, x, y);
            return;
        }

        // 计算卡牌起始位置（居中显示）
        float totalCardsWidth = shopCards.size() * cardWidth + (shopCards.size() - 1) * cardSpacing;
        float cardStartX = x + (getWidth() - totalCardsWidth) / 2;
        float cardStartY = y + footerHeight;

        // 获取鼠标位置用于悬停检测
        float mouseLocalX = getMouseLocalX();
        float mouseLocalY = getMouseLocalY();

        hoveredCardIndex = -1;

        for (int i = 0; i < shopCards.size(); i++) {
            Card card = shopCards.get(i);
            float cardX = cardStartX + i * (cardWidth + cardSpacing);
            float cardY = cardStartY;

            // 检测悬停
            boolean hover = mouseLocalX >= cardX && mouseLocalX <= cardX + cardWidth &&
                           mouseLocalY >= cardY && mouseLocalY <= cardY + cardHeight;

            if (hover) {
                hoveredCardIndex = i;
            }

            // 获取池数量
            int poolRemaining = -1;
            int poolMax = -1;
            if (sharedCardPool != null) {
                poolRemaining = sharedCardPool.getRemainingCopies(card.getId());
                poolMax = sharedCardPool.getMaxCopies(card.getId());
            }

            // 渲染卡牌 (CardRenderer 需要 SpriteBatch)
            if (batch instanceof SpriteBatch) {
                CardRenderer.render(shapeRenderer, (SpriteBatch) batch, card, cardX, cardY, cardWidth, cardHeight,
                        hover, false, 0, false, poolRemaining, poolMax);
            }
        }
    }

    /**
     * 渲染空商店提示
     */
    private void renderEmptyShop(Batch batch, float x, float y) {
        batch.begin();
        smallFont.setColor(Color.GRAY);
        String emptyText = I18N.get("shop_empty");
        tempGlyphLayout.setText(smallFont, emptyText);
        float textX = x + (getWidth() - tempGlyphLayout.width) / 2;
        float textY = y + getHeight() / 2;
        smallFont.draw(batch, tempGlyphLayout, textX, textY);
        batch.end();
    }

    /**
     * 渲染刷新按钮
     */
    private void renderRefreshButton(Batch batch, float x, float y) {
        // 刷新按钮位置（右下角）
        float buttonWidth = 120;
        float buttonHeight = 35;
        float buttonX = x + getWidth() - buttonWidth - 10;
        float buttonY = y + 10;

        // 更新刷新按钮区域
        refreshButtonArea.set(buttonX, buttonY, buttonWidth, buttonHeight);

        // 检测悬停
        float mouseLocalX = getMouseLocalX();
        float mouseLocalY = getMouseLocalY();
        isRefreshHovered = refreshButtonArea.contains(mouseLocalX, mouseLocalY);

        // 检查金币是否足够
        boolean canAfford = true;
        if (playerEconomy != null) {
            canAfford = playerEconomy.getGold() >= cardShop.getRefreshCost();
        }

        // 绘制按钮背景
        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (!canAfford) {
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1); // 灰色 - 买不起
        } else if (isRefreshHovered) {
            shapeRenderer.setColor(0.4f, 0.6f, 0.9f, 1); // 蓝色 - 悬停
        } else {
            shapeRenderer.setColor(0.2f, 0.4f, 0.7f, 1); // 深蓝色 - 正常
        }

        shapeRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight);
        shapeRenderer.end();

        // 绘制按钮边框
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight);
        shapeRenderer.end();

        // 绘制按钮文字
        batch.begin();
        smallFont.setColor(canAfford ? Color.WHITE : Color.GRAY);
        smallFont.getData().setScale(0.9f);
        String refreshText = I18N.format("refresh_cost", cardShop.getRefreshCost());
        tempGlyphLayout.setText(smallFont, refreshText);
        float textX = buttonX + (buttonWidth - tempGlyphLayout.width) / 2;
        float textY = buttonY + (buttonHeight + tempGlyphLayout.height) / 2;
        smallFont.draw(batch, tempGlyphLayout, textX, textY);
        batch.end();
    }

    /**
     * 添加点击监听器
     */
    private void addClickListener() {
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float localX, float localY) {
                handleClicked(localX, localY);
            }
        });
    }

    /**
     * 处理点击事件
     */
    private void handleClicked(float localX, float localY) {
        // 检查是否点击了刷新按钮
        if (refreshButtonArea.contains(localX, localY)) {
            // 检查金币是否足够
            if (playerEconomy == null || playerEconomy.getGold() >= cardShop.getRefreshCost()) {
                shopCallback.onRefreshShop();
            }
            return;
        }

        // 检查是否点击了卡牌
        if (hoveredCardIndex >= 0) {
            List<Card> shopCards = cardShop.getCurrentShopCards();
            if (hoveredCardIndex < shopCards.size()) {
                Card clickedCard = shopCards.get(hoveredCardIndex);
                shopCallback.onBuyCard(clickedCard);
            }
        }
    }

    /**
     * 获取鼠标相对于 Actor 的本地坐标
     */
    private float getMouseLocalX() {
        return getStage().screenToStageCoordinates(
            new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY())
        ).x - getX();
    }

    /**
     * 获取鼠标相对于 Actor 的本地坐标
     */
    private float getMouseLocalY() {
        return getStage().screenToStageCoordinates(
            new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY())
        ).y - getY();
    }

    /**
     * 更新悬停状态（每帧调用）
     */
    public void updateHoverState() {
        // 悬停状态在 draw 方法中更新
        // 这个方法可以用于外部触发更新
    }

    /**
     * 释放资源
     */
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
    }

    /**
     * 获取 CardShop 引用
     */
    public CardShop getCardShop() {
        return cardShop;
    }

    /**
     * 获取当前悬停的卡牌索引
     */
    public int getHoveredCardIndex() {
        return hoveredCardIndex;
    }

    /**
     * 获取当前悬停的卡牌
     */
    public Card getHoveredCard() {
        if (hoveredCardIndex >= 0) {
            List<Card> shopCards = cardShop.getCurrentShopCards();
            if (hoveredCardIndex < shopCards.size()) {
                return shopCards.get(hoveredCardIndex);
            }
        }
        return null;
    }
}
