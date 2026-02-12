package io.github.some_example_name.updater;

import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.model.BattleCharacter;

public class BattleCharacterUpdater {

    public void update(BattleCharacter character, float delta) {
        Vector2 totalMoveVal = character.moveComponent.getTotalMoveVal();
        character.setX(character.getX() + totalMoveVal.x * delta);
        character.setY(character.getY() + totalMoveVal.y * delta);
    }
}
