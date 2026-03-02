package com.voidvvv.autochess.sm.machine;

import com.badlogic.gdx.ai.msg.Telegraph;
import com.voidvvv.autochess.sm.state.BaseState;

public interface StateMachine<T> extends Telegraph {
    BaseState<T> getCurrent();
    void setOwn(T own);
    public void switchState (BaseState<T> next);
    void update(float delta);
}
