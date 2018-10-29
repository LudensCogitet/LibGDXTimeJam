package com.timejam.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Created by john on 10/27/18.
 */

public class Player extends Entity {
    private TimeJam gameRef;

    private Array<Integer> moveList;
    private int moveIndex = 0;

    private int initialX;
    private int initialY;

    private static float frameDuration = 0.6f;

    private int previousX;
    public int getPreviousX() { return this.previousX; }

    private int previousY;
    public int getPreviousY() { return this.previousY; }

    public Vector2 getPosition() { return new Vector2(this.x, this.y); }

    private boolean active;
    public boolean isActive() { return this.active; }

    private boolean gone;
    public boolean isGone() { return this.gone; }

    private boolean fresh = true;

    public void setActive(boolean value) { this.active = value; }

    Player(TimeJam gameRef, int x, int y, boolean active) {
        super(TYPE.PLAYER, x, y, gameRef.getGraphics(TimeJam.SPRITE_INFO.PLAYER_ACTIVE), Player.frameDuration, false);
        this.gameRef = gameRef;
        this.active = active;
        this.moveList = new Array<Integer>();
        this.initialX = x;
        this.initialY = y;
        this.previousX = x;
        this.previousY = y;
    }

    @Override
    public void update(float delta) {
        if(this.gone) { return; }
        super.update(delta);

        if(this.fresh) {
            this.fresh = false;
            return;
        }

        if(this.active) {
            int newX = this.x;
            int newY = this.y;
            boolean moved = false;
            int input = -1;

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                newY++;
                input = Input.Keys.UP;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                newY--;
                input = Input.Keys.DOWN;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                newX--;
                input = Input.Keys.LEFT;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                newX++;
                input = Input.Keys.RIGHT;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
                moved = true;
                input = Input.Keys.SHIFT_RIGHT;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !this.gone) {
                moved = true;
                input = Input.Keys.SPACE;
                this.gameRef.timeWarp();

                this.previousY = this.y;
                this.previousX = this.x;

                this.x = this.initialX;
                this.y = this.initialY;
                this.announceMovement();
                return;
            }

            if (newX != this.x || newY != this.y || moved) {
                if (this.gameRef.canMove(newX, newY)) {
                    this.moveList.add(input);
                    this.previousX = this.x;
                    this.previousY = this.y;

                    this.x = newX;
                    this.y = newY;

                    this.announceMovement();
                    this.gameRef.movePlayer();
                }
            }
        }
    }

    @Override
    public void act() {
        if(this.active || this.gone) { return; }

        int move = this.moveList.get(this.moveIndex);
        this.moveIndex++;

        int newX = this.x;
        int newY = this.y;
        boolean moved = false;

        if (move == Input.Keys.UP) {
            newY++;
        } else if (move == Input.Keys.DOWN) {
            newY--;
        } else if (move == Input.Keys.LEFT) {
            newX--;
        } else if (move == Input.Keys.RIGHT) {
            newX++;
        } else if (move == Input.Keys.SHIFT_RIGHT) {
            moved = true;
        } else if (move == Input.Keys.SPACE) {
            this.gone = true;

            newX = this.initialX;
            newY = this.initialY;
        }

        if (newX != this.x || newY != this.y || moved) {
            if (this.gameRef.canMove(newX, newY)) {
                this.previousX = this.x;
                this.previousY = this.y;

                this.x = newX;
                this.y = newY;
                this.announceMovement();
            }
        }
    }

    public void timeWarp() {
        if(this.active) {
            this.frames = this.gameRef.getGraphics(TimeJam.SPRITE_INFO.PLAYER_WALKING);
            this.sprite = new Animation<TextureRegion>(Player.frameDuration, this.frames);
            this.active = false;
        }

        if(this.moveList.size == 0 || this.moveList.peek() != Input.Keys.SPACE)
            this.moveList.add(Input.Keys.SPACE);

        this.gone = false;
        this.moveIndex = 0;
        this.time = 0;
        this.fresh = true;
        this.x = this.initialX;
        this.y = this.initialY;
        this.previousX = this.x;
        this.previousY = this.y;
    }

    private void announceMovement() {
        Entity levelEntityNext = this.gameRef.getLevelEntityAt(this.x, this.y);

        Entity levelEntityPrevious = this.gameRef.getLevelEntityAt(this.previousX, this.previousY);

        Entity actorEntityNext = this.gameRef.getActorEntityAt(this.x, this.y);

        Entity actorEntityPrevious = this.gameRef.getActorEntityAt(this.previousX, this.previousY);

        if(levelEntityNext != null) {
            levelEntityNext.react(this, Entity.VERB.ENTERING);
        }

        if(actorEntityNext != null) {
            actorEntityNext.react(this, Entity.VERB.ENTERING);
        }

        if (actorEntityPrevious != null) {
            actorEntityPrevious.react(this, Entity.VERB.EXITING);
        }

        if (levelEntityPrevious != null) {
            levelEntityPrevious.react(this, Entity.VERB.EXITING);
        }
    }
}
