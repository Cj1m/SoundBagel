package com.smoothsky.soundbagel;


import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Env;
import com.soundcloud.api.Request;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import de.voidplus.soundcloud.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by Christopher on 22/06/2015.
 */
public class Player {
    private MediaPlayer mp;
    public Context m;
    private int lastSongIDPlayed;

    public Player(){
        mp = new MediaPlayer();
    }

    public void play(Integer songID, Context m){
        stop();
        getSong(songID);
        this.m = m;
    }

    public void stop(){
        if(mp.isPlaying()){
            mp.stop();
        }
    }

    public void getSong(Integer songID){
        new RetrieveSong().execute(songID);
    }

    public void playSong(String[] urls){
        String url = urls[0];
        String name = urls[1];

        stop(); //Just incase
        mp = new MediaPlayer();

        try {
            mp.setDataSource(url);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mp.start();
        Toast.makeText(m.getApplicationContext(), "Now playing: " + name, Toast.LENGTH_SHORT).show();
    }

    public int getLastSongIDPlayed(){
        return lastSongIDPlayed;
    }


    class RetrieveSong extends AsyncTask<Integer, String[], String[]> {

        private Exception exception;

        protected String[] doInBackground(Integer... urls) {
            String id = "a2f5401ae82019f7de67e7bd52b66fd5";
            String secret = "a78aff755094071ef3b94580bd457c1e";
            SoundCloud soundcloud = new SoundCloud(id, secret);

            Track t = soundcloud.get("tracks/"+urls[0]);
            lastSongIDPlayed = urls[0];
            String[] arr = {t.getStreamUrl(), t.getTitle()};
            return arr;
        }

        protected void onPostExecute(String[] arr) {
            playSong(arr);
        }


    }
}


/*
String id = "a2f5401ae82019f7de67e7bd52b66fd5";
String secret = "a78aff755094071ef3b94580bd457c1e";
ApiWrapper wrapper = new ApiWrapper(id,secret, null, null);
*/