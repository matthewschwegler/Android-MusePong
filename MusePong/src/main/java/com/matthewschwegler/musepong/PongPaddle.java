package com.matthewschwegler.musepong;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by Matt Schwegler on 1/15/2016.
 */

public class PongPaddle extends GameObject {
    private Paint paint;

    PongPaddle(int x, int y, int width, int height, Paint paint)
    {
        super.x = x;
        super.y = y;
        super.width = width;
        super.height = height;

        this.paint = paint;
    }

    public void moveUp()
    {
        //System.out.println("TOP y: " + y + " pos " + (y-GamePanel.PADDLE_HEIGHT));
        if((y - GamePanel.PADDLE_MOVESPEED)  > 15){
            y -= GamePanel.PADDLE_MOVESPEED;
        }
    }

    public void moveDown(){
        System.out.println("TOP y: " + y + " pos " + (y+GamePanel.PADDLE_MOVESPEED) + " Bound " + (GamePanel.HEIGHT - 15));
        if((y + GamePanel.PADDLE_MOVESPEED + GamePanel.PADDLE_HEIGHT) < (GamePanel.HEIGHT - 15)){
            System.out.println("MOVING DOWN");
            y += GamePanel.PADDLE_MOVESPEED;
        }
    }

    public int getPaddleYCenter(){
        return (y+height)/2;
    }

    public void draw(Canvas canvas)
    {
        canvas.drawRect(x, y,
                x + width, y + height, paint);
    }

    public boolean inTopBound(){
        System.out.println("TOP y: " + y + " pos " + (y-GamePanel.PADDLE_HEIGHT));
        if(y  > 15){
            return true;
        } else {
            return false;
        }
    }

    public boolean intBottomBound(){
        System.out.println("Bottom y: " + y + " pos " + (y-GamePanel.PADDLE_HEIGHT));
        if(y < GamePanel.HEIGHT - 10){
            return true;
        } else {
            return false;
        }
    }

}
