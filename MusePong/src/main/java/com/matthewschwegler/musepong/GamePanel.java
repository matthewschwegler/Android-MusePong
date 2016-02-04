package com.matthewschwegler.musepong;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.interaxon.libmuse.Accelerometer;
import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MuseFileWriter;
import com.interaxon.libmuse.MuseManager;
import com.interaxon.libmuse.MusePreset;
import com.interaxon.libmuse.MuseVersion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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

    //Constants for controlling the muse
    public static final double ACCEL_THRESH = 300;

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

    //Muse objects
    private Muse muse = null;
    private ConnectionListener connectionListener = null;
    private DataListener dataListener = null;
    private boolean dataTransmission = true;
    private MuseFileWriter fileWriter = null;

    // Is based as "this" in Game.java as this in SetContent View
    public GamePanel (Context context)
    {
        super(context);

        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);


        //make gamePanel focusable so it can handle events
        setFocusable(true);

        //Muse listeners
        if(context instanceof  Activity) {
            WeakReference<Activity> weakActivity =
                    new WeakReference<Activity>((Activity) context);
            connectionListener = new ConnectionListener(weakActivity);
            dataListener = new DataListener(weakActivity);
        } else {
            System.out.println("Context not instance of activity");
        }
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

        //Muse initialization
        MuseManager.refreshPairedMuses();
        List<Muse> pairedMuses = MuseManager.getPairedMuses();
        if (pairedMuses.size() > 1) {
            System.out.println("There are: " + pairedMuses.size() + " muses registered");
        } else if (!pairedMuses.isEmpty()) {
            muse = pairedMuses.get(0);

            ConnectionState state = muse.getConnectionState();
            if (state == ConnectionState.CONNECTED ||
                    state == ConnectionState.CONNECTING) {
                Log.w("Muse Headband", "is connected for connecting");
            }
            configureLibrary();
            try {
                muse.runAsynchronously();
            } catch (Exception e) {
                Log.e("Muse Headband", e.toString());
            }
        } else {
            System.out.println("No muse device found");
        }
    }

    private void configureLibrary() {
        muse.registerConnectionListener(connectionListener);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ACCELEROMETER);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.EEG);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ALPHA_RELATIVE);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ARTIFACTS);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.BATTERY);
        muse.setPreset(MusePreset.PRESET_14);
        muse.enableDataTransmission(dataTransmission);
    }

    /**
     * Connection listener updates UI with new connection status and logs it.
     */
    class ConnectionListener extends MuseConnectionListener {

        final WeakReference<Activity> activityRef;

        ConnectionListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
            final ConnectionState current = p.getCurrentConnectionState();
            final String status = p.getPreviousConnectionState().toString() +
                    " -> " + current;
            final String full = "Muse " + p.getSource().getMacAddress() +
                    " " + status;
            Log.i("Muse Headband", full);
            Activity activity = activityRef.get();
            // UI thread is used here only because we need to update
            // TextView values. You don't have to use another thread, unless
            // you want to run disconnect() or connect() from connection packet
            // handler. In this case creating another thread is required.

            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView statusText =
                                (TextView) findViewById(R.id.con_status);
                        statusText.setText(status);
                        TextView museVersionText =
                                (TextView) findViewById(R.id.version);
                        if (current == ConnectionState.CONNECTED) {
                            MuseVersion museVersion = muse.getMuseVersion();
                            String version = museVersion.getFirmwareType() +
                                    " - " + museVersion.getFirmwareVersion() +
                                    " - " + Integer.toString(
                                    museVersion.getProtocolVersion());
                            museVersionText.setText(version);
                        } else {
                            museVersionText.setText(R.string.undefined);
                        }
                    }
                });
            }

        }
    }

    /**
     * Data listener will be registered to listen for: Accelerometer,
     * Eeg and Relative Alpha bandpower packets. In all cases we will
     * update UI with new values.
     * We also will log message if Artifact packets contains "blink" flag.
     * DataListener methods will be called from execution thread. If you are
     * implementing "serious" processing algorithms inside those listeners,
     * consider to create another thread.
     */
    class DataListener extends MuseDataListener {

        final WeakReference<Activity> activityRef;
        private MuseFileWriter fileWriter;

        DataListener(final WeakReference<Activity> activityRef) {
            System.out.println("Inside DataListener for muse!");
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseDataPacket(MuseDataPacket p) {
            //System.out.println("Inside receiveMuseDataPacket!");
            switch (p.getPacketType()) {
                case EEG:
                    updateEeg(p.getValues());
                    break;
                case ACCELEROMETER:
                    updateAccelerometer(p.getValues());
                    break;
                case ALPHA_RELATIVE:
                    updateAlphaRelative(p.getValues());
                    break;
                case BATTERY:
                    fileWriter.addDataPacket(1, p);
                    // It's library client responsibility to flush the buffer,
                    // otherwise you may get memory overflow.
                    if (fileWriter.getBufferedMessagesSize() > 8096)
                        fileWriter.flush();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
            if (p.getHeadbandOn() && p.getBlink()) {
                Log.i("Artifacts", "blink");
            }
        }

        private void updateAccelerometer(final ArrayList<Double> data) {
            //System.out.println("Inside updateAccelerometer!");
            Activity activity = activityRef.get();
            if (activity != null) {
                if(player1.getPlaying() ) {
                    double acc_x = data.get(Accelerometer.FORWARD_BACKWARD.ordinal());
                    //If outside defined deadzone move the paddle toward the finger on the screen.
                    if ( Math.abs(acc_x - ACCEL_THRESH) > 0) {
                        if ( acc_x < 0) {
                            player1.setUp(false);
                            player1.setDown(true);
                        }
                        //Finger is below paddle
                        else if ( acc_x > 0) {
                            player1.setUp(true);
                            player1.setDown(false);
                        }
                    } else {
                        player1.setUp(false);
                        player1.setDown(false);
                    }

                }
                /*
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView acc_x = (TextView) findViewById(R.id.acc_x);
                        TextView acc_y = (TextView) findViewById(R.id.acc_y);
                        TextView acc_z = (TextView) findViewById(R.id.acc_z);
                        acc_x.setText(String.format(
                                "%6.2f", data.get(Accelerometer.FORWARD_BACKWARD.ordinal())));
                        acc_y.setText(String.format(
                                "%6.2f", data.get(Accelerometer.UP_DOWN.ordinal())));
                        acc_z.setText(String.format(
                                "%6.2f", data.get(Accelerometer.LEFT_RIGHT.ordinal())));
                    }
                });
                */
            }
        }

        private void updateEeg(final ArrayList<Double> data) {
            Activity activity = activityRef.get();
            /*
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tp9 = (TextView) findViewById(R.id.eeg_tp9);
                        TextView fp1 = (TextView) findViewById(R.id.eeg_fp1);
                        TextView fp2 = (TextView) findViewById(R.id.eeg_fp2);
                        TextView tp10 = (TextView) findViewById(R.id.eeg_tp10);
                        tp9.setText(String.format(
                                "%6.2f", data.get(Eeg.TP9.ordinal())));
                        fp1.setText(String.format(
                                "%6.2f", data.get(Eeg.FP1.ordinal())));
                        fp2.setText(String.format(
                                "%6.2f", data.get(Eeg.FP2.ordinal())));
                        tp10.setText(String.format(
                                "%6.2f", data.get(Eeg.TP10.ordinal())));
                    }
                });
            }
            */
        }

        private void updateAlphaRelative(final ArrayList<Double> data) {
            Activity activity = activityRef.get();
            /*
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView elem1 = (TextView) findViewById(R.id.elem1);
                        TextView elem2 = (TextView) findViewById(R.id.elem2);
                        TextView elem3 = (TextView) findViewById(R.id.elem3);
                        TextView elem4 = (TextView) findViewById(R.id.elem4);
                        elem1.setText(String.format(
                                "%6.2f", data.get(Eeg.TP9.ordinal())));
                        elem2.setText(String.format(
                                "%6.2f", data.get(Eeg.FP1.ordinal())));
                        elem3.setText(String.format(
                                "%6.2f", data.get(Eeg.FP2.ordinal())));
                        elem4.setText(String.format(
                                "%6.2f", data.get(Eeg.TP10.ordinal())));
                    }
                });
            }
            */
        }

        public void setFileWriter(MuseFileWriter fileWriter) {
            this.fileWriter  = fileWriter;
        }
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
        super.draw(canvas);
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
