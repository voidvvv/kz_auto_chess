package io.github.some_example_name.sm.machine;

import com.badlogic.gdx.ai.msg.Telegraph;
import io.github.some_example_name.sm.state.BaseState;

public interface StateMachine<T> extends Telegraph {
    BaseState<T> getCurrent();
    void setOwn(T own);
    public void switchState (BaseState<T> next);
    void update(float delta);
}
