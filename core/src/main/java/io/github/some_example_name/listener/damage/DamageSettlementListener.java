package io.github.some_example_name.listener.damage;

import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.model.CharacterStats;
import io.github.some_example_name.model.battle.Damage;
import io.github.some_example_name.model.event.DamageEvent;

public class DamageSettlementListener implements DamageEventListener{
    @Override
    public void onDamageEvent(DamageEvent de) {
        BattleCharacter from = de.getFrom();
        BattleCharacter defender = de.getTo();
        Damage damage = de.getDamage();

        float damageVal = damageSettlement(damage, from, defender, de.getExtra());
        float newHp = Math.max(0f, defender.getCurrentHp() - damageVal);
        defender.setCurrentHp(newHp);
        // maybe send hp change event

    }

    private float damageSettlement(Damage damage, BattleCharacter from, BattleCharacter defender, Object extra) {
        CharacterStats as = from.getStats();
        CharacterStats ds = defender.getStats();
        if (as == null || ds == null) return 10;
        float raw = as.getAttack();
        float def = ds.getDefense();
        float d = Math.max(1, raw - def / 2);
        return d;
    }
}
