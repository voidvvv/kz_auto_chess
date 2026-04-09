package com.voidvvv.autochess.model;

import com.badlogic.gdx.math.Vector3;
import com.voidvvv.autochess.model.battle.Damage;

public class DamageShowModel {
    public Damage damage;
    public BattleCharacter from;
    public BattleCharacter to;
    public Vector3 pos = new Vector3();
    public float time;
    public boolean logged = false;
}
