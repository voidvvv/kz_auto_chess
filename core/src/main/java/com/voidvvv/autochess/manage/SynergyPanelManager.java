package com.voidvvv.autochess.manage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.voidvvv.autochess.KzAutoChess;
import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.event.GameEventListener;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.event.BattleStartEvent;
import com.voidvvv.autochess.event.BattleEndEvent;
import com.voidvvv.autochess.event.card.CardBuyEvent;
import com.voidvvv.autochess.event.card.CardSellEvent;
import com.voidvvv.autochess.event.card.CardUpgradeEvent;
import com.voidvvv.autochess.input.InputContext;
import com.voidvvv.autochess.logic.SynergyManager;
import com.voidvvv.autochess.model.SynergyDisplayModel;
import com.voidvvv.autochess.model.SynergyType;
import com.voidvvv.autochess.render.GameRenderer;
import com.voidvvv.autochess.render.RenderHolder;
import com.voidvvv.autochess.utils.ViewManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 羁绊面板管理器 - 生命周期和事件处理
 *
 * 负责羁绊视觉反馈面板的生命周期管理、数据缓存和事件处理。
 * 通过缓存机制优化性能，避免每帧重计算羁绊数据。
 *
 * Research Insights:
 * - 实现 GameEventListener 和 GameRenderer 接口
 * - 添加缓存机制避免每帧重计算
 * - 完整生命周期方法 (onEnter/pause/resume/onExit)
 * - 使用 InputContext.fromInput() 工厂方法
 * - 调用 synergyManager.markNeedsUpdate() 确保放置阶段数据同步
 */
public class SynergyPanelManager implements GameEventListener, GameRenderer {

    private final GameEventSystem eventSystem;
    private final SynergyManager synergyManager;
    private final ViewManagement viewManagement;
    private final KzAutoChess game;

    // ========== 缓存数据 ==========
    private List<SynergyDisplayModel> displayCache;
    private boolean cacheValid = false;

    // ========== 悬停状态 ==========
    private final float[] hoverTimer = {0f};
    private final SynergyDisplayModel[] hoveredSynergy = {null};

    // ========== 布局配置 (可配置化) ==========
    private float panelX = 20f;
    private float panelY = 100f;
    private float panelWidth = 220f;
    private float itemHeight = 45f;
    private float itemGap = 8f;

    // ========== 复用对象 ==========
    private final Vector2 tempVector = new Vector2();

    /**
     * 构造羁绊面板管理器
     *
     * @param eventSystem 事件系统
     * @param synergyManager 羁绊管理器
     * @param viewManagement 视图管理器（用于坐标转换）
     * @param game 游戏主类
     */
    public SynergyPanelManager(GameEventSystem eventSystem,
                              SynergyManager synergyManager,
                              ViewManagement viewManagement,
                              KzAutoChess game) {
        // 依赖验证（安全审查建议）
        this.eventSystem = Objects.requireNonNull(eventSystem, "eventSystem cannot be null");
        this.synergyManager = Objects.requireNonNull(synergyManager, "synergyManager cannot be null");
        this.viewManagement = Objects.requireNonNull(viewManagement, "viewManagement cannot be null");
        this.game = Objects.requireNonNull(game, "game cannot be null");

        this.displayCache = new ArrayList<>();
    }

    // ========== 生命周期方法 ==========

    /**
     * 进入游戏模式时调用
     * 注册事件监听器
     */
    public void onEnter() {
        eventSystem.registerListener(this);
        cacheValid = false;  // 初始化时重建缓存
        Gdx.app.log("SynergyPanelManager", "onEnter: registered listener");
    }

    /**
     * 暂停时调用
     */
    public void pause() {
        // 暂停时不需要特别处理
        Gdx.app.log("SynergyPanelManager", "pause");
    }

    /**
     * 恢复时调用
     */
    public void resume() {
        // 恢复时不需要特别处理
        Gdx.app.log("SynergyPanelManager", "resume");
    }

    /**
     * 退出游戏模式时调用
     * 注销事件监听器，清理状态
     */
    public void onExit() {
        eventSystem.unregisterListener(this);
        hoveredSynergy[0] = null;
        hoverTimer[0] = 0f;
        displayCache.clear();
        cacheValid = false;
        Gdx.app.log("SynergyPanelManager", "onExit: unregistered listener");
    }

    // ========== 更新方法 ==========

    /**
     * 每帧更新
     * 悬停检测在 render() 中处理
     */
    public void update(float delta) {
        // 悬停检测在 render() 中处理
    }

    // ========== 渲染方法 (GameRenderer 接口) ==========

    @Override
    public void render(RenderHolder holder) {
        // 重建缓存（如果需要）
        if (!cacheValid) {
            rebuildDisplayCache();
            cacheValid = true;
        }
        game.getViewManagement().getUIViewport().apply();

        // 设置 UI 投影矩阵（确保羁绊面板使用 UI 坐标系渲染）
        holder.getSpriteBatch().setProjectionMatrix(viewManagement.getUICamera().combined);
        holder.getShapeRenderer().setProjectionMatrix(viewManagement.getUICamera().combined);

        // 获取鼠标位置（使用 InputContext 工厂方法）
        InputContext context = InputContext.fromInput(viewManagement.getUICamera());

        // 转换到 UI 坐标
        tempVector.set(viewManagement.screenToUI((int)context.screenX, (int)context.screenY));

        // 渲染面板
        com.voidvvv.autochess.render.SynergyPanelRenderer.render(
            holder, displayCache,
            tempVector.x, tempVector.y,
            Gdx.graphics.getDeltaTime(),
            hoverTimer, hoveredSynergy,
            panelX, panelY, panelWidth, itemHeight, itemGap
        );
    }

    // ========== 事件处理 (GameEventListener 接口) ==========

    @Override
    public void onGameEvent(GameEvent event) {
        // 缓存失效标记（延迟到 render 时重建）
        if (event instanceof CardBuyEvent ||
            event instanceof CardSellEvent ||
            event instanceof CardUpgradeEvent ||
            event instanceof BattleStartEvent ||
            event instanceof BattleEndEvent) {
            cacheValid = false;

            // CRITICAL: 触发 SynergyManager 更新（数据完整性审查发现）
            // 确保放置阶段也能正确更新羁绊数据
            if (synergyManager != null) {
                synergyManager.markNeedsUpdate();
            }

            Gdx.app.debug("SynergyPanelManager", "Cache invalidated by event: " + event.getClass().getSimpleName());
        }
    }

    // ========== 私有方法 ==========

    /**
     * 重建显示缓存
     * 从 SynergyManager 获取最新的羁绊数据
     */
    private void rebuildDisplayCache() {
        displayCache.clear();

        try {
            // 获取羁绊数据
            Map<SynergyType, Integer> counts = synergyManager.getAllSynergyCounts();
            Map<SynergyType, Integer> levels = synergyManager.getAllActiveSynergyLevels();

            // 构建显示模型
            for (Map.Entry<SynergyType, Integer> entry : counts.entrySet()) {
                SynergyType type = entry.getKey();
                int count = entry.getValue();
                int level = levels.getOrDefault(type, 0);

                // 只显示已激活或即将激活的羁绊
                if (level > 0 || count > 0) {
                    String icon = getSynergyIcon(type);
                    int nextThreshold = type.getNextThreshold(level);

                    displayCache.add(new SynergyDisplayModel(
                        type, count, level, nextThreshold, icon
                    ));
                }
            }

            // 排序: 已激活在前，然后按等级排序
            displayCache.sort((a, b) -> {
                if (a.isActive() != b.isActive()) {
                    return a.isActive() ? -1 : 1;
                }
                return Integer.compare(b.getActiveLevel(), a.getActiveLevel());
            });

            Gdx.app.debug("SynergyPanelManager", "Rebuilt cache with " + displayCache.size() + " items");

        } catch (Exception e) {
            Gdx.app.error("SynergyPanelManager", "Failed to rebuild display cache", e);
            displayCache.clear();
        }
    }

    /**
     * 获取羁绊图标（Unicode emoji）
     *
     * @param type 羁绊类型
     * @return Unicode emoji 图标
     */
    private String getSynergyIcon(SynergyType type) {
        // Unicode emoji 图标
        return switch (type) {
            case WARRIOR -> "⚔️";
            case MAGE -> "🔮";
            case ARCHER -> "🏹";
            case ASSASSIN -> "🗡️";
            case TANK -> "🛡️";
            case DRAGON -> "🐉";
            case BEAST -> "🐺";
            case HUMAN -> "👤";
        };
    }

    // ========== Getter/Setter 方法（用于布局调整） ==========

    public void setPanelPosition(float x, float y) {
        this.panelX = x;
        this.panelY = y;
    }

    public void setPanelWidth(float width) {
        this.panelWidth = width;
    }

    public void setItemHeight(float height) {
        this.itemHeight = height;
    }

    public void setItemGap(float gap) {
        this.itemGap = gap;
    }
}
