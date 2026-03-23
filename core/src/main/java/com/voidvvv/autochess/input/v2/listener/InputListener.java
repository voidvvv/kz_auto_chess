package com.voidvvv.autochess.input.v2.listener;

import com.voidvvv.autochess.input.v2.event.InputEvent;

/**
 * 输入监听器接口
 * 用于监听和处理输入事件
 */
public interface InputListener {

    /**
     * 处理输入事件
     * @param event 输入事件对象
     * @return true 表示事件已被处理，false 表示继续传递给下一个监听器
     */
    boolean handle(InputEvent event);

    /**
     * 获取监听器优先级
     * 数值越大，优先级越高，越早接收到事件
     * 默认优先级为 0
     * @return 优先级值
     */
    default int getPriority() {
        return 0;
    }

    /**
     * 判断是否接受指定类型的事件
     * 可以用于过滤不需要处理的事件类型
     * 默认接受所有事件
     * @param event 输入事件
     * @return true 表示接受该事件，false 表示跳过该事件
     */
    default boolean accepts(InputEvent event) {
        return true;
    }

    /**
     * 监听器被激活时调用
     * 当监听器被注册到 InputHandlerV2 时调用
     */
    default void onAttached() {
    }

    /**
     * 监听器被停用时调用
     * 当监听器从 InputHandlerV2 中注销时调用
     */
    default void onDetached() {
    }
}
