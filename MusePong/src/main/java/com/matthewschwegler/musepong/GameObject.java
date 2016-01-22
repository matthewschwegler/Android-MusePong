package com.matthewschwegler.musepong;

import android.graphics.Rect;

/**
 * Created by Matt Schwegler on 1/15/2016.
 */
public abstract class GameObject {
    protected int x;
    protected int y;
    protected int deltaX;
    protected int deltaY;
    protected int width;
    protected int height;


    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getHeight()
    {
        return height;
    }

    public int getWidth()
    {
        return width;
    }

    public int getDeltaX(){
        return deltaX;
    }
    public int getDeltaY(){
        return deltaY;
    }
    public void setDeltaY(int deltaY){
        this.deltaY = deltaY;
    }
    public void setDeltaX(int deltaX){
        this.deltaX = deltaX;
    }

    // Will help to detect object collision
    public Rect getRectangle()
    {
        return new Rect(x,y, x+width, y+height);
    }
}
