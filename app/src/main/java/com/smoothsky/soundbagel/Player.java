package com.smoothsky.soundbagel;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Christopher on 22/06/2015.
 */
public class Player {
    private MediaPlayer mp;
    public Context m;
    private MapsActivity act;
    private int lastSongIDPlayed;
    private ArrayList<Song> playlist;

    //Listview for playlist
    private ListView playlistListView;
    private ArrayList<String> itemsArray;
    private ArrayAdapter<String> itemsAdapter;
    private InputStream artIs;
    private SeekBar seekBar;
    private Handler seekbarHandler = new Handler();

    public Player(Context m, MapsActivity act){
        this.m = m;
        this.act = act;
        mp = new MediaPlayer();
        playlist = new ArrayList<Song>();

        itemsArray = new ArrayList<String>();
        itemsAdapter = new ArrayAdapter<String>(m,android.R.layout.simple_list_item_1, itemsArray);
        playlistListView = (ListView) act.findViewById(R.id.listViewPlaylist);

        playlistListView.setAdapter(itemsAdapter);

        seekBar = (SeekBar) act.findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mp != null && fromUser){
                    mp.seekTo(progress);
                    seekBar.setProgress(progress);
                    
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mp != null){
                    int currentSongPos = mp.getCurrentPosition();
                    seekBar.setProgress(currentSongPos);
                }
                seekbarHandler.postDelayed(this, 1000);
            }
        });
    }

    public void addSongToPlaylist(int songID){
        getSong(songID);
    }

    private void addSongToPlaylist(String[] urls){
        String songURL = urls[0];
        String title = urls[1];
        String artURL = urls[2];
        String URL = urls[3];
        Song song = new Song(title, songURL, artURL, URL);

        playlist.add(song);
        itemsArray.add(song.getTitle());
        itemsAdapter.notifyDataSetChanged();

        if(!mp.isPlaying()){
            updateInfoData(playlist.get(0));
        }
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

    public void playSong(String streamURL){
        String url = streamURL;

        stop(); //Just incase
        mp = new MediaPlayer();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (playlist.size() > 0) {
                    Song nextSong = playlist.get(0);
                    playlist.remove(0);
                    itemsArray.remove(0);
                    itemsAdapter.notifyDataSetChanged();
                    updateInfoData(nextSong);
                }
            }
        });

        try {
            mp.setDataSource(url);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mp.start();
        seekBar.setMax(mp.getDuration());
        //Toast.makeText(m.getApplicationContext(), "Now playing: " + name, Toast.LENGTH_SHORT).show();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void updateInfoData(final Song nextSong){
        ((TextView)act.findViewById(R.id.currentlyPlaying)).setText(nextSong.getTitle());
        ((TextView)act.findViewById(R.id.textViewInfo)).setText("Soundcloud URL:\n" + nextSong.getSoundcloudURL() + " \nMore info coming soon!");

        System.out.println(nextSong.getArtURL());
        Thread getArtThread = new Thread(){
            public void run() {
                try {
                    String url = nextSong.getArtURL().replace("large", "crop");
                    artIs = (InputStream) new URL(url).getContent();
                    final Drawable d = Drawable.createFromStream(artIs, url);
                    act.runOnUiThread(new Runnable() {
                        public void run() {
                            ((ImageView)act.findViewById(R.id.infoBackground)).setImageDrawable(d);
                            playSong(nextSong.getSongURL());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        getArtThread.start();

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

            //lastSongIDPlayed = urls[0];
            String[] arr = {t.getStreamUrl(), t.getTitle(), t.getArtworkUrl(), t.getPermalinkUrl()};
            return arr;
        }

        protected void onPostExecute(String[] arr) {
            addSongToPlaylist(arr);
        }


    }
}


/*
String id = "a2f5401ae82019f7de67e7bd52b66fd5";
String secret = "a78aff755094071ef3b94580bd457c1e";
ApiWrapper wrapper = new ApiWrapper(id,secret, null, null);
*/