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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class EventsListActivity extends AppCompatActivity {

    private String userId;
    private String token;
    private View eventBox;
    private View menuView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventslist);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        token = intent.getStringExtra("token");

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
                url = new URL("https://rider-track-dev.herokuapp.com/api/users/"+userId+"/enrolledEvents");
                //create the connection
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "JWT "+token);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                //set the request method to GET
                connection.setRequestMethod("GET");
                //create your inputsream
                InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                String line = "";
                //read in the data from input stream, this can be done a variety of ways
                BufferedReader reader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                //get the string version of the response data
                response = sb.toString();
                Log.e ("Event List: ", response);
                //do what you want with the data now

                //always remember to close your input and output streams
                isr.close();
                reader.close();
            } catch (IOException e) {
                Log.e("HTTP GET", e.toString());
            } finally {
                connection.disconnect();
            }
            return true;
        }
    }

}

