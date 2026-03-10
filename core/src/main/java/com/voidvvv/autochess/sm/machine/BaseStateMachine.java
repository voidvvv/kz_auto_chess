package com.voidvvv.autochess.sm.machine;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.utils.Array;
import com.voidvvv.autochess.sm.state.BaseState;

public class BaseStateMachine<T> implements Telegraph, StateMachine<T> {
    private T own;
    private BaseState<T> current;
    private BaseState<T> previous;

    private BaseState<T> pendingNext;
    private boolean pendingForce;

    private final Array<StateChangeListener<T>> listeners = new Array<>(false, 4);

    @Override
    public void update(float delta) {
        processPendingSwitch();
        if (current != null) {
            current.update(own, delta);
        }
    }

    @Override
    public void switchState(BaseState<T> next) {
        pendingNext = next;
        pendingForce = false;
    }

    @Override
    public void forceSwitch(BaseState<T> next) {
        pendingNext = next;
        pendingForce = true;
    }

    @Override
    public void setInitialState(BaseState<T> state) {
        if (current != null) {
            current.exit(own);
        }
        previous = current;
        current = state;
        if (current != null) {
            current.enter(own);
        }
    }

    private void processPendingSwitch() {
        if (pendingNext == null) {
            return;
        }

        BaseState<T> next = pendingNext;
        boolean force = pendingForce;
        pendingNext = null;
        pendingForce = false;

        if (!force && current != null && !current.canExit(own, next)) {
            notifyRejected(current, next);
            return;
        }

        BaseState<T> from = current;
        if (current != null) {
            current.exit(own);
        }
        previous = from;
        current = next;
        current.enter(own);
        notifyChanged(from, current);
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        return false;
    }

    @Override
    public BaseState<T> getCurrent() {
        return current;
    }

    public BaseState<T> getPrevious() {
        return previous;
    }

    @Override
    public void setOwn(T own) {
        this.own = own;
    }

    @Override
    public void addListener(StateChangeListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(StateChangeListener<T> listener) {
        listeners.removeValue(listener, true);
    }

    private void notifyChanged(BaseState<T> from, BaseState<T> to) {
        for (int i = 0; i < listeners.size; i++) {
            listeners.get(i).onStateChanged(own, from, to);
        }
    }

    private void notifyRejected(BaseState<T> currentState, BaseState<T> rejected) {
        for (int i = 0; i < listeners.size; i++) {
            listeners.get(i).onStateChangeRejected(own, currentState, rejected);
        }
    }
}
