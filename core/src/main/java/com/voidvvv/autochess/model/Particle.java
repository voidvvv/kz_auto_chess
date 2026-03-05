package com.voidvvv.autochess.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * 粒子数据模型
 * 只包含粒子的属性数据，不包含逻辑
 */
public class Particle {
    public float x, y;
    public float size;
    public float lifetime;
    public float maxLifetime;
    public Color color;
    public Vector2 velocity = new Vector2();
    public float rotation;
    public float rotationSpeed;

    public Particle(float x, float y, Color color, float size, float lifetime) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.size = size;
        this.lifetime = lifetime;
        this.maxLifetime = lifetime;
    }

    public boolean isDead() {
        return lifetime <= 0 || size <= 0;
    }
}
