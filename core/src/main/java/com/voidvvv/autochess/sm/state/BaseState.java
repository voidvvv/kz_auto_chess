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

    /**
     * 转换守卫：当前状态是否允许退出并切换到 nextState。
     * 返回 false 表示拒绝此次转换（可被 forceSwitch 绕过）。
     */
    default boolean canExit(T entity, BaseState<T> nextState) {
        return true;
    }
}
