package com.timejam.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by john on 10/27/18.
 */

public class Wall extends Entity {
    Wall(TimeJam gameRef, int x, int y) {
        super(TYPE.WALL, x, y, gameRef.getGraphics(TimeJam.SPRITE_INFO.WALL_STATIC), 1, true);
    }

    @Override
    public TextureRegion getSprite() {
        return this.sprite.getKeyFrame(0);
    }

    public void setPosition(int x, int y) { this.x = x; this.y = y; }
}
