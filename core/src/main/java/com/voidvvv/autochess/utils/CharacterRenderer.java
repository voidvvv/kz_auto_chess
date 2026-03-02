package com.voidvvv.autochess.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Card;

/**
 * 角色渲染工具类
 * 将角色渲染逻辑从主方法中抽离，方便后续手动重写
 */
public class CharacterRenderer {

    /**
     * 渲染战场角色（带透明度）
     * @param shapeRenderer ShapeRenderer实例
     * @param character 要渲染的角色
     * @param alpha 透明度 (0.0-1.0)
     */
    public static void renderWithAlpha(ShapeRenderer shapeRenderer, BattleCharacter character, float alpha) {
        Card card = character.getCard();
        float x = character.getX();
        float y = character.getY();
        float size = character.getSize();

        // 根据卡牌类型选择不同的渲染方式
        switch (card.getType()) {
            case WARRIOR:
                renderWarriorWithAlpha(shapeRenderer, x, y, size, card, alpha);
                break;
            case MAGE:
                renderMageWithAlpha(shapeRenderer, x, y, size, card, alpha);
                break;
            case ARCHER:
                renderArcherWithAlpha(shapeRenderer, x, y, size, card, alpha);
                break;
            case ASSASSIN:
                renderAssassinWithAlpha(shapeRenderer, x, y, size, card, alpha);
                break;
            case TANK:
                renderTankWithAlpha(shapeRenderer, x, y, size, card, alpha);
                break;
        }
    }

    /**
     * 渲染战士（正方形）- 带透明度
     */
    private static void renderWarriorWithAlpha(ShapeRenderer shapeRenderer, float x, float y, float size, Card card, float alpha) {
        Color color = getColorByTier(card.getTier());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color.r, color.g, color.b, alpha);
        shapeRenderer.rect(x - size/2, y - size/2, size, size);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, alpha);
        shapeRenderer.rect(x - size/2, y - size/2, size, size);
        shapeRenderer.end();
    }

    /**
     * 渲染法师（圆形）- 带透明度
     */
    private static void renderMageWithAlpha(ShapeRenderer shapeRenderer, float x, float y, float size, Card card, float alpha) {
        Color color = getColorByTier(card.getTier());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color.r, color.g, color.b, alpha);
        shapeRenderer.circle(x, y, size/2);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, alpha);
        shapeRenderer.circle(x, y, size/2);
        shapeRenderer.end();
    }

    /**
     * 渲染射手（五角形）- 带透明度
     */
    private static void renderArcherWithAlpha(ShapeRenderer shapeRenderer, float x, float y, float size, Card card, float alpha) {
        Color color = getColorByTier(card.getTier());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color.r, color.g, color.b, alpha);
        renderPentagonFilled(shapeRenderer, x, y, size/2);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, alpha);
        renderPentagonLine(shapeRenderer, x, y, size/2);
        shapeRenderer.end();
    }

    /**
     * 渲染刺客（菱形）- 带透明度
     */
    private static void renderAssassinWithAlpha(ShapeRenderer shapeRenderer, float x, float y, float size, Card card, float alpha) {
        Color color = getColorByTier(card.getTier());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color.r, color.g, color.b, alpha);
        float halfSize = size / 2;
        shapeRenderer.triangle(x, y + halfSize, x - halfSize, y, x + halfSize, y);
        shapeRenderer.triangle(x, y - halfSize, x - halfSize, y, x + halfSize, y);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, alpha);
        shapeRenderer.triangle(x, y + halfSize, x - halfSize, y, x + halfSize, y);
        shapeRenderer.triangle(x, y - halfSize, x - halfSize, y, x + halfSize, y);
        shapeRenderer.end();
    }

    /**
     * 渲染坦克（六边形）- 带透明度
     */
    private static void renderTankWithAlpha(ShapeRenderer shapeRenderer, float x, float y, float size, Card card, float alpha) {
        Color color = getColorByTier(card.getTier());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color.r, color.g, color.b, alpha);
        renderHexagonFilled(shapeRenderer, x, y, size/2);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, alpha);
        renderHexagonLine(shapeRenderer, x, y, size/2);
        shapeRenderer.end();
    }

    /**
     * 渲染战场角色
     * @param shapeRenderer ShapeRenderer实例
     * @param character 要渲染的角色
     */
    public static void render(ShapeRenderer shapeRenderer, BattleCharacter character) {
        Card card = character.getCard();
        float x = character.getX();
        float y = character.getY();
        float size = character.getSize();

        // 根据卡牌类型选择不同的渲染方式
        switch (card.getType()) {
            case WARRIOR:
                renderWarrior(shapeRenderer, x, y, size, card);
                break;
            case MAGE:
                renderMage(shapeRenderer, x, y, size, card);
                break;
            case ARCHER:
                renderArcher(shapeRenderer, x, y, size, card);
                break;
            case ASSASSIN:
                renderAssassin(shapeRenderer, x, y, size, card);
                break;
            case TANK:
                renderTank(shapeRenderer, x, y, size, card);
                break;
        }
    }

    /**
     * 渲染战士（正方形）
     */
    private static void renderWarrior(ShapeRenderer shapeRenderer, float x, float y, float size, Card card) {
        Color color = getColorByTier(card.getTier());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x - size/2, y - size/2, size, size);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x - size/2, y - size/2, size, size);
        shapeRenderer.end();
    }

    /**
     * 渲染法师（圆形）
     */
    private static void renderMage(ShapeRenderer shapeRenderer, float x, float y, float size, Card card) {
        Color color = getColorByTier(card.getTier());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.circle(x, y, size/2);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(x, y, size/2);
        shapeRenderer.end();
    }

    /**
     * 渲染射手（五角形）
     */
    private static void renderArcher(ShapeRenderer shapeRenderer, float x, float y, float size, Card card) {
        Color color = getColorByTier(card.getTier());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        renderPentagonFilled(shapeRenderer, x, y, size/2);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        renderPentagonLine(shapeRenderer, x, y, size/2);
        shapeRenderer.end();
    }

    /**
     * 渲染刺客（菱形）
     */
    private static void renderAssassin(ShapeRenderer shapeRenderer, float x, float y, float size, Card card) {
        Color color = getColorByTier(card.getTier());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        float halfSize = size / 2;
        shapeRenderer.triangle(x, y + halfSize, x - halfSize, y, x + halfSize, y);
        shapeRenderer.triangle(x, y - halfSize, x - halfSize, y, x + halfSize, y);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.triangle(x, y + halfSize, x - halfSize, y, x + halfSize, y);
        shapeRenderer.triangle(x, y - halfSize, x - halfSize, y, x + halfSize, y);
        shapeRenderer.end();
    }

    /**
     * 渲染坦克（六边形）
     */
    private static void renderTank(ShapeRenderer shapeRenderer, float x, float y, float size, Card card) {
        Color color = getColorByTier(card.getTier());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        renderHexagonFilled(shapeRenderer, x, y, size/2);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        renderHexagonLine(shapeRenderer, x, y, size/2);
        shapeRenderer.end();
    }

    /**
     * 绘制五角形（填充模式）
     */
    private static void renderPentagonFilled(ShapeRenderer shapeRenderer, float centerX, float centerY, float radius) {
        int points = 5;
        float[] vertices = new float[points * 2];
        for (int i = 0; i < points; i++) {
            float angle = (float) (2 * Math.PI * i / points - Math.PI / 2);
            vertices[i * 2] = centerX + radius * (float) Math.cos(angle);
            vertices[i * 2 + 1] = centerY + radius * (float) Math.sin(angle);
        }

        // 使用三角形从中心点向外绘制五角形
        for (int i = 0; i < points; i++) {
            int nextI = (i + 1) % points;
            shapeRenderer.triangle(
                centerX, centerY,
                vertices[i * 2], vertices[i * 2 + 1],
                vertices[nextI * 2], vertices[nextI * 2 + 1]
            );
        }
    }

    /**
     * 绘制五角形（线条模式）
     */
    private static void renderPentagonLine(ShapeRenderer shapeRenderer, float centerX, float centerY, float radius) {
        int points = 5;
        float[] vertices = new float[points * 2];
        for (int i = 0; i < points; i++) {
            float angle = (float) (2 * Math.PI * i / points - Math.PI / 2);
            vertices[i * 2] = centerX + radius * (float) Math.cos(angle);
            vertices[i * 2 + 1] = centerY + radius * (float) Math.sin(angle);
        }

        // 绘制五角形的边
        for (int i = 0; i < points; i++) {
            int nextI = (i + 1) % points;
            shapeRenderer.line(
                vertices[i * 2], vertices[i * 2 + 1],
                vertices[nextI * 2], vertices[nextI * 2 + 1]
            );
        }
    }

    /**
     * 绘制六边形（填充模式）
     */
    private static void renderHexagonFilled(ShapeRenderer shapeRenderer, float centerX, float centerY, float radius) {
        int points = 6;
        float[] vertices = new float[points * 2];
        for (int i = 0; i < points; i++) {
            float angle = (float) (2 * Math.PI * i / points);
            vertices[i * 2] = centerX + radius * (float) Math.cos(angle);
            vertices[i * 2 + 1] = centerY + radius * (float) Math.sin(angle);
        }

        // 使用三角形从中心点向外绘制六边形
        for (int i = 0; i < points; i++) {
            int nextI = (i + 1) % points;
            shapeRenderer.triangle(
                centerX, centerY,
                vertices[i * 2], vertices[i * 2 + 1],
                vertices[nextI * 2], vertices[nextI * 2 + 1]
            );
        }
    }

    /**
     * 绘制六边形（线条模式）
     */
    private static void renderHexagonLine(ShapeRenderer shapeRenderer, float centerX, float centerY, float radius) {
        int points = 6;
        float[] vertices = new float[points * 2];
        for (int i = 0; i < points; i++) {
            float angle = (float) (2 * Math.PI * i / points);
            vertices[i * 2] = centerX + radius * (float) Math.cos(angle);
            vertices[i * 2 + 1] = centerY + radius * (float) Math.sin(angle);
        }

        // 绘制六边形的边
        for (int i = 0; i < points; i++) {
            int nextI = (i + 1) % points;
            shapeRenderer.line(
                vertices[i * 2], vertices[i * 2 + 1],
                vertices[nextI * 2], vertices[nextI * 2 + 1]
            );
        }
    }

    /**
     * 根据卡牌等级获取颜色
     */
    private static Color getColorByTier(int tier) {
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

