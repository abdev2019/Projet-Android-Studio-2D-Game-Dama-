package com.abdev.siirdem;



public class Deplacement
{
    public String pionActive="";
    public String placeAdeplacer="";
    public String signal = "";

    public Deplacement(){ pionActive = "0"; placeAdeplacer="0"; signal="false"; }

    public Deplacement(String s){ pionActive = "0"; placeAdeplacer="0"; signal="true"; }

    public Deplacement(String a, String p, String signal){
        pionActive = a;
        placeAdeplacer = p;
        this.signal = signal;
    }
}