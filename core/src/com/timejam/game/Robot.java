package com.timejam.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by john on 10/28/18.
 */

public class Robot extends Entity {
    private TimeJam gameRef;
    private Player target = null;

    private float visionRadius = 5f;

    Robot(TimeJam gameRef, int x, int y) {
        super(TYPE.ROBOT, x, y, gameRef.getGraphics(TimeJam.SPRITE_INFO.ROBOT_IDLE), 0.5f, false);
        this.gameRef = gameRef;
    }

    @Override
    public void act() {
        if(this.target == null) {
            Player potentialTarget = this.gameRef.findNearestPlayer(this.x, this.y);
            if(potentialTarget.getPosition().dst(new Vector2(this.x, this.y)) < visionRadius) {
                this.target = potentialTarget;
            }
            return;
        }

        if(this.target.isGone()) {
            this.target = null;
            return;
        }

        int newX = this.x;
        int newY = this.y;

        if(this.x < this.target.getX()) {
            newX++;
        } else if(this.x > this.target.getX()) {
            newX--;
        }

        if(this.y < this.target.getY()) {
            newY++;
        } else if(this.y > this.target.getY()) {
            newY--;
        }

        if(this.gameRef.canMove(newX, newY)) {
            //this.gameRef.swapActorEntity(this.x, this.y, newX, newY);
            this.x = newX;
            this.y = newY;

            if(this.target.x == this.x && this.target.y == this.y) {
                this.gameRef.lose();
            }
        }
    }
}
