package com.michael.alienBolt;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import static com.michael.alienBolt.R.id.adView;

/**
 * Created by micha on 2/10/2017.
 *
 * this is how you can create all diffrent scenes
 * main menu, gameplay etc.
 */

public class GameplayScene implements Scene {

    private SceneManager manager;

    private Rect r = new Rect();

    private RectPlayer player;
    private Point playerPoint;
    private ObstacleManager obstacleManager;

    private boolean movingPlayer = false;
    private int xDelta = 0;
    private int Start_Location;

    //if we collided and on game over screen
    private boolean gameOver = false;
    private long gameOverTime;

    private GameMusic gameMusic;

    private OrientationData orientationData;
    private long frameTime;

    //for ads
    private Handler mHandler;
    private boolean runOnce = false;
    private boolean showAd = false;
    private boolean hideAd = false;

    public GameplayScene(){
        //to call main UI thread
        mHandler = new Handler(Constants.CURRENT_CONTEXT.getMainLooper());

        player = new RectPlayer(new Rect(100, 100, 200, 200), Color.rgb(255, 0, 0));
        playerPoint = new Point(Constants.SCREEN_WIDTH/2, 3*Constants.SCREEN_HEIGHT/4);
        player.update(playerPoint);

        obstacleManager = new ObstacleManager(200, 400, 75, Color.BLUE);

        orientationData = new OrientationData();
        orientationData.register();
        frameTime = System.currentTimeMillis();

        gameMusic = new GameMusic(Constants.CURRENT_CONTEXT);

        Start_Location = Constants.SCREEN_WIDTH/2;
    }

    public void reset() {
        //for ad
        runOnce = false;
        showAd = false;
        hideAd = true;
        mHandler.post(myRunnable);

        playerPoint = new Point(Constants.SCREEN_WIDTH/2, 3*Constants.SCREEN_HEIGHT/4);
        player.update(playerPoint);
        obstacleManager = new ObstacleManager(200, 350, 75, Color.BLUE);
        movingPlayer = false;
        gameMusic.play();
    }

    @Override
    public void update() {
        if(!gameOver) {
            if(frameTime < Constants.INIT_TIME){
                frameTime = Constants.INIT_TIME;
            }
            int elapsedTime = (int) (System.currentTimeMillis() - frameTime);
            frameTime = System.currentTimeMillis();

            if (playerPoint.x < 0){
                playerPoint.x = 0;
            } else if(playerPoint.x > Constants.SCREEN_WIDTH){
                playerPoint.x = Constants.SCREEN_WIDTH;
            }

            player.update(playerPoint);
            obstacleManager.update();
//            FOLLOW THIS TO FIND FREEZE
            if(obstacleManager.playerCollide(player)){
                gameMusic.pause();
                gameOver = true;
                gameOverTime = System.currentTimeMillis();
            }
        } else {
            if(!runOnce){
                showAd = true;
                mHandler.post(myRunnable);
                runOnce = true;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(Color.rgb(0,0,125));
        //red background
//        canvas.drawColor(Color.rgb(102,3,23));


        player.draw(canvas);
        obstacleManager.draw(canvas);

        if(gameOver) {
            int score = getScore();
            Paint paint = new Paint();
            paint.setTextSize(100);
            paint.setColor(Color.rgb(110, 255, 12));
            drawCenterText(canvas, paint, "Game Over", "High Score: " + score);
        }
    }

    @Override
    public void terminate() {
        SceneManager.ACTIVE_SCENE = 0;
        gameMusic.stop();
    }

    @Override
    public void receiveTouch(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                if(!gameOver && player.getRectangle().contains((int)event.getX(), (int)event.getY())){
//                    movingPlayer = true;
//                }
                Start_Location = player.getRectangle().centerX();
                xDelta = (int)event.getX();
                if(gameOver && System.currentTimeMillis() - gameOverTime >= 2000){
                    reset();
                    gameOver = false;
                    orientationData.newGama();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int xPoint = 0;
                if(!gameOver)
                    if((Start_Location +(event.getX() - xDelta)) < 0){
                        xPoint = 1;
                    } else if((Start_Location +(event.getX() - xDelta)) > Constants.SCREEN_WIDTH){
                        xPoint = Constants.SCREEN_WIDTH - 1;
                    } else {
                        xPoint = (int) (Start_Location +(event.getX() - xDelta));
                    }
                    playerPoint.set(xPoint, (3*Constants.SCREEN_HEIGHT/4));
                break;
            case MotionEvent.ACTION_UP:
                movingPlayer = false;
                xDelta = 0;
                Start_Location = (int)(event.getX());
                break;
        }
    }

    private void drawCenterText(Canvas canvas, Paint paint, String text1, String text2) {
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();
        paint.getTextBounds(text1, 0, text1.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        float y = cHeight / 2f + r.height() / 2f - r.bottom;
        canvas.drawText(text1, x, y, paint);
        //Can't use bash "n" to get a new line so adding text below
        //or above center this way.
        canvas.drawText(text2, x - x/4, y + y/3, paint);
    }

    public int getScore() {
        SharedPreferences preferences = Constants.CURRENT_CONTEXT.getSharedPreferences(Constants.FILENAME, Context.MODE_PRIVATE);
        int storedScore = preferences.getInt("score", 0);
        int currentScore = obstacleManager.getScore();
        if(storedScore < currentScore){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("score", currentScore);
            editor.commit();
            return currentScore;
        }
        return storedScore;
    }

    Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            if(showAd) {
                Constants.sAdView.setVisibility(View.VISIBLE);
                Constants.sAdView.loadAd(new AdRequest.Builder()
                        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build());
                Constants.sAdView.setActivated(true);
                showAd = false;
            }
            if(hideAd){
                Constants.sAdView.setVisibility(View.GONE);
                hideAd = false;
            }
        }
    };

}
