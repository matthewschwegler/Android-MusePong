package com.matthewschwegler.musepong;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Random;
import java.util.Vector;

/**
 * Created by Matt Schwegler on 1/16/2016.
 */
public class PongBall extends  GameObject {
    private Paint paint;
    private int ballAngle;
    private int deltaSpeed;
    private Random rand;

    public PongBall(int x, int y, int deltaSpeed, int ballAngle, Paint paint)
    {
        super.x = x;
        super.y = y;
        super.width = GamePanel.BALL_SIZE;
        super.height = GamePanel.BALL_SIZE;
        this.deltaSpeed = deltaSpeed;
        this.ballAngle = ballAngle;
        this.paint = paint;
        rand = new Random(System.nanoTime());
        resetMovement();
    }

    public void update()
    {
        x += deltaX;
        y += deltaY;
    }

    public void draw(Canvas canvas)
    {
        canvas.drawCircle(x, y, GamePanel.BALL_SIZE, paint);
    }

    public void centerBall(){
        x = GamePanel.WIDTH/2;
        y = GamePanel.HEIGHT/2;
    }

    /*
    Generates random speeds for x and y axis, but will not return a speed of 0
     */
    public void resetMovement(){
        do{
            deltaX = (rand.nextInt(8) - 4);
        } while (deltaX == 0);
        do {
            deltaY = (rand.nextInt(8) - 4);
        } while(deltaY == 0);
    }

    public void increaseDeltaY(){
        deltaY += (rand.nextInt(2));
    }
    public void increaseDeltaX(){
        deltaX += (rand.nextInt(2));
    }
}
