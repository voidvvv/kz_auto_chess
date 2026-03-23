package com.voidvvv.autochess.input.v2.event;

import com.badlogic.gdx.Input.Buttons;

/**
 * 输入事件类
 * 封装输入事件的信息，包括输入类型、状态、位置、键码、按钮等
 */
public class InputEvent {

    /** 输入类型 */
    private final InputType inputType;

    /** 输入状态 */
    private final InputState inputState;

    /** 输入位置（鼠标/触摸） */
    private final InputPosition inputPosition;

    /** 输入键码（键盘） */
    private final int keyCode;

    /** 输入字符（键盘） */
    private final char character;

    /** 鼠标按钮 */
    private final MouseButton mouseButton;

    /** 触摸指针ID */
    private final int pointer;

    /** 滚轮滚动量 */
    private final float scrollAmountX;
    private final float scrollAmountY;

    /** 事件时间戳 */
    private final long timestamp;

    /**
     * 键盘事件构造函数
     */
    private InputEvent(Builder builder) {
        this.inputType = builder.inputType;
        this.inputState = builder.inputState;
        this.inputPosition = builder.inputPosition;
        this.keyCode = builder.keyCode;
        this.character = builder.character;
        this.mouseButton = builder.mouseButton;
        this.pointer = builder.pointer;
        this.scrollAmountX = builder.scrollAmountX;
        this.scrollAmountY = builder.scrollAmountY;
        this.timestamp = System.currentTimeMillis();
    }

    // ========== Getters ==========

    public InputType getInputType() {
        return inputType;
    }

    public InputState getInputState() {
        return inputState;
    }

    public InputPosition getInputPosition() {
        return inputPosition;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public char getCharacter() {
        return character;
    }

    public MouseButton getMouseButton() {
        return mouseButton;
    }

    public int getPointer() {
        return pointer;
    }

    public float getScrollAmountX() {
        return scrollAmountX;
    }

    public float getScrollAmountY() {
        return scrollAmountY;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // ========== 便捷判断方法 ==========

    public boolean isKeyboardEvent() {
        return inputType == InputType.KEYBOARD;
    }

    public boolean isMouseEvent() {
        return inputType == InputType.MOUSE;
    }

    public boolean isTouchEvent() {
        return inputType == InputType.TOUCH;
    }

    public boolean isScrollEvent() {
        return inputType == InputType.SCROLL;
    }

    public boolean isKeyPressed() {
        return inputType == InputType.KEYBOARD && inputState == InputState.PRESSED;
    }

    public boolean isKeyReleased() {
        return inputType == InputType.KEYBOARD && inputState == InputState.RELEASED;
    }

    public boolean isMouseButtonPressed() {
        return inputType == InputType.MOUSE && inputState == InputState.PRESSED;
    }

    public boolean isMouseButtonReleased() {
        return inputType == InputType.MOUSE && inputState == InputState.RELEASED;
    }

    public boolean isLeftMouseButton() {
        return mouseButton == MouseButton.LEFT;
    }

    public boolean isRightMouseButton() {
        return mouseButton == MouseButton.RIGHT;
    }

    public boolean isMiddleMouseButton() {
        return mouseButton == MouseButton.MIDDLE;
    }

    @Override
    public String toString() {
        return "InputEvent{" +
                "inputType=" + inputType +
                ", inputState=" + inputState +
                ", inputPosition=" + inputPosition +
                ", keyCode=" + keyCode +
                ", character=" + character +
                ", mouseButton=" + mouseButton +
                ", pointer=" + pointer +
                ", scrollAmountX=" + scrollAmountX +
                ", scrollAmountY=" + scrollAmountY +
                ", timestamp=" + timestamp +
                '}';
    }

    // ========== Builder 模式 ==========

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 创建一个新的 Builder，预填充当前事件的值
     */
    public Builder toBuilder() {
        return new Builder()
                .inputType(inputType)
                .inputState(inputState)
                .inputPosition(inputPosition)
                .keyCode(keyCode)
                .character(character)
                .mouseButton(mouseButton)
                .pointer(pointer)
                .scrollAmountX(scrollAmountX)
                .scrollAmountY(scrollAmountY);
    }

    public static class Builder {
        private InputType inputType;
        private InputState inputState;
        private InputPosition inputPosition;
        private int keyCode = -1;
        private char character = '\0';
        private MouseButton mouseButton = MouseButton.UNKNOWN;
        private int pointer = -1;
        private float scrollAmountX = 0;
        private float scrollAmountY = 0;

        public Builder inputType(InputType inputType) {
            this.inputType = inputType;
            return this;
        }

        public Builder inputState(InputState inputState) {
            this.inputState = inputState;
            return this;
        }

        public Builder inputPosition(InputPosition inputPosition) {
            this.inputPosition = inputPosition;
            return this;
        }

        public Builder keyCode(int keyCode) {
            this.keyCode = keyCode;
            return this;
        }

        public Builder character(char character) {
            this.character = character;
            return this;
        }

        public Builder mouseButton(MouseButton mouseButton) {
            this.mouseButton = mouseButton;
            return this;
        }

        public Builder pointer(int pointer) {
            this.pointer = pointer;
            return this;
        }

        public Builder scrollAmountX(float scrollAmountX) {
            this.scrollAmountX = scrollAmountX;
            return this;
        }

        public Builder scrollAmountY(float scrollAmountY) {
            this.scrollAmountY = scrollAmountY;
            return this;
        }

        public InputEvent build() {
            return new InputEvent(this);
        }
    }

    // ========== 内部枚举和类 ==========

    /**
     * 输入类型枚举
     */
    public enum InputType {
        KEYBOARD,   // 键盘输入
        MOUSE,      // 鼠标输入
        TOUCH,      // 触摸输入
        SCROLL      // 滚轮输入
    }

    /**
     * 输入状态枚举
     */
    public enum InputState {
        PRESSED,    // 按下
        RELEASED,   // 释放
        TYPED,      // 字符输入
        MOVED,      // 移动
        DRAGGED,    // 拖拽
        SCROLLED    // 滚动
    }

    /**
     * 鼠标按钮枚举
     */
    public enum MouseButton {
        LEFT(Buttons.LEFT),
        RIGHT(Buttons.RIGHT),
        MIDDLE(Buttons.MIDDLE),
        BACK(Buttons.BACK),
        FORWARD(Buttons.FORWARD),
        UNKNOWN(-1);

        private final int buttonCode;

        MouseButton(int buttonCode) {
            this.buttonCode = buttonCode;
        }

        public int getButtonCode() {
            return buttonCode;
        }

        public static MouseButton fromCode(int buttonCode) {
            for (MouseButton button : values()) {
                if (button.buttonCode == buttonCode) {
                    return button;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * 输入位置类
     */
    public static class InputPosition {
        /** 屏幕坐标X */
        private final float screenX;

        /** 屏幕坐标Y */
        private final float screenY;

        /** 世界坐标X (如果提供了摄像机) */
        private final float worldX;

        /** 世界坐标Y (如果提供了摄像机) */
        private final float worldY;

        /** 是否有世界坐标 */
        private final boolean hasWorldCoords;

        public InputPosition(float screenX, float screenY) {
            this(screenX, screenY, 0, 0, false);
        }

        public InputPosition(float screenX, float screenY, float worldX, float worldY) {
            this(screenX, screenY, worldX, worldY, true);
        }

        private InputPosition(float screenX, float screenY, float worldX, float worldY, boolean hasWorldCoords) {
            this.screenX = screenX;
            this.screenY = screenY;
            this.worldX = worldX;
            this.worldY = worldY;
            this.hasWorldCoords = hasWorldCoords;
        }

        public float getScreenX() {
            return screenX;
        }

        public float getScreenY() {
            return screenY;
        }

        public float getWorldX() {
            return worldX;
        }

        public float getWorldY() {
            return worldY;
        }

        public boolean hasWorldCoords() {
            return hasWorldCoords;
        }

        public static InputPosition fromScreenCoords(float screenX, float screenY) {
            return new InputPosition(screenX, screenY);
        }

        public static InputPosition fromScreenAndWorldCoords(float screenX, float screenY, float worldX, float worldY) {
            return new InputPosition(screenX, screenY, worldX, worldY);
        }

        @Override
        public String toString() {
            return "InputPosition{" +
                    "screenX=" + screenX +
                    ", screenY=" + screenY +
                    (hasWorldCoords ? ", worldX=" + worldX + ", worldY=" + worldY : "") +
                    '}';
        }
    }

    // ========== 静态工厂方法 ==========

    /**
     * 创建键盘按下事件
     */
    public static InputEvent keyDown(int keyCode) {
        return builder()
                .inputType(InputType.KEYBOARD)
                .inputState(InputState.PRESSED)
                .keyCode(keyCode)
                .build();
    }

    /**
     * 创建键盘释放事件
     */
    public static InputEvent keyUp(int keyCode) {
        return builder()
                .inputType(InputType.KEYBOARD)
                .inputState(InputState.RELEASED)
                .keyCode(keyCode)
                .build();
    }

    /**
     * 创建键盘字符输入事件
     */
    public static InputEvent keyTyped(char character) {
        return builder()
                .inputType(InputType.KEYBOARD)
                .inputState(InputState.TYPED)
                .character(character)
                .build();
    }

    /**
     * 创建鼠标按下事件
     */
    public static InputEvent mouseDown(float screenX, float screenY, int pointer, int button) {
        return builder()
                .inputType(InputType.MOUSE)
                .inputState(InputState.PRESSED)
                .inputPosition(InputPosition.fromScreenCoords(screenX, screenY))
                .mouseButton(MouseButton.fromCode(button))
                .pointer(pointer)
                .build();
    }

    /**
     * 创建鼠标释放事件
     */
    public static InputEvent mouseUp(float screenX, float screenY, int pointer, int button) {
        return builder()
                .inputType(InputType.MOUSE)
                .inputState(InputState.RELEASED)
                .inputPosition(InputPosition.fromScreenCoords(screenX, screenY))
                .mouseButton(MouseButton.fromCode(button))
                .pointer(pointer)
                .build();
    }

    /**
     * 创建鼠标移动事件
     */
    public static InputEvent mouseMoved(float screenX, float screenY) {
        return builder()
                .inputType(InputType.MOUSE)
                .inputState(InputState.MOVED)
                .inputPosition(InputPosition.fromScreenCoords(screenX, screenY))
                .build();
    }

    /**
     * 创建鼠标拖拽事件
     */
    public static InputEvent mouseDragged(float screenX, float screenY, int pointer) {
        return builder()
                .inputType(InputType.MOUSE)
                .inputState(InputState.DRAGGED)
                .inputPosition(InputPosition.fromScreenCoords(screenX, screenY))
                .pointer(pointer)
                .build();
    }

    /**
     * 创建触摸按下事件
     */
    public static InputEvent touchDown(float screenX, float screenY, int pointer, int button) {
        return builder()
                .inputType(InputType.TOUCH)
                .inputState(InputState.PRESSED)
                .inputPosition(InputPosition.fromScreenCoords(screenX, screenY))
                .mouseButton(MouseButton.fromCode(button))
                .pointer(pointer)
                .build();
    }

    /**
     * 创建触摸释放事件
     */
    public static InputEvent touchUp(float screenX, float screenY, int pointer, int button) {
        return builder()
                .inputType(InputType.TOUCH)
                .inputState(InputState.RELEASED)
                .inputPosition(InputPosition.fromScreenCoords(screenX, screenY))
                .mouseButton(MouseButton.fromCode(button))
                .pointer(pointer)
                .build();
    }

    /**
     * 创建触摸拖拽事件
     */
    public static InputEvent touchDragged(float screenX, float screenY, int pointer) {
        return builder()
                .inputType(InputType.TOUCH)
                .inputState(InputState.DRAGGED)
                .inputPosition(InputPosition.fromScreenCoords(screenX, screenY))
                .pointer(pointer)
                .build();
    }

    /**
     * 创建滚轮滚动事件
     */
    public static InputEvent scrolled(float amountX, float amountY) {
        return builder()
                .inputType(InputType.SCROLL)
                .inputState(InputState.SCROLLED)
                .scrollAmountX(amountX)
                .scrollAmountY(amountY)
                .build();
    }
}
