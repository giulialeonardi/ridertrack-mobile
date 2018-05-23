package com.mobileapp.ridertrack;

import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
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
    private long actualStartingTime;
    private Location location;
    private float distance;
    private Double distanceToFinishLine;
    private String city;
    private long  currentTime;
    private String type;
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
    private Intent locationService;
    private boolean serviceRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        /*
         * Preventing the screen sleep
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        displayLocationSettingsRequest(this);
        /*
         * Binding the activity to its layout and creating an array list of "Location" objects
         */
        setContentView(R.layout.activity_race);
        finishLine = findViewById(R.id.finish_line);
        speed = findViewById(R.id.speed);
        ImageView share = findViewById(R.id.facebook_sign_in_button);
        time = findViewById(R.id.time);
        locationArrayList = new ArrayList<>();
        serviceRunning = false;
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
        type = intent.getStringExtra("type");
        String startTime = intent.getStringExtra("startingTime");
        String closingTime = intent.getStringExtra("closingTime");
        Log.e(TAG, "Delay: " + delay);
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
                Log.e(TAG, "broadcast receiver" + intent.getExtras());
                Bundle extras = intent.getExtras();
                location = (Location) extras.get(INTENT_EXTRA);
                Log.e(TAG, "location" + location);
                distanceToFinishLine = (Double) extras.getDouble("distance");
                Log.e(TAG, "distance" + distanceToFinishLine);


                if (extras != null) {
                    location = (Location) extras.get(INTENT_EXTRA);
                        Log.e(TAG, "Location received: " + location);
                        locationArrayList.add(location);

                    distanceToFinishLine = (Double) extras.getDouble("distance");
                    try {
                        setDistanceToFinishLine(distanceToFinishLine);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.e("before", "extras.getString");
                    String tracking = extras.getString("tracking");
                    if (tracking != null && tracking.equals("stop")) {
                        Log.e("after", "extras.getString");
                        stopTracking();

                }
                currentTime = SystemClock.elapsedRealtime();
                Log.i(TAG, "Intent Extra key=" + INTENT_EXTRA + ":" + location);

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
        try {
            stopEvent(closingTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

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

    private void stopEvent(String stopTime) throws ParseException {
        if(!stopTime.equals("")) {
            SharedPreferences sp = getSharedPreferences("LastEventClosingTime", MODE_PRIVATE);
            SharedPreferences.Editor Ed = sp.edit();
            Ed.putString("closingTime", stopTime);
            Ed.commit();
         /*
         * Date and time at which the method must be executed
         */
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = dateFormatter.parse(stopTime);

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
                    stopTracking();
                }
            }, date);
        }
    }
    /**
     * StopTracking method closes the RaceActivity and redirects the user back to EventsListActivity
     */
    private void stopTracking(){
        SharedPreferences sp = getSharedPreferences("ActualStartingTime", MODE_PRIVATE);
        SharedPreferences.Editor Ed = sp.edit();
        Ed.putString("ast", "");
        Ed.commit();
        Log.e("before","stopService");
        stopService(locationService);
        Log.e("after","stopService");
        stopChronometer();
        Intent eventsList = new Intent(this, EventsListActivity.class);
        eventsList.putExtra("userId", userId);
        eventsList.putExtra("token", token);
        eventsList.putExtra("delay", delay);
        startActivity(eventsList);
        finish();
    }
    /**
     * StartChronometer method starts the chronometer
     */
    public void startChronometer() {
        SharedPreferences sp = getSharedPreferences("ActualStartingTime", MODE_PRIVATE);
        String ast=sp.getString("ast", "");
        SharedPreferences sp1 = getSharedPreferences("Counters", MODE_PRIVATE);
        final String spd = sp1.getString("speed", "0,00");
        final String distance = sp1.getString("distance", "0,00");
        Log.e("RACE", "Il valore di ast è "+ast);
        if(ast.equals("")) {
            actualStartingTime = SystemClock.elapsedRealtime();
            SharedPreferences.Editor Ed = sp.edit();
            Ed.putString("ast", String.valueOf(actualStartingTime));
            Ed.commit();
            startLocationService();
        }else{
            actualStartingTime = Long.valueOf(ast);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speed.setText(spd);
                    finishLine.setText(distance);
                }
            });
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                time.setBase(actualStartingTime);
                time.start();
                startingTime = time.getBase();
            }
        });
    }
    /**
     * StopChronometer method stops the chronometer
     */
    public void stopChronometer() {
        Log.e("before","stopchronometer");

        time.stop();
        SharedPreferences sp = getSharedPreferences("ActualStartingTime", MODE_PRIVATE);
        SharedPreferences.Editor Ed = sp.edit();
        Ed.putString("ast", "");
        Ed.commit();

        Log.e("after","stopchronometer");
    }
    /**
     * StartLocationService method starts LocationService, sharing data about current user and selected event.
     */
    private void startLocationService()
    {
        Log.e(TAG, "START LOCATION SERVICE");
        locationService = new Intent(this, LocationService.class);
        locationService.putExtra("userId", userId);
        locationService.putExtra("token", token);
        locationService.putExtra("eventId", eventId);
        locationService.putExtra("delay", delay);
        startService(locationService);
        serviceRunning = true;
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
        SharedPreferences sp = getSharedPreferences("Counters", MODE_PRIVATE);
        SharedPreferences.Editor Ed = sp.edit();
        Ed.putString("speed", String.valueOf(round(speed)));
        Ed.commit();
        startingTime = currentTime;
    }
    /**
     * SetDistanceToFinishLine method sets to UI the updated distance to the finish line
     * @param distanceToFinishLine: double variable, containing distance to the finish line in meters
     * @throws JSONException: Thrown to indicate a problem with the JSON API
     */
    private void setDistanceToFinishLine(Double distanceToFinishLine) throws JSONException {
        final String dist = String.valueOf(round(distanceToFinishLine.floatValue()));
        SharedPreferences sp = getSharedPreferences("Counters", MODE_PRIVATE);
        SharedPreferences.Editor Ed = sp.edit();
        Ed.putString("distance", dist);
        Ed.commit();
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
                }, 3000);
            }
        });
    }
    /**
     * ShareDialog function handles facebook sharing.
     * @param string: String variable, containing URL of the image to be shared
     */
    public void shareDialog(String string) {
        String message = "";

        if(actualStartingTime == 0){
            message = "Hey, there! I'll compete in "+name+". Track me on Ridertrack.";
        }else{
            message = "Hey, there! I'm competing in "+name+". Track me on Ridertrack.";
        }

        if (shareDialog.canShow(SharePhotoContent.class)) {
            /*
             * Create an object
             */
            ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                    .putString("og:type", "fitness.course")
                    .putString("og:title", "Ridertrack competition")
                    .putString("og:url","https://rider-track-dev.herokuapp.com/events/"+eventId)
                    .putString("og:image", string)
                    .putString("og:description",
                            message)
                    .putBoolean("og:rich_attachment", true)
                    .build();
            /*
             * Create an action
             */
            String actionType = "";
            if(type.equals("running") || type.equals("hiking") || type.equals("triathlon") || type.equals("other")){
                actionType = "fitness.run";
            }if(type.equals("cycling")){
                actionType = "fitness.bikes";
            }
            ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                    .setActionType(actionType)
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
                    if(status.equals("ongoing")) {
                        Log.e(TAG, "Recursive check succeeded");
                        startChronometer();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView stat = findViewById(R.id.status);
                                Resources res = getResources();
                                String text = String.format(res.getString(R.string.started), delay);
                                stat.setText(text);
                            }
                        });
                    }else{
                        /*
                         * Triggering recursion to wait for the event status to turn into "ongoing"
                         */
                        recursiveCheck();
                        Log.e(TAG, "RecursiveCheck called ");
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
                     * Handling response
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

    /**
     * DisplayLocationSettingsRequest method shows to user a pop up requiring the permission to activate GPS of the
     * mobile phone.
     * @param context: context in which method is called
     */
    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            /*
                             * Show the dialog by calling startResolutionForResult(), and check the result
                             * in onActivityResult().
                             */
                            status.startResolutionForResult(RaceActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }
    /**
     * Release memory when the UI becomes hidden or when system resources become low.
     * @param level the memory-related event that was raised.
     */
    public void onTrimMemory(int level) {

        /*
         * Determine which lifecycle or system event was raised.
         */
        switch (level) {

            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:

                /*
                 * Release any UI objects that currently hold memory.
                 * The user interface has moved to the background.
                 */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:

                /*
                 * Release any memory that your app doesn't need to run.
                 * The device is running low on memory while the app is running.
                 * The event raised indicates the severity of the memory-related event.
                 * If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                 * begin killing background processes.
                 */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:

                /*
                 * Release as much memory as the process can.
                 * The app is on the LRU list and the system is running low on memory.
                 * The event raised indicates where the app sits within the LRU list.
                 * If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                 * the first to be terminated.
                 */

                break;

            default:
                /*
                 * Release any non-critical data structures.
                 * The app received an unrecognized memory level value
                 * from the system. Treat this as a generic low-memory message.
                 */
                break;
        }
    }
}