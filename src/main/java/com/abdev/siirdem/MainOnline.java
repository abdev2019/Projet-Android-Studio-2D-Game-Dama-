package com.abdev.siirdem;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.FirebaseDatabase;


public class MainOnline extends AppCompatActivity implements View.OnClickListener
{
    FireBaseHelper  fire;
    int             posIdAdv=-1;
    DamaOnline      damaOnline=null;
    View            last=null;

    private AdView mAdView3;
    private InterstitialAd mInterstitialAd;

    // ok
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
        setContentView(R.layout.activity_mainonline);


        damaOnline = new DamaOnline(this);


        damaOnline.connecter( getIntent().getStringExtra("myName") );

        fire        = new FireBaseHelper(FirebaseDatabase.getInstance().getReference(), this);
        fire.myId = damaOnline.me.id;
        fire.blockedList = damaOnline.listBloque;
        fire.loadPlayers();


        // update views
        (findViewById(R.id.id_btnInvite1)).setOnClickListener(this);
        (findViewById(R.id.id_btnInvite2)).setOnClickListener(this);
        (findViewById(R.id.id_btnMain)).setOnClickListener(this);
        (findViewById(R.id.id_btnCopy)).setOnClickListener(this);
        ((ListView)findViewById(R.id.les_players)).setOnItemClickListener( new AdapterView.OnItemClickListener() { @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) { posIdAdv = position; view.setBackgroundColor(Color.parseColor("#BB0000FF")); if(last != null) last.setBackgroundColor(Color.TRANSPARENT); last = view; } });
        ((TextView)findViewById(R.id.myNameOnline)).setText( ("Hi "+getIntent().getStringExtra("myName")+" !") );
        ((TextView)findViewById(R.id.myCode)).setText( damaOnline.me.id );
        (findViewById(R.id.codeEnemy)).setOnKeyListener(new View.OnKeyListener() {@Override public boolean onKey(View v, int keyCode, KeyEvent event) { v.setBackgroundColor( Color.parseColor("#55FFFFFF") ); return false; } });

        // hover
        View.OnTouchListener onTouchListener = new View.OnTouchListener() { @Override public boolean onTouch(View v, MotionEvent event) { if(event.getAction()==MotionEvent.ACTION_DOWN) v.setBackgroundResource(R.drawable.btn_hover); else if(event.getAction()==MotionEvent.ACTION_UP) v.setBackgroundResource( R.drawable.button ); return false; } };
        (findViewById(R.id.id_btnInvite1)).setOnTouchListener(onTouchListener);
        (findViewById(R.id.id_btnInvite2)).setOnTouchListener(onTouchListener);
        (findViewById(R.id.id_btnMain)).setOnTouchListener(onTouchListener);

        (findViewById(R.id.id_btnMusic)).setOnClickListener(this);

        Music.play(this);

        MobileAds.initialize(this, getString(R.string.admob_app_id));

        mAdView3 = findViewById(R.id.adView3);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView3.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.admob_interstitial_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }



    // ok
    @Override public void onClick(View v)
    {
        if(v.getId() == R.id.id_btnInvite2)
        {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            else {
                if (fire.players.size() < 1) {
                    Toast.makeText(this, "No online player !", Toast.LENGTH_LONG).show();
                    return;
                } else if (posIdAdv < 0) {
                    Toast.makeText(this, "Please select enemy !", Toast.LENGTH_LONG).show();
                    return;
                }
                damaOnline.inviteAdvirsaire(fire.players.get(posIdAdv).id);
                last.setBackgroundColor(Color.TRANSPARENT);
                posIdAdv = -1;
                last = null;
            }
        } else

        if( v.getId()==R.id.id_btnInvite1 )
        {
            String idAdv = ((EditText)findViewById(R.id.codeEnemy)).getText().toString().trim();
            if (idAdv.isEmpty() || idAdv.length()<6);
            else
            for(PlayerOnline p : fire.players)
            {
                if( idAdv.equals(p.id) )
                {
                    damaOnline.inviteAdvirsaire( idAdv );
                    last.setBackgroundColor(Color.TRANSPARENT);
                    posIdAdv = -1;
                    last =null;
                    return;
                }
            }
            Toast.makeText(this,"Wrong code !",Toast.LENGTH_LONG).show();
            (findViewById(R.id.codeEnemy)).setBackgroundColor(Color.parseColor("#AAFF0000"));
        } else

        if( v.getId()==R.id.id_btnCopy )
        {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            else {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Copy", damaOnline.me.id + "");
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(this, "Copied !", Toast.LENGTH_LONG).show();
            }
        } else

        if(v.getId()==R.id.id_btnMusic)
        {
            Music.toggle(this);
        }else

        if(v.getId() == R.id.id_btnMain){
            damaOnline.deconnecter();
            finish();
        }
    }


    // ok
    @Override public void onDestroy() {
        Music.detruire();
        super.onDestroy();
    }

    @Override public void onPause() {
        super.onPause();
    }
    @Override public void onResume() {
        super.onResume();
    }


    // ok
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ( (keyCode == KeyEvent.KEYCODE_BACK) )
        {
            if(damaOnline!=null) damaOnline.deconnecter();
        }
        else if( (keyCode == KeyEvent.KEYCODE_HOME) )
        {
            if(damaOnline!=null) damaOnline.deconnecter();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }




    @Override public void onActivityResult(int req, int res, Intent data)
    {
        super.onActivityResult(req, res, data);

        if(damaOnline == null) return;
        if(damaOnline.me == null) return;

        if( !damaOnline.me.id.equals(data.getStringExtra("myId")) )
        {

            if(damaOnline.listenValues != null)
                fire.db.child("players").child(damaOnline.me.id).child("advirsaire")
                        .removeEventListener(damaOnline.listenValues);
            fire.db.child("players").child(damaOnline.me.id).setValue(null);
            ((TextView)findViewById(R.id.myCode)).setText( data.getStringExtra("myId") );
            damaOnline.reconnecter(data.getStringExtra("myId"));
            if(damaOnline.waitingAccept!=null) damaOnline.waitingAccept.dismiss();
        }
        else{
            damaOnline.waitAdvirsaire();
            if(damaOnline.waitingAccept!=null) damaOnline.waitingAccept.dismiss();
        }
    }



}


















