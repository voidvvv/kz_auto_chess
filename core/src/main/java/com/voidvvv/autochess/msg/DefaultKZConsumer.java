package com.voidvvv.autochess.msg;

import com.badlogic.gdx.ai.msg.Telegram;

public class DefaultKZConsumer implements KZConsumer {
    @Override
    public boolean handleMessage(Telegram telegram) {
        return false;
    }
}
