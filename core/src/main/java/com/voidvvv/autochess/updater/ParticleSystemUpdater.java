package com.voidvvv.autochess.updater;

import com.badlogic.gdx.graphics.Color;
import com.voidvvv.autochess.model.Particle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 粒子系统更新器
 * 负责粒子的生成和更新逻辑
 */
public class ParticleSystemUpdater {

    private final List<Particle> particles = new ArrayList<>();
    private final List<Particle> newParticles = new ArrayList<>();

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
            updateParticle(particle, deltaTime);

            // 移除死亡的粒子
            if (particle.isDead()) {
                iterator.remove();
            }
        }
    }

    /**
     * 更新单个粒子
     */
    private void updateParticle(Particle particle, float deltaTime) {
        if (particle.lifetime <= 0) {
            return;
        }

        // 更新位置
        particle.x += particle.velocity.x * deltaTime;
        particle.y += particle.velocity.y * deltaTime;

        // 更新旋转
        particle.rotation += particle.rotationSpeed * deltaTime;

        // 减少寿命
        particle.lifetime -= deltaTime;

        // 更新颜色透明度（淡出效果）
        float alpha = particle.lifetime / particle.maxLifetime;
        particle.color.a = Math.max(0, alpha * 0.7f);

        // 粒子大小随时间减小
        particle.size = Math.max(0, particle.size * alpha * 0.8f);
    }

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
        particle.rotationSpeed = (float) (Math.random() * 180 - 90);

        newParticles.add(particle);
    }

    /**
     * 生成爆炸粒子效果
     */
    public void spawnExplosion(float x, float y, Color color, int count, float baseSize) {
        for (int i = 0; i < count; i++) {
            float size = baseSize * (0.5f + (float) Math.random() * 0.5f);
            float lifetime = 0.4f + (float) Math.random() * 0.3f;
            float speed = 50f + (float) Math.random() * 100f;
            spawnParticle(x, y, color, size, lifetime, speed);
        }
    }

    /**
     * 生成拖尾粒子效果
     */
    public void spawnTrail(float x, float y, Color color, int count, float baseSize, float speed) {
        for (int i = 0; i < count; i++) {
            float offsetX = (float) (Math.random() * baseSize * 2 - baseSize);
            float offsetY = (float) (Math.random() * baseSize * 2 - baseSize);
            float size = baseSize * (0.2f + (float) Math.random() * 0.3f);
            float lifetime = 0.2f + (float) Math.random() * 0.2f;
            spawnParticle(x + offsetX, y + offsetY, color, size, lifetime, speed * 0.5f);
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

    /**
     * 获取所有粒子（用于渲染）
     */
    public List<Particle> getParticles() {
        return particles;
    }
}
