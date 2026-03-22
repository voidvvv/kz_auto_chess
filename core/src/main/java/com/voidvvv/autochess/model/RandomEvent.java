package com.voidvvv.autochess.model;

import java.util.List;

/**
 * 随机事件
 * 在事件关战斗胜利后触发
 */
public class RandomEvent {
    private final String id;
    private final String type;
    private final String title;
    private final String description;
    private final List<EventChoice> choices;

    public RandomEvent(String id, String type, String title, String description, List<EventChoice> choices) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.choices = choices;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<EventChoice> getChoices() {
        return choices;
    }
}
