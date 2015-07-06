package com.smoothsky.soundbagel;

import java.util.ArrayList;

/**
 * Created by Christopher on 22/06/2015.
 */
public class BagelShelf {
    private ArrayList<Bagel> bagels;




    public BagelShelf(){
        bagels = new ArrayList<Bagel>();
    }

    public void addBagel(Bagel b) {
        bagels.add(b);
        //TODO sync this with server aswell
    }


    public Bagel[] getBagels(){
        return bagels.toArray(new Bagel[bagels.size()]);
    }

    public boolean bagelExists(Bagel b){
        for(Bagel c : bagels){
            if(b.ID == c.ID){
                return true;
            }
        }
        return false;
    }
}
