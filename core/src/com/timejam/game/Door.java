package com.timejam.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by john on 10/27/18.
 */

public class Door extends Entity {
    private TimeJam gameRef;
    private Entity button;

    Door(TimeJam gameRef, int x, int y, int buttonX, int buttonY) {
        super(TYPE.DOOR, x, y, gameRef.getGraphics(TimeJam.SPRITE_INFO.HATCH), 1, true);
        this.gameRef = gameRef;
        this.button = new Button(gameRef, buttonX, buttonY, this);
        this.gameRef.addLevelEntity(this.button);
    }

    @Override
    public TextureRegion getSprite() {
        return this.sprite.getKeyFrame(this.solid ? 0 : 1);
    }

    @Override
    public void react(Entity entity, VERB verb) {
        if(!entity.equals(this.button)) { return; }

        switch(verb) {
            case ENTERING:
                this.solid = false;
                break;
            case EXITING:
                this.solid = true;
                break;
        }
    }

    @Override
    public void act() {
        if(this.solid) {
            Player player = this.gameRef.findPlayerAt(this.x, this.y);
            if(player != null) {
                this.gameRef.lose();
            }
        }
    }
}
