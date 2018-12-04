package com.abdev.siirdem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;


public class Vue extends SurfaceView implements Runnable
{
    // tools ****************************
    Context context;
    private  int wScreen, hScreen, detectTouch[];
    volatile boolean playing, isTouch;
    private  Thread gameThread;
    private  SurfaceHolder surfaceHolder;
    private  Paint paint;
    private  Canvas canvas;

    // les objets du jeu ******************************
    public int              dimension, nPion;
    public Bitmap           signalPionActive, signalPlaceVide,bg;
    public Plateau          plateau;
    public ArrayList<Place> placeVide ;
    boolean                 myTurn=false;
    protected Place         aDeplacer;
    public Player           player1, player2;
    protected boolean enMouvement = false;
    protected boolean online      = false;
    Retard retard;
    int pas;

    ArrayList<Pion>   lmnakhf = new ArrayList<>();
    Haha lmnkhof=null;
    Thread thread;

    //sound
    Sound sound;
    // historique ********
    Historique historique;

    // color
    boolean roleColor;


    private InterstitialAd mInterstitialAd;


    public Vue(Context context, int n, int ws, int hs, Historique historique) {
        super(context);
        this.context = context;

        passerRole();

        // init options and tools ****
        this.historique = historique;
        surfaceHolder   = getHolder();
        paint           = new Paint();
        detectTouch     = new int[2];
        isTouch         = false;
        playing         = false;
        gameThread      = null;
        wScreen         = ws;
        hScreen         = hs;
        nPion = (n*n - 1)/2;
        dimension = n;

        sound = new Sound(context);

        // init plateau ************************************
        int wh = (xToPercent(7)+yToPercent(7))/2;
        pas = (xToPercent(4)+yToPercent(4))/2;
        plateau  = new Plateau(n,ws,hs, (ws/n)/2, (hs/n)/2 );
        player1 = new Player(getContext(), true ,  nPion, wh);
        player2 = new Player(getContext(), false , nPion, wh);

        init();

        // init les bitmaps ************************************************************************
        bg               = creerBitmap(context, R.drawable.bg3 ,wScreen,hScreen);
        signalPionActive = creerBitmap(context, R.drawable.sel , wh-2, wh-2);
        signalPlaceVide  = creerBitmap(context, R.drawable.trv , wh+xToPercent(1), wh+xToPercent(1));
        retard = new Retard(context,sound,wh*5,wh*5, wScreen/2-(wh*5)/2, hScreen/2-(wh*5)/2);


        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId( context.getString(R.string.admob_interstitial_id) );
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    public void init() {
        int k=0;
        for(int i=0; i<dimension && k<nPion ;i++)
        {
            for(int j=0; j<dimension && k<nPion ;j++)
            {
                player1.pions[k] = new Pion(k+1);
                player1.pions[k].bitmap = player1.bitmap;
                plateau.place[i][j].pion = (player1.pions[k]);
                plateau.place[i][j].isVide = false;
                player1.pions[k].place = plateau.place[i][j];

                player2.pions[k] = new Pion(nPion+(k+1) );
                player2.pions[k].bitmap = player2.bitmap;
                plateau.place[(dimension-1)-i][(dimension-1)-j].pion = (player2.pions[k]);
                plateau.place[(dimension-1)-i][(dimension-1)-j].isVide = false;
                player2.pions[k].place = plateau.place[(dimension-1)-i][(dimension-1)-j];
                k++;
            }
        }

        player1.score = player2.score = nPion;
        placeVide     = null;
        aDeplacer     = null;
    }

    @Override public void run() {
        while (playing) {
            if(!enMouvement) {
                update();
            }
            else enMouvement = plateau.pionActive.mouver(aDeplacer, pas );

            draw();
            control();
        }
    }



    // updating objects
    public void update() {}
    public void deplacer(){}

    boolean testIfGagne() {

        if(myTurn){
            player2.score--;
            ((Activity)getContext()).runOnUiThread(new Runnable() {public void run() {
                ( (TextView)( ((Activity)getContext()).findViewById(R.id.scorePion2) ) ).setText(""+player2.score );
            }});
        }
        else {
            player1.score--;
            ((Activity)getContext()).runOnUiThread(new Runnable() {public void run() {
                ( (TextView)( ((Activity)getContext()).findViewById(R.id.scorePion1) ) ).setText( ""+player1.score );
            }});
        }

        if(player2.score <= 0)
        {
            ((Activity)getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle("Mission terminé");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    if (mInterstitialAd.isLoaded()) {
                                        mInterstitialAd.show();
                                    }
                                }
                            });
                    alertDialog.setMessage(player1.nom+"(Blue) Gagné \nScore : "+(player1.score) );
                    alertDialog.show();
                }
            });
            sound.win();
            return true;
        }
        else if(player1.score <= 0)
        {
            ((Activity)getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle("Mission terminé");
                    alertDialog.setMessage(player2.nom+"(Vert) Gagné \nScore : "+(player2.score) );
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    if (mInterstitialAd.isLoaded()) {
                                        mInterstitialAd.show();
                                    }
                                }
                            });
                    alertDialog.show();
                }
            });
            sound.lose();
            return true;
        }
        return false;
    }


    // drawing objects
    private void draw() {
        if (surfaceHolder.getSurface().isValid())
        {
            canvas = surfaceHolder.lockCanvas();

            canvas.drawBitmap(bg,0,0,paint);

            plateau.draw(canvas,paint);

            int wh = player1.bitmap.getWidth();
            for(int i=0;i<nPion;i++) {
                canvas.drawBitmap(player1.pions[i].bitmap, player1.pions[i].place.x-wh/2, player1.pions[i].place.y-wh/2, paint);
                canvas.drawBitmap(player2.pions[i].bitmap, player2.pions[i].place.x-wh/2, player2.pions[i].place.y-wh/2, paint);
            }

            if(plateau.pionActive != null && plateau.pionActive.place!=null)
               canvas.drawBitmap(signalPionActive,plateau.pionActive.place.x-signalPionActive.getWidth()/2,plateau.pionActive.place.y-signalPionActive.getWidth()/2,paint);

            if(placeVide!=null)
            for(Place p : placeVide)
              canvas.drawBitmap(signalPlaceVide,p.x-signalPlaceVide.getWidth()/2,p.y-signalPlaceVide.getWidth()/2,paint);

            if(!lmnakhf.isEmpty())
                for(int i=0;i<lmnakhf.size();i++)
                    canvas.drawBitmap(lmnkhof.bitmap, lmnakhf.get(i).place.x-lmnkhof.bitmap.getWidth()/2, lmnakhf.get(i).place.y-lmnkhof.bitmap.getWidth()/2, paint);

            if(retard.alert)
            {
                canvas.drawBitmap(retard.bitmap, retard.m, paint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }



    // functions left menu
    void passerRole() {
        Thread role = new Thread(new Runnable() { int i=0; Integer clrs[]={R.drawable.radpion1,R.drawable.radpion2};
            @Override public void run() {
            roleColor = true;
            while (roleColor)
            {
                ((Activity)getContext()).runOnUiThread(new Runnable() {public void run() {
                    if(myTurn)
                    {
                        (((Activity)getContext()).findViewById(R.id.id_img1) ).setBackgroundResource(clrs[i]);
                        (((Activity)getContext()).findViewById(R.id.id_img2) ).setBackgroundResource(R.drawable.radpion1);
                        //(((Activity)getContext()).findViewById(R.id.cadre1) ).setScaleX(i);
                        //(((Activity)getContext()).findViewById(R.id.cadre1) ).setScaleY(i);
                    }else
                    {
                        (((Activity)getContext()).findViewById(R.id.id_img2) ).setBackgroundResource(clrs[i]);
                        (((Activity)getContext()).findViewById(R.id.id_img1) ).setBackgroundResource(R.drawable.radpion1);
                        //(((Activity)getContext()).findViewById(R.id.cadre2) ).setScaleX(i);
                        //(((Activity)getContext()).findViewById(R.id.cadre2) ).setScaleY(i);
                    }
                    i++;
                    if(i==2) i = 0;
                }});
                try { Thread.sleep(900); }catch (Exception e){}
            }
        }});
        role.start();
    }


    // functions right menu
    public boolean reset() {
        lmnakhf.clear();
        if( !historique.isVide() )
        {
            Pion pion   = historique.getPion('a');
            Place place = historique.getPlace('a');
            Boolean b   = historique.getIsDem();

            if(pion!=null && place!=null && b!=null) {
                if (pion.isDem && !b)
                    pion.bitmap = (pion.id <= (nPion)) ? player1.bitmap : player2.bitmap;
                pion.isDem = b;
                plateau.place[pion.place.i][pion.place.j].isVide = true;
                plateau.place[pion.place.i][pion.place.j].pion = null;
                pion.place = place;
                plateau.place[place.i][place.j].isVide = false;
                plateau.place[place.i][place.j].pion = pion;
                plateau.pionActive = pion;
            }

            // pion mangé
            Pion pion2    = historique.getPion('m');
            place   = historique.getPlace('m');
            if( pion2!=null )
            {
                pion2.place = place;
                plateau.place[place.i][place.j].isVide = false;
                plateau.place[place.i][place.j].pion = pion2;
                //if(pion!=null)
                {
                    if (pion2.id <= nPion) {
                        player1.score++;
                        ((TextView) ((Activity)getContext()).findViewById(R.id.scorePion1)).setText(("" + player1.score));
                    } else {
                        player2.score++;
                        ((TextView) ((Activity)getContext()).findViewById(R.id.scorePion2)).setText(("" + player2.score));
                    }
                }
            }

            // passer le role
            if(pion!=null) {
                myTurn = (plateau.pionActive.id <= nPion);
                placeVide = plateau.cherchePlaceVide(myTurn);
            }else reset();

            return true;
        }
        return false;
    }
    public void resetAll(){
            historique.reset();
            player2.score = player1.score = nPion;
            ((TextView)((Activity)getContext()).findViewById(R.id.scorePion1)).setText("" + nPion);
            ((TextView)((Activity)getContext()).findViewById(R.id.scorePion2)).setText("" + nPion);
            plateau.reset();
            init();
    }






    // tools *******************************************************
    int xToPercent(int x){
        if(x<=0) return 0;
        if(x>100) x=100;
        x = (int)(wScreen/ (100/(float)x) );
        return x;
    }
    int yToPercent(int y){
        if(y<=0) return 0;
        if(y>100) y=100;
        y = (wScreen/ (100/y) );
        return y;
    }
    public static Bitmap creerBitmap(Context context, Integer im, int w, int h) {
        return Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(context.getResources(),
                        im ),
                w,
                h,
                false
        );
    }
    boolean isCollition(float x, float y, int w, int h) {
        return  x < detectTouch[0] &&
                x + w > detectTouch[0] &&
                y < detectTouch[1] &&
                y + h > detectTouch[1] ;
    }


    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            isTouch = true;
            detectTouch[0] = (int)event.getX();
            detectTouch[1] = (int)event.getY();
        }
        return super.onTouchEvent(event);
    }



    public void detruire() {
        roleColor = false;
        retard.detruire();
        bg = null;
        sound.detruire();
        signalPionActive = null;
        signalPlaceVide = null;
        detectTouch = null;
        paint = null;
        canvas = null;
        surfaceHolder = null;
        placeVide = null;

        player1.bitmap.recycle();
        player1.bitmap = null;
        player1.dem.recycle();
        player1.dem = null;

        player2.bitmap.recycle();
        player2.bitmap = null;
        player2.dem.recycle();
        player2.dem = null;

        player1.pions = null;
        player2.pions = null;
    }


}












