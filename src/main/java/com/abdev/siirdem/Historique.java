package com.abdev.siirdem;

import java.util.ArrayList;


public class Historique {
    private ArrayList<Pion> lastPionsActive = new ArrayList<Pion>();
    public ArrayList<Pion> lastPionsManger = new ArrayList<Pion>();
    private ArrayList<Boolean> isDem       = new ArrayList<Boolean>();

    private ArrayList<Place> lastPlacesPionsActive  = new ArrayList<Place>();
    private ArrayList<Place> lastPlacesPionsManger  = new ArrayList<Place>();

    public boolean isVide() {
        return !(lastPionsActive.size() > 0);
    }

    public void stockerPionActive(Pion pion, Place place) {
        lastPionsActive.add( pion );
        lastPlacesPionsActive.add(place);
        isDem.add( ((pion==null)?null:pion.isDem) );
    }

    public void stockerPionMange(Pion pion, Place place) {
        lastPionsManger.add(pion);
        lastPlacesPionsManger.add(place);
    }

    public Pion getPion(char w) {
        if( lastPionsActive.size() > 0 )
        {
            if(w=='a')  return lastPionsActive.remove(lastPionsActive.size()-1);
            return lastPionsManger.remove(lastPlacesPionsManger.size()-1);
        }
        return null;
    }

    public Place getPlace(char w) {
        if( lastPlacesPionsActive.size() > 0 )
        {
            if(w=='a') return lastPlacesPionsActive.remove(lastPlacesPionsActive.size()-1);
            return lastPlacesPionsManger.remove(lastPlacesPionsManger.size()-1);
        }
        return null;
    }

    public Boolean getIsDem() {
        if( isDem.size() > 0 ){
            return isDem.remove(isDem.size()-1);
        }
        return null;
    }

    public void reset(){
        isDem.clear();
        lastPionsActive.clear();
        lastPionsManger.clear();
        lastPlacesPionsActive.clear();
        lastPlacesPionsManger.clear();
    }

    public void detruire(){
        reset();
        isDem = null;
        lastPionsActive = null;
        lastPionsManger = null;
        lastPlacesPionsActive = null;
        lastPlacesPionsManger = null;
    }

}














