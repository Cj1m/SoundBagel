package com.smoothsky.soundbagel;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends ActionBarActivity {
    private String SERVER_IP;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private User usr;

    private Player p;
    private BagelShelf shelf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Set variables
        SERVER_IP =  getResources().getString(R.string.server_ip);
        String username = getIntent().getStringExtra("USERNAME");
        mDrawerList = (ListView) findViewById(R.id.navList);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        usr = new User(this, username);
        p = new Player(getApplicationContext(), this);
        shelf = new BagelShelf();
        //DisplayMetrics displaymetrics = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        //((TextView)findViewById(R.id.currentlyPlaying)).setWidth(displaymetrics.widthPixels - findViewById(R.id.playButton).getWidth() - findViewById(R.id.likeButton).getWidth());

        //Subroutines
        setUpSideMenu();
        setUpMapIfNeeded();
        //p.play(60603294,  this);
        usr.usrImg = mMap.addGroundOverlay(usr.userImgOptions);

        //Listeners
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                final LatLng latlng = latLng;
                final String usrname = usr.username;
                new Thread(new Runnable() {
                    public void run(){
                        sendBagelToServer(latlng, 213851825, usrname);
                    }
                }).start();

                //p.addSongToPlaylist(201315263);
                //Bagel b = new Bagel();
                //shelf.addBagel(b);
                //b.bagelCircle = mMap.addGroundOverlay(b.bagelCircleOptions);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    private void setUpSideMenu(){
        String[] osArray = { "My Bagels","Visited Bagels" ,"Twitter", "Facebook", "Website" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id == 0) {
                    Toast.makeText(MapsActivity.this, "Still in dev!", Toast.LENGTH_SHORT).show();
                }
                if (id == 1) {
                    Toast.makeText(MapsActivity.this, "Still in dev!", Toast.LENGTH_SHORT).show();
                }
                if (id == 2) {
                    openTwitterPage();
                }
                if (id == 3) {
                    Toast.makeText(MapsActivity.this, "Facebook coming soon!", Toast.LENGTH_SHORT).show();
                }
                if (id == 4) {
                    //Open SoundBagel website in browser
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://soundbagel.weebly.com"));
                    startActivity(browserIntent);
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);


    }

    private void openTwitterPage(){
        Intent intent = null;

        //Opens twitter app if installed, otherwise opens browser
        try {
            this.getPackageManager().getPackageInfo("com.twitter.android", 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=3336714863"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            // no Twitter app, revert to browser
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/SoundBagelApp"));
        }
        this.startActivity(intent);
    }

    public Bagel checkIfInBagel(LatLng userPosition){
        Bagel touchingBagel = null;
        for(Bagel b : shelf.getBagels()){
            if(intersectsBagel(userPosition, b)){
                touchingBagel = b;
            }
        }

        return touchingBagel;
    }

    public void playMusicIfInBagel(LatLng user){
        Bagel b = checkIfInBagel(user);
        if(b != null){
            if(p.getLastSongIDPlayed() != b.getSongID()) {
                int id = b.getSongID();
                //p.play(id, this);
                p.addSongToPlaylist(id);
            }
        }
    }

    private double getDistance(LatLng latLng1, LatLng latLng2){
        int radiusEarth = 6371000;
        double phi1 = Math.toRadians(latLng1.latitude);
        double phi2 = Math.toRadians(latLng2.latitude);

        double deltaPhi = Math.toRadians(latLng2.latitude - latLng1.latitude);
        double deltaLambda = Math.toRadians(latLng2.longitude - latLng1.longitude);

        double a = Math.sin(deltaPhi/2) * Math.sin(deltaPhi/2) + Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double distance = radiusEarth * c;
        System.out.println(distance);
        return distance;
    }

    public boolean intersectsBagel(LatLng user, Bagel b){
        double distanceBetween = getDistance(user,b.getPosition());

        System.out.println(distanceBetween + " radius: " + b.getRadius());
        return (distanceBetween < b.getRadius() + usr.getRadius());
    }

    private void sendBagelToServer(LatLng latLng, int songID, String usrname){
        final String SERVER_URL = "http://"+SERVER_IP+"/soundbagelbackend/add.php";
        HttpPost httppost;
        StringBuffer buffer;
        HttpResponse response;
        HttpClient httpclient;
        List<NameValuePair> nameValuePairs;
        ProgressDialog dialog = null;

        try{

            httpclient=new DefaultHttpClient();
            httppost= new HttpPost(SERVER_URL);
            //add data
            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("lat",latLng.latitude + ""));
            nameValuePairs.add(new BasicNameValuePair("long", latLng.longitude + ""));
            nameValuePairs.add(new BasicNameValuePair("songid", songID + ""));
            nameValuePairs.add(new BasicNameValuePair("username", usrname + ""));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            //Execute HTTP Post Request
            //response=httpclient.execute(httppost);


            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String serverResponse = httpclient.execute(httppost, responseHandler);
            System.out.println("Response : " + serverResponse);
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MapsActivity.this, "Response from PHP : " + serverResponse, Toast.LENGTH_LONG).show();
                }
            });

            if(serverResponse.equalsIgnoreCase("success")){
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MapsActivity.this, "Bagel successfully created!", Toast.LENGTH_LONG).show();
                    }
                });
                getBagelsFromServer(usr.getUserPosition());
            }else{

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MapsActivity.this, "Bagel could not be created!", Toast.LENGTH_LONG).show();
                    }
                });
            }

        }catch(Exception e){
            System.out.println("Exception : " + e.getMessage());
        }
    }

    public void getBagelsFromServer(LatLng latLng){
        String SERVER_URL = "http://"+SERVER_IP+"/soundbagelbackend/getnearbybagels.php";
        int radius = 1000000000;
        List<NameValuePair> nameValuePairs;
        DefaultHttpClient   httpclient;
        HttpPost httppost;


        nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("lat",latLng.latitude + ""));
        nameValuePairs.add(new BasicNameValuePair("long", latLng.longitude + ""));
        nameValuePairs.add(new BasicNameValuePair("radius", radius + ""));

        InputStream inputStream = null;
        String result = null;
        try {
            /*httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();*/
            httpclient=new DefaultHttpClient();
            httppost= new HttpPost(SERVER_URL);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            /*inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }*/
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String serverResponse = httpclient.execute(httppost, responseHandler);
            System.out.println("Result: " + serverResponse);

            //GET THE BAGELS!!!
            //JSONObject jObject = new JSONObject(serverResponse);
            JSONArray jArray = new JSONArray(serverResponse);
            System.out.println("Getting all " + jArray.length() +" bagels!");
            for (int i=0; i < jArray.length(); i++)
            {
                try {
                    final JSONObject oneObject = jArray.getJSONObject(i);


                    runOnUiThread(new Runnable() {
                        public void run() {
                            try{
                                int id = oneObject.getInt("id");
                                double latitude = oneObject.getDouble("lat_pos");
                                double longitude = oneObject.getDouble("long_pos");
                                int bagelRadius = oneObject.getInt("radius");
                                int songID = oneObject.getInt("song_id");
                                int likes = oneObject.getInt("likes");
                                String owner = oneObject.getString("username");

                                LatLng bagelPos = new LatLng(latitude,longitude);

                                Bagel b = new Bagel(id, bagelPos, bagelRadius, songID, likes, owner);

                                //prevent overlapping of same bagel
                                if(!shelf.bagelExists(b)){
                                    shelf.addBagel(b);
                                    b.bagelCircle = mMap.addGroundOverlay(b.bagelCircleOptions);
                                }

                            } catch (JSONException e) {
                                // Oops
                            }
                        }
                    });

                } catch (JSONException e) {
                    // Oops
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        finally {
            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        p.stop();
        super.onDestroy();
    }
}
