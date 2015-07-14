package com.smoothsky.soundbagel;

/**
 * Created by Christopher on 07/07/2015.
 */
public class Song {
    private String title,artist,songURL, artURL, soundcloudURL;
    public Song(String title,String songURL, String artURL, String soundcloudURL, String artist){
        this.title = title;
        this.artist = artist;
        this.songURL = songURL;
        this.artURL = artURL;
        this.soundcloudURL = soundcloudURL;
    }

    public String getSongURL(){
        return songURL;
    }
    public String getTitle(){
        return title;
    }
    public String getArtURL(){
        return artURL;
    }
    public String getSoundcloudURL(){
        return soundcloudURL;
    }
    public String getArtist() {
        return artist;
    }
}
