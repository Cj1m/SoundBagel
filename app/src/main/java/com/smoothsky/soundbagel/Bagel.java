package com.smoothsky.soundbagel;


import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Christopher on 22/06/2015.
 * 60603294 test soundcloud ID
 */
public class Bagel {
    public GroundOverlayOptions bagelCircleOptions;
    public GroundOverlay bagelCircle;
    public int ID;
    private LatLng position;
    private int songID;
    private int radius;
    private int likes;
    private String owner;

    public Bagel(int ID,LatLng pos,int radius, int songID, int likes, String owner){
        position = pos;
        this.radius = radius;
        this.songID = songID;
        bagelCircleOptions = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.bagel2))
                .position(pos, radius*2);
        this.ID = ID;
        this.likes = likes;
        this.owner = owner;
    }

    public int getSongID(){
        return songID;
    }
    public float getRadius(){
        return bagelCircle.getWidth() / 2;
    }
    public LatLng getPosition(){
        return position;
    }
    public String getOwner(){return owner;}
    public int getLikes(){return likes;}
    public void setSongID(int songID){
        this.songID = songID;
    }
}
