package com.mobileapp.ridertrack;

import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * MainActivity is the activity called by the launcher every time Ridertrack application is opened.
 * It displays the "Login" button, which redirects user to LoginActivity.
 */
public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * Retrieving information about user
         */
        SharedPreferences sp1=this.getSharedPreferences("Login", MODE_PRIVATE);
        String userId=sp1.getString("userId", null);
        String token = sp1.getString("token", null);
        String delay = sp1.getString("delay", null);
        /*
         * If user has already logged in Ridertrack application at least once, and not logged out after the last use,
         * he/she is not asked to login again and is directly redirected to EventsListActivity
         */
        if (userId != null && token !=null) {
            Intent eventsList = new Intent(getApplicationContext(), EventsListActivity.class);
            eventsList.putExtra("userId", userId);
            eventsList.putExtra("token", token);
            eventsList.putExtra("delay", Integer.valueOf(delay));
            startActivity(eventsList);
            finish();
        }
        /*
         * If user hasn't already logged in Ridertrack application at least once, or has logged out after the last use,
         * he/she is redirected to LoginActivity.
         */
        else {
            setContentView(R.layout.activity_main);
            Button facebookButton = (Button) findViewById(R.id.facebook_sign_in_button);
            Button googleButton = (Button) findViewById(R.id.google_sign_in_button);
            Button emailButton = (Button) findViewById(R.id.email_sign_in_button);
            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent emailLogin = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(emailLogin);
                    finish();
                }
            });

        /*
         * Google and Facebook login are not yet implemented: these methods are not reachable due to the "GONE"
         * visibility of the related buttons in the layout
         */
            googleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent googleAuth = new Intent(getApplicationContext(), GoogleAuth.class);
                    startActivity(googleAuth);
                    finish();
                }
            });

            facebookButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent emailLogin = new Intent(getApplicationContext(), RaceActivity.class);
                    startActivity(emailLogin);
                    finish();
                }
            });
        }
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