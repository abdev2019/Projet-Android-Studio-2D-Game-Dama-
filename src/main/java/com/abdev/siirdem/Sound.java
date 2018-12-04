package com.abdev.siirdem;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.util.Random;


public class Sound
{
    static SoundPool soundMove;
    static SoundPool soundEat;
    static SoundPool soundWin;
    static SoundPool soundLose;
    public SoundPool soundAlert;


    static int idmove;
    static int ideat;
    static int idwin[];
    static int idlose[];
    public int idAlert, streamAlert;

    public Sound(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            soundEat     = new SoundPool.Builder().setMaxStreams(10).build();
            soundMove    = new SoundPool.Builder().setMaxStreams(10).build();
            soundWin     = new SoundPool.Builder().setMaxStreams(30).build();
            soundLose    = new SoundPool.Builder().setMaxStreams(30).build();
            soundAlert   = new SoundPool.Builder().setMaxStreams(30).build();
        }
        else
        {
            soundMove    = new SoundPool(10, AudioManager.STREAM_MUSIC, 1);
            soundEat     = new SoundPool(10, AudioManager.STREAM_MUSIC, 1);
            soundWin     = new SoundPool(30, AudioManager.STREAM_MUSIC, 1);
            soundLose    = new SoundPool(30, AudioManager.STREAM_MUSIC, 1);
            soundAlert   = new SoundPool(30, AudioManager.STREAM_MUSIC, 1);
        }

        ideat  = soundEat.load(context, R.raw.eat, 1);
        idmove = soundMove.load(context, R.raw.deplacer, 1);
        idAlert = soundAlert.load(context, R.raw.alert, 1);
        idwin=new int[2];
        idlose=new int[3];
        idwin[0]  = soundWin.load(context, R.raw.win1, 1);
        idwin[1]  = soundWin.load(context, R.raw.win2, 1);
        idlose[0] = soundLose.load(context, R.raw.haha1, 1);
        idlose[1] = soundLose.load(context, R.raw.haha2, 1);
        idlose[2] = soundLose.load(context, R.raw.haha3, 1);
    }

    public void move(){
        soundMove.play(idmove, 1, 1, 1, 0, 1);
    }

    public void eat(){
        soundEat.play(ideat, 1, 1, 1, 0, 1);
    }

    public void lose(){
        soundLose.play(idlose[ (new Random().nextInt(3))  ], 1, 1, 1, 0, 1);
    }

    public void win(){
        soundWin.play(idwin[(new Random().nextInt(2))], 1, 1, 1, 0, 1);
    }

    public void alert() {
        streamAlert = soundAlert.play(idAlert , 1, 1, 1, 0, 1 );
    }

    public void detruire(){
         soundMove = null;
         soundEat= null;
         soundWin= null;
         soundLose= null;
        soundAlert = null;
    }

}
