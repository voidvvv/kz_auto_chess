package io.github.some_example_name.model.battle;

public class Damage {
    public static enum DamageType {
        PhySic,
        Magic,
        Real,
        ;
    }
    public float val;
    public boolean critical;
    public DamageType type;

    public Damage(float val, DamageType type) {
        this.val = val;
        this.type = type;
    }
}
