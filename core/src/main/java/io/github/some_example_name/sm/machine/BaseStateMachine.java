package io.github.some_example_name.sm.machine;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.sm.state.BaseState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

public class BaseStateMachine<T> implements Telegraph {
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
            stateStack.push(current);
            while (stateStack.size() >= stackSize) {
                stateStack.poll();
            }
            BaseState<T> last = current;
            current = next;
            last.exit(own);
            current.enter(own);
            next = null;
        }
    }


    @Override
    public boolean handleMessage(Telegram msg) {
        return false;
    }
}
