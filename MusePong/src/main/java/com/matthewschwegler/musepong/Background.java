package com.matthewschwegler.musepong;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Matt Schwegler on 1/15/2016.
 */
public class Background {
    private Bitmap image;
    private int x = 0;
    private int y = 0;

    public Background(Bitmap res)
    {
        image = res;
    }

    public void update()
    {

    }

    public void draw(Canvas canvas)
    {
        canvas.drawBitmap(image, x, y, null);
    }

}
