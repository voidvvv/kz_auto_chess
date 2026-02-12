package io.github.some_example_name.listener.damage;

import io.github.some_example_name.model.event.DamageEvent;

public interface DamageEventListener {
    void onDamageEvent (DamageEvent de);
    void postDamageEvent (DamageEvent de);
}
