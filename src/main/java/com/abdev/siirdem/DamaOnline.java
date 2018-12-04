package com.abdev.siirdem;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


//  0 : no enemy
// -1 : enemy refused
// -2 : enemy is occuped
// -3 : enemy close invitation
// -4 : enemy deconnected


public class DamaOnline{

    // online ******************************
    Context context;
    FireBaseHelper fire;
    PlayerOnline me, adv, playerAdv;
    public ArrayList<Integer> corespondance ;
    ValueEventListener listenValues=null;
    ValueEventListener listenAccept=null;

    Modal waitingAccept=null;

    public ArrayList<String> listBloque=null;




    public DamaOnline(Context c)
    {
        fire = new FireBaseHelper(FirebaseDatabase.getInstance().getReference(), (Activity) c);
        adv = null;
        me = null;
        context = c;
        corespondance = new ArrayList<>();
        listBloque = new ArrayList<>();
    }


    public void initCorespandance(int nPion)
    {
        for(int i=nPion*2+1; i>0; i--)
            corespondance.add(i) ;
    }


    public void connecter(String nom)
    {
        me = new PlayerOnline("0",nom,new Deplacement(), "0", "false", "");
        me.id =  fire.newIdPlayer();
        fire.save(me);
        waitAdvirsaire();
    }


    public void waitAdvirsaire()
    {
        fire.db.child("players").child(me.id).child("advirsaire").addValueEventListener( listenValues = new ValueEventListener()
        {
            @Override public void onDataChange(DataSnapshot dataSnapshot)
            {
                final String idAdv = dataSnapshot.getValue(String.class);

                try {
                    int val = Integer.parseInt( idAdv );
                    if( val == -3 )  {
                        if(waitingAccept != null) waitingAccept.dismiss();
                        fire.db.child("players").child(me.id).child("advirsaire").setValue("0");
                    }
                    return;
                }
                catch (Exception e) {}

                fire.db.child("players").child(idAdv).addListenerForSingleValueEvent( new ValueEventListener()
                {
                    @Override public void onDataChange(DataSnapshot snapshot)
                    {
                        if (    snapshot.exists()  &&
                                snapshot.hasChild("id") &&
                                snapshot.hasChild("advirsaire") &&
                                snapshot.hasChild("nom" ) &&
                                snapshot.hasChild("deplacement" )
                        ){
                            playerAdv = snapshot.getValue(PlayerOnline.class);
                            if( listBloque!=null && listBloque.contains(idAdv) ){
                                playerAdv.advirsaire = "-1";
                                me.advirsaire = "0";
                                fire.save(playerAdv);
                                fire.save(me);
                                return;
                            }

                            if(waitingAccept != null) waitingAccept.dismiss();
                            waitingAccept = new Modal((Activity) context) {
                                @Override public void onClick(View v) {
                                    switch (v.getId())
                                    {
                                        case R.id.btn_yes:
                                            fire.db.child("players").child(me.id).child("advirsaire")
                                                    .removeEventListener(listenValues);
                                            playerAdv.advirsaire = ""+me.id;
                                            fire.save(playerAdv);
                                            dismiss();
                                            Intent intent = new Intent(context, Activite.class);
                                            intent.putExtra("online","true");
                                            intent.putExtra("idAdv",playerAdv.id);
                                            intent.putExtra("myId",me.id);
                                            intent.putExtra("myName",me.nom);
                                            intent.putExtra("nameAdv",playerAdv.nom);
                                            Music.detruire();
                                            ((Activity) context).startActivityForResult(intent,0);
                                            return;


                                        case R.id.btn_no:
                                            playerAdv.advirsaire = "-1";
                                            me.advirsaire = "0";
                                            fire.save(playerAdv);
                                            fire.save(me);
                                            dismiss();
                                            break;

                                        case R.id.id_btnBloquer:
                                            listBloque.add(playerAdv.id);
                                            playerAdv.advirsaire = "-1";
                                            me.advirsaire = "0";
                                            fire.blockedList = listBloque;
                                            fire.save(playerAdv);
                                            fire.save(me);
                                            dismiss();
                                            break;
                                    }
                                }
                            };
                            waitingAccept.clear();
                            waitingAccept.show();
                            final float scale = context.getResources().getDisplayMetrics().density;
                            LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(
                                    (int) (60 * scale + 0.5f)
                                    ,(int) (40 * scale + 0.5f)
                            );
                            l.gravity = Gravity.END;
                            waitingAccept.findViewById(R.id.id_btnBloquer).setLayoutParams(l);

                            waitingAccept.setTitle("Invitation");
                            waitingAccept.setMessage(playerAdv.nom+" want play with you.\nAgree ?");
                            waitingAccept.yes.setText("Agree");
                            waitingAccept.no.setText("Cancel");
                        }
                    }

                    @Override public void onCancelled(DatabaseError firebaseError) { }
                }
                );
            }

            @Override public void onCancelled(DatabaseError error) {}
        }
        );
    }


    public void inviteAdvirsaire(final String idAdv)
    {
        fire.db.child("players").child(me.id).child("advirsaire").removeEventListener(listenValues); //ok

        fire.db.child("players").child(idAdv).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override public void onDataChange(DataSnapshot snapshot)
            {
                if (    snapshot.exists()  &&
                        snapshot.hasChild("id") &&
                        snapshot.hasChild("advirsaire") &&
                        snapshot.hasChild("nom" ) &&
                        snapshot.hasChild("deplacement" )
                ){
                    final PlayerOnline playerAdv = snapshot.getValue(PlayerOnline.class);

                    try
                    {
                        final int val = Integer.parseInt( playerAdv.advirsaire );
                        if( val==0 )
                        {
                            fire.db.child("players").child(idAdv).child("advirsaire").setValue(me.id) ;

                            if( listBloque.contains(idAdv) )
                            {
                                listBloque.remove(idAdv);
                                fire.blockedList = listBloque;
                                Toast.makeText(context,"You have unblocked "+playerAdv.nom+" !",Toast.LENGTH_LONG).show();
                            }

                            if(waitingAccept != null) waitingAccept.dismiss();
                            waitingAccept = new Modal((Activity)context) {
                                @Override public void onClick(View v)
                                {
                                    switch (v.getId())
                                    {
                                        case R.id.btn_no :
                                            fire.db.child("players").child(idAdv).child("advirsaire").setValue("-3") ;
                                            fire.db.child("players").child(me.id).child("advirsaire").setValue("0") ;
                                            fire.db.child("players").child(me.id).child("advirsaire")
                                                    .removeEventListener(listenAccept);
                                            fire.db.child("players").child(me.id).child("advirsaire")
                                                    .addValueEventListener(listenValues);
                                            dismiss();
                                            break;
                                    }
                                }
                            };
                            waitingAccept.clear();
                            waitingAccept.show();
                            waitingAccept.setTitle("Connecting...");
                            waitingAccept.setMessage("Wait your enemy for accepting your request");
                            waitingAccept.yes.setText("");
                            waitingAccept.yes.setVisibility(View.INVISIBLE);
                            waitingAccept.no.setText("Cancel");

                            fire.db.child("players").child(me.id).child("advirsaire").addValueEventListener(
                                listenAccept = new ValueEventListener() {
                                    @Override public void onDataChange(DataSnapshot dataSnapshot) {
                                        try
                                        {
                                            if ( Integer.parseInt(dataSnapshot.getValue(String.class)) == -1)
                                            {
                                                //adv = null;
                                                if(waitingAccept != null) waitingAccept.dismiss();
                                                waitingAccept = new Modal((Activity) context) {
                                                    @Override public void onClick(View v) {
                                                        switch (v.getId()) {
                                                            case R.id.btn_no:
                                                                fire.db.child("players").child(me.id).child("advirsaire")
                                                                        .setValue("0");
                                                                fire.db.child("players").child(me.id).child("advirsaire")
                                                                        .removeEventListener(listenAccept);
                                                                fire.db.child("players").child(me.id).child("advirsaire")
                                                                        .addValueEventListener(listenValues);
                                                                dismiss();
                                                                break;
                                                        }
                                                    }
                                                };
                                                waitingAccept.clear();
                                                waitingAccept.show();
                                                waitingAccept.setTitle("Invitation");
                                                waitingAccept.setMessage(playerAdv.nom+" refuse your invitation !");
                                                waitingAccept.yes.setText("");
                                                waitingAccept.no.setText("Cancel");
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            fire.db.child("players").child(me.id).child("advirsaire")
                                                    .removeEventListener(listenAccept);
                                            fire.db.child("players").child(me.id).child("advirsaire")
                                                    .removeEventListener(listenValues);

                                            if(waitingAccept != null) waitingAccept.dismiss();
                                            Intent intent = new Intent(context, Activite.class);
                                            intent.putExtra("online","true");
                                            intent.putExtra("idAdv",dataSnapshot.getValue(String.class));
                                            intent.putExtra("myId",me.id);
                                            intent.putExtra("myName",me.nom);
                                            intent.putExtra("nameAdv",playerAdv.nom);
                                            //adv = playerAdv;
                                            Music.detruire();
                                            ((Activity) context).startActivityForResult(intent,0);
                                        }
                                    }

                                    @Override public void onCancelled(DatabaseError databaseError) {}
                                }
                            );
                        }
                    } catch (Exception e){};
                }
            }

            @Override public void onCancelled(DatabaseError firebaseError) { }
        });
    }


    public void sendDeplacement(int idPion, int place, boolean passer)
    {
        me.deplacement.placeAdeplacer = "0";
        me.deplacement.pionActive     = "0";
        me.deplacement.signal         = ""+(!passer);

        adv.deplacement.pionActive     = ""+idPion;
        adv.deplacement.placeAdeplacer = ""+place;
        adv.deplacement.signal         = ""+passer;

        try {
            fire.db.child("players").child(me.id).child("deplacement").setValue(me.deplacement);
            fire.db.child("players").child(adv.id).child("deplacement").setValue(adv.deplacement);
        } catch (DatabaseException e) {}
    }


    public void deplacementOk()
    {
        try {
            fire.db.child("players").child(adv.id).child("deplacement").child("placeAdeplacer").setValue("-10");
        } catch (DatabaseException e) {}
    }


    public void sendReset(String reset)
    {
        try {
            fire.db.child("players").child(adv.id).child("reset").setValue(reset);
        } catch (DatabaseException e) {}
    }


    public void initReset()
    {
        try {
            fire.db.child("players").child(me.id).child("reset").setValue("0");
        } catch (DatabaseException e) {}
    }


    public void initDeplacement()
    {
        try {
            fire.db.child("players")
                    .child(me.id)
                    .child("deplacement")
                    .child("placeAdeplacer")
                    .setValue( "0" );
            fire.db.child("players")
                    .child(me.id)
                    .child("deplacement")
                    .child("pionActive")
                    .setValue( "0" );
        } catch (DatabaseException e) {}
    }


    public void alert()
    {
        try {
            fire.db.child("players").child(adv.id).child("retard").setValue("true");
        } catch (DatabaseException e) {}
    }



    public void finishGame()
    {
        try {
            try { Integer.parseInt(me.advirsaire); }catch (Exception e){
                fire.db.child("players").child(me.advirsaire).child("advirsaire").setValue("-4");
            }

            me.deplacement.placeAdeplacer = "0";
            me.deplacement.pionActive = "0";
            me.deplacement.signal = "false";
            me.advirsaire = "0";
            me.reset = "0";
            me.message = "";
            fire.save(me);
        } catch (DatabaseException e) {}
    }


    public void deconnecter()
    {

        if(listenValues != null) {
            fire.db.child("players").child(me.id).child("advirsaire").removeEventListener(listenValues);
        }

        if(listenAccept != null)
            fire.db.child("players").child(me.id).child("advirsaire").removeEventListener(listenAccept);

        if(me != null)
            fire.db.child("players").child(me.id).setValue(null);

        //me  = null;
        //adv = null;
    }


    public void reconnecter(String id)
    {
        fire.db.child("players").child(id).addListenerForSingleValueEvent
        (
                new ValueEventListener()
                {
                    @Override public void onDataChange(DataSnapshot snapshot) {
                        try
                        {
                            me = snapshot.getValue(PlayerOnline.class);

                            me.advirsaire = "0"   ;
                            fire.save(me);
                            fire.myId = me.id;
                            fire.loadPlayers();
                            Toast.makeText(context,"reconnected !",Toast.LENGTH_LONG).show();
                            waitAdvirsaire();
                        }

                        catch (Exception e)
                        {
                            Modal m = new Modal((Activity)context) {
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
                            m.setTitle("");
                            m.setMessage("Connexion failed");
                            m.yes.setText("");
                            m.no.setText("Cancel");
                        }
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {}
                }
        );
    }




    public static boolean isNetworkConnected(Context c) {
        ConnectivityManager cm = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


}















