package io.github.some_example_name.event;

import io.github.some_example_name.model.DamageShowModel;
import io.github.some_example_name.model.ModelHolder;

public class DamageRenderListener implements DamageEventListener{
    ModelHolder<DamageShowModel> damageShowModelModelHolder;

    public DamageRenderListener(ModelHolder<DamageShowModel> damageShowModelModelHolder) {
        this.damageShowModelModelHolder = damageShowModelModelHolder;
    }

    @Override
    public void onDamageEvent(DamageEvent de) {
        DamageShowModel model = new DamageShowModel();
        model.damage = de.getDamage();
        model.pos.set( de.getTo().getX(),  de.getTo().getY(), 0f);
        model.time = 1F;
        model.from = de.getFrom();
        model.to = de.getTo();
        this.damageShowModelModelHolder.addModel(model);
    }
}
