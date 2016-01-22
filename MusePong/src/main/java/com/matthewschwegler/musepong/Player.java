package com.matthewschwegler.musepong;

import android.graphics.Canvas;

/**
 * Created by Matt Schwegler on 1/16/2016.
 */
public class Player extends GameObject{
    private PongPaddle pongPaddle;
    private boolean playing;
    private boolean up;
    private boolean down;
    private int score;

    public Player(PongPaddle pongPaddle) {
        this.pongPaddle = pongPaddle;
        playing = false;
        up = false;
        down = false;
        score = 0;
        super.x = pongPaddle.getX();
        super.y = pongPaddle.getY();
        super.height = pongPaddle.getHeight();
        super.width = pongPaddle.getWidth();
    }

    public void setUp(boolean up){
        this.up = up;
    }
    public void setDown(boolean down){
        this.down = down;
    }

    public boolean getPlaying(){
        return playing;
    }
    public void setPlaying(boolean playing){
        this.playing = playing;
    }

    public int getPaddlePosition(){
        return pongPaddle.getPaddleYCenter();
    }

    public void update(){
        if(up){
            pongPaddle.moveUp();
            super.y = pongPaddle.getY();
        } else if(down) {
            pongPaddle.moveDown();
            super.y = pongPaddle.getY();
        }
    }

    public void ballCollision(PongBall pongBall){
        pongBall.setDeltaX(-pongBall.getDeltaX());
    }

    public void draw(Canvas canvas){
        pongPaddle.draw(canvas);
    }

    public int getScore(){
        return score;
    }
    public void setScore(int score){
        this.score = score;
    }

}
