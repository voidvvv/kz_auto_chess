package com.voidvvv.autochess.logic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * 血量配置加载器
 * 从 life_config.json 文件加载血量系统配置
 */
public final class LifeConfig {
    private final int initialHealth;
    private final int damagePerEnemy;

    private LifeConfig(int initialHealth, int damagePerEnemy) {
        this.initialHealth = initialHealth;
        this.damagePerEnemy = damagePerEnemy;
    }

    /**
     * 从配置文件加载配置
     * @return LifeConfig 实例，如果加载失败则返回默认配置
     */
    public static LifeConfig load() {
        try {
            FileHandle file = Gdx.files.internal("life_config.json");
            JsonValue root = new JsonReader().parse(file);

            int initialHealth = root.getInt("initialHealth", 50);
            int damagePerEnemy = root.getInt("damagePerEnemy", 1);

            Gdx.app.log("LifeConfigLoader", "Loaded config: initialHealth=" + initialHealth +
                    ", damagePerEnemy=" + damagePerEnemy);

            return new LifeConfig(initialHealth, damagePerEnemy);
        } catch (Exception e) {
            Gdx.app.error("LifeConfigLoader", "Failed to load config, using defaults: " + e.getMessage());
            return createDefault();
        }
    }

    /**
     * 创建默认配置
     */
    public static LifeConfig createDefault() {
        return new LifeConfig(50, 1);
    }

    // Getters
    public int getInitialHealth() {
        return initialHealth;
    }

    public int getDamagePerEnemy() {
        return damagePerEnemy;
    }

    @Override
    public String toString() {
        return String.format("LifeConfig{initialHealth=%d, damagePerEnemy=%d}",
                initialHealth, damagePerEnemy);
    }
}
