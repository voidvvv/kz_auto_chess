package com.voidvvv.autochess.model;

import com.badlogic.gdx.math.Vector2;

public class MoveComponent {
    // walk
    public boolean canWalk = false;
    public float speed;
    public Vector2 dir = new Vector2();

    // other
    public Vector2 otherVel = new Vector2();
}

