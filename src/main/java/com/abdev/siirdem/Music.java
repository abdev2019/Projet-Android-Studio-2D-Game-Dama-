package com.abdev.siirdem;


import android.app.Activity;
import android.media.MediaPlayer;

public class Music
{
    private static MediaPlayer music = null;

    public static void play(Activity a)
    {
        if(isPlaying()) return;
        music = MediaPlayer.create(a, R.raw.halo);
        music.start();
        music.setLooping(true);
        (a.findViewById(R.id.id_btnMusic)).setBackgroundResource(R.drawable.soundoff);
    }

    public static void stop(Activity a)
    {
        if(!isPlaying()) return;
        music.stop();
        music = null;
        (a.findViewById(R.id.id_btnMusic)).setBackgroundResource(R.drawable.sound);
    }

    public static void toggle(Activity a)
    {
        if(isPlaying()) stop(a);
        else  play(a);
    }


    private static boolean isPlaying()
    {
        if(music != null) return true;
        return false;
    }

    public static void detruire()
    {
        if(!isPlaying()) return;
        music.stop();
        music = null;
    }

}
