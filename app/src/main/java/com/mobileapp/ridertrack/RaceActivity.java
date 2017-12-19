package com.mobileapp.ridertrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
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
    private String token;
    private String eventId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //prevents the screen sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_race);
        locationArrayList = new ArrayList<>();

       Intent intent = getIntent();
       userId = intent.getStringExtra("userId");
       token = intent.getStringExtra("token");
       eventId = intent.getStringExtra("eventId");

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
                        Log.i("ACTIVITY", "Intent Extra key=" + INTENT_EXTRA + ":" + location);
                        locationArrayList.add(location);
                        Log.i("ACTIVITY", "Array size" + ":" + locationArrayList.size());
                        //calculates the speed only at the reception of at least two different inputs
                        if(locationArrayList.size() > 1){
                                distance = locationArrayList.get(locationArrayList.size() - 2).distanceTo(locationArrayList.get(locationArrayList.size() - 1));
                                Log.e("Distance", String.valueOf(distance));
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
        Intent startLocationService = new Intent(this, LocationService.class);
        startLocationService.putExtra("userId", userId);
        startLocationService.putExtra("token", token);
        startLocationService.putExtra("eventId", eventId);
        startService(startLocationService);
    }


    public void startChronometer() {
        time.start();
        startLocationService();
    }

    public void stopChronometer() {
        time.stop();
    }

    public void calculateSpeed(float distance){
        float timeDistance = currentTime - startingTime;
        float speed = 3.6f * ((distance*1000)/timeDistance);
        Log.e("Speed: ", String.valueOf(speed));
        this.speed.setText(String.valueOf(round(speed,2)));
        startingTime = currentTime;
    }

    public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }
    private void updateFinishLineDistance(float distance){
        this.finishLine.setText(String.valueOf(round(distance,2)));
    }
}