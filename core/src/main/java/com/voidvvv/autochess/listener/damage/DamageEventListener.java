package com.voidvvv.autochess.listener.damage;

import com.voidvvv.autochess.model.event.DamageEvent;

public interface DamageEventListener {
    void onDamageEvent (DamageEvent de);
    void postDamageEvent (DamageEvent de);
}
