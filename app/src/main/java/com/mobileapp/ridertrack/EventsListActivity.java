package com.mobileapp.ridertrack;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class EventsListActivity extends AppCompatActivity {

    private String userId;
    private String token;
    private View menuView;
    private ArrayList<Event> eventsList;
    private View mProgressView;
    private View mListView;
    private String logoData;
    private int delay;
    private LinearLayout scrollView;
    private int counter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventslist);
        counter = 0;
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        token = intent.getStringExtra("token");
        delay = intent.getIntExtra("delay", 5);
        SharedPreferences sp=getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor Ed=sp.edit();
        Ed.putString("delay", String.valueOf(delay));
        Ed.commit();
        mProgressView = findViewById(R.id.login_progress);
        mListView = findViewById(R.id.list);
        showProgress(true);
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
                        // Handle item selection
                        switch (item.getItemId()) {
                            case R.id.position:
                                Intent timeout = new Intent(getApplicationContext(), TimeoutActivity.class);
                                timeout.putExtra("userId", userId);
                                timeout.putExtra("token", token);
                                startActivity(timeout);
                                finish();
                                return true;
                            case R.id.logout:
                                SharedPreferences sp=getSharedPreferences("Login", MODE_PRIVATE);
                                SharedPreferences.Editor Ed=sp.edit();
                                Ed.putString("userId", null);
                                Ed.putString("token", null);
                                Ed.putString("delay", null);
                                Ed.commit();
                                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(main);
                                finish();
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popup.show(); //showing popup menu
            }
        }); //closing the setOnClickListener method


            }



    public class GetListOfEvents extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = null;
                String response = null;
                url = new URL("https://rider-track-dev.herokuapp.com/api/users/" + userId + "/enrolledEvents");
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
                    eventsList = new ArrayList<>();
                    splitResponse(sb);
                    Log.e("Number of events", String.valueOf(eventsList.size()));
                    return true;
                }if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED){
                    String line = "";

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            connection.getErrorStream()));

                    StringBuffer sb = new StringBuffer("");

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Dialog dialog = new Dialog(EventsListActivity.this);
                            dialog.setContentView(R.layout.popup_error);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            Button close = (Button) dialog.findViewById(R.id.close);
                            close.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                    SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
                                    SharedPreferences.Editor Ed = sp.edit();
                                    Ed.putString("userId", null);
                                    Ed.putString("token", null);
                                    Ed.putString("delay", null);
                                    Ed.commit();
                                    Intent main = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(main);
                                    finish();
                                }
                            });
                            dialog.show();
                        }
                        });
                    in.close();
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
    public class GetStartingPoint extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = null;
                String response = null;
                url = new URL("https://rider-track-dev.herokuapp.com/api/events/" + strings[0] + "/route");
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

                    extractStartingPoint(sb, strings[0]);
                    in.close();
                    return true;
                }else{
                    String line = "";

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            connection.getErrorStream()));

                    StringBuffer sb = new StringBuffer("");

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }
                    in.close();
                    return false;
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
            //Log.i("Exception", str.substring(0, 4000));
            //longInfo(str.substring(4000));
            Log.i("Exception", str.substring(str.length()-200, str.length()-1));
        } else
            Log.i("Exception", str);
    }

    private void splitResponse(StringBuffer sb) throws JSONException, ParseException {
        JSONObject response = new JSONObject(sb.toString());
        JSONArray events = response.getJSONArray("events");
        if (events.length() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgress(false);
                    TextView noEvents = findViewById(R.id.no_events);
                    Button goWeb = findViewById(R.id.go_website);
                    noEvents.setVisibility(View.VISIBLE);
                    goWeb.setVisibility(View.VISIBLE);
                    goWeb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://rider-track-dev.herokuapp.com/events"));
                            startActivity(browserIntent);
                        }
                    });
                }
            });
        } else {
            for (int i = 0; i < events.length(); i++) {
                JSONObject jObject = events.getJSONObject(i);
                manageEvent(jObject);
            }
            inflateLayout();
        }
    }

    private void extractStartingPoint(StringBuffer sb, String eventId) throws JSONException {
        JSONObject response = new JSONObject(sb.toString());
        JSONArray coord = response.getJSONArray("coordinates");
        if(!coord.toString().equals("[]")) {
            JSONObject jObject = coord.getJSONObject(0);
            String latLng = null;
            if (jObject.has("lat") && jObject.has("lng")) {
                String lat = jObject.getString("lat");
                String lng = jObject.getString("lng");
                latLng = lat + "," + lng;
                findEventFromID(eventId).setStartingPoint(latLng);
                counter = counter + 1;
            }
        }
    }

    private void manageEvent(JSONObject jObject) throws JSONException, ParseException {
        Event event = new Event();
        if(jObject.has("_id")){
            String id = jObject.getString("_id");
            event.setId(id);
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
            if(!event.getStatus().equals("passed")){
                eventsList.add(event);
                new GetStartingPoint().execute(id);}
        }

    }

    private void inflateLayout() throws JSONException {
        if(eventsList.size() == 0){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgress(false);
                    TextView noEvents = findViewById(R.id.no_events);
                    Button goWeb = findViewById(R.id.go_website);
                    noEvents.setVisibility(View.VISIBLE);
                    goWeb.setVisibility(View.VISIBLE);
                    goWeb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://rider-track-dev.herokuapp.com/events"));
                            startActivity(browserIntent);
                        }
                    });
                }
            });
        }else {
            scrollView = (LinearLayout) findViewById(R.id.scroll_down);
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgress(false);
                }
            });
            for (int i = 0; i < eventsList.size(); i++) {
                //Creating copy of event box by inflating it
                final LinearLayout event = (LinearLayout) inflater.inflate(R.layout.event_box, null);
                event.setClickable(false);
                event.setTag(eventsList.get(i).getId());
                TextView title = event.findViewById(R.id.event_name);
                title.setText(eventsList.get(i).getName());
                TextView city = event.findViewById(R.id.location);
                city.setText(eventsList.get(i).getCity());
                TextView date = event.findViewById(R.id.date);
                date.setText(eventsList.get(i).getStartingDate());
                TextView time = event.findViewById(R.id.time);
                time.setText(eventsList.get(i).getStartingTime());
                TextView type = event.findViewById(R.id.type);
                String typeLowercase = eventsList.get(i).getType();
                String typeUppercase = typeLowercase.substring(0,1).toUpperCase() + typeLowercase.substring(1);
                type.setText(typeUppercase);
                ImageView logo = event.findViewById(R.id.event_image);
                logoData = eventsList.get(i).getLogo().substring(eventsList.get(i).getLogo().indexOf("["), eventsList.get(i).getLogo().indexOf("]") + 2).replace(" ", "");
                JSONArray arr = new JSONArray(logoData);
                byte[] myArray = new byte[logoData.length()];
                for (int j = 0; j < arr.length(); j++) {
                    myArray[j] = (byte) arr.getInt(j);
                }
                Bitmap bmp = BitmapFactory.decodeByteArray(myArray, 0, myArray.length);
                logo.setImageBitmap(bmp);
                LinearLayout event_box = findViewById(R.id.event_box);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(event_box.getLayoutParams());
                params.setMargins(10, 20, 10, 20);
                event.setLayoutParams(params);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.addView(event);
                    }
                });
                event.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent raceActivity = new Intent(getApplicationContext(), RaceActivity.class);
                        raceActivity.putExtra("userId", userId);
                        raceActivity.putExtra("token", token);
                        raceActivity.putExtra("eventId", event.getTag().toString());
                        raceActivity.putExtra("delay", delay);
                        raceActivity.putExtra("name", findEventNameFromID(event.getTag().toString()));
                        raceActivity.putExtra("city", findEventCityFromID(event.getTag().toString()));
                        raceActivity.putExtra("startingPoint", findEventStartingPointFromID(event.getTag().toString()));
                        raceActivity.putExtra("startingTime", findEventStartingTimeFromID(event.getTag().toString()));
                        startActivity(raceActivity);
                    }
                });
            }
        }
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            mListView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mListView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private String findEventNameFromID(String eventId) {
        String name = null;
        for (Event event : eventsList) {
            if (event.getId().equals(eventId)) {
                name = event.getName();
            }
        }
        return name;
    }

    private Event findEventFromID(String eventId) {
        int index = -1;
        for (int i=0; i<eventsList.size(); i++) {
            if (eventsList.get(i).getId().equals(eventId)) {
              index = i;
            }
        }
        return eventsList.get(index);
    }

    private String findEventStartingPointFromID(String eventId) {
        String latLng = "";
        for (Event event : eventsList) {
            if (event.getId().equals(eventId)) {
                latLng = event.getStartingPoint();
            }
        }
        return latLng;
    }
    private String findEventStartingTimeFromID(String eventId) {
        String time = "";
        String date = "";
        String startingTime = "";
        for (Event event : eventsList) {
            if (event.getId().equals(eventId)) {
                time = event.getStartingTime()+":00";
                String[] splitted = event.getStartingDate().split("/");
                String day = splitted[0];
                String month = splitted[1];
                String year = splitted[2];
                date = year + "-" + month + "-" + day;
                startingTime = date + " " + time;
            }
        }
        return startingTime;
    }
    private String findEventCityFromID(String eventId) {
        String city = "";

        for (Event event : eventsList) {
            if (event.getId().equals(eventId)) {
                city = event.getCity();
            }
        }
        return city;
    }

}

