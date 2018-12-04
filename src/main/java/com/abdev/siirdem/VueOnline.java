package com.abdev.siirdem;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


public class VueOnline extends Vue
{

    private DamaOnline damaOnline=null;
    Synchronisation    synchronisation;

    ValueEventListener listenDeplacements = null;
    ValueEventListener listenReset = null;
    ValueEventListener listentDisconnect = null;
    ValueEventListener listenRetard = null;
    ValueEventListener listenMessage=null;



    public VueOnline(Context context, int nPion, int ws, int hs, Historique historique, DamaOnline damaOnline)
    {
        super(context, nPion, ws, hs, historique);
        this.damaOnline = damaOnline;
        damaOnline.fire.save(damaOnline.me);
        damaOnline.fire.save(damaOnline.adv);

        synchronisation = new Synchronisation(damaOnline);
        synchronisation.synchronize();

        listenValuesOnServer();
    }


    @Override public void update()
    {
        // deplacement *********************************************************************
        if(plateau.pionActive!=null && aDeplacer != null) {
            deplacer();
            if(online)
            {
                aDeplacer           = null;
                plateau.pionActive  = null;
                online = false;
                myTurn = Boolean.valueOf(damaOnline.me.deplacement.signal);
                damaOnline.deplacementOk();
            }
        }


        // detect pion selected ************************************************************
        if(myTurn && isTouch)
        {
            int wh = player1.bitmap.getWidth();

            if(!lmnakhf.isEmpty())
            {
                for (int i = 0; i < lmnakhf.size(); i++)
                    if (isCollition(lmnakhf.get(i).place.x-wh/2, lmnakhf.get(i).place.y-wh/2,wh, wh)) {
                        synchronisation.put(lmnakhf.get(i).id,-1, false);
                        historique.stockerPionActive(null,null);
                        historique.stockerPionMange(lmnakhf.get(i), plateau.place[lmnakhf.get(i).place.i][lmnakhf.get(i).place.j]);
                        plateau.place[lmnakhf.get(i).place.i][lmnakhf.get(i).place.j].isVide = true;
                        plateau.place[lmnakhf.get(i).place.i][lmnakhf.get(i).place.j].pion = null;
                        lmnakhf.get(i).place = new Place(-1, -100, 0, -1, -1);
                        testIfGagne();
                        lmnakhf.clear();
                        break;
                    }
                return;
            }

            // detect the pion selected
            for (int i = 0; i < nPion; i++)
                if (isCollition(player1.pions[i].place.x-wh/2, player1.pions[i].place.y-wh/2,wh, wh)) {
                    plateau.pionActive = player1.pions[i];
                    placeVide = plateau.cherchePlaceVide(true);
                    break;
                }

            // deplacement
            if(plateau.pionActive!=null && placeVide!=null)
            {
                wh = signalPlaceVide.getWidth();
                for(Place p : placeVide)
                    if (isCollition(p.x-wh/2, p.y-wh/2,wh,wh)) {
                        aDeplacer = p;
                        enMouvement = true;
                        plateau.pionActive.place = new Place(plateau.pionActive.place);
                    }
            }

            isTouch = false;
        }
    }


    @Override public void deplacer()
    {
        if(lmnkhof!=null) lmnkhof.playing = false;

        sound.move();

        Place tmp      = plateau.pionActive.place;

        // historique
        historique.stockerPionActive(plateau.pionActive, plateau.place[tmp.i][tmp.j]);


        lmnakhf.clear();
        Pion tmpActive = plateau.pionActive;
        for(int i=0;i<nPion;i++)
        {
            plateau.pionActive = myTurn?player1.pions[i]:player2.pions[i];
            if(historique.lastPionsManger.contains(plateau.pionActive)) continue;
            if(plateau.chercherPionAmanger(myTurn) != null)
                lmnakhf.add(plateau.pionActive);
        }
        plateau.pionActive =  tmpActive;


        // deplacement
        plateau.place[tmp.i][tmp.j].isVide = true;
        plateau.place[tmp.i][tmp.j].pion = null;
        plateau.pionActive.place = aDeplacer;
        aDeplacer.pion = plateau.pionActive;
        aDeplacer.isVide = false;

        // test if dem
        if (!plateau.pionActive.isDem && plateau.pionActive.setIfDem(myTurn ? dimension : 1))
            plateau.pionActive.bitmap = myTurn ? player1.dem : player2.dem;


        // get pion a manger !!!
        if ( (tmp=plateau.getPionAmanger(tmp.i, tmp.j)) != null)
        {
            sound.eat();
            historique.stockerPionMange(tmp.pion, plateau.place[tmp.i][tmp.j]);

            tmp.pion.place = new Place(-1, -100, 0, -1, -1);
            tmp.isVide = true;
            tmp.pion = null;

            testIfGagne();

            // autre pion a manger !!!!!
            if(myTurn)
            {
                placeVide = plateau.chercherPionAmanger(myTurn);
                if (placeVide == null)
                {
                    synchronisation.put(plateau.pionActive.id, aDeplacer.num, true);
                    plateau.pionActive = null;
                    myTurn = false;
                }
                else synchronisation.put(plateau.pionActive.id, aDeplacer.num, false);
            }

            if(!lmnakhf.isEmpty()) lmnakhf.clear();
        }

        else // aucun pion a manger !
        {
            if(!lmnakhf.isEmpty())
            {
                lmnkhof = new Haha(context, plateau.pionActive.place.x, plateau.pionActive.place.y, (xToPercent(6)+yToPercent(6))/2 );
                thread = new Thread(lmnkhof);
                thread.start();
                sound.lose();
            }

            if(myTurn)
            {
                synchronisation.put(plateau.pionActive.id, aDeplacer.num, true);
                myTurn = false;
            }

            historique.stockerPionMange(null, null);
            plateau.pionActive = null;
            placeVide = null;
        }

        aDeplacer = null;
    }


    //online
    public void listenValuesOnServer()
    {
        listenDisconnect();
        listenDeplacement();
        listenResetRestart();
        listenRetard();
        listenMessage();
    }

    public void listenDeplacement()  {
        damaOnline.fire.db.child("players").child(damaOnline.me.id).child("deplacement")
                .addValueEventListener(
                        listenDeplacements = new ValueEventListener()
                        {
                            @Override public void onDataChange(final DataSnapshot dataSnapshot)
                            {
                                String pionActive     = dataSnapshot.child("pionActive").getValue(String.class);
                                String placeAdeplacer = dataSnapshot.child("placeAdeplacer").getValue(String.class);
                                String turn           = dataSnapshot.child("signal").getValue(String.class);
                                damaOnline.me.deplacement.signal = turn;

                                if(placeAdeplacer.equals("0") || pionActive.equals("0")){
                                    myTurn = Boolean.valueOf(turn);
                                    return;
                                }

                                if( placeAdeplacer.equals("-10") ) return;

                                myTurn = false;

                                if( placeAdeplacer.equals("-1") )
                                {
                                    for (int i = 0; i < nPion; i++)
                                        if (player1.pions[i].id == (Integer.parseInt(pionActive)) - nPion)
                                        {
                                            historique.stockerPionActive(null,null);
                                            historique.stockerPionMange(player1.pions[i], plateau.place[player1.pions[i].place.i][player1.pions[i].place.j]);
                                            plateau.place[player1.pions[i].place.i][player1.pions[i].place.j].isVide = true;
                                            plateau.place[player1.pions[i].place.i][player1.pions[i].place.j].pion = null;
                                            player1.pions[i].place = new Place(-1, -100, 0, -1, -1);
                                            testIfGagne();
                                            lmnakhf.clear();
                                            damaOnline.deplacementOk();
                                            return;
                                        }
                                }


                                // detect
                                if(Integer.parseInt(pionActive)>0 )
                                {
                                    plateau.pionActive = null;
                                    for (int i = 0; i < nPion; i++)
                                        if (player2.pions[i].id == (Integer.parseInt(pionActive)) + nPion)
                                        {
                                            plateau.pionActive = player2.pions[i];
                                            break;
                                        }
                                }

                                if( plateau.pionActive!=null )
                                {
                                    int num = Integer.parseInt(placeAdeplacer);
                                    if (num > 0)
                                    {
                                        for (int i = 0; i < dimension; i++)
                                            for (int j = 0; j < dimension; j++)
                                                if( plateau.place[i][j].num == damaOnline.corespondance.get(num-1) ){
                                                    aDeplacer = plateau.place[i][j];
                                                    enMouvement = true;
                                                    online = true;
                                                    plateau.pionActive.place = new Place(plateau.pionActive.place);
                                                    return;
                                                }
                                    }
                                }

                                myTurn = Boolean.valueOf(turn);;
                            }
                            @Override public void onCancelled(DatabaseError error) {}
                        });
    }
    public void listenResetRestart() {
        damaOnline.fire.db.child("players").child(damaOnline.me.id).child("reset")
                .addValueEventListener(
                        listenReset = new ValueEventListener()
                        {
                            @Override public void onDataChange(final DataSnapshot dataSnapshot)
                            {
                                final String reset = dataSnapshot.getValue(String.class);

                                if( reset.equals("0") ) return;

                                if( reset.equals("1") || reset.equals("2") )
                                {
                                    Modal m = new Modal((Activity)getContext()) {
                                        @Override public void onClick(View v)
                                        {
                                            switch (v.getId())
                                            {
                                                case R.id.btn_no :
                                                    if(reset.equals("1")) damaOnline.sendReset("-1");
                                                    else damaOnline.sendReset("-2");
                                                    dismiss();
                                                    break;

                                                case R.id.btn_yes :
                                                    dismiss();
                                                    if(reset.equals("1")) {
                                                        damaOnline.sendReset("11");
                                                        reset();
                                                    }
                                                    else {
                                                        damaOnline.sendReset("22");
                                                        resetAll();
                                                    }
                                            }
                                        }
                                    };
                                    m.clear();
                                    m.show();
                                    m.setTitle( reset.equals("1")?"Undo...":"Restart..." );
                                    m.setMessage(damaOnline.adv.nom+" want "+ (reset.equals("1")?"undo":"restart") +" !\nAgree ?");
                                    m.yes.setText("Agree");
                                    m.no.setText("Cancel");
                                }

                                else if(reset.equals("-1") || reset.equals("-2"))
                                {
                                    Modal m = new Modal((Activity)getContext()) {
                                        @Override public void onClick(View v)
                                        {
                                            switch (v.getId())
                                            {
                                                case R.id.btn_no :
                                                    dismiss();
                                                    break;
                                            }
                                        }
                                    };
                                    m.clear();
                                    m.show();
                                    m.setTitle( reset.equals("-1")?"Undo...":"Restart..." );
                                    m.setMessage( reset.equals("-1")?"undo not accepted !":"restart not accepted !" );
                                    m.yes.setText("");
                                    m.yes.setVisibility(View.INVISIBLE);
                                    m.no.setText( "Cancel" );
                                }

                                else if(reset.equals("11")) {
                                    Toast.makeText(context,"Undo ok",Toast.LENGTH_LONG).show();
                                    reset();
                                }

                                else if(reset.equals("22")) {
                                    Toast.makeText(context,"Restart ok",Toast.LENGTH_LONG).show();
                                    resetAll();
                                }

                                damaOnline.initReset();
                            }
                            @Override public void onCancelled(DatabaseError error) {}
                        });
    }
    public void listenDisconnect()   {
        damaOnline.fire.db.child("players").child(damaOnline.me.id).child("advirsaire")
                .addValueEventListener(
                        listentDisconnect = new ValueEventListener()
                        {
                            @Override public void onDataChange(final DataSnapshot dataSnapshot)
                            {
                                if( dataSnapshot.getValue(String.class).equals("-4") )
                                {
                                    damaOnline.me.advirsaire = "-4";
                                    Modal m = new Modal((Activity)getContext()) {
                                        @Override public void onClick(View v)
                                        {
                                            switch (v.getId())
                                            {
                                                case R.id.btn_no :
                                                    dismiss();
                                                    ((Activity) context).finish();
                                                    break;
                                            }
                                        }
                                    };
                                    m.clear();
                                    m.show();
                                    m.setTitle("");
                                    m.setMessage("Enemy disconnected");
                                    m.yes.setText("");
                                    m.yes.setVisibility(View.INVISIBLE);
                                    m.no.setText("Cancel");
                                }
                            }
                            @Override public void onCancelled(DatabaseError error) {}
                        });
    }
    public void listenRetard()       {
    damaOnline.fire.db.child("players").child(damaOnline.me.id).child("retard")
            .addValueEventListener(
                    listenRetard = new ValueEventListener()
                    {
                        @Override public void onDataChange(final DataSnapshot dataSnapshot)
                        {
                            if( Boolean.valueOf(dataSnapshot.getValue(String.class)) )
                            {
                                retard.alert();
                                ((Activity)context).runOnUiThread(new Runnable() {public void run() {
                                    new Handler().postDelayed(new Runnable() {@Override public void run() {
                                        sound.soundAlert.stop(sound.streamAlert);
                                        retard.bThread = false;
                                        retard.alert   = false;
                                        retard.stop();
                                    }}, 5000);
                                }});

                                damaOnline.fire.db.child("players").child(damaOnline.me.id).child("retard")
                                        .setValue("false");
                            }
                        }
                        @Override public void onCancelled(DatabaseError error) {}
                    });
    }
    public void listenMessage()      {
        damaOnline.fire.db.child("players").child(damaOnline.me.id).child("message")
                .addValueEventListener(
                        listenMessage = new ValueEventListener()
                        {
                            @Override public void onDataChange(final DataSnapshot dataSnapshot)
                            {
                                String tmp = dataSnapshot.getValue(String.class);
                                if( tmp.length() > 0 )
                                {
                                    Modal modalMessage = new Modal((Activity) context)
                                    {
                                        @Override public void onClick(View v)
                                        {
                                            switch (v.getId())
                                            {
                                                case R.id.btn_yes:
                                                    if(damaOnline.adv != null)
                                                        damaOnline.fire.db.child("players")
                                                                .child(damaOnline.adv.id)
                                                                .child("message")
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
                                    modalMessage.setMessage(damaOnline.adv.nom+" : "+tmp);
                                    ((LinearLayout)modalMessage.findViewById(R.id.idModalContent))
                                            .addView(modalMessage.editText);
                                    modalMessage.yes.setText("Reply");
                                    modalMessage.no.setText("Cancel");
                                    modalMessage.message.setGravity(Gravity.LEFT);
                                    damaOnline.fire.db.child("players").child(damaOnline.me.id).child("message")
                                            .setValue("");
                                }
                            }
                            @Override public void onCancelled(DatabaseError error) {}
                        });
    }






    @Override public void detruire()
    {
        synchronisation.detruire();

        damaOnline.fire.db.child("players").child(damaOnline.me.id).child("advirsaire")
                .removeEventListener(listentDisconnect);

        damaOnline.fire.db.child("players").child(damaOnline.me.id).child("reset")
                .removeEventListener(listenReset);

        damaOnline.fire.db.child("players").child(damaOnline.me.id).child("deplacement")
                .removeEventListener(listenDeplacements);

        damaOnline.fire.db.child("players").child(damaOnline.me.id).child("retard")
                .removeEventListener(listenRetard);

        damaOnline.fire.db.child("players").child(damaOnline.me.id).child("message")
                .removeEventListener(listenMessage);
    }


}









