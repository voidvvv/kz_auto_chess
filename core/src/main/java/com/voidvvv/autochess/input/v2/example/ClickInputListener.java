package com.voidvvv.autochess.input.v2.example;

import com.voidvvv.autochess.input.v2.event.InputEvent;
import com.voidvvv.autochess.input.v2.listener.adapter.InputAdapter;

/**
 * 点击输入监听器示例
 * 只响应鼠标左键点击事件
 */
public class ClickInputListener extends InputAdapter {

    private final ClickCallback callback;
    private boolean enabled = true;

    public interface ClickCallback {
        void onClick(float screenX, float screenY, float worldX, float worldY);
    }

    public ClickInputListener(ClickCallback callback) {
        this.callback = callback;
    }

    @Override
    protected boolean handleMousePressed(InputEvent event) {
        if (!enabled) {
            return false;
        }

        // 只处理左键点击
        if (event.isLeftMouseButton() &&
            event.getInputState() == InputEvent.InputState.PRESSED) {

            float screenX = event.getInputPosition().getScreenX();
            float screenY = event.getInputPosition().getScreenY();

            float worldX = event.getInputPosition().hasWorldCoords()
                    ? event.getInputPosition().getWorldX()
                    : screenX;
            float worldY = event.getInputPosition().hasWorldCoords()
                    ? event.getInputPosition().getWorldY()
                    : screenY;

            if (callback != null) {
                callback.onClick(screenX, screenY, worldX, worldY);
            }

            return true; // 消费事件
        }
        return false;
    }

    @Override
    public int getPriority() {
        return 10; // 较高优先级
    }

    @Override
    public boolean accepts(InputEvent event) {
        // 只接受鼠标事件
        return event.isMouseEvent();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
