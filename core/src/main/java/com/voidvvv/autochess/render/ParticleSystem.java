package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 简单的粒子系统
 * 用于管理投掷物的粒子效果（拖尾、爆炸等）
 */
public class ParticleSystem {

    /**
     * 粒子类
     */
    public static class Particle {
        public float x, y;
        public float size;
        public Color color;
        public float lifetime;
        public float maxLifetime;
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

        public void update(float deltaTime) {
            if (lifetime <= 0) {
                return;
            }

            // 更新位置
            x += velocity.x * deltaTime;
            y += velocity.y * deltaTime;

            // 更新旋转
            rotation += rotationSpeed * deltaTime;

            // 减少寿命
            lifetime -= deltaTime;

            // 更新颜色透明度（淡出效果）
            float alpha = lifetime / maxLifetime;
            color.a = Math.max(0, alpha * 0.7f); // 保持一定的透明度

            // 粒子大小随时间减小
            size = Math.max(0, size * alpha * 0.8f);
        }

        public boolean isDead() {
            return lifetime <= 0 || size <= 0;
        }
    }

    private final List<Particle> particles = new ArrayList<>();
    private final List<Particle> newParticles = new ArrayList<>(); // 缓存新粒子，避免修改遍历中的列表

    /**
     * 生成新粒子
     */
    public void spawnParticle(float x, float y, Color color, float size, float lifetime, float speed) {
        Particle particle = new Particle(x, y, color, size, lifetime);

        // 设置随机速度方向
        float angle = (float) (Math.random() * Math.PI * 2);
        float speedVariation = speed * (0.5f + (float) Math.random() * 0.5f);
        particle.velocity.x = (float) Math.cos(angle) * speedVariation;
        particle.velocity.y = (float) Math.sin(angle) * speedVariation;

        // 设置随机旋转
        particle.rotation = (float) (Math.random() * 360);
        particle.rotationSpeed = (float) (Math.random() * 180 - 90); // -90到90度/秒

        newParticles.add(particle);
    }

    /**
     * 生成爆炸粒子效果
     */
    public void spawnExplosion(float x, float y, Color color, int count, float baseSize) {
        for (int i = 0; i < count; i++) {
            // 随机粒子大小
            float size = baseSize * (0.5f + (float) Math.random() * 0.5f);

            // 随机生命周期
            float lifetime = 0.4f + (float) Math.random() * 0.3f;

            // 随机速度
            float speed = 50f + (float) Math.random() * 100f;

            spawnParticle(x, y, color, size, lifetime, speed);
        }
    }

    /**
     * 生成拖尾粒子效果
     */
    public void spawnTrail(float x, float y, Color color, int count, float baseSize, float speed) {
        for (int i = 0; i < count; i++) {
            // 随机偏移
            float offsetX = (float) (Math.random() * baseSize * 2 - baseSize);
            float offsetY = (float) (Math.random() * baseSize * 2 - baseSize);

            // 随机粒子大小（比爆炸粒子小）
            float size = baseSize * (0.2f + (float) Math.random() * 0.3f);

            // 较短的生命周期
            float lifetime = 0.2f + (float) Math.random() * 0.2f;

            spawnParticle(x + offsetX, y + offsetY, color, size, lifetime, speed * 0.5f);
        }
    }

    /**
     * 更新所有粒子
     */
    public void update(float deltaTime) {
        // 添加新粒子
        if (!newParticles.isEmpty()) {
            particles.addAll(newParticles);
            newParticles.clear();
        }

        if (particles.isEmpty()) {
            return;
        }

        // 更新所有粒子
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.update(deltaTime);

            // 移除死亡的粒子
            if (particle.isDead()) {
                iterator.remove();
            }
        }
    }

    /**
     * 渲染所有粒子
     */
    public void render(ShapeRenderer shapeRenderer) {
        if (particles.isEmpty()) {
            return;
        }

        for (Particle particle : particles) {
            if (particle.lifetime <= 0) {
                continue;
            }

            // 设置粒子颜色
            shapeRenderer.setColor(particle.color);

            // 渲染粒子（圆形）
            shapeRenderer.circle(particle.x, particle.y, particle.size);

            // 可选：渲染旋转的正方形（更明显的效果）
            // shapeRenderer.rect(
            //     particle.x - particle.size / 2,
            //     particle.y - particle.size / 2,
            //     particle.size / 2,
            //     particle.size / 2,
            //     particle.size,
            //     particle.size,
            //     1, 1, particle.rotation
            // );
        }
    }

    /**
     * 清理所有粒子
     */
    public void clear() {
        particles.clear();
        newParticles.clear();
    }

    /**
     * 释放资源
     */
    public void dispose() {
        clear();
    }

    /**
     * 获取粒子数量
     */
    public int getParticleCount() {
        return particles.size() + newParticles.size();
    }

    /**
     * 检查粒子系统是否为空
     */
    public boolean isEmpty() {
        return particles.isEmpty() && newParticles.isEmpty();
    }
}