package com.voidvvv.autochess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.KzAutoChess;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.input.v2.InputHandlerV2;
import com.voidvvv.autochess.manage.CardShopManager;
import com.voidvvv.autochess.model.CardPool;
import com.voidvvv.autochess.model.CardShop;
import com.voidvvv.autochess.model.PlayerDeck;
import com.voidvvv.autochess.model.PlayerEconomy;
import com.voidvvv.autochess.model.SharedCardPool;
import com.voidvvv.autochess.render.CardShopRendererAdapter;
import com.voidvvv.autochess.render.RenderHolder;
import com.voidvvv.autochess.utils.ViewManagement;

import java.util.List;

/**
 * CardShopTestScreen - 卡牌商店渲染器测试界面
 *
 * 用于独立测试 CardShopRenderer 的渲染效果和交互功能
 */
public class CardShopTestScreen implements Screen {

    private final KzAutoChess game;

    // 渲染组件
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private ViewManagement viewManagement;

    // 商店系统
    private CardPool cardPool;
    private SharedCardPool sharedCardPool;
    private CardShop cardShop;
    private PlayerEconomy playerEconomy;
    private PlayerDeck playerDeck;
    private GameEventSystem eventSystem;
    private CardShopManager cardShopManager;

    // 输入系统
    private InputHandlerV2 inputHandlerV2;
    private CardShopRendererAdapter shopRendererAdapter;

    public CardShopTestScreen(KzAutoChess game) {
        this.game = game;
    }

    @Override
    public void show() {
        // 初始化渲染组件
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // 初始化视图管理
        viewManagement = new ViewManagement();
        viewManagement.create();

        // 初始化输入处理器（配置 UI Camera）
        inputHandlerV2 = new InputHandlerV2(viewManagement.getUICamera());
        Gdx.input.setInputProcessor(inputHandlerV2);

        // 初始化商店系统
        initShopSystem();

        // 创建商店渲染器适配器
        shopRendererAdapter = new CardShopRendererAdapter(cardShopManager);

        // 注册输入监听器
        inputHandlerV2.registerListener(shopRendererAdapter.getInputListener());

        // 刷新商店显示初始卡牌
        cardShop.refresh();
    }

    /**
     * 初始化商店系统
     */
    private void initShopSystem() {
        // 创建卡池
        cardPool = new CardPool();

        // 创建共享卡池
        sharedCardPool = new SharedCardPool();
        sharedCardPool.initialize(cardPool);
        cardPool.setSharedCardPool(sharedCardPool);

        // 创建商店
        cardShop = new CardShop(cardPool);

        // 创建玩家经济（初始金币 50，方便测试）
        playerEconomy = new PlayerEconomy(50, 1);

        // 创建玩家卡组
        playerDeck = new PlayerDeck();

        // 创建事件系统
        eventSystem = new GameEventSystem();

        // 创建商店管理器
        cardShopManager = new CardShopManager(
            cardShop,
            playerEconomy,
            playerDeck,
            sharedCardPool,
            eventSystem
        );
    }

    @Override
    public void render(float delta) {
        // 清屏
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 处理 ESC 键返回
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            game.setScreen(new StartScreen(game));
            return;
        }

        // 更新输入处理器
        inputHandlerV2.update(delta);

        // 分发事件
        eventSystem.dispatch();
        eventSystem.clear();

        // 应用 UI 视口
        viewManagement.getUIViewport().apply();

        // 设置投影矩阵
        batch.setProjectionMatrix(viewManagement.getUICamera().combined);
        shapeRenderer.setProjectionMatrix(viewManagement.getUICamera().combined);

        // 创建 RenderHolder
        RenderHolder renderHolder = new RenderHolder(batch, shapeRenderer);

        // 渲染商店
        shopRendererAdapter.render(renderHolder);

        // 渲染提示信息
        renderInstructions();
    }

    /**
     * 渲染操作提示
     */
    private void renderInstructions() {
        batch.begin();
        try {
            var font = com.voidvvv.autochess.utils.FontUtils.getDefaultFont();
            font.setColor(1, 1, 1, 1);

            float y = Gdx.graphics.getHeight() - 20;
            float x = 20;

            font.draw(batch, "CardShop Renderer Test", x, y);
            font.draw(batch, "Click cards to buy (cost: card tier gold)", x, y - 25);
            font.draw(batch, "Click refresh button to refresh (cost: 2 gold)", x, y - 50);
            font.draw(batch, "Press ESC to return to main menu", x, y - 75);

            // 显示当前金币
            String goldText = "Gold: " + playerEconomy.getGold();
            font.draw(batch, goldText, Gdx.graphics.getWidth() - 150, y);
        } finally {
            batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewManagement.update(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        // 注销输入监听器
        if (shopRendererAdapter != null && inputHandlerV2 != null) {
            inputHandlerV2.unregisterListener(shopRendererAdapter.getInputListener());
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (inputHandlerV2 != null) inputHandlerV2.dispose();
    }

}
