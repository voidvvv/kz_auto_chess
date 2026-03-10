package com.voidvvv.autochess.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class MoveComponent {
    // walk
    public boolean canWalk = false;
    public float speed;
    public Vector2 dir = new Vector2();

    // other (deprecated, will be replaced by movementEffects)
    @Deprecated
    public Vector2 otherVel = new Vector2();

    // 移动效果列表（纯数据容器）
    public Array<MovementEffect> movementEffects = new Array<>();
}

