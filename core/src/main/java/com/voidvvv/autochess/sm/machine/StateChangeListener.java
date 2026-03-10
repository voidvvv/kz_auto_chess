package com.voidvvv.autochess.sm.machine;

import com.voidvvv.autochess.sm.state.BaseState;

/**
 * 状态转换监听器，用于调试和扩展。
 */
public interface StateChangeListener<T> {
    /**
     * 状态成功切换后回调
     */
    void onStateChanged(T entity, BaseState<T> fromState, BaseState<T> toState);

    /**
     * 状态切换被守卫拒绝时回调
     */
    void onStateChangeRejected(T entity, BaseState<T> currentState, BaseState<T> rejectedState);
}
