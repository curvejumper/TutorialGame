package com.michael.tutorialgame;

import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * Created by micha on 2/10/2017.
 */

public interface Scene {
    public void update();
    public void draw(Canvas canvas);

    //switches active scenes
    public void terminate();

    public void receiveTouch(MotionEvent event);
}
