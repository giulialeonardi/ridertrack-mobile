package com.mobileapp.ridertrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;

import static com.mobileapp.ridertrack.LocationService.Constants.INTENT_EXTRA;

public class RaceActivity extends AppCompatActivity {

    private Chronometer time;
    private TextView speed;
    private TextView finishLine;
    private Button share;
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
    private int delay;
    private String name;
    private Location startingPoint;
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //prevents the screen sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_race);
        locationArrayList = new ArrayList<>();
        finishLine = findViewById(R.id.finish_line);
        speed = findViewById(R.id.speed);
        share = findViewById(R.id.facebook_sign_in_button);
        time = findViewById(R.id.time);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        token = intent.getStringExtra("token");
        eventId = intent.getStringExtra("eventId");
        delay = intent.getIntExtra("delay", 10);
        name = intent.getStringExtra("name");
        String[] latLng = intent.getStringExtra("startingPoint").split(",");
        Double lat = Double.parseDouble(latLng[0]);
        Double lng = Double.parseDouble(latLng[1]);
        startingPoint = new Location("");//provider name is unnecessary
        startingPoint.setLatitude(lat);//your coords of course
        startingPoint.setLongitude(lng);
        share.setVisibility(View.VISIBLE);
        share.setClickable(true);

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
        startChronometer();
        startingTime = time.getBase();

        //speed calculation
        receiver = new BroadcastReceiver() {
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

    class RetrieveMap extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String string;
            if(location != null) {
                string = "http://maps.googleapis.com/maps/api/staticmap?&zoom=16&size=800x400&maptype=roadmap&sensor=true&center=" + location.getLatitude() + "," + location.getLongitude() +
                        "&markers=color:black|" + location.getLatitude() + "," + location.getLongitude();
            }else{
                string = "http://maps.googleapis.com/maps/api/staticmap?&zoom=16&size=800x400&maptype=roadmap&sensor=true&center=" +
                        startingPoint.getLatitude() + "," + startingPoint.getLongitude() +
                        "&markers=color:black|" + startingPoint.getLatitude() + "," + startingPoint.getLongitude();
            }
            URL url = null;
            HttpURLConnection connection = null;
            try {
                url = new URL(string);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) { // check HTTP code
                    Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                    shareDialog(bitmap, string);
                }

                return null;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
    }
    public void shareDialog(Bitmap bitmap, String string) {

        if (shareDialog.canShow(SharePhotoContent.class)) {

// Create an object
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .setUserGenerated(true)
                    .build();
            ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                    .putString("og:type", "fitness.course")
                    .putString("og:url","https://rider-track-dev.herokuapp.com/api/events/"+eventId+"/participants/positions")
                    .putString("og:image", string)
                    .putString("og:description",
                            "Track me on Ridertrack")
                    .putString("og:title", "Hey, there! I'm competing")
                    .build();

            // Create an action
            ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                    .setActionType("fitness.run")
                    .putBoolean("fb:explicitly_shared", true)
                    .putString("message", "Hey, there! I'm competing")
                    .putObject("fitness:course", object)
                    .build();
            // Create the content
            ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                    .setPreviewPropertyName("fitness:course").setAction(action)
                    .build();

            ShareDialog.show(this, content);
        }
    }
}