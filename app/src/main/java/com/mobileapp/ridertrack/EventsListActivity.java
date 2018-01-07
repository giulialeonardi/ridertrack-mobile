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
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;

/**
 * EventsListActivity is created after a successful login.
 * It performs a GET to the server, asking for all the events in which current user is enrolled
 * and shows them as a list.
 * Each event is clickable, and redirects user to the RaceActivity related to the event clicked.
 * In this activity, the user is allowed to choose the time interval (in seconds) between two consequent data
 * dispatches during a race, and to logout from Ridertrack application: both these features are available by clicking
 * on the scroll down menu, on the right top of the screen.
 */
public class EventsListActivity extends AppCompatActivity {

    private static final String TAG = "EventsListActivity";
    /**
     * Variables related to current user.
     */
    private String userId;
    private String token;
    private int delay;
    /**
     * Variables related to layout components
     */
    private View menuView;
    private LinearLayout scrollView;
    private View mProgressView;
    private View mListView;
    /**
     * Variables aimed to events list management
     */
    private int counter;
    private ArrayList<Event> eventsList;

    /**
     * OnCrate method actions:
     * 1 - Bind the activity to its layout
     * 2 - Retrieve information about current user, shared by the activity which started EventsListActivity
     * 3 - Bind layout elements to variables
     * 4 - Handle the pop up menu with timeout choice and logout options
     * 5 - Initialize counter
     * 6 - Send a GET request to server, in order to retrieve the list of events in which the current user
     * is enrolled and shows a progress bar waiting for result
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * 1 - Binding the activity to its layout
         */
        setContentView(R.layout.activity_eventslist);
        /*
         * 2 - Retrieving information about current user, shared by the activity which started EventsListActivity
         */
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        token = intent.getStringExtra("token");
        delay = intent.getIntExtra("delay", 5);
        SharedPreferences sp=getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor Ed=sp.edit();
        Ed.putString("delay", String.valueOf(delay));
        Ed.commit();
        /*
         * 3 - Binding layout elements to variables
         */
        mProgressView = findViewById(R.id.login_progress);
        mListView = findViewById(R.id.list);
        menuView = findViewById(R.id.menu);
        /*
         * 4 - Handling the pop up menu with timeout choice and logout options
         */
        menuView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EventsListActivity.this, menuView);
                /*
                 * Inflating the popup
                 */
                popup.getMenuInflater()
                        .inflate(R.menu.menu_eventslist, popup.getMenu());
                /*
                 * Registering popup with OnMenuItemClickListener
                 */
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        /*
                         * Handling item selection
                         */
                        switch (item.getItemId()) {
                            /*
                             * Redirecting to TimeoutActivity, in which user is allowed to choose the time
                             * interval between two consequent data dispatches from the app to the server
                             */
                            case R.id.position:
                                Intent timeout = new Intent(getApplicationContext(), TimeoutActivity.class);
                                timeout.putExtra("userId", userId);
                                timeout.putExtra("token", token);
                                timeout.putExtra("delay", delay);
                                startActivity(timeout);
                                return true;
                            /*
                             * Logging out the user and resetting all the related information
                             */
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

                /*
                 * Showing the popup when the menu icon is clicked
                 */
                popup.show();
         /*
          * Closing the setOnClickListener method
          */
            }
        });

        /*
         * 5 - Initializing counter
         */
        counter = 0;

        /*
         * 6 - Sending a GET request to server, in order to retrieve the list of events in which the current user
         * is enrolled and showing progress bar waiting for result
         */
        showProgress(true);
        new GetListOfEvents().execute();
    }
    /**
     * SplitResponse method turns the String Buffer received from GetEventsList method, to a JSONArray of
     * JSONObjects event.
     * If JSONArray is empty, it shows the default layout; otherwise it calls the method "manageEvent" for every event (JSONObject) of
     * the JSONArray and, finally, shows the event list by inflating layout.
     *
     * @param sb: StringBuffer variable, containing response received in relation to GetEventsList method GET request.
     * @throws JSONException: Thrown to indicate a problem with the JSON API. Exception shown as log message.
     * @throws ParseException: Signals that an error has been reached unexpectedly while parsing. Exception shown as log message.
     */
    private void splitResponse(StringBuffer sb) throws JSONException, ParseException {
        JSONObject response = new JSONObject(sb.toString());
        JSONArray events = response.getJSONArray("events");
        Log.e("Number of events of GET", String.valueOf(events.length()));
        /*
         * Checking if the list of events is empty
         */
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
        }
        /*
         * Calling for each event (JSONObject) of the JSONArray the method to handle it and, finally, inflate the layout
         */
        else {
            for (int i = 0; i < events.length(); i++) {
                JSONObject jObject = events.getJSONObject(i);
                manageEvent(jObject);
            }
            inflateLayout();
        }
    }

    /**
     * ManageEvent method checks the "status" field of the JSONObject event passed as param: if it is different from "passed", it locally
     * creates a new object of type "Event", checks which fields related to the event are included in the JSONObject
     * (only not null fields of each event are sent to the app), extracts them from the JSONObject and assigns them to the local object "event", just created.
     * Then, the new local event is added to the events list (ArrayList of objects of type "Event") and GetStartingPoint
     * asynchronous method is called, in order to retrieve coordinates about the event starting point.
     * The method stores in the events list only yet to come events.
     *
     * @param jObject: JSONObject variable, containing a single event
     * @throws JSONException: Thrown to indicate a problem with the JSON API. Exception shown as log message.
     * @throws ParseException: Signals that an error has been reached unexpectedly while parsing. Exception shown as log message.
     */
    private void manageEvent(JSONObject jObject) throws JSONException, ParseException {
        if(jObject.has("status")) {
            String status = jObject.getString("status");
            Log.e("Status of " + jObject.getString("name"), status);
            if (!status.equals("passed")) {
                Event event = new Event();
                event.setStatus(status);
                if (jObject.has("_id")) {
                    String id = jObject.getString("_id");
                    event.setId(id);

                    if (jObject.has("name")) {
                        String name = jObject.getString("name");
                        event.setName(name);
                    }
                    if (jObject.has("organizerId")) {
                        String organizerId = jObject.getString("organizerId");
                        event.setOrganizerId(organizerId);
                    }
                    if (jObject.has("type")) {
                        String type = jObject.getString("type");
                        event.setType(type);
                    }
                    if (jObject.has("description")) {
                        String description = jObject.getString("description");
                        event.setDescription(description);
                    }
                    if (jObject.has("country")) {
                        String country = jObject.getString("country");
                        event.setCountry(country);
                    }
                    if (jObject.has("city")) {
                        String city = jObject.getString("city");
                        event.setCity(city);
                    }
                    if (jObject.has("startingDateString")) {
                        String startingDate = jObject.getString("startingDateString");
                        event.setStartingDate(startingDate);
                    }
                    if (jObject.has("startingTimeString")) {
                        String startingTime = jObject.getString("startingTimeString");
                        event.setStartingTime(startingTime);
                    }
                    if (jObject.has("maxDuration")) {
                        int maxDuration = jObject.getInt("maxDuration");
                        event.setMaxDuration(maxDuration);
                    }
                    if (jObject.has("length")) {
                        int length = jObject.getInt("length");
                        event.setLength(length);
                    }
                    if (jObject.has("price")) {
                        Double price = jObject.getDouble("price");
                        event.setPrice(price);
                    }
                    if (jObject.has("maxParticipants")) {
                        int maxParticipants = jObject.getInt("maxParticipants");
                        event.setMaxParticipants(maxParticipants);
                    }
                    if (jObject.has("enrollmentOpeningAt")) {
                        String enrollmentOpeningAt = jObject.getString("enrollmentOpeningAt");
                        event.setEnrollmentOpeningAt(enrollmentOpeningAt);
                    }
                    if (jObject.has("enrollmentClosingAt")) {
                        String enrollmentClosingAt = jObject.getString("enrollmentClosingAt");
                        event.setEnrollmentClosingAt(enrollmentClosingAt);
                    }
                    if (jObject.has("logo")) {
                        String logo = jObject.getString("logo");
                        event.setLogo(logo);
                    }
                    if (jObject.has("created_at")) {
                        String created_at = jObject.getString("created_at");
                        event.setCreatedAt(created_at);
                    }
                    if (jObject.has("updated_at")) {
                        String updated_at = jObject.getString("updated_at");
                        event.setUpdatedAt(updated_at);
                    }
                    eventsList.add(event);
                    new GetStartingPoint().execute(id);
                }
            }
        }
    }

    /**
     * InflateLayout method shows on UI as many events as the ArrayList "eventsList" contains, by duplicating
     * yet existing event_box layout and filling the fields of each box with event related information.
     *
     * @throws JSONException: Thrown to indicate a problem with the JSON API. Exception shown as log message.
     */
    private void inflateLayout() throws JSONException {
        /*
         * Checking if list is empty
         */
        if(eventsList.size() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*
                     * Stopping showing progress bar
                     */
                    showProgress(false);
                    /*
                     * Displaying default layout
                     */
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
                    TextView click = findViewById(R.id.click_event);
                    click.setVisibility(View.VISIBLE);
                }
            });
            for (int i = 0; i < eventsList.size(); i++) {
                /*
                 *Creating copy of event box by inflating it
                 */
                final LinearLayout event;
                if (inflater != null) {
                    event = (LinearLayout) inflater.inflate(R.layout.event_box, null);
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
                    String typeUppercase = typeLowercase.substring(0, 1).toUpperCase() + typeLowercase.substring(1);
                    type.setText(typeUppercase);
                    ImageView logo = event.findViewById(R.id.event_image);
                    /*
                     * Splitting logo string
                     */
                    String logoData = eventsList.get(i).getLogo().substring(eventsList.get(i).getLogo().indexOf("["), eventsList.get(i).getLogo().indexOf("]") + 2).replace(" ", "");
                    JSONArray arr = new JSONArray(logoData);
                    /*
                     * Converting string to ByteArray
                     */
                    byte[] myArray = new byte[logoData.length()];
                    for (int j = 0; j < arr.length(); j++) {
                        myArray[j] = (byte) arr.getInt(j);
                    }
                    /*
                     * Creating Bitmap from ByteArray
                     */
                    Bitmap bmp = BitmapFactory.decodeByteArray(myArray, 0, myArray.length);
                    logo.setImageBitmap(bmp);
                    /*
                     * Setting layout params
                     */
                    LinearLayout event_box = findViewById(R.id.event_box);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(event_box.getLayoutParams());
                    params.setMargins(30, 0, 30, 20);
                    event.setLayoutParams(params);
                    /*
                     * Adding event view
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.addView(event);
                        }
                    });
                    event.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            /*
                             * Sending data about the event clicked by the user to the new activity and starting it
                             */
                            Intent raceActivity = new Intent(getApplicationContext(), RaceActivity.class);
                            raceActivity.putExtra("userId", userId);
                            raceActivity.putExtra("token", token);
                            raceActivity.putExtra("eventId", event.getTag().toString());
                            raceActivity.putExtra("delay", delay);
                            raceActivity.putExtra("name", findEventNameFromID(event.getTag().toString()));
                            raceActivity.putExtra("city", findEventCityFromID(event.getTag().toString()));
                            raceActivity.putExtra("type", findEventTypeFromID(event.getTag().toString()));
                            raceActivity.putExtra("startingPoint", findEventStartingPointFromID(event.getTag().toString()));
                            raceActivity.putExtra("startingTime", findEventStartingTimeFromID(event.getTag().toString()));
                            startActivity(raceActivity);
                        }
                    });
                }
            }
        }
    }

    /**
     * ExtractStartingPoint method splits response received by GetStartingPoint method GET request, extracts coordinates of the
     * starting point and sets them to the related field of the event whose id has been passed to the method.
     *
     * @param sb: StringBuffer variable, containing response received in relation to GetStartingPoint method GET request
     * @param eventId: String varible, containing id of the event of interest
     * @throws JSONException: Thrown to indicate a problem with the JSON API. Exception shown as log message.
     */
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

    /**
     * ShowProgress method shows the progress UI, waiting for the events list to be shown.
     *
     * @param show: Boolean variable, pinpointing visibility
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        /*
         * On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
         * for very easy animations. If available, uses these APIs to fade-in
         * the progress spinner.
         */
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

    /**
     * FindEventNameFromID method returns the name of event whose id has been passed as param.
     *
     * @param eventId: String variable, containing the id of the
     * @return String: event name
     */
    private String findEventNameFromID(String eventId) {
        String name = null;
        for (Event event : eventsList) {
            if (event.getId().equals(eventId)) {
                name = event.getName();
            }
        }
        return name;
    }

    /**
     * FindEventFromID method returns the event whose id has been passed as param.
     *
     * @param eventId: String variable, containing the id of the
     * @return Event: event
     */
    private Event findEventFromID(String eventId) {
        int index = -1;
        for (int i=0; i<eventsList.size(); i++) {
            if (eventsList.get(i).getId().equals(eventId)) {
              index = i;
            }
        }
        return eventsList.get(index);
    }

    /**
     * FindEventNameFromID method returns the name of event whose id has been passed as param.
     *
     * @param eventId: String variable, containing the id of the
     * @return String: event name
     */
    private String findEventStartingPointFromID(String eventId) {
        String latLng = "";
        for (Event event : eventsList) {
            if (event.getId().equals(eventId)) {
                latLng = event.getStartingPoint();
            }
        }
        return latLng;
    }

    /**
     * FindEventStartingTimeFromID method returns the starting time of event whose id has been passed as param.
     *
     * @param eventId: String variable, containing the id of the
     * @return String: event starting time
     */
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

    /**
     * FindEventCityFromID method returns the city of event whose id has been passed as param.
     *
     * @param eventId: String variable, containing the id of the
     * @return String: event city
     */
    private String findEventCityFromID(String eventId) {
        String city = "";

        for (Event event : eventsList) {
            if (event.getId().equals(eventId)) {
                city = event.getCity();
            }
        }
        return city;
    }
    /**
     * FindEventTypeFromID method returns the type of event whose id has been passed as param.
     *
     * @param eventId: String variable, containing the id of the
     * @return String: event type
     */
    private String findEventTypeFromID(String eventId) {
        String type = "";

        for (Event event : eventsList) {
            if (event.getId().equals(eventId)) {
                type = event.getType();
            }
        }
        return type;
    }
    /**
     * Asynchronous methods which handle GET requests to server.
     */
    public class GetListOfEvents extends AsyncTask<String, Void, Boolean> {
        /**
         * GetListOfEvents method performs a GET request to the server, in order to retrieve the list of events in
         * which the user in enrolled.
         * No params needed.
         *
         * @param strings: not needed
         * @return Boolean: true if the request ends correctly, false otherwise
         */
        @Override
        protected Boolean doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = null;
                String response = null;
                url = new URL("https://rider-track-dev.herokuapp.com/api/users/" + userId + "/enrolledEvents");
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
                Log.e("RESPONSE", responseCode + connection.getResponseMessage());

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
                     * Creating array list of events
                     */
                    eventsList = new ArrayList<>();
                    /*
                     * Handling response
                     */
                    splitResponse(sb);
                    return true;
                }
                /*
                 * If the response code is 401, the GET request hasn't concluded successfully:
                 * the token related to current user could be expired and app is not authorized to retrieve
                 * the list of personal events.
                 * An error message is shown and user is redirected to MainActivity to login again.
                 */
                if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED){
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
                                    /*
                                     * Logout and reset of data about the current user
                                     */
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
                    return false;
                }
                /*
                 * If the response code is different from 200 or 401, the GET request hasn't concluded successfully:
                 * the exception is displayed as log message
                 */
                else{
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
                    in.close();
                    Log.e(TAG, sb.toString());
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG,e.toString());
                return false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    public class GetStartingPoint extends AsyncTask<String, Void, Boolean> {
        /**
         * GetStartingPoint method performs a GET request to the server, in order to retrieve the starting point of
         * the event, whose id is passed as param.
         * The method needs to receive the id of the event as param.
         *
         * @param strings: event Id
         * @return Boolean: true if the request ends correctly, false otherwise
         */
        @Override
        protected Boolean doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = null;
                String response = null;
                url = new URL("https://rider-track-dev.herokuapp.com/api/events/" + strings[0] + "/route");
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
                     * Handling response
                     */
                    extractStartingPoint(sb, strings[0]);
                    in.close();
                    return true;
                }
                /*
                 * If the response code is different from 200, the GET request hasn't concluded successfully:
                 * the exception is displayed as log message
                 */
                else{
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
                    Log.e(TAG, sb.toString());
                    return false;
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

