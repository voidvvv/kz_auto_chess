package com.voidvvv.autochess.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.voidvvv.autochess.KzAutoChess;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.event.drag.DragEvent;
import com.voidvvv.autochess.event.drag.DragStartedEvent;
import com.voidvvv.autochess.event.drag.DragMovedEvent;
import com.voidvvv.autochess.event.drag.DragCancelledEvent;
import com.voidvvv.autochess.event.drag.DroppedEvent;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Battlefield;
import com.voidvvv.autochess.model.PlayerDeck;
import com.voidvvv.autochess.model.GamePhase;
import com.voidvvv.autochess.utils.RenderConfig;

/**
 * 游戏输入处理器
 * 处理所有输入事件（键盘、鼠标、触摸）
 * 将输入转换为高层事件并发布到事件系统
 */
public class GameInputHandler implements InputProcessor {

    private final KzAutoChess game;
    private final GameEventSystem eventSystem;

    // 拖拽状态
    private Object draggingObject;
    private DragEvent.DragTarget dragTargetType = DragEvent.DragTarget.NONE;
    private float dragX, dragY;
    private boolean isDragging = false;

    // 外部引用（需要在set方法中注入）
    private Battlefield battlefield;
    private PlayerDeck playerDeck;

    public GameInputHandler(KzAutoChess game, GameEventSystem eventSystem) {
        this.game = game;
        this.eventSystem = eventSystem;
    }

    /**
     * 设置战场引用（由GameScreen在show时调用）
     */
    public void setBattlefield(Battlefield battlefield) {
        this.battlefield = battlefield;
    }

    /**
     * 设置卡组引用（由GameScreen在show时调用）
     */
    public void setPlayerDeck(PlayerDeck playerDeck) {
        this.playerDeck = playerDeck;
    }

    @Override
    public boolean keyDown(int keycode) {
        // 处理热键（F5切换渲染模式等）
        if (keycode == Input.Keys.F5) {
            RenderConfig.toggleRendering();
            Gdx.app.log("GameInputHandler", "Rendering toggled");
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // 转换为世界坐标用于游戏世界输入
        Vector2 worldPos = game.getViewManagement().screenToWorld(screenX, screenY);

        // 处理战场拖拽
        if (game.getGamePhase() == GamePhase.PLACEMENT && battlefield != null) {
            handleBattlefieldTouch(worldPos);
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector2 worldPos = game.getViewManagement().screenToWorld(screenX, screenY);

        if (game.getGamePhase() == GamePhase.PLACEMENT) {
            handleBattlefieldDrag(worldPos);
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Vector2 worldPos = game.getViewManagement().screenToWorld(screenX, screenY);

        if (game.getGamePhase() == GamePhase.PLACEMENT) {
            if (isDragging) {
                finishDrag(worldPos);
            }
        }

        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        // 拖拽被取消时清除拖拽状态
        if (isDragging) {
            cancelDrag();
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    /**
     * 处理战场触摸
     */
    private void handleBattlefieldTouch(Vector2 worldPos) {
        if (battlefield == null) return;

        // 检查是否点击了战场上的角色
        BattleCharacter character = battlefield.getCharacterAt(worldPos.x, worldPos.y);

        if (character != null && !character.isEnemy() && !character.isDead()) {
            draggingObject = character;
            dragTargetType = DragEvent.DragTarget.BATTLEFIELD;
            dragX = worldPos.x;
            dragY = worldPos.y;
            isDragging = true;

            eventSystem.postEvent(new DragStartedEvent(character, dragX, dragY));

            Gdx.app.log("GameInputHandler", "Started dragging character: " + character);
        }
    }

    /**
     * 处理拖拽移动
     */
    private void handleBattlefieldDrag(Vector2 worldPos) {
        if (isDragging && draggingObject != null && battlefield != null) {
            dragX = worldPos.x;
            dragY = worldPos.y;

            eventSystem.postEvent(new DragMovedEvent(dragX, dragY));

            // 检查拖拽目标类型
            if (battlefield.contains(worldPos.x, worldPos.y)) {
                dragTargetType = DragEvent.DragTarget.BATTLEFIELD;
            } else {
                // 暂时将战场外的拖拽都标记为CANCEL，后续可以添加DECK区域检测
                dragTargetType = DragEvent.DragTarget.CANCEL;
            }
        }
    }

    /**
     * 完成拖拽
     */
    private void finishDrag(Vector2 worldPos) {
        if (isDragging) {
            isDragging = false;
            Object prevDraggingObject = draggingObject;
            draggingObject = null;

            if (dragTargetType != DragEvent.DragTarget.CANCEL) {
                eventSystem.postEvent(new DroppedEvent(prevDraggingObject, dragX, dragY, dragTargetType));

                Gdx.app.log("GameInputHandler", "Dropped " + prevDraggingObject + " at " + dragTargetType);
            } else {
                eventSystem.postEvent(new DragCancelledEvent());
                Gdx.app.log("GameInputHandler", "Drag cancelled");
            }
        }
    }

    /**
     * 取消拖拽
     */
    public void cancelDrag() {
        if (isDragging) {
            isDragging = false;
            draggingObject = null;
            dragTargetType = DragEvent.DragTarget.NONE;
            eventSystem.postEvent(new DragCancelledEvent());
            Gdx.app.log("GameInputHandler", "Drag cancelled");
        }
    }

    /**
     * 获取拖拽状态（供UI渲染使用）
     */
    public boolean isDragging() {
        return isDragging;
    }

    /**
     * 获取拖拽的对象
     */
    public Object getDraggingObject() {
        return draggingObject;
    }

    /**
     * 获取拖拽位置
     */
    public float getDragX() {
        return dragX;
    }

    public float getDragY() {
        return dragY;
    }

    /**
     * 获取拖拽目标类型
     */
    public DragEvent.DragTarget getDragTargetType() {
        return dragTargetType;
    }
}
