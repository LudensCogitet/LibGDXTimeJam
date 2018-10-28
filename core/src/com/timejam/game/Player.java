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

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                newY++;
                this.moveList.add(Input.Keys.UP);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                newY--;
                this.moveList.add(Input.Keys.DOWN);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                newX--;
                this.moveList.add(Input.Keys.LEFT);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                newX++;
                this.moveList.add(Input.Keys.RIGHT);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
                moved = true;
                this.moveList.add(Input.Keys.SHIFT_RIGHT);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !this.gone) {
                this.moveList.add(Input.Keys.SPACE);
                this.gameRef.timeWarp();

                newX = this.initialX;
                newY = this.initialY;
            }

            if (newX != this.x || newY != this.y || moved) {
                if (this.gameRef.canMove(newX, newY)) {
                    this.previousX = this.x;
                    this.previousY = this.y;

                    this.x = newX;
                    this.y = newY;

                    this.gameRef.movePlayer(this);
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
            this.gameRef.movePlayer(this);
        }

        if (newX != this.x || newY != this.y || moved) {
            if (this.gameRef.canMove(newX, newY)) {
                this.previousX = this.x;
                this.previousY = this.y;

                this.x = newX;
                this.y = newY;
                this.gameRef.movePlayer(this);
            }
        }
    }

    public void timeWarp() {
        if(this.active) {
            this.frames = this.gameRef.getGraphics(TimeJam.SPRITE_INFO.PLAYER_WALKING);
            this.sprite = new Animation<TextureRegion>(Player.frameDuration, this.frames);
            this.active = false;
        }

        this.gone = false;
        this.moveIndex = 0;
        this.time = 0;
        this.fresh = true;
        this.x = this.initialX;
        this.y = this.initialY;
        this.previousX = this.x;
        this.previousY = this.y;
    }
}
