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
import android.widget.Chronometer;
import android.widget.ImageView;
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

/**
 * RaceActivity is created every time user clicks on an event box, in EventsListActivity.
 * This activity displays a timer, that automatically starts when the race starts, a speed calculator and
 * updated distance to the finish line.
 * A button on the bottom of the screen allow user to share on facebook his/her presence at the race.
 */
public class RaceActivity extends AppCompatActivity {

    private static final String TAG = "RaceActivity";
    /**
     * Variables related to UI
     */
    private Chronometer time;
    private TextView speed;
    private TextView finishLine;
    /**
     * Variables related to race parameters
     */
    private String name;
    private String eventId;
    private Location startingPoint;
    private long startingTime;
    private Location location;
    private float distance;
    private Double distanceToFinishLine;
    private String city;
    private long  currentTime;
    /**
     * Variables related to current user
     */
    private String userId;
    private String token;
    private int delay;
    /**
     * Variables related to activity management
     */
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private ArrayList<Location> locationArrayList;
    private BroadcastReceiver receiver;
    private LocalBroadcastManager lbm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * Preventing the screen sleep
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /*
         * Binding the activity to its layout and creating an array list of "Location" objects
         */
        setContentView(R.layout.activity_race);
        finishLine = findViewById(R.id.finish_line);
        speed = findViewById(R.id.speed);
        ImageView share = findViewById(R.id.facebook_sign_in_button);
        time = findViewById(R.id.time);
        locationArrayList = new ArrayList<>();
        /*
         * Retrieving information about current user and selected event
         */
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        token = intent.getStringExtra("token");
        eventId = intent.getStringExtra("eventId");
        delay = intent.getIntExtra("delay", 5);
        name = intent.getStringExtra("name");
        city = intent.getStringExtra("city");
        String startTime = intent.getStringExtra("startingTime");
        /*
         * Setting the chronometer
         */
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
        /*
         * Setting starting point
         */
        if (intent.getStringExtra("startingPoint") != null){
        String[] latLng = intent.getStringExtra("startingPoint").split(",");
        Double lat = Double.parseDouble(latLng[0]);
        Double lng = Double.parseDouble(latLng[1]);
        startingPoint = new Location(""); //Provider name is unnecessary
        startingPoint.setLatitude(lat);
        startingPoint.setLongitude(lng);
        }
        share.setVisibility(View.VISIBLE);
        share.setClickable(true);
        /*
         * Setting broadcast receiver: at the reception of a new location, it adds it to the array of Locations,
         * at the reception of a new value "distance to the finish line", it displays it on the screen;
         * at the reception of a "stop tracking" message, it ends the dispatch of data.
         */
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
                Log.i(TAG, "Intent Extra key=" + INTENT_EXTRA + ":" + location);
                locationArrayList.add(location);
                    /*
                     * Calculating speed only at the reception of at least two different inputs
                     */
                    if (locationArrayList.size() > 1) {
                        distance = locationArrayList.get(locationArrayList.size() - 2).distanceTo(locationArrayList.get(locationArrayList.size() - 1));
                        calculateSpeed(distance);

                    }
                }
            }
        };
        /*
         * Setting share dialog for Facebook sharing
         */
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrieveMap();
            }
        });
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Toast.makeText(RaceActivity.this, "Shared correctly on Facebook!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("RESULT", "error=" + error.toString());
                Toast.makeText(RaceActivity.this, "Unable to share on Facebook.", Toast.LENGTH_SHORT).show();
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

    /**
     * onResume registers the broadcast receiver.
     */
    @Override
    public void onResume() {
        super.onResume();
        lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(receiver, new IntentFilter(LocationService.Constants.INTENT_ACTION));
        super.onResume();
    }
    /**
     * onPause unregisters the broadcast receiver.
     */
    @Override
    public void onPause() {
        lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(receiver);
        super.onPause();
    }

    /**
     * StartTracking method runs "CheckIfOngoing" method at the data and time setted
     * @param startTime: String variable, containing the time in format "yyyy-MM-dd HH:mm:ss" at which the method must be runned
     * @throws ParseException: Signals that an error has been reached unexpectedly while parsing.
     */
    private void startTracking(String startTime) throws ParseException {
        /*
         * Date and time at which the method must be executed
         */
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = dateFormatter.parse(startTime);
        /*
         * Creating the timer
         */
        Timer timer = new Timer();
        /*
         * To execute method once
         */
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new CheckIfOngoing().execute();
            }
        }, date);
    }
    /**
     * StopTracking method closes the RaceActivity and redirects the user back to EventsListActivity
     */
    private void stopTracking(){
        stopChronometer();
        Intent eventsList = new Intent(getApplicationContext(), EventsListActivity.class);
        eventsList.putExtra("userId", userId);
        eventsList.putExtra("token", token);
        startActivity(eventsList);
        finish();
    }
    /**
     * StartChronometer method starts the chronometer
     */
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
    /**
     * StopChronometer method stops the chronometer
     */
    public void stopChronometer() {
        time.stop();
    }
    /**
     * StartLocationService method starts LocationService, sharing data about current user and selected event.
     */
    private void startLocationService()
    {
        Intent startLocationService = new Intent(this, LocationService.class);
        startLocationService.putExtra("userId", userId);
        startLocationService.putExtra("token", token);
        startLocationService.putExtra("eventId", eventId);
        startLocationService.putExtra("delay", delay);
        startService(startLocationService);
    }
    /**
     * CalculateSpeed method calculates the average speed at which the user ran the distance passed as param.
     * @param distance: float variable, containg the distance (in meters) covered by the user
     *                between two consequent detected locations
     */
    public void calculateSpeed(float distance){
        float timeDistance = currentTime - startingTime;
        float speed = 3.6f * ((distance*1000)/timeDistance);
        Log.e("Speed: ", String.valueOf(speed));
        this.speed.setText(String.valueOf(round(speed)));
        startingTime = currentTime;
    }
    /**
     * SetDistanceToFinishLine method sets to UI the updated distance to the finish line
     * @param distanceToFinishLine: double variable, containing distance to the finish line in meters
     * @throws JSONException: Thrown to indicate a problem with the JSON API
     */
    private void setDistanceToFinishLine(Double distanceToFinishLine) throws JSONException {
        final String dist = String.valueOf(round(distanceToFinishLine.floatValue()));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finishLine.setText(dist);
            }
        });
    }
    /**
     * BigDecimal method rounds the float value passed as param at the decimal place required, returning the related
     * BigDecimal
     * @param d : float variable, containing the value to be rounded
     * @return Rounded value, BigDecimal
     */
    public static BigDecimal round(float d) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd;
    }
    /**
     * RecursiveCheck method calls "CheckIfOngoing" function every 5 seconds
     */
    private void recursiveCheck(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler ha = new Handler();
                ha.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new CheckIfOngoing().execute();
                    }
                }, 5000);
            }
        });
    }
    /**
     * ShareDialog function handles facebook sharing.
     * @param string: String variable, containing URL of the image to be shared
     */
    public void shareDialog(String string) {

        if (shareDialog.canShow(SharePhotoContent.class)) {
            /*
             * Create an object
             */
            ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                    .putString("og:type", "fitness.course")
                    .putString("og:url","https://rider-track-dev.herokuapp.com/events/"+eventId)
                    .putString("og:image", string)
                    .putString("og:description",
                            "Hey, there! I'm competing in "+name+". Track me on Ridertrack.")
                    .putBoolean("og:rich_attachment", true)
                    .putString("og:title", "Hey, there! I'm competing")
                    .build();
            /*
             * Create an action
             */
            ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                    .setActionType("fitness.run")
                    .putObject("fitness:course", object)
                    .build();
            /*
             * Create the content
             */
            ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                    .setPreviewPropertyName("fitness:course").setAction(action)
                    .build();
            /*
             * Shows dialog
             */
            ShareDialog.show(this, content);
        }
    }
    /**
     * RetrieveMap method retrieves the location on the map and starts facebook sharing
     */
    private void retrieveMap(){
        String string;
        if (location != null) {
            string = "http://maps.googleapis.com/maps/api/staticmap?&zoom=16&size=800x400&maptype=roadmap&sensor=true&center=" + location.getLatitude() + "," + location.getLongitude() +
                    "&markers=color:red|" + location.getLatitude() + "," + location.getLongitude();
        } else {
            if (startingPoint != null) {
                string = "http://maps.googleapis.com/maps/api/staticmap?&zoom=16&size=800x400&maptype=roadmap&sensor=true&center=" +
                        startingPoint.getLatitude() + "," + startingPoint.getLongitude() +
                        "&markers=color:red|" + startingPoint.getLatitude() + "," + startingPoint.getLongitude();
            } else {
                string = "http://maps.googleapis.com/maps/api/staticmap?&zoom=16&size=800x400&maptype=roadmap&sensor=true&center=" + city +
                        "&markers=color:red|" + city;
            }
        }
        shareDialog(string);
    }

    /**
     * Asynchronous method managing GET requests to server.
     */
    public class CheckIfOngoing extends AsyncTask<String, Void, Boolean> {
        /**
         * CheckIfOngoing method performs a GET request to the server, in order to retrieve the status of the event.
         * Params not needed.
         * @param strings: not needed.
         * @return Boolean: true if the request ends correctly, false otherwise
         */
        @Override
        protected Boolean doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = null;
                url = new URL("https://rider-track-dev.herokuapp.com/api/events/"+ eventId);
                /*
                 * Creating connection
                 */
                connection = (HttpURLConnection) url.openConnection();
                /*
                 * Setting the request property "Authorization" to personal token of current user
                 */
                connection.setRequestProperty("Authorization", "JWT " + token);
                /*
                 * Setting the request method to GET
                 */
                connection.setRequestMethod("GET");
                /*
                 * Reading in the data from input stream
                 */
                int responseCode = connection.getResponseCode();
                /*
                 * If the response code is 200, the GET request has concluded successfully
                 */
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    /*
                     * Creating input stream
                     */
                    String line = "";
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    /*
                     * Closing input stream
                     */
                    in.close();
                    /*
                     * Handling response
                     */
                    JSONObject resp = new JSONObject(sb.toString());
                    JSONObject event = resp.getJSONObject("event");
                    String status = event.getString("status");
                    /*
                     * Starting chronometer if the status of the event is "ongoing"
                     */
                    if(status.equals("ongoing")){
                       startChronometer();
                       TextView stat = findViewById(R.id.status);
                       stat.setText(R.string.started);
                    }else{
                        /*
                         * Triggering recursion to wait for the event status to turn into "ongoing"
                         */
                        recursiveCheck();
                    }
                    return true;
                }else{
                    /*
                     * Creating error stream
                     */
                    String line = "";
                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            connection.getErrorStream()));
                    StringBuffer sb = new StringBuffer("");
                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }
                    /*
                     * Handling response: error pop up shown
                     */
                    Log.e(TAG, sb.toString());
                    in.close();
                    return true;
                }
            } catch (Exception e) {
               Log.e(TAG, e.toString());
                return false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }
}