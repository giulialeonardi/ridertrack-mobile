package com.mobileapp.ridertrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import static com.mobileapp.ridertrack.LocationService.Constants.INTENT_EXTRA;

public class RaceActivity extends AppCompatActivity {

    private Chronometer time;
    private TextView speed;
    private TextView finishLine;
    private long startingTime;
    private Location location;
    private float distance;
    private long  currentTime;
    //private Location startingPoint;
    private ArrayList<Location> locationArrayList;
    private BroadcastReceiver receiver;
    private LocalBroadcastManager lbm;
    private String userId;
    //TODO: get userId from login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //prevents the screen sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_race);
        locationArrayList = new ArrayList<>();

        //time calculation
        time = findViewById(R.id.time);
        time.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener(){
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                int h   = (int)(time /3600000);
                int m = (int)(time - h*3600000)/60000;
                int s= (int)(time - h*3600000- m*60000)/1000 ;
                String t = (h < 10 ? "0"+h: h)+":"+(m < 10 ? "0"+m: m)+":"+ (s < 10 ? "0"+s: s);
                chronometer.setText(t);
            }
        });
        time.setText("00:00:00");
        startChronometer();
        startingTime = time.getBase();

        //speed calculation
        speed = findViewById(R.id.speed);
        receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                        location = (Location) extras.get(INTENT_EXTRA);
                        currentTime = SystemClock.elapsedRealtime();
                        String lat = String.valueOf(location.getLatitude());
                        String lng = String.valueOf(location.getLongitude());
                        Long tsLong = System.currentTimeMillis()/1000;
                        String timeStamp = tsLong.toString();
                        new SendPostRequest().execute(userId, lat, lng, timeStamp);
                        Log.i("ACTIVITY", "Intent Extra key=" + INTENT_EXTRA + ":" + location);
                        locationArrayList.add(location);
                        Log.i("ACTIVITY", "Array size" + ":" + locationArrayList.size());

                        //calculates the speed only at the reception of at least two different inputs
                        if(locationArrayList.size() > 1){
                                distance = locationArrayList.get(locationArrayList.size() - 2).distanceTo(locationArrayList.get(locationArrayList.size() - 1));
                                calculateSpeed(distance);

                    }
                }
            }
        };


        //distance to finish line calculation
        finishLine = findViewById(R.id.finish_line);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register the broadcast receiver.
        lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(receiver, new IntentFilter(LocationService.Constants.INTENT_ACTION));
        super.onResume();
    }

    @Override
    public void onPause() {
        // Unregister the receiver
        lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(receiver);
        super.onPause();
    }


    private void startLocationService()
    {
        startService(new Intent(this, LocationService.class));
    }


    public void startChronometer() {
        time.start();
        startLocationService();
    }

    public void stopChronometer() {
        time.stop();
    }

    public void calculateSpeed(float distance){
    currentTime = SystemClock.elapsedRealtime();
    float timeDistance = currentTime - startingTime;
    float speed = (distance*1000)/timeDistance;
    Log.e("Speed: ", String.valueOf(speed));
    this.speed.setText(String.valueOf(round(speed,3)));
    startingTime = currentTime;
    }

    public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... params) {

            try {

                URL url = new URL("https://rider-track-dev.herokuapp.com/api/events/:eventId/participants/positions"); // here is your URL path
                //TODO: get eventId
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("userId", params[0]);
                postDataParams.put("lat", params[1]);
                postDataParams.put("lng", params[2]);
                postDataParams.put("timestamp", params[3]);
                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }
}