package com.voidvvv.autochess.model;

import com.badlogic.gdx.math.Vector2;

public class MoveComponent {
    private final Vector2 tmp = new Vector2();
    // walk
    public boolean canWalk = false;
    public float speed;
    public Vector2 dir = new Vector2();

    // other
    public Vector2 otherVel = new Vector2();


    public Vector2 getTotalMoveVal() {
        if (canWalk) {
            tmp.set(dir.nor()).scl(speed);
        } else {
            tmp.set(0,0);
        }
        return tmp.add(otherVel);
    }
}
