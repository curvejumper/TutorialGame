package com.michael.tutorialgame;

import android.graphics.Canvas;
import android.provider.Settings;
import android.view.SurfaceHolder;

/**
 * Created by micha on 2/8/2017.
 */

public class MainThread extends Thread {
    //meat of the game

    //b/c most phones can do 30 but more would just be unnessecary
    public static final int MAX_FPS = 30;
    private double averageFPS;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;
    public static Canvas canvas;

    public void setRunning(boolean running){ this.running = running;}

    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel){
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }


    //This is the game loop
    @Override
    public void run() {
        long startTime;
        //b/c 1000 milliseconds is a second
        long timeMillis = 1000/MAX_FPS;
        long waitTime;
        int frameCount = 0;
        long totalTime = 0;
        long targetTime = 1000/MAX_FPS;

        while(running){
            startTime = System.nanoTime();
            canvas = null;

            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    this.gamePanel.update();
                    this.gamePanel.draw(canvas);
                }
                } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            timeMillis = (System.nanoTime() - startTime/1000000);
            waitTime = targetTime - timeMillis;
            try {
                if(waitTime > 0){
                    this.sleep(waitTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            totalTime += System.nanoTime() - startTime;
            frameCount++;
            if(frameCount == MAX_FPS){
                //converting from nano to milli
                averageFPS = 1000/(totalTime / frameCount/1000000);
                frameCount = 0;
                totalTime = 0;
                System.out.println(averageFPS);
            }
        }
    }
}
