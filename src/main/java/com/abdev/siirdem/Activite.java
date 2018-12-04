package com.abdev.siirdem;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class Activite extends AppCompatActivity implements View.OnClickListener
{
    // objects game
    int n = 5,  nPion=((n*n-1)/2);
    Historique  historique;
    DamaOnline  damaOnline=null;
    Vue vue;

    Intent intent;


    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Point size = new Point();
        (getWindowManager().getDefaultDisplay()).getSize(size);
        setContentView(R.layout.activity_playing);
        MediaPlayer music = MediaPlayer.create( getApplicationContext(), R.raw.start); music.start();


        // historique
        historique = new Historique();

        if( Boolean.valueOf( getIntent().getStringExtra("online") ) ) {
            damaOnline = new DamaOnline(this);
            damaOnline.initCorespandance(nPion);
            damaOnline.me = new PlayerOnline(getIntent().getStringExtra("idAdv"), getIntent().getStringExtra("myName"), new Deplacement(), "0", "false", "");
            damaOnline.adv = new PlayerOnline(getIntent().getStringExtra("myId"), getIntent().getStringExtra("nameAdv"), new Deplacement("true") ,   "0", "false","");
            damaOnline.me.id = getIntent().getStringExtra("myId");
            damaOnline.adv.id = getIntent().getStringExtra("idAdv");

            intent = new Intent();
            intent.putExtra("myId", damaOnline.me.id+"");
            intent.putExtra("reconnect", "true");
            setResult(Activity.RESULT_OK, intent);

            findViewById(R.id.id_btnMessage).setVisibility(View.VISIBLE);
            findViewById(R.id.id_btnMessage).setOnClickListener(this);

            vue = new VueOnline(this, n, (int) (size.x / (100 / (float) 85)), size.y, historique, damaOnline);
        }
        else vue = new VueLocal(this, n, (int) (size.x / (100 / (float) 85)), size.y, historique);

        ((TextView)findViewById(R.id.scorePion1)).setText(""+nPion );
        ((TextView)findViewById(R.id.scorePion2)).setText(""+nPion);

        findViewById(R.id.id_btnRestart).setOnClickListener(this);
        findViewById(R.id.id_btnReset).setOnClickListener(this);
        findViewById(R.id.id_btnMain).setOnClickListener(this);
        findViewById(R.id.id_btnAlert).setOnClickListener(this);

        ((LinearLayout)findViewById(R.id.main_jeu)).addView(vue);

        Music.play(this);
        (findViewById(R.id.id_btnMusic)).setOnClickListener(this);
    }


    @Override public void onClick(View v)
    {
        switch ( v.getId() )
        {
            case R.id.id_btnMain    : finish(); break;


            case R.id.id_btnReset   :
            {
                if(damaOnline != null) {
                    damaOnline.sendReset("1");
                    Toast.makeText(getApplicationContext(),"Wait...", Toast.LENGTH_LONG).show();
                }
                else vue.reset();
                break;
            }


            case R.id.id_btnRestart :
            {
                if(damaOnline != null) {
                    damaOnline.sendReset("2");
                    Toast.makeText(getApplicationContext(),"Wait...", Toast.LENGTH_LONG).show();
                }
                else vue.resetAll();
                break;
            }


            case R.id.id_btnAlert:
            {
                final ImageButton btn = (ImageButton)findViewById(R.id.id_btnAlert);
                btn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alert));
                new Handler().postDelayed(new Runnable() {@Override public void run() { btn.setAnimation(null); }}, 1500);
                if(damaOnline!=null) damaOnline.alert();
                else {
                    if(vue.retard.actif) {
                        findViewById(R.id.id_btnAlert).setBackgroundResource(R.drawable.stopalarm);
                        vue.retard.bThread = false;
                        vue.retard.alert = false;
                        vue.retard.sound.soundAlert.stop(vue.retard.sound.streamAlert);
                        vue.retard.thread = null;
                        vue.retard.stop();
                    }
                    else findViewById(R.id.id_btnAlert).setBackgroundResource(R.drawable.alarm);
                    vue.retard.actif = !vue.retard.actif;
                }
                break;
            }


            case R.id.id_btnMessage :
            {
                Modal modalMessage = new Modal(this) {
                        @Override public void onClick(View v)
                        {
                            switch (v.getId())
                            {
                                case R.id.btn_yes:
                                    if(damaOnline.adv != null)
                                    damaOnline.fire.db.child("players").child(damaOnline.adv.id).child("message")
                                            .setValue(""+editText.getText());
                                    dismiss();
                                    break;

                                case R.id.btn_no :
                                    dismiss();
                                    break;
                            }
                        }
                };

                modalMessage.editText.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                modalMessage.clear();
                modalMessage.show();
                modalMessage.setTitle("Message");
                modalMessage.setMessage("Write your msg :");
                ((LinearLayout)modalMessage.findViewById(R.id.idModalContent))
                        .addView(modalMessage.editText);
                modalMessage.yes.setText("Send");
                modalMessage.no.setText("Cancel");
                break;
            }


            case R.id.id_btnMusic :
            {
                Music.toggle(this);
            }
        }
    }

    @Override protected void onDestroy(){
        if(damaOnline != null)
            damaOnline.finishGame();
        detruire();

        super.onDestroy();
        System.gc();
    }

    @Override protected void onPause() {
        if(damaOnline!=null)
            damaOnline.finishGame();
        super.onPause();
        vue.pause();
    }
    @Override protected void onResume() {
        super.onResume();
        vue.resume();
    }


    void detruire() {
        Music.detruire();
        vue.detruire();
        historique.detruire();
        historique = null;
        vue        = null;
        if(damaOnline!=null) damaOnline = null;
    }

}
















