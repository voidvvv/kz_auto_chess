package io.github.some_example_name.updater;

import io.github.some_example_name.event.DamageEvent;
import io.github.some_example_name.model.battle.DamageEventHolder;
import io.github.some_example_name.event.DamageEventListener;
import io.github.some_example_name.model.battle.DamageEventListenerHolder;

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
            for (DamageEventListener listener: listeners) {
                listener.onDamageEvent(de);
            }
        }
        damageEventHolder.clear();
    }
}
