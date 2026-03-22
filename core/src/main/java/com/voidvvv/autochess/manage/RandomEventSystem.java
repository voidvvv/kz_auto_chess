package com.voidvvv.autochess.manage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.voidvvv.autochess.model.ActiveEffect;
import com.voidvvv.autochess.model.EventChoice;
import com.voidvvv.autochess.model.EventEffectType;
import com.voidvvv.autochess.model.RandomEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 随机事件系统
 * 管理随机事件的触发和应用
 */
public class RandomEventSystem {

    private Map<String, RandomEvent> events;
    private Random random;

    public RandomEventSystem() {
        this.events = new HashMap<>();
        this.random = new Random();
    }

    /**
     * 从 JSON 文件加载事件配置
     */
    public void loadFromJson(String path) {
        try {
            FileHandle file = Gdx.files.internal(path);
            if (!file.exists()) {
                Gdx.app.error("RandomEventSystem", "Config file not found: " + path);
                loadDefaultEvents();
                return;
            }

            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(file);

            JsonValue eventsArray = root.get("events");
            if (eventsArray != null && eventsArray.isArray()) {
                for (JsonValue eventValue : eventsArray) {
                    String id = eventValue.getString("id", "");
                    String type = eventValue.getString("type", "MISC");
                    String title = eventValue.getString("title", "");
                    String description = eventValue.getString("description", "");

                    List<EventChoice> choices = new ArrayList<>();
                    JsonValue choicesArray = eventValue.get("choices");
                    if (choicesArray != null && choicesArray.isArray()) {
                        for (JsonValue choiceValue : choicesArray) {
                            String text = choiceValue.getString("text", "");
                            JsonValue effectValue = choiceValue.get("effect");
                            if (effectValue != null) {
                                String effectTypeStr = effectValue.getString("type", "GOLD");
                                float value = effectValue.getFloat("value", 0);
                                EventEffectType effectType = EventEffectType.valueOf(effectTypeStr);
                                choices.add(new EventChoice(text, effectType, value));
                            }
                        }
                    }

                    RandomEvent event = new RandomEvent(id, type, title, description, choices);
                    events.put(id, event);
                }
            }

            Gdx.app.log("RandomEventSystem", "Loaded " + events.size() + " events");
        } catch (Exception e) {
            Gdx.app.error("RandomEventSystem", "Failed to load config, using defaults", e);
            loadDefaultEvents();
        }
    }

    /**
     * 加载默认事件
     */
    private void loadDefaultEvents() {
        // 富商的馈赠
        List<EventChoice> merchantChoices = new ArrayList<>();
        merchantChoices.add(new EventChoice("接受 10 金币", EventEffectType.GOLD, 10));
        events.put("merchant_gift", new RandomEvent(
                "merchant_gift", "ECONOMY", "富商的馈赠",
                "一位富商路过，决定赞助你的旅程。", merchantChoices));

        // 训练有素
        List<EventChoice> trainingChoices = new ArrayList<>();
        trainingChoices.add(new EventChoice("攻击力 +10%", EventEffectType.ATTACK_BOOST, 0.1f));
        trainingChoices.add(new EventChoice("生命值 +10%", EventEffectType.HEALTH_BOOST, 0.1f));
        trainingChoices.add(new EventChoice("防御力 +10%", EventEffectType.DEFENSE_BOOST, 0.1f));
        events.put("training_bonus", new RandomEvent(
                "training_bonus", "COMBAT", "训练有素",
                "你的部队经过额外训练，战斗力提升。", trainingChoices));

        Gdx.app.log("RandomEventSystem", "Loaded default events");
    }

    /**
     * 触发随机事件
     */
    public RandomEvent triggerEvent() {
        if (events.isEmpty()) {
            return null;
        }
        List<RandomEvent> eventList = new ArrayList<>(events.values());
        int index = random.nextInt(eventList.size());
        return eventList.get(index);
    }

    /**
     * 应用事件效果
     * @param choice 选择的事件选项
     * @param activeEffectsManager 活跃效果管理器
     * @param economyManager 经济管理器（可选，用于金币效果）
     * @return 应用的效果
     */
    public ActiveEffect applyEffect(EventChoice choice, ActiveEffectsManager activeEffectsManager,
                                  EconomyManager economyManager) {
        ActiveEffect effect = new ActiveEffect(
                "event_" + System.currentTimeMillis(),
                "事件奖励",
                choice.getEffectType(),
                choice.getValue()
        );

        switch (choice.getEffectType()) {
            case GOLD:
                if (economyManager != null) {
                    economyManager.getGoldManager().earn((int) choice.getValue(), "event_reward");
                }
                break;
            case ATTACK_BOOST:
            case HEALTH_BOOST:
            case DEFENSE_BOOST:
            case FREE_CARD:
            case HEAL:
                activeEffectsManager.addEffect(effect);
                break;
        }

        return effect;
    }

    /**
     * 设置随机种子（用于测试）
     */
    public void setSeed(long seed) {
        this.random = new Random(seed);
    }
}
