package com.voidvvv.autochess.sm.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;

public interface BaseState<T> extends State<T> {
    default void update(T entity){
        this.update(entity, Gdx.app.getGraphics().getDeltaTime());
    }

    void update(T entity, float delta);

    boolean isState(BaseState<T> other);

    String name();
}
