package com.abdev.siirdem;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;


public class Retard
{
    Context context;
    Bitmap bitmaps[], bitmap;
    public boolean alert;
    Sound sound;

    Thread thread;
    boolean bThread;

    public boolean actif=true;

    public Handler  handler1=null, handler2=null;
    public Runnable runnable1=null, runnable2=null;

    Matrix m;
    int x, y;


    public Retard(Context context, Sound s, int w, int h, int x, int y)
    {
        this.context = context;
        this.sound = s;

        m = new Matrix();
        this.x = x;
        this.y = y;

        bitmaps = new Bitmap[2];
        bitmaps[0] = Vue.creerBitmap(context,R.drawable.retard0,w, h);
        bitmaps[1] = Vue.creerBitmap(context,R.drawable.retard1,w, h);
        bitmap = bitmaps[0];

        handler1 = new Handler();
        handler2 = new Handler();

        runnable2 = new Runnable() {@Override public void run() { detect(); }};
        runnable1 = new Runnable() {@Override public void run() { alert(); handler2.postDelayed(runnable2, 5000); }};
    }


    public void alert()
    {
        alert   = true;
        bThread = false;
        thread  = null;

        bThread = true;
        thread  = new Thread( new Runnable() {
            int i=0, rot = 10, k=0;
            @Override public void run() {
                sound.alert();
                while (bThread) {
                    bitmap = bitmaps[k];
                    m.setTranslate(x,y);
                    m.postRotate(i, x+bitmap.getWidth()/2, y+bitmap.getHeight()/2);
                    i+=rot;
                    if(i== 30) rot  = -10;
                    if(i==-30) rot  =  10;
                    if(k==0)k=1; else k=0;
                    try{ Thread.sleep(100); }catch (Exception e){}
                }
            }
        } );

        thread.start();
    }


    public void detect()
    {
        bThread = false;
        alert = false;
        sound.soundAlert.stop(sound.streamAlert);
        ((Activity)context).runOnUiThread(new Runnable() {public void run() {
            stop();
            handler1.postDelayed(runnable1,45000);
        }});
    }


    public void stop()
    {
        handler2.removeCallbacks(runnable2);
        handler1.removeCallbacks(runnable1);
    }


    public void detruire()
    {
        bThread = false;
        alert = false;
        sound.soundAlert.stop(sound.streamAlert);
        handler1.removeCallbacks(runnable1);
        handler2.removeCallbacks(runnable2);
        handler1 = null;
        handler2 = null;
        thread = null;
        runnable1 = null;
        runnable2 = null;
        bitmaps = null;
        bitmap = null;
    }




}












