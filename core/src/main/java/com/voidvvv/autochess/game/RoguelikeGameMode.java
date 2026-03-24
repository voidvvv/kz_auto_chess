package com.voidvvv.autochess.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.voidvvv.autochess.KzAutoChess;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.input.InputContext;
import com.voidvvv.autochess.input.v2.InputHandlerV2;
import com.voidvvv.autochess.input.v2.event.InputEvent;
import com.voidvvv.autochess.input.v2.listener.InputListener;
import com.voidvvv.autochess.manage.CardManager;
import com.voidvvv.autochess.model.CardPool;
import com.voidvvv.autochess.model.CardShop;
import com.voidvvv.autochess.render.GameRenderer;
import com.voidvvv.autochess.render.RenderCoordinator;
import com.voidvvv.autochess.render.RenderHolder;
import com.voidvvv.autochess.render.roguelike.StageRender;
import com.voidvvv.autochess.utils.FontUtils;
import com.voidvvv.autochess.utils.I18N;

/**
 * Roguelike 游戏模式
 *
 * 职责：
 * - 实现 GameMode 接口的所有方法
 * - 管理多个 Manager 的生命周期
 * - 协调 Manager 之间的通信
 * - 处理游戏阶段转换（备战 → 战斗 → 事件 → 下一阶段）
 */
public class RoguelikeGameMode implements GameMode {
    // ========== 依赖注入 ==========
    private final KzAutoChess game;
    private GameEventSystem gameEventSystem;

    // ========== 渲染组件 ==========
    private RenderCoordinator renderCoordinator;
    private Stage uiStage;  // UI stage（Scene2D）
    private InputHandlerV2 inputHandler;
    private Skin skin;

    private GameRenderer uiRender;

    // ========== UI 组件 ==========
    private Table rootTable;          // 根表格
    private Table headerTable;         // 标题栏
    private Table statsTable;          // 状态显示（阶段、金币、血量）
    private Table actionTable;         // 操作按钮
    private Label stageLabel;          // 阶段显示
    private Label goldLabel;           // 金币显示
    private Label hpLabel;             // 血量显示

    // UI 按钮引用
    private TextButton buyButton;
    private TextButton upgradeButton;
    private TextButton refreshButton;
    private TextButton nextStageButton;

    // UI 字体
    private BitmapFont uiFont;

    // ========== 数据模型 ==========
    private CardPool cardPool;
    private boolean initiated = false;
    private CardShop cardShop;

    // ========== 游戏状态 ==========
    private RoguelikePhase currentPhase = RoguelikePhase.PREPARATION;

    /**
     * Roguelike 游戏阶段枚举
     */
    public enum RoguelikePhase {
        PREPARATION,  // 备战阶段
        BATTLE,       // 战斗阶段
        EVENT,        // 随机事件阶段
        GAME_OVER,    // 游戏结束
        VICTORY       // 胜利
    }

    /**
     * 构造函数
     * @param game 游戏主类实例
     */
    public RoguelikeGameMode(KzAutoChess game, RenderCoordinator renderCoordinator, GameEventSystem gameEventSystem) {
        this.game = game;
        this.renderCoordinator = renderCoordinator;
        this.gameEventSystem = gameEventSystem;
    }

    // ========== GameMode 接口实现 ==========

    @Override
    public void onEnter() {
        Gdx.app.log("RoguelikeGameMode", "onEnter - 初始化 Roguelike 游戏模式");

        // 1. 初始化渲染组件
//        initRenderComponents();

        // 2. 初始化数据模型
        initDataModels();

        // 3. 初始化 UI 组件
        initUI();

        // 4. 初始化输入处理
        initInputHandler();




        initiated = true;
        Gdx.app.log("RoguelikeGameMode", "onEnter - 初始化完成");
    }

    private void initCardShop() {
        cardShop = new CardShop(cardPool);
        cardShop.refresh();
    }

    @Override
    public void update(float delta) {
        if (!initiated) return;

        // 更新输入处理器
        if (inputHandler != null) {
            inputHandler.update(delta);
        }

        // 更新 UI Stage
        if (uiStage != null) {
            uiStage.act(delta);
        }

        // TODO: 添加更多 Manager 的 update 调用
        // 例如：battleManager.update(delta), economyManager.update(delta), etc.
    }

    @Override
    public void render(RenderHolder holder) {

    }

    @Override
    public void handleInput(InputContext context) {
        // 输入处理现在由 InputHandlerV2 通过事件系统处理
        // 此方法保留用于兼容性
    }

    @Override
    public void pause() {
        Gdx.app.log("RoguelikeGameMode", "pause");
        // TODO: 通知所有 Manager 暂停
    }

    @Override
    public void resume() {
        Gdx.app.log("RoguelikeGameMode", "resume");
        // TODO: 通知所有 Manager 恢复
    }

    @Override
    public void onExit() {
        Gdx.app.log("RoguelikeGameMode", "onExit - 清理资源");

        // TODO: 清理所有 Manager
        // 例如：battleManager.onExit(), economyManager.onExit(), etc.

        // 清理输入处理器
        if (inputHandler != null) {
            inputHandler.dispose();
            inputHandler = null;
        }
        this.renderCoordinator.removeRenderer(uiRender);
        uiRender = null;
        initiated = false;
        Gdx.app.log("RoguelikeGameMode", "onExit - 清理完成");
    }

    @Override
    public void dispose() {
        Gdx.app.log("RoguelikeGameMode", "dispose");

        // 释放 UI Stage
        if (uiStage != null) {
            uiStage.dispose();
            uiStage = null;
        }

        // 释放渲染协调器
        if (renderCoordinator != null) {
            renderCoordinator = null;
        }

        // 释放数据模型
        cardPool = null;
    }

    // ========== 公共 Getter 方法 ==========

    /**
     * 获取 UI Stage（用于 InputMultiplexer）
     */
    public Stage getUIStage() {
        return uiStage;
    }

    /**
     * 获取输入处理器（用于 InputMultiplexer）
     */
    public InputHandlerV2 getInputHandler() {
        return inputHandler;
    }

    /**
     * 获取当前游戏阶段
     */
    public RoguelikePhase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * 设置游戏阶段
     */
    public void setPhase(RoguelikePhase phase) {
        Gdx.app.log("RoguelikeGameMode", "阶段切换: " + currentPhase + " -> " + phase);
        this.currentPhase = phase;
    }

    // ========== 私有初始化方法 ==========


    private void initDataModels() {
        // 初始化卡池
        initCardPool();
        initCardShop();
    }

    private void initCardPool() {
        cardPool = new CardPool();
    }

    private void initUI() {
        // 创建 UI Stage，使用 ScreenViewport
        uiStage = new Stage(game.getViewManagement().getUIViewport());

        // 加载皮肤
        try {
            skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
            Gdx.app.log("RoguelikeGameMode", "Skin loaded successfully");
        } catch (Exception e) {
            Gdx.app.error("RoguelikeGameMode", "Failed to load skin: " + e.getMessage());
            skin = new Skin();  // 创建空皮肤作为后备
        }

        // 加载字体
        uiFont = FontUtils.getDefaultFont();

        // 创建 UI 布局
        createLayout();
        // 加入到渲染编排中
        uiRender = new StageRender(this.uiStage);
        this.renderCoordinator.addRenderer(uiRender);
    }

    /**
     * 创建备战界面布局
     */
    private void createLayout() {
        rootTable = new Table();
        rootTable.setFillParent(true);

        // 1. 标题栏
        headerTable = createHeaderTable();
        rootTable.add(headerTable).growX().height(80).row();

        // 2. 状态显示（阶段、金币、血量）
        statsTable = createStatsTable();
        rootTable.add(statsTable).growX().height(60).row();

        // 3. 操作按钮区域
        actionTable = createActionTable();
        rootTable.add(actionTable).growX().height(60).row();

        // 添加到 Stage
        uiStage.addActor(rootTable);

        Gdx.app.log("RoguelikeGameMode", "UI layout created");
    }

    /**
     * 创建标题栏
     */
    private Table createHeaderTable() {
        Table table = new Table();
        table.top().left().pad(10);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = uiFont;
        labelStyle.fontColor = Color.WHITE;

        Label titleLabel = new Label(I18N.get("roguelike.title", "无尽模式"), labelStyle);
        table.add(titleLabel).left().pad(10);

        return table;
    }

    /**
     * 创建状态显示表格（阶段、金币、血量）
     */
    private Table createStatsTable() {
        Table table = new Table();
        table.top().left().pad(10);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = uiFont;
        labelStyle.fontColor = Color.WHITE;

        // 阶段显示
        stageLabel = new Label("第 1 / 30 阶段", labelStyle);
        table.add(stageLabel).left().pad(10);

        // 金币显示
        goldLabel = new Label("金币: 10", labelStyle);
        table.add(goldLabel).left().pad(10);

        // 血量显示
        hpLabel = new Label("血量: 100", labelStyle);
        table.add(hpLabel).left().pad(10);

        return table;
    }

    /**
     * 创建操作按钮表格
     */
    private Table createActionTable() {
        Table table = new Table();
        table.bottom().pad(20);

        // 创建按钮样式
        TextButton.TextButtonStyle buttonStyle = createButtonStyle();

        // 购买按钮
        buyButton = new TextButton(I18N.get("roguelike.buy", "购买"), buttonStyle);
        buyButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                onBuyButtonClicked();
            }
        });
        table.add(buyButton).width(120).height(50).pad(10);

        // 升级按钮
        upgradeButton = new TextButton(I18N.get("roguelike.upgrade", "升级"), buttonStyle);
        upgradeButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                onUpgradeButtonClicked();
            }
        });
        table.add(upgradeButton).width(120).height(50).pad(10);

        // 刷新按钮
        refreshButton = new TextButton(I18N.get("roguelike.refresh", "刷新 (2金币)"), buttonStyle);
        refreshButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                onRefreshButtonClicked();
            }
        });
        table.add(refreshButton).width(150).height(50).pad(10);

        // 下一阶段按钮
        nextStageButton = new TextButton(I18N.get("roguelike.next_stage", "下一阶段"), buttonStyle);
        nextStageButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                onNextStageButtonClicked();
            }
        });
        table.add(nextStageButton).width(150).height(50).pad(10);

        return table;
    }

    /**
     * 创建按钮样式
     */
    private TextButton.TextButtonStyle createButtonStyle() {
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = uiFont;
        buttonStyle.fontColor = Color.WHITE;

        // 尝试从皮肤加载按钮样式
        if (skin != null) {
            try {
                if (skin.has("default", TextButton.TextButtonStyle.class)) {
                    TextButton.TextButtonStyle skinStyle = skin.get("default", TextButton.TextButtonStyle.class);
                    if (skinStyle.up != null) buttonStyle.up = skinStyle.up;
                    if (skinStyle.down != null) buttonStyle.down = skinStyle.down;
                    if (skinStyle.over != null) buttonStyle.over = skinStyle.over;
                }
            } catch (Exception e) {
                Gdx.app.debug("RoguelikeGameMode", "Could not load button style from skin: " + e.getMessage());
            }
        }

        return buttonStyle;
    }

    // ========== 按钮点击回调 ==========

    private void onBuyButtonClicked() {
        Gdx.app.log("RoguelikeGameMode", "购买按钮点击");
        // TODO: 实现购买逻辑
    }

    private void onUpgradeButtonClicked() {
        Gdx.app.log("RoguelikeGameMode", "升级按钮点击");
        // TODO: 实现升级逻辑
    }

    private void onRefreshButtonClicked() {
        Gdx.app.log("RoguelikeGameMode", "刷新按钮点击");
        // TODO: 实现刷新逻辑
    }

    private void onNextStageButtonClicked() {
        Gdx.app.log("RoguelikeGameMode", "下一阶段按钮点击");
        // TODO: 实现阶段切换逻辑
        setPhase(RoguelikePhase.BATTLE);
    }

    private void initInputHandler() {
        // 创建输入处理器
        inputHandler = new InputHandlerV2();

        // TODO: 注册 InputListener 实现具体输入逻辑
        // 将在 Phase 4 中实现
        inputHandler.registerListener(new InputListener() {
            @Override
            public boolean handle(InputEvent event) {
                if (event.getInputType().equals(InputEvent.InputType.KEYBOARD) && event.getKeyCode() == Input.Keys.R
                    && event.getInputState() == InputEvent.InputState.RELEASED) {
                    // 刷新商店
                    Gdx.app.log("Shop","商店刷新");
                }
                return false;
            }
        });
    }
}
