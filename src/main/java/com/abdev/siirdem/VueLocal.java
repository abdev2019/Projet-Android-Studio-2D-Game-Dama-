package com.abdev.siirdem;


import android.content.Context;


public class VueLocal extends Vue
{

    public VueLocal(final Context context, int nPion, int ws, int hs, Historique historique) {
        super(context, nPion, ws, hs, historique);
    }



    public void update()
    {
        // deplacement *********************************************************************
        if(plateau.pionActive!=null && aDeplacer != null) {
            deplacer();
        }


        // detect pion selected ************************************************************
        if(isTouch)
        {
            int wh = player1.bitmap.getWidth();

            if(!lmnakhf.isEmpty())
            {
                for (int i = 0; i < lmnakhf.size(); i++)
                    if (isCollition(lmnakhf.get(i).place.x-wh/2, lmnakhf.get(i).place.y-wh/2,wh, wh)) {
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
            if(myTurn)
            {
                for (int i = 0; i < nPion; i++)
                    if (isCollition(player1.pions[i].place.x-wh/2, player1.pions[i].place.y-wh/2,wh, wh)) {
                        plateau.pionActive = player1.pions[i];
                        placeVide = plateau.cherchePlaceVide(myTurn);
                        break;
                    }
            }
            else
            {
                for (int i = 0; i < nPion; i++)
                    if (isCollition(player2.pions[i].place.x-wh/2, player2.pions[i].place.y-wh/2,wh, wh)) {
                        plateau.pionActive = player2.pions[i];
                        placeVide = plateau.cherchePlaceVide(myTurn);
                        break;
                    }
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



    public void deplacer()
    {
        if(retard.actif) retard.detect();


        if(lmnkhof!=null) {
             lmnkhof.playing = false;
        }

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
            {
                lmnakhf.add(plateau.pionActive);
            }
        }

        plateau.pionActive =  tmpActive;

        sound.move();


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
            placeVide = plateau.chercherPionAmanger(myTurn);
            if (placeVide == null)
            {
                plateau.pionActive = null;
                myTurn = !myTurn;
            }

            lmnakhf.clear();
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

            historique.stockerPionMange(null, null);

            plateau.pionActive = null;
            placeVide = null;
            myTurn = !myTurn;
        }

        aDeplacer = null;
    }


}












