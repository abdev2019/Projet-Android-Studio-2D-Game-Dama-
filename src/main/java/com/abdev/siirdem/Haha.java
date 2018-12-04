package com.abdev.siirdem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;


public class Haha implements Runnable{
    Context context;

    Bitmap bitmap;
    public int x, y, k=1, h=1, wh;
    public boolean playing=true;
    Thread thread;

    public Haha(Context context, int x, int y, int wh)
    {
        bitmap = Vue.creerBitmap(context, (new Random().nextInt(2)==0)?R.drawable.haha1:R.drawable.haha3  , wh, wh);
        this.x = x;
        this.y = y;
        this.context = context;
        this.wh = wh;
    }


    @Override public void run()
    {
        Bitmap tmp = BitmapFactory.decodeResource(context.getResources(), (new Random().nextInt(2)==0)?R.drawable.haha1:R.drawable.haha3 );
        while (playing)
        {
            bitmap = Bitmap.createScaledBitmap(tmp, k, k, false );
            k += h;
            if( k > wh*2) h = -1;
            if(k<=10)  h=1;
            try{ thread.sleep(17); } catch(InterruptedException e){ e.printStackTrace(); }
        }
    }

}
