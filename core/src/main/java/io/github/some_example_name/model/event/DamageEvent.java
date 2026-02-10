package io.github.some_example_name.model.event;

import io.github.some_example_name.model.BattleCharacter;
import io.github.some_example_name.model.battle.Damage;

public class DamageEvent {
    private BattleCharacter from;
    private BattleCharacter to;
    private Object extra;
    private Damage damage;

    public BattleCharacter getFrom() {
        return from;
    }

    public void setFrom(BattleCharacter from) {
        this.from = from;
    }

    public BattleCharacter getTo() {
        return to;
    }

    public void setTo(BattleCharacter to) {
        this.to = to;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public Damage getDamage() {
        return damage;
    }

    public void setDamage(Damage damage) {
        this.damage = damage;
    }
}
