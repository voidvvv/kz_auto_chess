package com.voidvvv.autochess.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.utils.FontUtils;
import com.voidvvv.autochess.utils.I18N;

/**
 * 卡牌渲染组件
 * 将卡牌的渲染逻辑从主方法中抽离，方便后续拓展
 */
public class CardRenderer {
    private static BitmapFont cardFont;

    static {
        cardFont = FontUtils.getSmallFont();
    }

    /**
     * 渲染卡牌
     * @param shapeRenderer ShapeRenderer实例
     * @param batch SpriteBatch实例
     * @param card 要渲染的卡牌
     * @param x 卡牌X坐标
     * @param y 卡牌Y坐标
     * @param width 卡牌宽度
     * @param height 卡牌高度
     * @param hover 是否悬停
     * @param showCount 是否显示数量（用于卡组中的卡牌）
     * @param count 数量（如果showCount为true）
     */
    public static void render(ShapeRenderer shapeRenderer, SpriteBatch batch,
                             Card card, float x, float y, float width, float height,
                             boolean hover, boolean showCount, int count) {
        // 绘制卡牌背景
        Color cardColor = getCardColorByTier(card.getTier());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (hover) {
            shapeRenderer.setColor(cardColor.r * 1.2f, cardColor.g * 1.2f, cardColor.b * 1.2f, 1);
        } else {
            shapeRenderer.setColor(cardColor);
        }
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        // 绘制卡牌边框
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(hover ? Color.YELLOW : Color.WHITE);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        // 绘制卡牌信息
        batch.begin();
        float scale = showCount ? 0.85f : 1.0f;
        cardFont.getData().setScale(scale);

        // 卡牌名称
        cardFont.setColor(Color.WHITE);
        String name = card.getName();
        GlyphLayout nameLayout = new GlyphLayout(cardFont, name);
        if (showCount && nameLayout.width > width - 10) {
            name = name.length() > 4 ? name.substring(0, 4) : name;
            nameLayout = new GlyphLayout(cardFont, name);
        }
        cardFont.draw(batch, nameLayout, x + 5, y + height - 8);

        // 卡牌类型
        cardFont.setColor(Color.LIGHT_GRAY);
        cardFont.getData().setScale(showCount ? 0.7f : 0.8f);
        GlyphLayout typeLayout = new GlyphLayout(cardFont, card.getType().getDisplayName());
        cardFont.draw(batch, typeLayout, x + 5, y + height - (showCount ? 25 : 30));

        // 卡牌等级
        cardFont.setColor(Color.CYAN);
        String tierText = showCount ?
                    I18N.format("tier_short", card.getTier()) :
                    I18N.format("tier_level", card.getTier());
        GlyphLayout tierLayout = new GlyphLayout(cardFont, tierText);
        cardFont.draw(batch, tierLayout, x + 5, y + 8);

        // 如果不是卡组中的卡牌，显示费用
        if (!showCount) {
            cardFont.setColor(Color.GOLD);
            cardFont.getData().setScale(0.8f);
            String costText = I18N.format("cost_text", card.getCost());
            GlyphLayout costLayout = new GlyphLayout(cardFont, costText);
            cardFont.draw(batch, costLayout, x + 5, y + 20);
        }

        batch.end();

        // 如果显示数量，绘制数量标识
        if (showCount && count > 0) {
            renderCountBadge(shapeRenderer, batch, x, y, width, height, count);
        }
    }

    /**
     * 渲染数量标识
     */
    private static void renderCountBadge(ShapeRenderer shapeRenderer, SpriteBatch batch,
                                        float cardX, float cardY, float cardWidth, float cardHeight, int count) {
        float countBadgeSize = 22;
        float badgeX = cardX + cardWidth - countBadgeSize/2 - 2;
        float badgeY = cardY + cardHeight - countBadgeSize/2 - 2;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0.8f, 0f, 1); // 金色背景
        shapeRenderer.circle(badgeX, badgeY, countBadgeSize/2);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(badgeX, badgeY, countBadgeSize/2);
        shapeRenderer.end();

        batch.begin();
        cardFont.setColor(Color.BLACK);
        cardFont.getData().setScale(0.75f);
        String countText = I18N.format("count_text", count);
        GlyphLayout countLayout = new GlyphLayout(cardFont, countText);
        float countX = badgeX - countLayout.width/2;
        float countY = badgeY + countLayout.height/2;
        cardFont.draw(batch, countLayout, countX, countY);
        batch.end();
    }

    /**
     * 根据卡牌等级获取颜色
     */
    private static Color getCardColorByTier(int tier) {
        switch (tier) {
            case 1: return new Color(0.7f, 0.7f, 0.7f, 1); // 灰色
            case 2: return new Color(0.3f, 0.7f, 0.3f, 1); // 绿色
            case 3: return new Color(0.3f, 0.5f, 0.9f, 1); // 蓝色
            case 4: return new Color(0.7f, 0.3f, 0.9f, 1); // 紫色
            case 5: return new Color(0.9f, 0.7f, 0.2f, 1); // 金色
            default: return Color.WHITE;
        }
    }
}

