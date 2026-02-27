package io.github.some_example_name.sm.machine;

import io.github.some_example_name.sm.state.BaseState;

public interface StateMachine<T> {
    BaseState<T> getCurrent();
    void setOwn(T own);
    public void switchState (BaseState<T> next);
    void update(float delta);
}
