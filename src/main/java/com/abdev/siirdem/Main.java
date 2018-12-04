package com.abdev.siirdem;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;


public class Main extends AppCompatActivity implements View.OnClickListener
{

    private AdView mAdView1, mAdView2;
    private InterstitialAd mInterstitialAd;

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
        setContentView(R.layout.main);

        (findViewById(R.id.id_btnLocal)).setOnClickListener(this);
        (findViewById(R.id.id_btnOnline)).setOnClickListener(this);
        (findViewById(R.id.id_btnShare)).setOnClickListener(this);
        (findViewById(R.id.myName)).setOnKeyListener(new View.OnKeyListener() {
            @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
                v.setBackgroundResource( R.drawable.edit2 ); return false;
            }
        });

        (findViewById(R.id.id_btnShare)).setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN) v.setBackgroundResource(R.drawable.btn_hover);
                else if(event.getAction()==MotionEvent.ACTION_UP) v.setBackgroundResource( R.drawable.button );
                return false;
            }
        });

        (findViewById(R.id.id_btnLocal)).setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
               // if(event.getAction()==MotionEvent.ACTION_DOWN) v.setBackgroundResource(R.drawable.pion1);
               // else if(event.getAction()==MotionEvent.ACTION_UP) v.setBackgroundResource( R.drawable.pion2 );
                return false;
            }
        });

        (findViewById(R.id.id_btnOnline)).setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
               // if(event.getAction()==MotionEvent.ACTION_DOWN) v.setBackgroundResource(R.drawable.pion2);
               // else if(event.getAction()==MotionEvent.ACTION_UP) v.setBackgroundResource( R.drawable.pion1 );
                return false;
            }
        });

        Music.play(this);
        (findViewById(R.id.id_btnMusic)).setOnClickListener(this);

        MobileAds.initialize(this, getString(R.string.admob_app_id));


        mAdView1 = findViewById(R.id.adView1);
        mAdView2 = findViewById(R.id.adView2);

        AdRequest adRequest1 = new AdRequest.Builder().build();
        AdRequest adRequest2 = new AdRequest.Builder().build();
        mAdView1.loadAd(adRequest1);
        mAdView2.loadAd(adRequest2);


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId( getString(R.string.admob_interstitial_id) );
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }


    @Override public void onClick(View v)
    {
        if(v.getId()==R.id.id_btnShare)
        {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.abdev.siirdem");
            startActivity(Intent.createChooser(shareIntent, "Share..."));
            return;
        }
        else if(v.getId() == R.id.id_btnLocal)
        {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            else {
                Intent intent = new Intent(this, Activite.class);
                intent.putExtra("online", "false");

                startActivity(intent);
            }
            return;
        }
        else if(v.getId()==R.id.id_btnMusic)
        {
            Music.toggle(this);
            return;
        }

        String myName = ((EditText)findViewById(R.id.myName)).getText().toString().trim();
        if (myName.isEmpty()) {
            (findViewById(R.id.myName)).setBackgroundColor(Color.parseColor("#AAFF0000"));
            return;
        }

        if(v.getId() == R.id.id_btnOnline)
        {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            else {
                if (DamaOnline.isNetworkConnected(this)) {
                    Intent intent = new Intent(this, MainOnline.class);
                    intent.putExtra("myName", myName);
                    Music.detruire();
                    startActivity(intent);
                } else {
                    Modal modal = new Modal(this) {
                        @Override
                        public void onClick(View v) {
                            if (v.getId() == no.getId())
                                dismiss();
                        }
                    };
                    modal.clear();
                    modal.show();
                    modal.setTitle("Connection failed");
                    modal.setMessage("Internet connection is not available !");
                    modal.no.setText("Cancel");
                    modal.yes.setVisibility(View.INVISIBLE);
                }
            }
        }
    }




    @Override public void onDestroy()
    {
        Music.detruire();
        super.onDestroy();
    }
    @Override public void onPause()
    {
        super.onPause();
    }
    @Override public void onResume(){
        super.onResume();
    }

}


