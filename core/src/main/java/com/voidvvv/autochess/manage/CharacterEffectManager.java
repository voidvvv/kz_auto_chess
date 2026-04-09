package com.voidvvv.autochess.manage;

import com.badlogic.gdx.graphics.Color;
import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.event.GameEventListener;
import com.voidvvv.autochess.event.combat.AttackEvent;
import com.voidvvv.autochess.event.skill.SkillCastEvent;
import com.voidvvv.autochess.listener.damage.DamageEventListener;
import com.voidvvv.autochess.model.CharacterEffectModel;
import com.voidvvv.autochess.model.event.DamageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色视觉效果管理器
 * 负责管理角色的视觉效果（抖动、突进、闪烁）
 * 效果仅影响渲染，不影响碰撞/判定
 */
public class CharacterEffectManager implements GameEventListener, DamageEventListener {

    private final com.voidvvv.autochess.event.GameEventSystem eventSystem;
    private final Map<Integer, List<CharacterEffectModel>> effectsByCharacter;
    private final List<CharacterEffectModel> allEffects;

    // 配置常量
    private static final float SHAKE_DURATION = 0.15f;  // 攻击抖动持续时间（秒）
    private static final float SHAKE_INTENSITY = 8f;     // 抖动像素强度
    private static final float DASH_DURATION = 0.3f;     // 技能突进持续时间（秒）
    private static final float DASH_DISTANCE = 20f;        // 突进像素距离
    private static final float FLASH_DURATION = 0.15f;    // 受伤闪烁持续时间（秒）

    public CharacterEffectManager(com.voidvvv.autochess.event.GameEventSystem eventSystem) {
        this.eventSystem = eventSystem;
        this.effectsByCharacter = new HashMap<>();
        this.allEffects = new ArrayList<>();
    }

    // ========== 生命周期管理 ==========

    public void onEnter() {
        eventSystem.registerListener(this);
    }

    public void onExit() {
        eventSystem.unregisterListener(this);
        clearAll();
    }

    public void pause() {
        // 视觉效果在暂停时不需要特殊处理
    }

    public void resume() {
        // 视觉效果在恢复时不需要特殊处理
    }

    public void update(float delta) {
        updateEffects(delta);
        cleanupExpiredEffects();
    }

    // ========== GameEventListener 实现 ==========

    @Override
    public void onGameEvent(GameEvent event) {
        if (event instanceof SkillCastEvent) {
            onSkillCastEvent((SkillCastEvent) event);
        } else if (event instanceof AttackEvent) {
            onAttackEvent((AttackEvent) event);
        }
    }

    // ========== DamageEventListener 实现 ==========

    @Override
    public void onDamageEvent(DamageEvent event) {
        if (event.getTo() == null) return;

        CharacterEffectModel flashEffect = new CharacterEffectModel(
                CharacterEffectModel.EffectType.FLASH_RED,
                event.getTo().getId(),
                FLASH_DURATION
        ).withFlashColor(Color.RED);

        addEffect(event.getTo().getId(), flashEffect);
    }

    @Override
    public void postDamageEvent(DamageEvent event) {
    }

    private void onSkillCastEvent(SkillCastEvent event) {
        if (event.getCaster() == null) return;

        CharacterEffectModel dashEffect = new CharacterEffectModel(
                CharacterEffectModel.EffectType.DASH_UP,
                event.getCaster().getId(),
                DASH_DURATION
        ).withDashDistance(DASH_DISTANCE);

        dashEffect.setOriginalY(event.getCaster().getY());
        addEffect(event.getCaster().getId(), dashEffect);
    }

    // ========== 效果添加 ==========

    private void onAttackEvent(AttackEvent event) {
        if (event.getAttacker() == null) return;

        CharacterEffectModel shakeEffect = new CharacterEffectModel(
                CharacterEffectModel.EffectType.SHAKE,
                event.getAttacker().getId(),
                SHAKE_DURATION
        );

        addEffect(event.getAttacker().getId(), shakeEffect);
    }

    public void addAttackShake(int characterId) {
        CharacterEffectModel shakeEffect = new CharacterEffectModel(
                CharacterEffectModel.EffectType.SHAKE,
                characterId,
                SHAKE_DURATION
        );

        addEffect(characterId, shakeEffect);
    }

    // ========== 效果管理 ==========

    private void addEffect(Integer characterId, CharacterEffectModel effect) {
        effectsByCharacter.computeIfAbsent(characterId, k -> new ArrayList<>()).add(effect);
        allEffects.add(effect);
    }

    private void updateEffects(float delta) {
        long currentTime = System.currentTimeMillis();

        for (CharacterEffectModel effect : allEffects) {
            if (!effect.isActive()) continue;

            long elapsedMs = currentTime - effect.getStartTime();
            float elapsed = elapsedMs / 1000f;
            float progress = Math.min(elapsed / (effect.getDuration() / 1000f), 1.0f);
            effect.setProgress(progress);

            if (progress >= 1.0f) {
                effect.setActive(false);
            }
        }
    }

    private void cleanupExpiredEffects() {
        List<CharacterEffectModel> expired = new ArrayList<>();

        for (CharacterEffectModel effect : allEffects) {
            if (!effect.isActive()) {
                expired.add(effect);
            }
        }

        allEffects.removeAll(expired);

        for (List<CharacterEffectModel> effectList : effectsByCharacter.values()) {
            effectList.removeAll(expired);
        }
    }

    public void clearAll() {
        allEffects.clear();
        effectsByCharacter.clear();
    }

    // ========== 渲染效果计算 ==========

    /**
     * 获取角色的渲染效果
     * @param characterId 角色ID
     * @return 视觉效果对象（偏移、颜色、透明度）
     */
    public CharacterRenderEffects getRenderEffects(Integer characterId) {
        List<CharacterEffectModel> effects = getEffectsForCharacter(characterId);
        float xOffset = 0f;
        float yOffset = 0f;
        Color tintColor = null;
        float alpha = 1.0f;

        for (CharacterEffectModel effect : effects) {
            if (!effect.isActive()) continue;

            float progress = effect.getProgress();

            switch (effect.getType()) {
                case SHAKE:
                    // 水平抖动：正弦波，随时间衰减
                    xOffset += (float) Math.sin(progress * Math.PI * 4f) * SHAKE_INTENSITY * (1 - progress);
                    break;

                case DASH_UP:
                    // 向上突进后返回：ease-out 二次曲线
                    float dashProgress = 1 - (1 - progress) * (1 - progress);
                    yOffset -= effect.getDashDistance() * (float) Math.sin(dashProgress * Math.PI);
                    break;

                case FLASH_RED:
                    // 红色闪烁：透明度随时间衰减
                    tintColor = effect.getFlashColor();
                    alpha = 0.8f * (1 - progress);
                    break;
            }
        }

        return new CharacterRenderEffects(xOffset, yOffset, tintColor, alpha);
    }

    public List<CharacterEffectModel> getEffectsForCharacter(Integer characterId) {
        return effectsByCharacter.getOrDefault(characterId, new ArrayList<>());
    }

    public List<CharacterEffectModel> getAllEffects() {
        return new ArrayList<>(allEffects);
    }

    /**
     * 渲染效果数据对象
     */
    public static class CharacterRenderEffects {
        public final float xOffset;
        public final float yOffset;
        public final Color tintColor;
        public final float alpha;

        public CharacterRenderEffects(float xOffset, float yOffset, Color tintColor, float alpha) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.tintColor = tintColor;
            this.alpha = alpha;
        }
    }
}
