package com.matthewschwegler.musepong;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Matt Schwegler on 1/15/2016.
 */
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    //Declared constants for the game
    public static final int FPS = 30;
    public static final int MILLION = 1000000;
    //Game demensions
    public static final int WIDTH = 700;
    public static final int HEIGHT = 448;
    //Paddle constants
    public static final int PADDLE_HEIGHT = 80;
    public static final int PADDLE_WIDTH = 10;
    public static final int PADDLE_MOVESPEED = 4;
    public static final int PADDLE_MOVETHRESHOLD = 20;
    public static final int PADDLE_DEADZONE = 20;
    //Ball constants
    public static final int BALL_SIZE = 10;
    public static final int START_ANGLE = 40;
    public static final int START_SPEED = 3;
    //Wall constants
    public static final int WALL_THICKNESS = 10;
    public static final int WALL_COLOR = Color.WHITE;

    private MainThread mainThread;
    private Background background;
    private PongPaddle pongPaddleWest;
    private PongBall pongBall;
    private TopBottomWalls northWall;
    private TopBottomWalls southWall;
    private SideWalls eastWall;
    private SideWalls westWall;
    private Player player1;

    //GamePanel control booleans
    private boolean reset = false;
    private boolean player2Scored = false;

    //Temp score variable
    private int genericScore;

    // Is based as "this" in Game.java as this in SetContent View
    public GamePanel (Context context)
    {
        super(context);

        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {

    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000) {
            counter++;
            try {
                mainThread.setRunning(false);
                mainThread.join();
                retry = false;
                mainThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        //Create game objects
        background = new Background(BitmapFactory.decodeResource(getResources(),R.drawable.black_pong_field_with_border));

        //Pong paddle
        Paint paintPaddle = new Paint();
        paintPaddle.setColor(Color.WHITE);
        pongPaddleWest = new PongPaddle(20, HEIGHT/2 - PADDLE_HEIGHT, PADDLE_WIDTH, PADDLE_HEIGHT, paintPaddle);

        //Create player1
        player1 = new Player(pongPaddleWest);

        //Create pong ball
        Paint paintBall = new Paint();
        paintBall.setColor((Color.WHITE));
        pongBall = new PongBall(WIDTH/2, HEIGHT/2, START_ANGLE, START_SPEED, paintBall );

        //Create walls
        northWall = new TopBottomWalls(0, 0, WIDTH, WALL_THICKNESS);
        southWall = new TopBottomWalls(0,HEIGHT -WALL_THICKNESS, WIDTH, WALL_THICKNESS);
        eastWall = new SideWalls(WIDTH - WALL_THICKNESS, 0, WALL_THICKNESS, HEIGHT);
        westWall = new SideWalls(0, 0, WALL_THICKNESS, HEIGHT);

        mainThread = new MainThread(getHolder(), this);
        //we can safely start the gameloop
        mainThread.setRunning(true);
        mainThread.start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        //players finger on the screen.
        if(event.getAction() == MotionEvent.ACTION_DOWN) {

            if (!player1.getPlaying()) {
                player1.setPlaying(true);
                System.out.println("Playing set to: " + player1.getPlaying());
            }
        }

       // System.out.println("getAction == " + event.getAction() + " ACTION_MOVE == " + MotionEvent.ACTION_MOVE);
        if(player1.getPlaying() && event.getAction() == MotionEvent.ACTION_MOVE) {


            //If outside defined deadzone move the paddle toward the finger on the screen.
            if (!insideDeadZone(event.getY())) {
                if ((player1.getPaddlePosition() * scaleY(getHeight())) < event.getY()) {
                    player1.setUp(false);
                    player1.setDown(true);
                }
                //Finger is below paddle
                else if ((player1.getPaddlePosition() * scaleY(getHeight())) > event.getY()) {
                    player1.setUp(true);
                    player1.setDown(false);
                }
            } else {
                player1.setUp(false);
                player1.setDown(false);
            }

        }

        //players finger is off the screen.
        if(event.getAction() == MotionEvent.ACTION_UP) {
            //System.out.println("Finger Lifted");
            //If finger is lifted stop all movement
            if(player1.getPlaying()){
                player1.setDown(false);
                player1.setUp(false);
            }

        }

        return true;
    }

    public void update() {
        //
        if (player1.getPlaying() && !reset) {

            player1.update();
            pongBall.update();

            //Wall collisions
            if (collision(pongBall, northWall)) {
                //pongBall.increaseDeltaY();
                northWall.ballCollision(pongBall);
            }
            if (collision(pongBall, southWall)) {
                //pongBall.increaseDeltaY();
                southWall.ballCollision(pongBall);
            }
            if (collision(pongBall, eastWall)) {
                //pongBall.increaseDeltaX();;
                eastWall.ballCollision(pongBall);
            }
            if (collision(pongBall, westWall)) {
                player2Scored = true;
                reset = true;
                genericScore += 1;
            }
            //Paddle Collisions
            //System.out.println("player1: x: " +player1.getX() +" y: "+player1.getY()+" w: "+player1.getWidth()+" h: "+player1.getHeight() );
            //System.out.println("ball: x: "+pongBall.getX()+" y: "+pongBall.getY()+" w: "+pongBall.getWidth()+" h: "+pongBall.getHeight());
            if (collision(pongBall, player1)) {
                //pongBall.increaseDeltaX();
                player1.ballCollision(pongBall);
                player1.setScore(player1.getScore() + 1);
            }
        }
        //Reset the ball
        else if (player2Scored) {
            pongBall.centerBall();
            reset = false;
        }

    }

    @Override
    public void draw(Canvas canvas){
        final float scaleFactorX = scaleX(getWidth());
        final float scaleFactorY = scaleY(getHeight());
        //background.draw(canvas);

        if (canvas != null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            background.draw(canvas);

            //Draw player1 (paddleLeft)
            player1.draw(canvas);

            pongBall.draw(canvas);

            drawText(canvas);

            // Only draw the walls once
            if(!northWall.getDrawn()) {
                northWall.draw(canvas);
                northWall.setDrawn(true);
            }
            if(!southWall.getDrawn()){
                southWall.draw(canvas);
                northWall.setDrawn(true);
            }
            if(!eastWall.getDrawn()){
                eastWall.draw(canvas);
                eastWall.setDrawn(true);
            }
            if(!westWall.getDrawn()){
                westWall.draw(canvas);
                westWall.setDrawn(true);
            }

            //Restore the canvas size after scaling, prevents infinite scaleing
            canvas.restoreToCount(savedState);
        }
    }

    public boolean collision(GameObject object_one, GameObject object_two)
    {
        if(Rect.intersects(object_one.getRectangle(),object_two.getRectangle())){
            return true;
        } else {
            return false;
        }
    }

    private float scaleY(float y){
        return (y / (HEIGHT * 1.f));
    }
    private float scaleX(float x){
        return (x / (WIDTH * 1.f));
    }

    //Paddle won't move if inside dead zone,
    private boolean insideDeadZone(float eventY){
        if ( Math.abs(player1.getPaddlePosition() * scaleY(getHeight()) - eventY) < PADDLE_DEADZONE){
            return true;
        }
        else {
            return false;
        }
    }

    public void drawText(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("" + player1.getScore(), WIDTH / 2 - 30, HEIGHT - 15, paint);
        canvas.drawText("" + genericScore, WIDTH/2 + 16, HEIGHT - 15, paint);
        //canvas.drawText("BEST: " + best, WIDTH-215, HEIGHT - 10, paint );

        /*
        if(!player.getPlaying() && newGameCreated && reset){
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH / 2 - 50, HEIGHT / 2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH / 2 - 50, HEIGHT / 2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH/2-50, HEIGHT/2 + 40, paint1);
        }
        */
    }

}
