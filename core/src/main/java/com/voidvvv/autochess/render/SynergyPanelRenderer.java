package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.SynergyDisplayModel;
import com.voidvvv.autochess.utils.FontUtils;
import com.voidvvv.autochess.utils.I18N;

import java.util.List;

/**
 * 羁绊面板渲染器 - 静态渲染方法模式
 *
 * 负责渲染羁绊视觉反馈面板，包括面板背景、羁绊项列表和工具提示。
 * 遵循项目渲染层规范，使用批处理优化减少状态切换。
 *
 * Research Insights:
 * - 遵循 LifeBarRenderer 静态方法模式
 * - 批处理 ShapeRenderer/SpriteBatch 调用减少状态切换
 * - 复用 GlyphLayout 避免 GC 压力
 * - 实现悬停延迟 (0.3s) 避免闪烁
 */
public class SynergyPanelRenderer {

    // ========== 颜色常量 - 复用避免每帧创建 ==========
    private static final Color ACTIVE_COLOR = new Color(1.0f, 0.85f, 0.2f, 1);      // 金色 - 已激活
    private static final Color ACTIVE_BG = new Color(1.0f, 0.85f, 0.2f, 0.15f);     // 金色背景
    private static final Color NEAR_COLOR = new Color(0.4f, 0.7f, 1.0f, 1);         // 浅蓝 - 即将激活
    private static final Color NEAR_BG = new Color(0.4f, 0.7f, 1.0f, 0.1f);         // 浅蓝背景
    private static final Color BG_COLOR = new Color(0.1f, 0.1f, 0.15f, 0.85f);      // 深灰背景
    private static final Color BORDER_COLOR = new Color(0.4f, 0.4f, 0.5f, 1);       // 边框
    private static final Color TOOLTIP_BG = new Color(0.15f, 0.15f, 0.2f, 0.95f);   // 工具提示背景

    // ========== 悬停延迟配置 ==========
    private static final float HOVER_DELAY = 0.3f;

    // ========== 默认布局配置 ==========
    private static final float DEFAULT_PANEL_X = 20f;
    private static final float DEFAULT_PANEL_Y = 100f;
    private static final float DEFAULT_PANEL_WIDTH = 220f;
    private static final float DEFAULT_ITEM_HEIGHT = 45f;
    private static final float DEFAULT_ITEM_GAP = 8f;

    /**
     * 渲染羁绊面板（主入口方法）
     *
     * @param holder 渲染上下文
     * @param displayModels 羁绊显示数据列表
     * @param mouseX 鼠标 X 坐标 (UI 坐标系)
     * @param mouseY 鼠标 Y 坐标 (UI 坐标系)
     * @param delta 帧增量时间
     * @param hoverTimerRef 悬停计时器引用 [0] = 当前计时值
     * @param hoveredSynergyRef 悬停羁绊引用 [0] = 当前悬停的羁绊
     */
    public static void render(RenderHolder holder,
                             List<SynergyDisplayModel> displayModels,
                             float mouseX, float mouseY,
                             float delta,
                             float[] hoverTimerRef,
                             SynergyDisplayModel[] hoveredSynergyRef) {
        render(holder, displayModels, mouseX, mouseY, delta,
               hoverTimerRef, hoveredSynergyRef,
               DEFAULT_PANEL_X, DEFAULT_PANEL_Y, DEFAULT_PANEL_WIDTH,
               DEFAULT_ITEM_HEIGHT, DEFAULT_ITEM_GAP);
    }

    /**
     * 渲染羁绊面板（完整参数版本）
     *
     * @param holder 渲染上下文
     * @param displayModels 羁绊显示数据列表
     * @param mouseX 鼠标 X 坐标 (UI 坐标系)
     * @param mouseY 鼠标 Y 坐标 (UI 坐标系)
     * @param delta 帧增量时间
     * @param hoverTimerRef 悬停计时器引用
     * @param hoveredSynergyRef 悬停羁绊引用
     * @param panelX 面板 X 位置
     * @param panelY 面板 Y 位置
     * @param panelWidth 面板宽度
     * @param itemHeight 单项高度
     * @param itemGap 项目间距
     */
    public static void render(RenderHolder holder,
                             List<SynergyDisplayModel> displayModels,
                             float mouseX, float mouseY,
                             float delta,
                             float[] hoverTimerRef,
                             SynergyDisplayModel[] hoveredSynergyRef,
                             float panelX, float panelY,
                             float panelWidth, float itemHeight, float itemGap) {

        if (displayModels == null || displayModels.isEmpty()) {
            return;  // 空状态处理
        }

        SpriteBatch batch = holder.getSpriteBatch();
        ShapeRenderer shapeRenderer = holder.getShapeRenderer();
        BitmapFont font = FontUtils.getSmallFont();

        // 1. 批量渲染面板背景
        float panelHeight = calculatePanelHeight(displayModels.size(), itemHeight, itemGap);
        renderPanelBackground(shapeRenderer, panelX, panelY, panelWidth, panelHeight);

        // 2. 批量渲染羁绊项 - 先 ShapeRenderer 后 SpriteBatch
        SynergyDisplayModel newHovered = renderSynergyItems(
            shapeRenderer, batch, font, displayModels,
            mouseX, mouseY, delta, hoverTimerRef, hoveredSynergyRef,
            panelX, panelY, panelWidth, itemHeight, itemGap
        );

        // 3. 更新悬停状态
        updateHoverState(newHovered, hoveredSynergyRef, hoverTimerRef, delta);

        // 4. 渲染工具提示 (如果悬停)
        if (hoveredSynergyRef[0] != null && hoverTimerRef[0] >= HOVER_DELAY) {
            renderTooltip(shapeRenderer, batch, font, hoveredSynergyRef[0], mouseX, mouseY);
        }
    }

    /**
     * 渲染面板背景
     */
    private static void renderPanelBackground(ShapeRenderer shapeRenderer,
                                             float x, float y, float width, float height) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(BG_COLOR);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(BORDER_COLOR);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
    }

    /**
     * 渲染羁绊项列表（批处理优化版本）
     * @return 新检测到的悬停羁绊（可能为 null）
     */
    private static SynergyDisplayModel renderSynergyItems(ShapeRenderer shapeRenderer,
                                                         SpriteBatch batch,
                                                         BitmapFont font,
                                                         List<SynergyDisplayModel> displayModels,
                                                         float mouseX, float mouseY,
                                                         float delta,
                                                         float[] hoverTimerRef,
                                                         SynergyDisplayModel[] hoveredSynergyRef,
                                                         float panelX, float panelY,
                                                         float panelWidth,
                                                         float itemHeight, float itemGap) {

        // 复用 GlyphLayout (性能优化)
        GlyphLayout glyphLayout = new GlyphLayout();
        SynergyDisplayModel newHovered = null;

        // 第一阶段: 批量渲染所有背景矩形
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < displayModels.size(); i++) {
            SynergyDisplayModel model = displayModels.get(i);
            float itemY = panelY + 10 + i * (itemHeight + itemGap);

            // 检测悬停
            boolean isHovering = mouseX >= panelX && mouseX <= panelX + panelWidth &&
                                mouseY >= itemY && mouseY <= itemY + itemHeight;
            if (isHovering) {
                newHovered = model;
            }

            // 渲染背景色
            Color bgColor;
            Color borderColor;
            if (model.isActive()) {
                bgColor = ACTIVE_BG;
                borderColor = ACTIVE_COLOR;
            } else {
                bgColor = NEAR_BG;
                borderColor = NEAR_COLOR;
            }

            // 悬停时高亮
            if (isHovering) {
                shapeRenderer.setColor(borderColor.r * 1.3f, borderColor.g * 1.3f,
                                       borderColor.b * 1.3f, 0.3f);
            } else {
                shapeRenderer.setColor(bgColor);
            }
            shapeRenderer.rect(panelX + 2, itemY, panelWidth - 4, itemHeight);

            // 边框
            shapeRenderer.setColor(borderColor);
            shapeRenderer.rect(panelX + 2, itemY, panelWidth - 4, itemHeight);
        }
        shapeRenderer.end();

        // 第二阶段: 批量渲染所有文字
        batch.begin();
        try {
            for (int i = 0; i < displayModels.size(); i++) {
                SynergyDisplayModel model = displayModels.get(i);
                float itemY = panelY + 10 + i * (itemHeight + itemGap);

                // 图标
                font.setColor(Color.WHITE);
                glyphLayout.setText(font, model.getIcon());
                font.draw(batch, glyphLayout, panelX + 10, itemY + itemHeight - 12);

                // 名称 - 使用 I18N
                String nameKey = "synergy_" + model.getSynergyType().getKey();
                String name = I18N.get(nameKey, model.getSynergyType().getDisplayName());
                font.setColor(model.isActive() ? ACTIVE_COLOR : Color.WHITE);
                glyphLayout.setText(font, name);
                font.draw(batch, glyphLayout, panelX + 45, itemY + itemHeight - 12);

                // 等级/进度
                String levelText;
                if (model.isActive()) {
                    levelText = String.format("Lv.%d", model.getActiveLevel());
                } else {
                    int threshold = model.getNextThreshold();
                    if (threshold > 0) {
                        levelText = String.format("%d/%d", model.getCurrentCount(), threshold);
                    } else {
                        levelText = String.format("%d", model.getCurrentCount());
                    }
                }
                font.setColor(Color.LIGHT_GRAY);
                glyphLayout.setText(font, levelText);
                font.draw(batch, glyphLayout, panelX + panelWidth - glyphLayout.width - 10,
                         itemY + itemHeight - 12);
            }
        } finally {
            batch.end();
        }

        return newHovered;
    }

    /**
     * 更新悬停状态
     */
    private static void updateHoverState(SynergyDisplayModel newHovered,
                                        SynergyDisplayModel[] hoveredSynergyRef,
                                        float[] hoverTimerRef,
                                        float delta) {
        if (newHovered != null && newHovered.equals(hoveredSynergyRef[0])) {
            // 继续悬停同一项
            hoverTimerRef[0] += delta;
        } else if (newHovered != null) {
            // 新的悬停项
            hoveredSynergyRef[0] = newHovered;
            hoverTimerRef[0] = 0f;
        } else {
            // 没有悬停
            hoveredSynergyRef[0] = null;
            hoverTimerRef[0] = 0f;
        }
    }

    /**
     * 渲染工具提示
     * 显示羁绊名称、当前等级和进度信息
     *
     * TODO: 后续版本将从 SynergyEffect 读取具体加成数值
     */
    private static void renderTooltip(ShapeRenderer shapeRenderer, SpriteBatch batch,
                                     BitmapFont font,
                                     SynergyDisplayModel model,
                                     float mouseX, float mouseY) {
        // 工具提示尺寸
        float tooltipWidth = 200f;
        float tooltipPadding = 10f;
        float lineHeight = 22f;

        // 计算工具提示位置（避免超出屏幕）
        float tooltipX = mouseX + 15f;
        float tooltipY = mouseY - 10f;

        // 构建文本内容
        String nameKey = "synergy_" + model.getSynergyType().getKey();
        String name = I18N.get(nameKey, model.getSynergyType().getDisplayName());

        StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(name).append("\n");

        if (model.isActive()) {
            tooltipText.append(String.format("等级: %d", model.getActiveLevel())).append("\n");
            tooltipText.append(String.format("当前拥有: %d 个单位", model.getCurrentCount())).append("\n");

            // 下一级信息
            if (model.getNextThreshold() > 0) {
                tooltipText.append(String.format("下一级需要: %d 个单位", model.getNextThreshold()));
            } else {
                tooltipText.append("已达到最高等级");
            }
        } else {
            tooltipText.append("未激活\n");
            tooltipText.append(String.format("当前拥有: %d 个单位\n", model.getCurrentCount()));
            if (model.getNextThreshold() > 0) {
                tooltipText.append(String.format("激活需要: %d 个单位", model.getNextThreshold()));
            }
        }

        // 计算文本宽度（粗略估计）
        float maxTextWidth = tooltipWidth - 2 * tooltipPadding;

        // 计算工具提示高度
        int lineCount = tooltipText.toString().split("\n").length;
        float tooltipHeight = lineCount * lineHeight + 2 * tooltipPadding;

        // 渲染工具提示背景
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(TOOLTIP_BG);
        shapeRenderer.rect(tooltipX, tooltipY - tooltipHeight, tooltipWidth, tooltipHeight);
        shapeRenderer.end();

        // 渲染边框
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(BORDER_COLOR);
        shapeRenderer.rect(tooltipX, tooltipY - tooltipHeight, tooltipWidth, tooltipHeight);
        shapeRenderer.end();

        // 渲染文本
        batch.begin();
        try {
            String[] lines = tooltipText.toString().split("\n");
            float y = tooltipY - tooltipPadding - 5f;

            // 标题行（羁绊名称）- 使用金色
            font.setColor(ACTIVE_COLOR);
            GlyphLayout layout = new GlyphLayout();
            layout.setText(font, lines[0]);
            font.draw(batch, layout, tooltipX + tooltipPadding, y);
            y -= lineHeight;

            // 内容行 - 使用白色
            font.setColor(Color.WHITE);
            for (int i = 1; i < lines.length; i++) {
                layout.setText(font, lines[i]);
                font.draw(batch, layout, tooltipX + tooltipPadding, y);
                y -= lineHeight;
            }
        } finally {
            batch.end();
        }
    }

    /**
     * 计算面板高度
     */
    private static float calculatePanelHeight(int itemCount, float itemHeight, float itemGap) {
        return 20 + itemCount * (itemHeight + itemGap);
    }
}
