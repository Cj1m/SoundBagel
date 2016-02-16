package com.smoothsky.soundbagel;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
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
import org.w3c.dom.Text;

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
    private TextView currentTime;
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
        currentTime = (TextView) act.findViewById(R.id.songCurrentTime);

        final MapsActivity mAct =act;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mp != null && fromUser){
                    mp.seekTo(progress);

                    int seconds = progress / 1000;
                    int minutes = (int)Math.floor(seconds / 60);
                    seconds = seconds % 60;
                    String secs = seconds + "";
                    if(secs.length() == 1){secs = "0" + secs;}

                    seekBar.setProgress(progress);
                    currentTime.setText(minutes + ":" + secs);
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
                    int seconds = currentSongPos / 1000;
                    int minutes = (int)Math.floor(seconds / 60);
                    seconds = seconds % 60;
                    String secs = seconds + "";
                    if(secs.length() == 1){secs = "0" + secs;}

                    seekBar.setProgress(currentSongPos);
                    currentTime.setText(minutes + ":" + secs);
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
        String artist = urls[4];
        int duration = Integer.parseInt(urls[5]);
        Song song = new Song(title, songURL, artURL, URL, artist, duration);

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
        ((TextView)act.findViewById(R.id.currentlyPlayingArtist)).setText(nextSong.getArtist());
        ((TextView)act.findViewById(R.id.textViewInfo)).setText("Soundcloud URL:\n" + nextSong.getSoundcloudURL() + " \nMore info coming soon!");
        ((TextView)act.findViewById(R.id.songEndTime)).setText(nextSong.getDurationText());
        System.out.println(nextSong);
        Thread getArtThread = new Thread(){
            public void run() {
                try {
                    String url = nextSong.getArtURL().replace("large", "crop");
                    artIs = (InputStream) new URL(url).getContent();
                    final Drawable d = Drawable.createFromStream(artIs, url);
                    Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
                    final Bitmap background = fastblur(bitmap, 10);
                    act.runOnUiThread(new Runnable() {
                        public void run() {
                            ((ImageView)act.findViewById(R.id.infoBackground)).setImageBitmap(background);
                            ((ImageView)act.findViewById(R.id.songArt)).setImageDrawable(d);
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
            lastSongIDPlayed = urls[0];
            String artist = t.getUser().getUsername();
            String[] arr = {t.getStreamUrl(), t.getTitle(), t.getArtworkUrl(), t.getPermalinkUrl(), artist, t.getDuration().toString()};
            return arr;
        }

        protected void onPostExecute(String[] arr) {
            addSongToPlaylist(arr);
        }


    }

    public Bitmap fastblur(Bitmap sentBitmap, int radius) {
        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
        // Thanks Mario Klingemann for sharing this code! Saved me a lot of time!!!

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }
}


/*
String id = "a2f5401ae82019f7de67e7bd52b66fd5";
String secret = "a78aff755094071ef3b94580bd457c1e";
ApiWrapper wrapper = new ApiWrapper(id,secret, null, null);
*/