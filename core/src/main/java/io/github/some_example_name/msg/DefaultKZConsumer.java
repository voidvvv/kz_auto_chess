package io.github.some_example_name.msg;

import com.badlogic.gdx.ai.msg.Telegram;

import java.util.function.Consumer;

public class DefaultKZConsumer implements KZConsumer {
    @Override
    public boolean handleMessage(Telegram telegram) {
        return false;
    }
}
