package com.abdev.siirdem;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class FireBaseHelper
{
    Activity activity;
    DatabaseReference db;
    public ArrayList<PlayerOnline> players = new ArrayList<>();
    public String myId=null;
    public ArrayList<String> blockedList=null;





    public FireBaseHelper(DatabaseReference db, Activity a) {
        this.db = db;
        activity = a;
    }

    public String newIdPlayer(){
        return db.child("players").push().getKey();
    }
    public void save(PlayerOnline player) {
        try {
            db.child("players").child(player.id).setValue(player);
        } catch (DatabaseException e) {}
    }


    // first activity : liste players ok
    public void loadPlayers() {
        db.addChildEventListener(new ChildEventListener() {
            @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
            }
            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
            }
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {
                fetchData(dataSnapshot);
            }
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
            }
            @Override public void onCancelled(DatabaseError databaseError) {
            }
        });

        db.addValueEventListener( new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()){
                        players.clear();
                        ((ListView)activity.findViewById(R.id.les_players)).setAdapter(null);
                        ((TextView)activity.findViewById(R.id.nbrPlayers)).setText(("0 players"));
                        ((TextView)activity.findViewById(R.id.pOnline)).setText(("No player"));
                    }
                }
                @Override public void onCancelled(DatabaseError databaseError) {}
        });

    }
    private void fetchData(DataSnapshot dataSnapshot) {
        players.clear();
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            PlayerOnline p = ds.getValue(PlayerOnline.class);

            try {
                int val = Integer.parseInt(p.advirsaire);
                if( (val==0) && ( myId==null || !myId.equals(p.id) ) )
                        players.add(p);
            }
            catch (Exception e){};
        }

        if( players.size()>0 ) remplireListe();
        else {
            ((TextView)activity.findViewById(R.id.pOnline)).setText(("No player"));
            ((ListView)activity.findViewById(R.id.les_players)).setAdapter(null);
        }
    }

    public void remplireListe() {
        ((TextView)activity.findViewById(R.id.pOnline)).setText((""));

        int n = players.size();

        String[] values = new String[n];
        final ArrayList<Integer> pos = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            values[i] = "" + (players.get(i).nom);
            //if(blockedList.contains(players.get(i).id))
                pos.add(i);
        }

        ((TextView)activity.findViewById(R.id.nbrPlayers)).setText( (n+" players"));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                activity,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                values
        ){
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);
                TextView textView=(TextView) view.findViewById(android.R.id.text1);

                //if(pos.contains(position)) {
                //    textView.setText( textView.getText()+" [blocked]" );
                //    textView.setTextColor(Color.RED);
                //}
                //else
                    textView.setTextColor(Color.WHITE);

                return view;
            }
        };

        ((ListView)activity.findViewById(R.id.les_players)).setAdapter(adapter);
    }


}












