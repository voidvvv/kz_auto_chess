package com.voidvvv.autochess.sm.machine;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.voidvvv.autochess.sm.state.BaseState;

import java.util.ArrayDeque;
import java.util.Deque;

public class BaseStateMachine<T> implements Telegraph, StateMachine<T> {
    private T own;
    private final int stackSize = 20;
    private final Deque<BaseState<T>> stateStack = new ArrayDeque<>();
    private BaseState<T> current;

    private BaseState<T> next;

    public void update(float delta) {
        switchToNextState();
        updateCurrentState(delta);
    }

    public void switchState (BaseState<T> next) {
        this.next = next;
    }

    private void updateCurrentState(float delta) {
        if (current != null) {
            current.update(own, delta);
        }
    }

    private void switchToNextState() {
        if (next != null) {
            if (current != null ) {
                stateStack.push(current);
                BaseState<T> last = current;
                last.exit(own);
            }
            while (stateStack.size() >= stackSize) {
                stateStack.poll();
            }

            current = next;
            current.enter(own);
            next = null;
        }
    }


    @Override
    public boolean handleMessage(Telegram msg) {
        return false;
    }

    @Override
    public BaseState<T> getCurrent() {
        return this.current;
    }

    @Override
    public void setOwn(T own) {
        this.own = own;
    }
}
