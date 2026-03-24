package com.voidvvv.autochess.input.v2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.voidvvv.autochess.input.v2.event.InputEvent;
import com.voidvvv.autochess.input.v2.listener.InputListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 输入处理器 V2
 * 实现了 InputProcessor 接口，将 LibGDX 原生输入事件转换为 InputEvent 并分发给监听器
 *
 * 设计特点：
 * 1. 线程安全的监听器列表
 * 2. 支持优先级排序
 * 3. 支持事件过滤
 * 4. 支持世界坐标转换（需要设置 Camera）
 */
public class InputHandlerV2 implements InputProcessor {

    /** 监听器列表（线程安全） */
    private final List<InputListener> listeners;

    /** 是否需要重新排序监听器 */
    private boolean needsSort = false;

    /** 用于世界坐标转换的摄像机（可选） */
    private Camera camera;

    /** 临时向量对象，避免每帧创建新对象 */
    private final Vector3 unprojectVector;

    /** 当前帧累积的事件（用于在 update 中分发） */
    private final List<InputEvent> eventQueue;

    /** 是否启用立即分发模式（默认为 false，在 update 中批量分发） */
    private boolean immediateDispatch = false;

    /** 是否启用事件队列（默认为 true） */
    private boolean enableEventQueue = true;

    /**
     * 默认构造函数
     */
    public InputHandlerV2() {
        this.listeners = new CopyOnWriteArrayList<>();
        this.unprojectVector = new Vector3();
        this.eventQueue = new ArrayList<>();
    }

    /**
     * 带摄像机构造函数
     * @param camera 用于世界坐标转换的摄像机
     */
    public InputHandlerV2(Camera camera) {
        this();
        this.camera = camera;
    }

    // ========== 监听器管理 ==========

    /**
     * 注册输入监听器
     * @param listener 要注册的监听器
     */
    public void registerListener(InputListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("InputListener cannot be null");
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            listener.onAttached();
            needsSort = true;
        }
    }

    /**
     * 注销输入监听器
     * @param listener 要注销的监听器
     */
    public void unregisterListener(InputListener listener) {
        if (listeners.remove(listener)) {
            listener.onDetached();
        }
    }

    /**
     * 清空所有监听器
     */
    public void clearListeners() {
        for (InputListener listener : listeners) {
            listener.onDetached();
        }
        listeners.clear();
    }

    /**
     * 获取监听器数量
     */
    public int getListenerCount() {
        return listeners.size();
    }

    // ========== 摄像机管理 ==========

    /**
     * 设置用于世界坐标转换的摄像机
     * @param camera 摄像机对象
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    /**
     * 获取当前摄像机
     */
    public Camera getCamera() {
        return camera;
    }

    // ========== 模式配置 ==========

    /**
     * 设置是否启用立即分发模式
     * 立即分发模式：事件在 InputProcessor 回调中立即分发
     * 批量分发模式（默认）：事件在 update() 方法中批量分发
     * @param immediate true 启用立即分发，false 使用批量分发
     */
    public void setImmediateDispatch(boolean immediate) {
        this.immediateDispatch = immediate;
    }

    /**
     * 设置是否启用事件队列
     * @param enable true 启用队列，false 禁用队列（立即分发）
     */
    public void setEnableEventQueue(boolean enable) {
        this.enableEventQueue = enable;
    }

    // ========== 更新方法 ==========

    /**
     * 每帧更新方法
     * 在批量分发模式下，此方法会分发队列中累积的所有事件
     * @param delta 距离上一帧的时间（秒）
     */
    public void update(float delta) {
        if (!immediateDispatch && !eventQueue.isEmpty()) {
            dispatchQueuedEvents();
        }
    }

    /**
     * 分发队列中的所有事件
     */
    private void dispatchQueuedEvents() {
        if (needsSort) {
            sortListeners();
        }

        for (InputEvent event : eventQueue) {
            dispatchEvent(event);
        }
        eventQueue.clear();
    }

    /**
     * 分发单个事件给所有监听器
     * @param event 要分发的事件
     * @return true 如果事件被处理，false 否则
     */
    private boolean dispatchEvent(InputEvent event) {
        // 按优先级顺序遍历监听器
        for (InputListener listener : listeners) {
            // 检查监听器是否接受该事件
            if (!listener.accepts(event)) {
                continue;
            }
            // 分发事件，如果返回 true 表示事件已被处理，停止传递
            if (listener.handle(event)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 对监听器按优先级排序
     */
    private void sortListeners() {
        if (listeners instanceof ArrayList) {
            ((ArrayList<InputListener>) listeners).sort(Comparator.comparingInt(InputListener::getPriority).reversed());
        } else {
            // 如果不是 ArrayList，创建新的排序列表
            List<InputListener> sorted = new ArrayList<>(listeners);
            sorted.sort(Comparator.comparingInt(InputListener::getPriority).reversed());
            listeners.clear();
            listeners.addAll(sorted);
        }
        needsSort = false;
    }

    /**
     * 添加事件到队列
     * @param event 要添加的事件
     */
    private void queueEvent(InputEvent event) {
        if (immediateDispatch || !enableEventQueue) {
            if (needsSort) {
                sortListeners();
            }
            dispatchEvent(event);
        } else {
            eventQueue.add(event);
        }
    }

    // ========== 坐标转换辅助方法 ==========

    /**
     * 将屏幕坐标转换为世界坐标
     * @param screenX 屏幕X坐标
     * @param screenY 屏幕Y坐标
     * @return 包含世界坐标的 InputEvent.InputPosition
     */
    private InputEvent.InputPosition screenToWorldPosition(float screenX, float screenY) {
        if (camera != null) {
            unprojectVector.set(screenX, screenY, 0);
            camera.unproject(unprojectVector);
            return InputEvent.InputPosition.fromScreenAndWorldCoords(
                    screenX, screenY,
                    unprojectVector.x, unprojectVector.y
            );
        }
        return InputEvent.InputPosition.fromScreenCoords(screenX, screenY);
    }

    // ========== InputProcessor 实现 ==========

    @Override
    public boolean keyDown(int keycode) {
        InputEvent event = InputEvent.keyDown(keycode);
        queueEvent(event);
        return false; // 返回 false 允许其他处理器继续处理
    }

    @Override
    public boolean keyUp(int keycode) {
        InputEvent event = InputEvent.keyUp(keycode);
        queueEvent(event);
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        InputEvent event = InputEvent.keyTyped(character);
        queueEvent(event);
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        InputEvent.InputPosition position = screenToWorldPosition(screenX, screenY);
        InputEvent event = InputEvent.builder()
                .inputType(InputEvent.InputType.TOUCH)
                .inputState(InputEvent.InputState.PRESSED)
                .inputPosition(position)
                .pointer(pointer)
                .mouseButton(InputEvent.MouseButton.fromCode(button))
                .build();
        queueEvent(event);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        InputEvent.InputPosition position = screenToWorldPosition(screenX, screenY);
        InputEvent event = InputEvent.builder()
                .inputType(InputEvent.InputType.TOUCH)
                .inputState(InputEvent.InputState.RELEASED)
                .inputPosition(position)
                .pointer(pointer)
                .mouseButton(InputEvent.MouseButton.fromCode(button))
                .build();
        queueEvent(event);
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        // 触摸取消通常不需要特殊处理，按 touchUp 处理
        return touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        InputEvent.InputPosition position = screenToWorldPosition(screenX, screenY);
        InputEvent event = InputEvent.builder()
                .inputType(InputEvent.InputType.TOUCH)
                .inputState(InputEvent.InputState.DRAGGED)
                .inputPosition(position)
                .pointer(pointer)
                .build();
        queueEvent(event);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        InputEvent.InputPosition position = screenToWorldPosition(screenX, screenY);
        InputEvent event = InputEvent.builder()
                .inputType(InputEvent.InputType.MOUSE)
                .inputState(InputEvent.InputState.MOVED)
                .inputPosition(position)
                .build();
        queueEvent(event);
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        InputEvent event = InputEvent.scrolled(amountX, amountY);
        queueEvent(event);
        return false;
    }

    // ========== 清理方法 ==========

    /**
     * 清理资源
     * 调用此方法会清空所有监听器
     */
    public void dispose() {
        clearListeners();
        eventQueue.clear();
    }
}
