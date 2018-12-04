package com.abdev.siirdem;

import android.content.Context;
import android.graphics.Bitmap;

public class Player {
    public int id;
    public String nom="";
    public Bitmap bitmap, dem;
    public Pion[] pions;
    public int score;

    public Player(Context context, boolean player , int n, int wh){
        score = 0;
        pions = new Pion[n];
        bitmap = Vue.creerBitmap(context, player?R.drawable.pion1:R.drawable.pion2 , wh, wh);
        dem = Vue.creerBitmap(context, player?R.drawable.dem1:R.drawable.dem2 , wh, wh);
        id = 0;
    }
}
