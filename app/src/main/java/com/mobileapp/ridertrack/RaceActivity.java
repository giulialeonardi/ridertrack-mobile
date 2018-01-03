package com.mobileapp.ridertrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
    private ArrayList<Location> locationArrayList;
    private BroadcastReceiver receiver;
    private LocalBroadcastManager lbm;
    private String userId;
    private String token;
    private String eventId;
    private int delay;
    private String name;
    private Location startingPoint;
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private Double distanceToFinishLine;
    private String city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //prevents the screen sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_race);
        locationArrayList = new ArrayList<>();
        finishLine = findViewById(R.id.finish_line);
        speed = findViewById(R.id.speed);
        Button share = findViewById(R.id.facebook_sign_in_button);
        time = findViewById(R.id.time);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        token = intent.getStringExtra("token");
        eventId = intent.getStringExtra("eventId");
        delay = intent.getIntExtra("delay", 5);
        name = intent.getStringExtra("name");
        city = intent.getStringExtra("city");
        String startTime = intent.getStringExtra("startingTime");
        try {
            startTracking(startTime);
            //time calculation
            time.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                    int h = (int) (time / 3600000);
                    int m = (int) (time - h * 3600000) / 60000;
                    int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                    String t = (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
                    chronometer.setText(t);
                }
            });
            time.setText("00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (intent.getStringExtra("startingPoint") != null){
        String[] latLng = intent.getStringExtra("startingPoint").split(",");
        Double lat = Double.parseDouble(latLng[0]);
        Double lng = Double.parseDouble(latLng[1]);
        startingPoint = new Location("");//provider name is unnecessary
        startingPoint.setLatitude(lat);//your coords of course
        startingPoint.setLongitude(lng);
        }
        share.setVisibility(View.VISIBLE);
        share.setClickable(true);




        //speed calculation
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if(intent.hasCategory(INTENT_EXTRA)){
                if (extras != null) {
                    location = (Location) extras.get(INTENT_EXTRA);
                }

                if(intent.hasCategory("distance")) {
                    distanceToFinishLine = (Double) extras.getDouble("distance");
                    try {
                        setDistanceToFinishLine(distanceToFinishLine);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                    if(intent.hasCategory("tracking")) {
                        String tracking = extras.getString("tracking");
                        if (tracking != null && tracking.equals("stop")) {
                            stopTracking();
                        }
                    }
                    currentTime = SystemClock.elapsedRealtime();
                    Log.i("ACTIVITY", "Intent Extra key=" + INTENT_EXTRA + ":" + location);
                    locationArrayList.add(location);
                    Log.i("ACTIVITY", "Array size" + ":" + locationArrayList.size());
                    //calculates the speed only at the reception of at least two different inputs
                    if (locationArrayList.size() > 1) {
                        distance = locationArrayList.get(locationArrayList.size() - 2).distanceTo(locationArrayList.get(locationArrayList.size() - 1));
                        Log.e("Distance", String.valueOf(distance));
                        calculateSpeed(distance);

                    }
                }
            }
        };

        //distance to finish line calculation

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RetrieveMap().execute();
            }
        });
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.e("RESULT", "Success");
                //   getAndAddSlots();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("RESULT", "error=" + error.toString());
                Toast.makeText(RaceActivity.this, "Unable to Share Image to Facebook.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        try {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }catch(Exception e){
            e.printStackTrace();
        }}

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
        startLocationService.putExtra("delay", delay);
        startService(startLocationService);
    }
    private void startTracking(String startTime) throws ParseException {
        //the Date and time at which you want to execute
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = dateFormatter.parse(startTime);

        //Now create the time and schedule it
        Timer timer = new Timer();

        //Use this if you want to execute it once
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new CheckIfOngoing().execute();
            }
        }, date);
    }

    public void startChronometer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                time.setBase(SystemClock.elapsedRealtime());
                time.start();
                startingTime = time.getBase();
                startLocationService();
            }
        });
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


    class RetrieveMap extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String string;
            if(location != null) {
                string = "http://maps.googleapis.com/maps/api/staticmap?&zoom=16&size=800x400&maptype=roadmap&sensor=true&center=" + location.getLatitude() + "," + location.getLongitude() +
                        "&markers=color:red|" + location.getLatitude() + "," + location.getLongitude();
            }else {
                if (startingPoint != null) {
                    string = "http://maps.googleapis.com/maps/api/staticmap?&zoom=16&size=800x400&maptype=roadmap&sensor=true&center=" +
                            startingPoint.getLatitude() + "," + startingPoint.getLongitude() +
                            "&markers=color:red|" + startingPoint.getLatitude() + "," + startingPoint.getLongitude();
                }else{
                    string = "http://maps.googleapis.com/maps/api/staticmap?&zoom=16&size=800x400&maptype=roadmap&sensor=true&center=" + city +
                            "&markers=color:red|" + city;
                }
            }
            URL url = null;
            HttpURLConnection connection = null;
            try {
                url = new URL(string);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) { // check HTTP code
                    shareDialog(string);
                }

                return null;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
    }
    public void shareDialog(String string) {

        if (shareDialog.canShow(SharePhotoContent.class)) {

// Create an object
            ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                    .putString("og:type", "fitness.course")
                    .putString("og:url","https://rider-track-dev.herokuapp.com/events/"+eventId)
                    .putString("og:image", string)
                    .putString("og:description",
                            "Hey, there! I'm competing in "+name+". Track me on Ridertrack.")
                    .putBoolean("og:rich_attachment", true)
                    .putString("og:title", "Hey, there! I'm competing")
                    .build();

            // Create an action
            ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                    .setActionType("fitness.run")
                    .putObject("fitness:course", object)
                    .build();
            // Create the content
            ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                    .setPreviewPropertyName("fitness:course").setAction(action)
                    .build();

            ShareDialog.show(this, content);
        }
    }
    private void setDistanceToFinishLine(Double distanceToFinishLine) throws JSONException {
        final String dist = String.valueOf(round(distanceToFinishLine.floatValue(), 2));

            Log.e("Distance", String.valueOf(distanceToFinishLine));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finishLine.setText(dist);
                }
            });

    }

    public class CheckIfOngoing extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = null;
                String response = null;
                url = new URL("https://rider-track-dev.herokuapp.com/api/events/"+ eventId);
                //create the connection
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", "JWT " + token);
                //set the request method to GET
                connection.setRequestMethod("GET");

                //read in the data from input stream, this can be done a variety of ways
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    //create your inputsream
                    String line = "";

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    StringBuffer sb = new StringBuffer("");

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }
                    in.close();

                    JSONObject resp = new JSONObject(sb.toString());
                    JSONObject event = resp.getJSONObject("event");
                    String status = event.getString("status");
                    Log.e("Response", status);
                    if(status.equals("ongoing")){
                       startChronometer();
                       TextView stat = findViewById(R.id.status);
                       stat.setText(R.string.started);
                    }else{
                        recursiveCheck();
                    }
                    return true;
                }else{
                    //create your inputsream
                    String line = "";

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            connection.getErrorStream()));

                    StringBuffer sb = new StringBuffer("");

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }
                    Log.e("Response", sb.toString());
                    in.close();
                    return true;
                }
            } catch (Exception e) {

               Log.e("[RaceActivity]", e.toString());
                return false;

            } finally {
                connection.disconnect();
            }
        }
    }

    private void recursiveCheck(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler ha = new Handler();
                ha.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new CheckIfOngoing().execute();
                        Log.e("Event not ", "started");
                    }
                }, 5000);
            }
        });
    }

    private void stopTracking(){
        stopChronometer();
        Intent eventsList = new Intent(getApplicationContext(), EventsListActivity.class);
        eventsList.putExtra("userId", userId);
        eventsList.putExtra("token", token);
        startActivity(eventsList);
    }
}