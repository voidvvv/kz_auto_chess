package com.voidvvv.autochess.input.v2.listener.adapter;

import com.voidvvv.autochess.input.v2.event.InputEvent;
import com.voidvvv.autochess.input.v2.listener.InputListener;

/**
 * 输入监听器适配器类
 * 提供默认实现，方便用户只需要重写感兴趣的方法
 */
public abstract class InputAdapter implements InputListener {

    @Override
    public boolean handle(InputEvent event) {
        // 根据事件类型分发到不同的处理方法
        switch (event.getInputType()) {
            case KEYBOARD:
                return handleKeyboard(event);
            case MOUSE:
                return handleMouse(event);
            case TOUCH:
                return handleTouch(event);
            case SCROLL:
                return handleScroll(event);
            default:
                return false;
        }
    }

    /**
     * 处理键盘事件
     */
    protected boolean handleKeyboard(InputEvent event) {
        switch (event.getInputState()) {
            case PRESSED:
                return handleKeyPressed(event);
            case RELEASED:
                return handleKeyReleased(event);
            case TYPED:
                return handleKeyTyped(event);
            default:
                return false;
        }
    }

    /**
     * 处理鼠标事件
     */
    protected boolean handleMouse(InputEvent event) {
        switch (event.getInputState()) {
            case PRESSED:
                return handleMousePressed(event);
            case RELEASED:
                return handleMouseReleased(event);
            case MOVED:
                return handleMouseMoved(event);
            case DRAGGED:
                return handleMouseDragged(event);
            default:
                return false;
        }
    }

    /**
     * 处理触摸事件
     */
    protected boolean handleTouch(InputEvent event) {
        switch (event.getInputState()) {
            case PRESSED:
                return handleTouchPressed(event);
            case RELEASED:
                return handleTouchReleased(event);
            case DRAGGED:
                return handleTouchDragged(event);
            default:
                return false;
        }
    }

    /**
     * 处理滚轮事件
     */
    protected boolean handleScroll(InputEvent event) {
        return handleScrolled(event);
    }

    // ========== 具体的键盘事件处理方法 ==========

    protected boolean handleKeyPressed(InputEvent event) {
        return false;
    }

    protected boolean handleKeyReleased(InputEvent event) {
        return false;
    }

    protected boolean handleKeyTyped(InputEvent event) {
        return false;
    }

    // ========== 具体的鼠标事件处理方法 ==========

    protected boolean handleMousePressed(InputEvent event) {
        return false;
    }

    protected boolean handleMouseReleased(InputEvent event) {
        return false;
    }

    protected boolean handleMouseMoved(InputEvent event) {
        return false;
    }

    protected boolean handleMouseDragged(InputEvent event) {
        return false;
    }

    // ========== 具体的触摸事件处理方法 ==========

    protected boolean handleTouchPressed(InputEvent event) {
        return false;
    }

    protected boolean handleTouchReleased(InputEvent event) {
        return false;
    }

    protected boolean handleTouchDragged(InputEvent event) {
        return false;
    }

    // ========== 具体的滚轮事件处理方法 ==========

    protected boolean handleScrolled(InputEvent event) {
        return false;
    }
}
