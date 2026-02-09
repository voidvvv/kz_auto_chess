package io.github.some_example_name.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.some_example_name.model.battle.Damage;

public class DamageShowModel {
    public Damage damage;
    public BattleCharacter from;
    public BattleCharacter to;
    public Vector3 pos = new Vector3();
    public float time;
}
