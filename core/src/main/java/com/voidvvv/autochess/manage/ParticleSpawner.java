package com.voidvvv.autochess.manage;

import com.badlogic.gdx.graphics.Color;
import com.voidvvv.autochess.model.Projectile;
import com.voidvvv.autochess.updater.ParticleSystemUpdater;

/**
 * 粒子生成器
 * 负责创建和管理粒子效果
 * 从渲染器中分离粒子创建逻辑
 */
public class ParticleSpawner {

    private final ParticleSystemUpdater particleSystemUpdater;

    public ParticleSpawner(ParticleSystemUpdater particleSystemUpdater) {
        this.particleSystemUpdater = particleSystemUpdater;
    }

    /**
     * 为投掷物生成粒子效果
     * @param projectile 投掷物
     * @param color 粒子颜色
     */
    public void spawnParticle(Projectile projectile, Color color) {
        if (particleSystemUpdater == null || projectile == null) {
            return;
        }

        float x = projectile.getX();
        float y = projectile.getY();
        float dirX = projectile.getDirection().x;
        float dirY = projectile.getDirection().y;
        float speed = projectile.getSpeed();

        // 在投掷物后方生成粒子
        float particleX = x - dirX * projectile.getRadius() * 0.8f;
        float particleY = y - dirY * projectile.getRadius() * 0.8f;

        // 根据投掷物类型生成不同数量的粒子
        int particleCount;
        switch (projectile.getType()) {
            case ARROW:
                particleCount = 2;
                break;
            case MAGIC_BALL:
                particleCount = 4;
                break;
            default:
                particleCount = 1;
                break;
        }

        for (int i = 0; i < particleCount; i++) {
            // 随机粒子参数
            float offsetX = (float) (Math.random() * projectile.getRadius() * 0.5f - projectile.getRadius() * 0.25f);
            float offsetY = (float) (Math.random() * projectile.getRadius() * 0.5f - projectile.getRadius() * 0.25f);
            float particleSpeed = speed * 0.1f + (float) Math.random() * speed * 0.05f;
            float lifetime = 0.2f + (float) Math.random() * 0.2f;
            float size = projectile.getRadius() * (0.3f + (float) Math.random() * 0.2f);

            // 调整粒子颜色（稍暗一些）
            Color particleColor = new Color(color);
            particleColor.r *= 0.8f;
            particleColor.g *= 0.8f;
            particleColor.b *= 0.8f;
            particleColor.a = 0.7f;

            particleSystemUpdater.spawnParticle(
                    particleX + offsetX, particleY + offsetY,
                    particleColor, size, lifetime, particleSpeed
            );
        }
    }

    /**
     * 清除所有粒子
     */
    public void clear() {
        if (particleSystemUpdater != null) {
            particleSystemUpdater.clear();
        }
    }
}
