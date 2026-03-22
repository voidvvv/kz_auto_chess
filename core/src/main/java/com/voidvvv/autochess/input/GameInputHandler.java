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
import com.voidvvv.autochess.game.AutoChessGameMode;
import com.voidvvv.autochess.game.GameMode;
import com.voidvvv.autochess.game.RoguelikeGameMode;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.GamePhase;
import com.voidvvv.autochess.ui.GameUIManager;
import com.voidvvv.autochess.utils.RenderConfig;

/**
 * 游戏输入处理器
 * 处理所有输入事件：商店点击、卡组拖拽、战场角色拖拽
 * 将输入转换为高层操作（通过 GameMode 协调）
 */
public class GameInputHandler implements InputProcessor {

    private final KzAutoChess game;
    private final GameEventSystem eventSystem;

    private AutoChessGameMode autoChessGameMode;
    private RoguelikeGameMode roguelikeGameMode;
    private GameUIManager gameUIManager;

    // 拖拽状态
    private Card draggingCard;
    private BattleCharacter draggingCharacter;
    private float dragX, dragY;

    public GameInputHandler(KzAutoChess game, GameEventSystem eventSystem) {
        this.game = game;
        this.eventSystem = eventSystem;
    }

    public void initialize(GameMode gameMode) {
        if (gameMode instanceof AutoChessGameMode) {
            this.autoChessGameMode = (AutoChessGameMode) gameMode;
            this.roguelikeGameMode = null;
        } else if (gameMode instanceof RoguelikeGameMode) {
            this.roguelikeGameMode = (RoguelikeGameMode) gameMode;
            this.autoChessGameMode = null;
        }
    }

    /**
     * 检查是否有有效的游戏模式
     */
    private boolean hasValidGameMode() {
        return autoChessGameMode != null || roguelikeGameMode != null;
    }

    /**
     * 获取当前游戏阶段
     */
    private GamePhase getPhase() {
        if (autoChessGameMode != null) return autoChessGameMode.getPhase();
        if (roguelikeGameMode != null) return roguelikeGameMode.getPhase();
        return GamePhase.PLACEMENT;
    }

    /**
     * 检查战场是否包含某个点
     */
    private boolean battlefieldContains(float x, float y) {
        if (autoChessGameMode != null) return autoChessGameMode.battlefieldContains(x, y);
        if (roguelikeGameMode != null) return roguelikeGameMode.getBattleManager().contains(x, y);
        return false;
    }

    /**
     * 获取指定位置的角色
     */
    private BattleCharacter getCharacterAt(float x, float y) {
        if (autoChessGameMode != null) return autoChessGameMode.getCharacterAt(x, y);
        if (roguelikeGameMode != null) return roguelikeGameMode.getBattleManager().getCharacterAt(x, y);
        return null;
    }

    /**
     * 移动角色
     */
    private void moveCharacter(BattleCharacter character, float x, float y) {
        if (autoChessGameMode != null) autoChessGameMode.moveCharacter(character, x, y);
        if (roguelikeGameMode != null) {
            // Roguelike模式通过BattleManager移动
            roguelikeGameMode.getBattleManager().moveCharacter(character, x, y);
        }
    }

    /**
     * 放置角色
     */
    private BattleCharacter placeCharacter(Card card, float x, float y) {
        if (autoChessGameMode != null) return autoChessGameMode.placeCharacter(card, x, y);
        if (roguelikeGameMode != null) {
            // Roguelike模式通过BattleManager放置
            return roguelikeGameMode.getBattleManager().placeCharacter(card, x, y);
        }
        return null;
    }

    /**
     * 移除角色
     */
    private void removeCharacter(BattleCharacter character) {
        if (autoChessGameMode != null) autoChessGameMode.removeCharacter(character);
        if (roguelikeGameMode != null) {
            roguelikeGameMode.getBattleManager().removeCharacter(character);
        }
    }

    /**
     * 获取卡牌管理器
     */
    private com.voidvvv.autochess.manage.CardManager getCardManager() {
        if (autoChessGameMode != null) return autoChessGameMode.getCardManager();
        if (roguelikeGameMode != null) return roguelikeGameMode.getCardManager();
        return null;
    }

    public void setGameUIManager(GameUIManager gameUIManager) {
        this.gameUIManager = gameUIManager;
    }

    // ========== 每帧调用 ==========

    public void update(float delta) {
        if (!hasValidGameMode()) return;

        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();

        Vector2 uiCoords = game.getViewManagement().screenToUI(screenX, screenY);
        Vector2 worldCoords = game.getViewManagement().screenToWorld(screenX, screenY);

        if (Gdx.input.justTouched()) {
            handleMouseClick(uiCoords.x, uiCoords.y, worldCoords.x, worldCoords.y);
        }

        if (draggingCard != null) {
            dragX = uiCoords.x;
            dragY = uiCoords.y;
            if (gameUIManager != null) {
                gameUIManager.updateDragState(draggingCard, dragX, dragY, true);
            }
        }
        if (draggingCharacter != null) {
            dragX = worldCoords.x;
            dragY = worldCoords.y;
            if (gameUIManager != null) {
                gameUIManager.updateDragState(draggingCharacter, dragX, dragY, true);
            }
        }
    }

    // ========== 点击处理 ==========

    private void handleMouseClick(float uiX, float uiY, float worldX, float worldY) {
        if (!hasValidGameMode()) return;
        if (getPhase() == GamePhase.BATTLE) return;

        // 1. 正在拖拽角色 → 处理放置
        if (draggingCharacter != null) {
            handleCharacterDrop(uiX, uiY, worldX, worldY);
            return;
        }

        // 2. 正在拖拽卡牌 → 处理放置
        if (draggingCard != null) {
            handleCardDrop(worldX, worldY);
            return;
        }

        // 3. 检查点击战场角色
        BattleCharacter character = getCharacterAt(worldX, worldY);
        if (character != null && !character.isEnemy() && !character.isDead()) {
            draggingCharacter = character;
            eventSystem.postEvent(new DragStartedEvent(character, worldX, worldY));
            return;
        }

        // 4. 检查点击卡组卡牌
        if (gameUIManager != null) {
            com.voidvvv.autochess.manage.CardManager cardManager = getCardManager();
            if (cardManager != null) {
                Card deckCard = gameUIManager.getCardAtDeckPosition(uiX, uiY);
                if (deckCard != null && cardManager.getCardCount(deckCard) > 0) {
                    draggingCard = deckCard;
                    eventSystem.postEvent(new DragStartedEvent(deckCard, uiX, uiY));
                    return;
                }

                // 5. 检查点击商店卡牌 → 通过 GameUIManager 回调处理
                Card shopCard = gameUIManager.getCardAtShopPosition(uiX, uiY);
                if (shopCard != null) {
                    gameUIManager.onShopCardClickedFromInput(shopCard);
                }
            }
        }
    }

    private void handleCharacterDrop(float uiX, float uiY, float worldX, float worldY) {
        if (battlefieldContains(worldX, worldY)) {
            moveCharacter(draggingCharacter, worldX, worldY);
        } else if (gameUIManager != null && gameUIManager.isInDeckArea(uiX, uiY)) {
            Card card = draggingCharacter.getCard();
            com.voidvvv.autochess.manage.CardManager cardManager = getCardManager();
            if (cardManager != null) {
                cardManager.getPlayerDeck().addCard(card);
            }
            removeCharacter(draggingCharacter);
        }
        clearDragState();
    }

    private void handleCardDrop(float worldX, float worldY) {
        if (battlefieldContains(worldX, worldY)) {
            BattleCharacter placed = placeCharacter(draggingCard, worldX, worldY);
            if (placed != null) {
                com.voidvvv.autochess.manage.CardManager cardManager = getCardManager();
                if (cardManager != null) {
                    cardManager.getPlayerDeck().removeCard(draggingCard);
                }
            }
        }
        clearDragState();
    }

    private void clearDragState() {
        draggingCard = null;
        draggingCharacter = null;
        if (gameUIManager != null) {
            gameUIManager.updateDragState(null, 0, 0, false);
        }
        eventSystem.postEvent(new DragCancelledEvent());
    }

    // ========== InputProcessor 实现 ==========

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.F5) {
            RenderConfig.toggleRendering();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) { return false; }

    @Override
    public boolean keyTyped(char character) { return false; }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        if (draggingCard != null || draggingCharacter != null) {
            clearDragState();
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }

    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }

    // ========== 新架构生命周期方法 ==========

    public void handleInput(InputContext context) {
        // 输入处理已在 update() 中通过轮询实现
    }

    public void pause() {}
    public void resume() {}
    public void onExit() { clearDragState(); }
    public void dispose() {}

    // ========== Getters ==========

    public boolean isDragging() {
        return draggingCard != null || draggingCharacter != null;
    }

    public Object getDraggingObject() {
        if (draggingCard != null) return draggingCard;
        return draggingCharacter;
    }

    public float getDragX() { return dragX; }
    public float getDragY() { return dragY; }

    public void cancelDrag() {
        if (isDragging()) {
            clearDragState();
        }
    }
}
