package com.voidvvv.autochess.input.v2.example;

import com.badlogic.gdx.Input;
import com.voidvvv.autochess.input.v2.event.InputEvent;
import com.voidvvv.autochess.input.v2.listener.adapter.InputAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 键盘快捷键监听器示例
 * 支持注册快捷键和对应的回调函数
 */
public class KeyboardShortcutListener extends InputAdapter {

    private final Map<Integer, Consumer<InputEvent>> keyPressActions;
    private final Map<Integer, Consumer<InputEvent>> keyReleaseActions;
    private boolean enabled = true;

    public KeyboardShortcutListener() {
        this.keyPressActions = new HashMap<>();
        this.keyReleaseActions = new HashMap<>();
    }

    /**
     * 注册按键按下时的动作
     * @param keycode 按键码
     * @param action 回调函数
     * @return this，支持链式调用
     */
    public KeyboardShortcutListener onPress(int keycode, Consumer<InputEvent> action) {
        keyPressActions.put(keycode, action);
        return this;
    }

    /**
     * 注册按键释放时的动作
     * @param keycode 按键码
     * @param action 回调函数
     * @return this，支持链式调用
     */
    public KeyboardShortcutListener onRelease(int keycode, Consumer<InputEvent> action) {
        keyReleaseActions.put(keycode, action);
        return this;
    }

    /**
     * 注册按键按下和释放时的动作（同一个动作）
     * @param keycode 按键码
     * @param action 回调函数
     * @return this，支持链式调用
     */
    public KeyboardShortcutListener onKey(int keycode, Consumer<InputEvent> action) {
        keyPressActions.put(keycode, action);
        keyReleaseActions.put(keycode, action);
        return this;
    }

    /**
     * 清除所有快捷键绑定
     */
    public void clearAll() {
        keyPressActions.clear();
        keyReleaseActions.clear();
    }

    /**
     * 移除指定按键的绑定
     */
    public void removeKey(int keycode) {
        keyPressActions.remove(keycode);
        keyReleaseActions.remove(keycode);
    }

    @Override
    protected boolean handleKeyPressed(InputEvent event) {
        if (!enabled) {
            return false;
        }

        Consumer<InputEvent> action = keyPressActions.get(event.getKeyCode());
        if (action != null) {
            action.accept(event);
            return true; // 消费事件
        }
        return false;
    }

    @Override
    protected boolean handleKeyReleased(InputEvent event) {
        if (!enabled) {
            return false;
        }

        Consumer<InputEvent> action = keyReleaseActions.get(event.getKeyCode());
        if (action != null) {
            action.accept(event);
            return true; // 消费事件
        }
        return false;
    }

    @Override
    public int getPriority() {
        return 100; // 高优先级，确保快捷键优先处理
    }

    @Override
    public boolean accepts(InputEvent event) {
        // 只接受键盘事件
        return event.isKeyboardEvent();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    // ========== 常用按键快捷注册方法 ==========

    /**
     * 注册 ESC 键按下时的动作
     */
    public KeyboardShortcutListener onEscape(Consumer<InputEvent> action) {
        return onPress(Input.Keys.ESCAPE, action);
    }

    /**
     * 注册 ENTER 键按下时的动作
     */
    public KeyboardShortcutListener onEnter(Consumer<InputEvent> action) {
        return onPress(Input.Keys.ENTER, action);
    }

    /**
     * 注册 SPACE 键按下时的动作
     */
    public KeyboardShortcutListener onSpace(Consumer<InputEvent> action) {
        return onPress(Input.Keys.SPACE, action);
    }

    /**
     * 注册 F1-F12 功能键按下时的动作
     */
    public KeyboardShortcutListener onFunctionKey(int functionNumber, Consumer<InputEvent> action) {
        if (functionNumber < 1 || functionNumber > 12) {
            throw new IllegalArgumentException("Function key must be between 1 and 12");
        }
        int keycode = Input.Keys.F1 + (functionNumber - 1);
        return onPress(keycode, action);
    }
}
