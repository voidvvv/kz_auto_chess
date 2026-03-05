package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.KzAutoChess;
import com.voidvvv.autochess.model.Projectile;
import com.voidvvv.autochess.manage.ProjectileManager;
import com.voidvvv.autochess.updater.ParticleSystemUpdater;

import java.util.List;

/**
 * 投掷物渲染器
 * 负责渲染战场上的所有投掷物和粒子效果
 */
public class ProjectileRenderer {

    private final KzAutoChess game;
    private final ShapeRenderer shapeRenderer;
    private final ParticleSystemUpdater particleSystemUpdater;
    private final ParticleSystem particleSystemRenderer;

    public ProjectileRenderer(KzAutoChess game, ShapeRenderer shapeRenderer) {
        this.game = game;
        this.shapeRenderer = shapeRenderer;
        this.particleSystemUpdater = new ParticleSystemUpdater();
        this.particleSystemRenderer = new ParticleSystem();
    }

    /**
     * 渲染所有投掷物
     */
    public void render(ProjectileManager projectileManager, float deltaTime) {
        if (projectileManager == null) {
            return;
        }

        // 更新粒子系统
        particleSystemUpdater.update(deltaTime);

        List<Projectile> projectiles = projectileManager.getProjectiles();
        if (projectiles.isEmpty()) {
            return;
        }

        // 设置投影矩阵
        shapeRenderer.setProjectionMatrix(game.getViewManagement().getWorldCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 渲染每个投掷物
        for (Projectile projectile : projectiles) {
            renderProjectile(projectile, deltaTime);
        }

        shapeRenderer.end();

        // 渲染粒子效果
        renderParticles();
    }

    /**
     * 渲染单个投掷物
     */
    private void renderProjectile(Projectile projectile, float deltaTime) {
        float x = projectile.getX();
        float y = projectile.getY();
        float radius = projectile.getRadius();
        Color color = projectile.getColor();

        // 设置投掷物颜色
        shapeRenderer.setColor(color);

        // 根据投掷物类型渲染不同的形状
        switch (projectile.getType()) {
            case ARROW:
                renderArrow(projectile, x, y, radius);
                break;
            case MAGIC_BALL:
                renderMagicBall(projectile, x, y, radius);
                break;
        }

        // 检查是否需要生成粒子
        if (projectile.shouldSpawnParticle()) {
            spawnParticle(projectile);
        }
    }

    /**
     * 渲染箭矢
     */
    private void renderArrow(Projectile projectile, float x, float y, float radius) {
        // 绘制圆形主体
        shapeRenderer.circle(x, y, radius);

        // 绘制箭矢头部（指向飞行方向）
        float dirX = projectile.getDirection().x;
        float dirY = projectile.getDirection().y;
        float headLength = radius * 1.5f;

        // 头部顶点
        float headX = x + dirX * radius * 2f;
        float headY = y + dirY * radius * 2f;

        // 计算垂直方向
        float perpX = -dirY;
        float perpY = dirX;
        float wingLength = radius * 0.7f;

        // 绘制箭头
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.triangle(
                headX, headY,
                x + dirX * radius - perpX * wingLength, y + dirY * radius - perpY * wingLength,
                x + dirX * radius + perpX * wingLength, y + dirY * radius + perpY * wingLength
        );
    }

    /**
     * 渲染魔法球
     */
    private void renderMagicBall(Projectile projectile, float x, float y, float radius) {
        // 绘制外部光晕
        float glowRadius = radius * 1.2f;
        Color baseColor = projectile.getColor();

        // 创建渐变色
        Color innerColor = new Color(baseColor);
        innerColor.a = 0.8f;

        Color outerColor = new Color(baseColor);
        outerColor.a = 0.3f;

        // 绘制外圈（半透明）
        shapeRenderer.setColor(outerColor);
        shapeRenderer.circle(x, y, glowRadius);

        // 绘制内圈（实心）
        shapeRenderer.setColor(innerColor);
        shapeRenderer.circle(x, y, radius);

        // 绘制核心高光
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(x + radius * 0.3f, y + radius * 0.3f, radius * 0.3f);
    }

    /**
     * 生成粒子效果
     */
    private void spawnParticle(Projectile projectile) {
        float x = projectile.getX();
        float y = projectile.getY();
        Color color = projectile.getColor();
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
     * 渲染粒子效果
     */
    private void renderParticles() {
        if (particleSystemUpdater.isEmpty()) {
            return;
        }

        shapeRenderer.setProjectionMatrix(game.getViewManagement().getWorldCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        particleSystemRenderer.render(shapeRenderer, particleSystemUpdater.getParticles());

        shapeRenderer.end();
    }

    /**
     * 清理资源
     */
    public void dispose() {
        particleSystemUpdater.clear();
    }

    /**
     * 获取粒子系统更新器（用于其他类访问）
     */
    public ParticleSystemUpdater getParticleSystemUpdater() {
        return particleSystemUpdater;
    }
}