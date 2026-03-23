package com.voidvvv.autochess.input.v2.example;

import com.badlogic.gdx.Input;
import com.voidvvv.autochess.input.v2.event.InputEvent;
import com.voidvvv.autochess.input.v2.listener.InputListener;

/**
 * 调试输入监听器示例
 * 打印所有输入事件，用于调试目的
 */
public class DebugInputListener implements InputListener {

    private final String name;
    private boolean enabled = true;

    public DebugInputListener() {
        this("DebugInputListener");
    }

    public DebugInputListener(String name) {
        this.name = name;
    }

    @Override
    public boolean handle(InputEvent event) {
        if (!enabled) {
            return false;
        }

        // 只打印键盘和鼠标事件，避免移动事件刷屏
        switch (event.getInputType()) {
            case KEYBOARD:
                System.out.println("[" + name + "] Keyboard: " +
                        (event.getInputState() == InputEvent.InputState.PRESSED ? "Pressed" : "Released") +
                        " KeyCode=" + Input.Keys.toString(event.getKeyCode()));
                break;
            case MOUSE:
                if (event.getInputState() == InputEvent.InputState.PRESSED ||
                    event.getInputState() == InputEvent.InputState.RELEASED) {
                    System.out.println("[" + name + "] Mouse: " +
                            event.getInputState() + " " + event.getMouseButton() +
                            " at (" + event.getInputPosition().getScreenX() +
                            ", " + event.getInputPosition().getScreenY() + ")");
                }
                break;
            case SCROLL:
                System.out.println("[" + name + "] Scroll: X=" +
                        event.getScrollAmountX() + " Y=" + event.getScrollAmountY());
                break;
        }
        return false; // 不消费事件，继续传递
    }

    @Override
    public int getPriority() {
        return -100; // 最低优先级，最后处理
    }

    /**
     * 启用/禁用监听器
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onAttached() {
        System.out.println("[" + name + "] DebugInputListener attached");
    }

    @Override
    public void onDetached() {
        System.out.println("[" + name + "] DebugInputListener detached");
    }
}
