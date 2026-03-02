package com.voidvvv.autochess.updater;

import com.voidvvv.autochess.model.DamageShowModel;
import com.voidvvv.autochess.model.ModelHolder;
import com.voidvvv.autochess.model.battle.Damage;

import java.util.List;

public class DamageRenderUpdater {
    private ModelHolder<DamageShowModel> damageShowModelModelHolder;

    public DamageRenderUpdater(ModelHolder<DamageShowModel> damageShowModelModelHolder) {
        this.damageShowModelModelHolder = damageShowModelModelHolder;
    }

    public void update (float delta) {
        List<DamageShowModel> models = damageShowModelModelHolder.getModels();
        for (var model : models) {
            if (model.time == 1f) {
                Damage damage = model.damage;
                System.out.printf("[%s] %s 攻击了 [%s] %s, 造成了 %s 伤害, 类型 : %s, 暴击： %s%n",
                    model.from.getCamp(),
                    model.from.getName(),
                    model.to.getCamp(),
                    model.to.getName(),
                    model.damage.val,
                    damage.type,
                    damage.critical);
            }
            model.time -= delta;
            if (model.time <= 0f) {
                damageShowModelModelHolder.removeModel(model);
            }
        }
    }
}
