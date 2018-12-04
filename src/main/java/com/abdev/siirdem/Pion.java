package com.abdev.siirdem;

import android.graphics.Bitmap;


public class Pion
{
    public int id;
    public boolean isDem;
    public Place place;
    public Bitmap bitmap;

    public Pion(int id) {
        this.id = id;
        isDem   = false;
        place   = null;
    }

    public boolean setIfDem(int n){
        if( isDem || place.i==n-1 ) {
            if(!isDem){ isDem = true; }
            return isDem;
        }
        return false;
    }

    public boolean mouver(Place newPlace, float pas){

        if( newPlace.x > place.x )
        {
            place.x += pas;
            if(newPlace.x <= place.x) { place.x = newPlace.x; return false; }
        }
        else
        if( newPlace.x < place.x )
        {
            place.x -= pas;
            if(newPlace.x >= place.x) { place.x = newPlace.x; return false; }
        }
        else
        if( newPlace.y > place.y )
        {
            place.y += pas;
            if(newPlace.y <= place.y) { place.y = newPlace.y; return false; }
        }
        else
        if( newPlace.y < place.y )
        {
            place.y -= pas;
            if(newPlace.y >= place.y) { place.y = newPlace.y; return false; }
        }

        return true;
    }

}



















