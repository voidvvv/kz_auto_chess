package com.voidvvv.autochess.updater;

import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.CharacterStats;
import com.voidvvv.autochess.model.battle.Damage;
import com.voidvvv.autochess.model.event.DamageEvent;
import com.voidvvv.autochess.model.battle.DamageEventHolder;
import com.voidvvv.autochess.listener.damage.DamageEventListener;
import com.voidvvv.autochess.model.battle.DamageEventListenerHolder;

import java.util.List;

public class BattleUpdater {
    private DamageEventHolder damageEventHolder;
    private DamageEventListenerHolder damageEventListenerHolder;


    public BattleUpdater(DamageEventHolder damageEventHolder, DamageEventListenerHolder damageEventListenerHolder) {
        this.damageEventHolder = damageEventHolder;
        this.damageEventListenerHolder = damageEventListenerHolder;
    }

    public void update (float delta) {
        List<DamageEventListener> listeners = damageEventListenerHolder.getModels();
        for (DamageEvent de : damageEventHolder.getModels()) {
            // Notify all listeners of the damage event (pre-settlement)
            for (DamageEventListener listener : listeners) {
                listener.onDamageEvent(de);
            }

            // Settle damage exactly once, regardless of listener count
            BattleCharacter from = de.getFrom();
            BattleCharacter defender = de.getTo();
            Damage damage = de.getDamage();
            float damageVal = damageSettlement(damage, from, defender, de.getExtra());
            float newHp = Math.max(0f, defender.getCurrentHp() - damageVal);
            defender.setCurrentHp(newHp);

            // Notify all listeners that damage has been settled (post-settlement)
            for (DamageEventListener listener : listeners) {
                listener.postDamageEvent(de);
            }
        }
        damageEventHolder.clear();
    }

    private float damageSettlement(Damage damage, BattleCharacter from, BattleCharacter defender, Object extra) {
        CharacterStats as = from.getStats();
        CharacterStats ds = defender.getStats();
        if (as == null || ds == null) return 10;

        float raw = as.getAttack();
        float def = ds.getDefense();
        float d;

        // 根据伤害类型进行不同的伤害计算
        if (damage.type == Damage.DamageType.Magic) {
            // 魔法伤害：忽略一部分防御力
            d = Math.max(1, raw - def / 4);
        } else if (damage.type == Damage.DamageType.Real) {
            // 真实伤害：完全忽略防御
            d = raw;
        } else {
            // 物理伤害：正常计算
            d = Math.max(1, raw - def / 2);
        }

        return d;
    }

}


