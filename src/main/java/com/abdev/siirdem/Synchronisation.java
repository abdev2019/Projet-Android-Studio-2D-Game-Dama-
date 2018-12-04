package com.abdev.siirdem;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Synchronisation
{
    ArrayList<Integer> idPion;
    ArrayList<Integer> numPlace;
    ArrayList<Boolean> passeRole;
    boolean             wait  ;

    ValueEventListener listener;

    DamaOnline damaOnline;

    Thread thread;
    boolean working;


    public Synchronisation(DamaOnline damaOnline)
    {
        idPion = new ArrayList<>();
        numPlace = new ArrayList<>();
        passeRole = new ArrayList<>();
        this.damaOnline = damaOnline;
        wait   = false;
        working = true;
    }


    public void put( int idP, int numP, boolean pass )
    {
        idPion.add(idP);
        numPlace.add(numP);
        passeRole.add(pass);
    }


    public void synchronize()
    {
        damaOnline.fire.db.child("players").child(damaOnline.me.id).child("deplacement").child("placeAdeplacer")
        .addValueEventListener(
        listener = new ValueEventListener()
        {
            @Override public void onDataChange(final DataSnapshot dataSnapshot) {
                if (Integer.parseInt(dataSnapshot.getValue(String.class)) == -10)
                {
                    wait = false;
                    //((Activity)damaOnline.context).runOnUiThread(new Runnable() {public void run() {
                    //}});
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        thread = new Thread( new Runnable() { @Override public void run()
        {
            while (working)
            {
                 if(!idPion.isEmpty() && !wait)
                 {
                     damaOnline.sendDeplacement(idPion.remove(0),numPlace.remove(0),passeRole.remove(0));
                     wait = true;
                 }

                try{ Thread.sleep(17); } catch(InterruptedException e){ e.printStackTrace(); }
            }
        }});

        thread.start();
    }


    public void detruire()
    {
        damaOnline.fire.db.child("players").child(damaOnline.me.id).child("deplacement").child("placeAdeplacer")
                .removeEventListener(listener);
        working = false;
        thread = null;
        listener = null;
        passeRole = null;
        numPlace = null;
    }


}







