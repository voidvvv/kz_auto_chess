package io.github.some_example_name.battle;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;

public class BattleTelegraph implements Telegraph {
    public static final BattleTelegraph INSTANCE = new BattleTelegraph();
    @Override
    public boolean handleMessage(Telegram telegram) {
        return false;
    }
}
