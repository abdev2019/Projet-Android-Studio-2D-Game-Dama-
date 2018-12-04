package com.abdev.siirdem;



public class Place
{
    public int num, i,j;
    public int x, y;
    public Pion pion;
    public boolean isVide;

    public Place(int num, int x, int y, int i, int j) {
        this.x   = x;
        this.y   = y;
        this.num = num;
        isVide   = true;
        pion     = null;
        this.i = i;
        this.j = j;
    }

    public  Place(Place p) {
        num = p.num;
        i = p.i;
        j = p.j;
        x = p.x;
        y = p.y;
        pion = p.pion;
        isVide = p.isVide;
    }

}
