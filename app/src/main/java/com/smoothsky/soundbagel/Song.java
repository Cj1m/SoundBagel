package com.smoothsky.soundbagel;

/**
 * Created by Christopher on 07/07/2015.
 */
public class Song {
    private String title,artist,songURL, artURL, soundcloudURL;
    private int duration;
    public Song(String title,String songURL, String artURL, String soundcloudURL, String artist, int duration){
        this.title = title;
        this.artist = artist;
        this.songURL = songURL;
        this.artURL = artURL;
        this.soundcloudURL = soundcloudURL;
        this.duration = duration;
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
    public int getDuration() {
        return duration;
    }
    public String getDurationText() {
        int seconds = getDuration() / 1000;
        int minutes = (int)Math.floor(seconds / 60);
        seconds = seconds % 60;
        String secs = seconds + "";
        if(secs.length() == 1){secs = "0" + secs;}
        return minutes + ":" + secs;
    }
}
