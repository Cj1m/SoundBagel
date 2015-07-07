package com.smoothsky.soundbagel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity{
    //Declare Variables
    private String SERVER_IP;
    private String LOGIN_URL;

    private HttpPost httppost;
    private StringBuffer buffer;
    private HttpResponse response;
    private HttpClient httpclient;
    private List<NameValuePair> nameValuePairs;
    private ProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SERVER_IP = getResources().getString(R.string.server_ip);
        LOGIN_URL = "http://"+SERVER_IP+"/SoundBagelBackend/login.php";
    }

    public void login(View view){
        Intent mapIntent = new Intent(this, MapsActivity.class);
        EditText usernameBox = (EditText) findViewById(R.id.userName);
        EditText passwordBox = (EditText) findViewById(R.id.userPassword);
        final String username = usernameBox.getText().toString();
        final String password = passwordBox.getText().toString();

        if(username.trim().length() > 0 && password.trim().length() > 0){
            new Thread(new Runnable() {
                public void run(){
                    tryLogin(username, password);
                }
            }).start();

        }else{
            Toast.makeText(getApplicationContext(), "Please enter login details", Toast.LENGTH_LONG).show();
        }
    }

    private void tryLogin(final String username, String password){
        System.out.println("Logging in!");

        try{

            httpclient=new DefaultHttpClient();
            httppost= new HttpPost(LOGIN_URL);
            //add your data
            nameValuePairs = new ArrayList<NameValuePair>(2);
            // Always use the same variable name for posting i.e the android side variable name and php side variable name should be similar,
            nameValuePairs.add(new BasicNameValuePair("username",username));  // $Edittext_value = $_POST['Edittext_value'];
            nameValuePairs.add(new BasicNameValuePair("password", password));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            //Execute HTTP Post Request
            response=httpclient.execute(httppost);


            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpclient.execute(httppost, responseHandler);
            System.out.println("Response : " + response);
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Response from PHP : " + response, Toast.LENGTH_LONG).show();
                }
            });

            if(response.equalsIgnoreCase("success")){
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "User: " + username + " Login Success", Toast.LENGTH_SHORT).show();
                    }
                });

                Intent mapIntent = new Intent(this, MapsActivity.class);
                mapIntent.putExtra("USERNAME", username);
                startActivity(mapIntent);
            }else{

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "User: " + username + " Incorrect password!", Toast.LENGTH_LONG).show();
                    }
                });
            }

        }catch(Exception e){
            System.out.println("Exception : " + e.getMessage());
        }
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

        return super.onOptionsItemSelected(item);
    }
}
