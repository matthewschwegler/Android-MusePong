package com.matthewschwegler.musepong;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;

/**
 * Created by Matt Schwegler on 1/15/2016.
 */
public class MainThread extends Thread {
    private double averageFPS;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;
    public static Canvas canvas;

    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    @Override
    public void run() {
        long startTime;
        long timeMilli;
        long waitTime;
        long totalTime = 0;
        long frameCount = 0;
        long targetTime = 1000 / GamePanel.FPS;

        while (running) {
            startTime = System.nanoTime();
            canvas = null;

            //try locking the canvas for pixel editing
            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    this.gamePanel.update();
                    this.gamePanel.draw(canvas);
                }
            } catch (Exception e) {
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            timeMilli = (System.nanoTime() - startTime) / GamePanel.MILLION;
            waitTime = targetTime - timeMilli;

            try {
                this.sleep(waitTime);
            } catch (Exception e) {
            }

            totalTime += System.nanoTime() - startTime;
            frameCount++;
            if (frameCount == GamePanel.FPS) {
                averageFPS = 1000 / ((totalTime / frameCount) / GamePanel.MILLION);
                frameCount = 0;
                totalTime = 0;
                //Prints out average FPS
                //System.out.println(averageFPS);
            }
        }
    }

    public void setRunning(boolean stateUpdate)
    {
        this.running = stateUpdate;
    }
}
