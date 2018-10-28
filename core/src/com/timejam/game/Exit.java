package com.timejam.game;

/**
 * Created by john on 10/28/18.
 */

public class Exit extends Entity {
    private TimeJam gameRef;

    Exit(TimeJam gameRef, int x, int y) {
        super(TYPE.EXIT, x, y, gameRef.getGraphics(TimeJam.SPRITE_INFO.EXIT), 0.2f, false);
        this.gameRef = gameRef;
    }

    @Override
    public void react(Entity entity, VERB verb) {
        if(entity.type == TYPE.PLAYER) {
            this.gameRef.win();
        }
    }
}
