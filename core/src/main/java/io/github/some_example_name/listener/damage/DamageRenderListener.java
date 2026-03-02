package io.github.some_example_name.listener.damage;

import io.github.some_example_name.model.DamageShowModel;
import io.github.some_example_name.model.ModelHolder;
import io.github.some_example_name.model.event.DamageEvent;

public class DamageRenderListener implements DamageEventListener{
    ModelHolder<DamageShowModel> damageShowModelModelHolder;

    public DamageRenderListener(ModelHolder<DamageShowModel> damageShowModelModelHolder) {
        this.damageShowModelModelHolder = damageShowModelModelHolder;
    }

    @Override
    public void onDamageEvent(DamageEvent de) {

    }

    @Override
    public void postDamageEvent(DamageEvent de) {
        DamageShowModel model = new DamageShowModel();
        model.damage = de.getDamage();
        model.pos.set( de.getTo().getX(),  de.getTo().getY(), 0f);
        model.time = 0.2F;
        model.from = de.getFrom();
        model.to = de.getTo();
        this.damageShowModelModelHolder.addModel(model);
    }

}
