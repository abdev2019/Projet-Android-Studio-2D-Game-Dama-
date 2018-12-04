package com.abdev.siirdem;


 import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;


public class Plateau
{
    private int   w,h,n,depx,depy;
    public Place place[][];
    public Pion  pionActive;

    public Plateau(int n, int ws, int hs, int depx,int depy){
        w = ws;
        h = hs;
        this.depx = depx;
        this.depy = depy;

        this.n = n;
        pionActive = null;
        place  = new Place[n][];
        for (int i=0;i<n;i++) place[i] = new Place[n];
        reset();
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        for(int i=0;i<n;i++)
        {
            canvas.drawLine( place[i][0].x,   place[i][0].y,  w-w/n+depx,             place[i][0].y, paint );
            canvas.drawLine( place[0][i].x,   place[0][i].y,  place[0][i].x, h-h/n+depy            , paint );
        }
    }

    public ArrayList<Place> cherchePlaceVide(boolean PLAYER1) {
        ArrayList<Place> pVide,tmp;

        // explorer les lignes
        if(pionActive.isDem)
        {
            pVide = chercheDansColone(pionActive.place.i,pionActive.place.j,'+');
            if(pVide==null) pVide = chercheDansColone(pionActive.place.i,pionActive.place.j,'-');
            else{
                tmp = chercheDansColone(pionActive.place.i,pionActive.place.j,'-');
                if(tmp!=null) pVide.addAll(tmp);
            }
        }
        else pVide = chercheDansColone(pionActive.place.i,pionActive.place.j, ((PLAYER1)?'+':'-') );


        // explorer les colones
        if(pVide==null) pVide = chercheDansLigne(pionActive.place.i,pionActive.place.j);
        else {
            tmp = chercheDansLigne(pionActive.place.i,pionActive.place.j);
            if(tmp!=null) pVide.addAll(tmp);
        }

        return (pVide==null || pVide.isEmpty())? null : pVide;
    }

    public ArrayList<Place> chercheDansLigne(int ligne, int col) {
        ArrayList<Place> pVide = new ArrayList<Place>();
        int id = ((n*n-1)/2);
        boolean deja=false;

        for (int i = col+1; i < n; i++)
        {
            if (place[ligne][i].isVide) {
                pVide.add(place[ligne][i]);
                if(!pionActive.isDem)break;
            }
            else
            if ( !deja )
            {
                if( ( (pionActive.id<=id)? (place[ligne][i].pion.id<=id) : (place[ligne][i].pion.id>id) )  )
                    break;
                deja = true;
            }
            else break;
        }

        deja=false;
        for (int i = col-1; i >=0; i--)
        {
            if (place[ligne][i].isVide) {
                pVide.add(place[ligne][i]);
                if(!pionActive.isDem)break;
            }
            else if (!deja)
            {
                if( (pionActive.id<=id)? (place[ligne][i].pion.id<=id) : (place[ligne][i].pion.id>id)  ) break;
               deja = true;
            }
            else break;
        }

        return (pVide.isEmpty())?null:pVide;
    }

    public ArrayList<Place> chercheDansColone(int ligne, int col, char signe) {
        ArrayList<Place> pVide = new ArrayList<Place>();
        boolean deja=false;
        int id=((n*n-1)/2);

        if(signe=='+') {
            for (int i = ligne+1; i < n; i++)
            {
                if (place[i][col].isVide){
                    pVide.add(place[i][col]);
                    if(!pionActive.isDem)break;
                }
                else
                if ( !deja )
                {
                    if( (pionActive.id<=id)? (place[i][col].pion.id<=id) : (place[i][col].pion.id>id)  ) break;
                    deja = true;
                }
                else break;
            }
        }
        else{
            for (int i = ligne-1; i>=0; i--)
            {
                if (place[i][col].isVide){
                    pVide.add(place[i][col]);
                    if(!pionActive.isDem)break;
                }
                else
                if ( !deja )
                {
                    if( (pionActive.id<=id)? (place[i][col].pion.id<=id) : (place[i][col].pion.id>id)  ) break;
                    deja = true;
                }
                else break;
            }
        }

        return (pVide.isEmpty())?null:pVide;
    }

    public Place getPionAmanger(int i, int j) {
        int debut,fin;

        if( i == pionActive.place.i )
        {
            if(pionActive.place.j > j){
                debut = j+1;
                fin = pionActive.place.j;
            }
            else {
                debut = pionActive.place.j+1;
                fin = j;
            }
            for(int ii=debut; ii<fin ;ii++)
            {
                if (place[i][ii].pion != null) return place[i][ii];
            }
        }

        else if( j == pionActive.place.j )
        {
            if(pionActive.place.i > i) {
                debut = i+1;
                fin = pionActive.place.i;
            }
            else{
                debut = pionActive.place.i+1;
                fin = i;
            }
            for(int ii=debut; ii<fin ;ii++)
            {
                if (place[ii][j].pion != null) return place[ii][j];
            }
        }

        return null;
    }

    public ArrayList<Place> chercherPionAmanger(boolean pl) {
        ArrayList<Place> tmp = cherchePlaceVide(pl);
        ArrayList<Place> res = new ArrayList<>();

        if(tmp!=null)
        {
            boolean deja=false;
            for (Place p : tmp)
            {
                if(getPionAmanger(p.i,p.j)!=null)
                {
                    res.add(p);
                    //if (!pionActive.isDem)
                        //return res;
                }
            }
        }
        return res.isEmpty()?null:res;
    }

    public void reset(){
        int k=1, x=0, y=0;
        for (int i=0;i<n;i++)
        {
            x = 0;
            for (int j=0;j<n;j++)
            {
                place[i][j] = new Place(k++, x+depx, y+depy,i,j);
                x += w/n;
            }
            y += h/n;
        }
    }

}













