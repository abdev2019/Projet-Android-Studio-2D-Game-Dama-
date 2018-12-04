package com.abdev.siirdem;



public class PlayerOnline
{
    public String id="";
    public String advirsaire="";
    public String nom="";
    public Deplacement deplacement;
    public String reset="";
    public String retard = "";
    public String message="";


    public PlayerOnline(){}

    public PlayerOnline(String advirsaire, String nom, Deplacement deplacement, String reset, String retard, String msg )
    {
        this.advirsaire = advirsaire;
        this.nom = nom;
        this.deplacement = deplacement;
        this.reset = reset;
        this.retard = retard;
        message = msg;
    }


    public void reset(boolean s)
    {
        this.deplacement.signal = ""+s;
        this.deplacement.placeAdeplacer = "0";
        this.deplacement.pionActive = "0";
        this.reset = "0";
        this.retard = "false";
        message = "";
    }

}
