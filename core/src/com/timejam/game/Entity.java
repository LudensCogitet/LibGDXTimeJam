package com.timejam.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Entity {
    enum TYPE {
        PLAYER(1),
        BUTTON(2),
        DOOR(3),
        WALL(4),
        ROBOT(5),
        EXIT(6);

        TYPE(int index) { this.INDEX = index; }
        public final int INDEX;
    }

    enum VERB {
        ENTERING,
        EXITING
    }

    Entity(TYPE type, int x, int y, Array<TextureRegion> frames, float animSpeed, boolean solid) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.frames = frames;
        this.solid = solid;

        this.sprite = new Animation<TextureRegion>(animSpeed, frames);
        this.sprite.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
    }

    protected Array<TextureRegion> frames;
    public Animation<TextureRegion> sprite;
    public TextureRegion getSprite() {
        return this.sprite.getKeyFrame(this.time);
    }

    protected float time;

    protected int x;
    protected int y;
    public int getX() { return this.x; }
    public int getY() { return this.y; }

    protected boolean solid;
    public boolean isSolid() { return this.solid; }

    protected TYPE type;
    public TYPE getType() { return this.type; }

    public void update(float delta) {
        this.time += delta;
    }

    public void think() {}
    public void act() {}
    public void react(Entity entity, VERB verb) {}
}