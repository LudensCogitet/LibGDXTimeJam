package com.timejam.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by john on 10/27/18.
 */

public class Button extends Entity {
    private Entity target;
    private int pressed = 0;

    Button(TimeJam gameRef, int x, int y, Entity target) {
        super(TYPE.BUTTON, x, y, gameRef.getGraphics(TimeJam.SPRITE_INFO.BUTTON), 1, false);
        this.target = target;
    }

    @Override
    public TextureRegion getSprite() {
        return this.sprite.getKeyFrame(this.pressed > 0 ? 1 : 0);
    }

    @Override
    public void react(Entity entity, VERB verb) {
        switch(verb) {
            case ENTERING:
                this.pressed++;
                this.target.react(this, VERB.ENTERING);
                break;
            case EXITING:
                this.pressed--;
                if(this.pressed < 0) this.pressed = 0;
                if(this.pressed <= 0) {
                    this.target.react(this, VERB.EXITING);
                }
                break;
        }
    }
}
