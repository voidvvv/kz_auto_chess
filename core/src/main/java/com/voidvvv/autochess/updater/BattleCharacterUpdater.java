package com.voidvvv.autochess.updater;

import com.badlogic.gdx.math.Vector2;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.logic.MovementCalculator;

public class BattleCharacterUpdater {

    private final MovementCalculator movementCalculator = new MovementCalculator();

    public void update(BattleCharacter character, float delta) {
        Vector2 totalMoveVal = movementCalculator.calculateTotalMove(character.moveComponent);
        character.setX(character.getX() + totalMoveVal.x * delta);
        character.setY(character.getY() + totalMoveVal.y * delta);
    }
}
