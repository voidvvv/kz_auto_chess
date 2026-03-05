package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.Particle;

import java.util.List;

/**
 * 粒子渲染器
 * 只负责粒子的渲染逻辑
 */
public class ParticleSystem {

    /**
     * 渲染所有粒子
     */
    public void render(ShapeRenderer shapeRenderer, List<Particle> particles) {
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
}
