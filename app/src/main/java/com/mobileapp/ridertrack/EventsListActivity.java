package com.mobileapp.ridertrack;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import javax.net.ssl.HttpsURLConnection;

public class EventsListActivity extends AppCompatActivity {

    private String userId;
    private String token;
    private View eventBox;
    private View menuView;
    private ArrayList<Event> eventsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventslist);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        token = intent.getStringExtra("token");
        Log.e("Token", token);

        new GetListOfEvents().execute();

        menuView = findViewById(R.id.menu);
        menuView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EventsListActivity.this, menuView);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.menu_eventslist, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(
                                EventsListActivity.this,
                                "You Clicked : " + item.getTitle(),
                                Toast.LENGTH_SHORT
                        ).show();

                        // Handle item selection
                        switch (item.getItemId()) {
                            case R.id.position:
                                //newGame();
                                return true;
                            case R.id.logout:
                                //showHelp();
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popup.show(); //showing popup menu
            }
        }); //closing the setOnClickListener method

                eventBox = findViewById(R.id.event_box);
                //TODO: put the event id as the box id
                eventBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent raceActivity = new Intent(getApplicationContext(), RaceActivity.class);
                        raceActivity.putExtra("userId", userId);
                        raceActivity.putExtra("token", token);
                        //raceActivity.putExtra("eventId", eventId);
                        startActivity(raceActivity);
                    }
                });
            }


    public class GetListOfEvents extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = null;
                String response = null;
                url = new URL("https://rider-track-dev.herokuapp.com/api/users/" + userId + "/enrolledEvents");
                Log.e("[EventsListActivity]", "URL error");
                //create the connection
                connection = (HttpURLConnection) url.openConnection();
                Log.e("[EventsListActivity]", "open connection error");
                connection.setRequestProperty("Authorization", "JWT " + token);
                Log.e("[EventsListActivity]", "error after token");
                //set the request method to GET
                connection.setRequestMethod("GET");
                Log.e("[EventsListActivity]", "error after GET");

                //read in the data from input stream, this can be done a variety of ways
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    //create your inputsream
                    Log.e("[EventsListActivity]", "input stream if");
                    String line = "";

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    StringBuffer sb = new StringBuffer("");

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();

                    /*if( non ci sono eventi in programma){
                    }else{
                     */
                    eventsList = new ArrayList<>();
                    splitResponse(sb);
                    Log.e("Number of events", String.valueOf(eventsList.size()));
                    for(Event event : eventsList){
                        Log.e("Event name", event.getName());
                    }
                    return true;
                }else{
                    //create your inputsream
                    Log.e("[EventsListActivity]", "input stream else");
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

                longInfo(e.toString());
                return false;

            } finally {
                connection.disconnect();
            }
        }
    }

    public static void longInfo(String str) {
        if(str.length() > 4000) {
            Log.i("Exception", str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            Log.i("Exception", str);
    }

    private void splitResponse(StringBuffer sb) throws JSONException {
        JSONObject response = new JSONObject(sb.toString());
        JSONArray events = response.getJSONArray("events");

        for (int i = 0; i < events.length(); i++) {
            JSONObject jObject = events.getJSONObject(i);
            manageEvent(jObject);
        }
    }

    private void manageEvent(JSONObject jObject) throws JSONException {
        Event event = new Event();
        eventsList.add(event);
        if(jObject.has("name")){
        String name = jObject.getString("name");
        event.setName(name);}
        if(jObject.has("organizerId")){
        String organizerId = jObject.getString("organizerId");
        event.setOrganizerId(organizerId);}
        if(jObject.has("type")){
            String type = jObject.getString("type");
            event.setType(type);}
        if(jObject.has("status")){
            String status = jObject.getString("status");
            event.setStatus(status);}
        if(jObject.has("description")){
            String description = jObject.getString("description");
            event.setDescription(description);}
        if(jObject.has("country")){
            String country = jObject.getString("country");
            event.setCountry(country);}
        if(jObject.has("city")){
            String city = jObject.getString("city");
            event.setCity(city);}
        if(jObject.has("startingDate")){
            String startingDate = jObject.getString("startingDate");
            event.setStartingDate(startingDate);}
        if(jObject.has("startingTime")){
            String startingTime = jObject.getString("startingTime");
            event.setStartingTime(startingTime);}
        if(jObject.has("maxDuration")){
            int maxDuration = jObject.getInt("maxDuration");
            event.setMaxDuration(maxDuration);}
        if(jObject.has("length")){
            int length = jObject.getInt("length");
            event.setLength(length);}
        if(jObject.has("price")){
            Double price = jObject.getDouble("price");
            event.setPrice(price);}
        if(jObject.has("maxParticipants")){
            int maxParticipants = jObject.getInt("maxParticipants");
            event.setMaxParticipants(maxParticipants);}
        if(jObject.has("enrollmentOpeningAt")){
            String enrollmentOpeningAt = jObject.getString("enrollmentOpeningAt");
            event.setEnrollmentOpeningAt(enrollmentOpeningAt);}
        if(jObject.has("enrollmentClosingAt")){
            String enrollmentClosingAt = jObject.getString("enrollmentClosingAt");
            event.setEnrollmentClosingAt(enrollmentClosingAt);}
        if(jObject.has("logo")){
            String logo = jObject.getString("logo");
            event.setLogo(logo);}
        if(jObject.has("created_at")){
            String created_at = jObject.getString("created_at");
            event.setCreatedAt(created_at);}
        if(jObject.has("updated_at")){
            String updated_at = jObject.getString("updated_at");
            event.setUpdatedAt(updated_at);}
    }

}

