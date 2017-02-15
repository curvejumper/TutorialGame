package com.michael.tutorialgame;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;
import java.net.URI;

/**
 * Created by micha on 2/12/2017.
 */

public class GameMusic implements Music, MediaPlayer.OnPreparedListener{

    private MediaPlayer mediaPlayer;

    public GameMusic(Context context){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        try {
            mediaPlayer.setDataSource(context, Uri.parse("android.resource://com.michael.tutorialgame/" + R.raw.happy));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //use async so that media prepares on a separate thread
        mediaPlayer.prepareAsync();
        mediaPlayer.setLooping(true);
    }

    @Override
    public void stop() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public void play() {
        mediaPlayer.seekTo(0);
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        System.out.println("________________Media playing_________________");
        mediaPlayer.start();
    }
}
