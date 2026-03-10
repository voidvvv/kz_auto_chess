package com.voidvvv.autochess.sm.machine;

import com.badlogic.gdx.ai.msg.Telegraph;
import com.voidvvv.autochess.sm.state.BaseState;

public interface StateMachine<T> extends Telegraph {
    BaseState<T> getCurrent();
    void setOwn(T own);

    /**
     * 请求切换状态（受守卫约束，可能被拒绝）
     */
    void switchState(BaseState<T> next);

    /**
     * 强制切换状态，绕过当前状态的 canExit 守卫
     */
    void forceSwitch(BaseState<T> next);

    void update(float delta);

    /**
     * 设置初始状态并立即进入（不经过延迟切换）
     */
    void setInitialState(BaseState<T> state);

    void addListener(StateChangeListener<T> listener);
    void removeListener(StateChangeListener<T> listener);
}
