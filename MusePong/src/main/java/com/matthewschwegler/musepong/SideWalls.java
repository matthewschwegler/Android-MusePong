package com.matthewschwegler.musepong;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Matt Schwegler on 1/16/2016.
 */
public class SideWalls extends GameObject {
    private Paint paint;
    private boolean drawn;

    public SideWalls(int x, int y, int width, int height) {
        super.y = y;
        super.x = x;
        super.width = width;
        super.height = height;
        drawn = false;
        paint = new Paint();
        paint.setColor(GamePanel.WALL_COLOR);
    }

    public boolean getDrawn(){
        return drawn;
    }
    public void setDrawn(boolean drawn){
        this.drawn = drawn;
    }

    public void ballCollision(PongBall pongBall)
    {
        pongBall.setDeltaX(pongBall.getDeltaX() * -1);
    }

    public void draw(Canvas canvas){
        canvas.drawRect(x, y,
                x + width, height, paint);
    }
}
