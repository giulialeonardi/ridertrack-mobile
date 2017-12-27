package com.mobileapp.ridertrack;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class LocationService extends Service
{
    private static final String TAG = "LOCATION SERVICE";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private String  userId;
    private String token;
    private String eventId;
    private ArrayList<Location> listOfLocations;
    private ArrayList<String> listOfTimestamps;
    private int delay;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
            listOfLocations = new ArrayList<>();
            listOfTimestamps = new ArrayList<>();

        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            //calculate timestamp
            Long tsLong = System.currentTimeMillis()/1000;
            String timeStamp = tsLong.toString();
            listOfLocations.add(location);
            listOfTimestamps.add(timeStamp);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }


    public class Constants {
        public static final String INTENT_ACTION = "com.mobileapp.ridertrack.LOCATION";
        public static final String INTENT_EXTRA   = "Extra data";
    }


    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        userId = intent.getStringExtra("userId");
        token = intent.getStringExtra("token");
        eventId = intent.getStringExtra("eventId");
        delay = intent.getIntExtra("delay", 10);
        Log.e("I'm sending data to "+ eventId, " every "+ delay+ " seconds");
        //Delay is in millis
        final int delayMillis = delay * 1000;
        Log.e("[Location Service]", String.valueOf(delayMillis));
        final Handler ha = new Handler();
        ha.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (listOfLocations.size() > 0) {
                    Location location = listOfLocations.get(listOfLocations.size()-1);
                    String timeStamp = listOfTimestamps.get(listOfTimestamps.size()-1);
                    //send to server
                    new SendPostRequest().execute(userId, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), timeStamp);
                    //send to activity
                    Intent intent = new Intent();
                    intent.setAction(Constants.INTENT_ACTION);
                    intent.putExtra(Constants.INTENT_EXTRA, location);
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
                    lbm.sendBroadcast(intent);
                    ha.postDelayed(this, delayMillis);
                    listOfLocations.remove(listOfLocations.size()-1);
                    listOfTimestamps.remove(listOfTimestamps.size()-1);
                } else {
                    Log.e("No data available", "Wait " + delayMillis/2000 + " seconds");
                    ha.postDelayed(this, delayMillis/2);
                }
            }
        }, delayMillis);

        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... params) {

            try {
                URL url = new URL("https://rider-track-dev.herokuapp.com/api/events/"+eventId+"/participants/positions"); // here is your URL path
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("userId", params[0]);
                postDataParams.put("lat", params[1]);
                postDataParams.put("lng", params[2]);
                postDataParams.put("timestamp", params[3]);
                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestProperty("Authorization", "JWT " + token);
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
                    Log.e("false", String.valueOf(responseCode));
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
