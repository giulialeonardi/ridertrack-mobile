package com.mobileapp.ridertrack;


import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * TimeoutActivity is created when the user clicks on the menu icon on the top right of the EventsListActivty and
 * selects the choice "Data Sender".
 * It displays six buttons, that correspond with different time intervals: this screen allows the user to choose
 * how often (after how many minutes) he/she wants to send data about his/her position during any race.
 */
public class TimeoutActivity extends AppCompatActivity {

    private static final String TAG = "TimeoutActivity";

    /**
     * Variables related to buttons
     */
    private Button timeout_2;
    private Button timeout_3;
    private Button timeout_5;
    private Button timeout_10;
    private Button timeout_20;
    private Button timeout_30;
    /**
     * Variables related to current user
     */
    private String userId;
    private String token;
    private int delay;
    private int newDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeout);
        /*
         * Retrieving information about current user
         */
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        token = intent.getStringExtra("token");
        delay = intent.getIntExtra("delay", 5);
        /*
         * Binding layout elements to variables
         */
        timeout_2 = (Button) findViewById(R.id.timeout_2);
        timeout_3 = (Button) findViewById(R.id.timeout_3);
        timeout_5 = (Button) findViewById(R.id.timeout_5);
        timeout_10 = (Button) findViewById(R.id.timeout_10);
        timeout_20 = (Button) findViewById(R.id.timeout_20);
        timeout_30 = (Button) findViewById(R.id.timeout_30);
        Button done = (Button) findViewById(R.id.done);

        switch(delay){
            case 2:
                timeout_2.setBackgroundColor(Color.rgb(255, 153, 153));
                break;
            case 3:
                timeout_3.setBackgroundColor(Color.rgb(255, 153, 153));
                break;
            case 5:
                timeout_5.setBackgroundColor(Color.rgb(255, 153, 153));
                break;
            case 10:
                timeout_10.setBackgroundColor(Color.rgb(255, 153, 153));
                break;
            case 20:
                timeout_20.setBackgroundColor(Color.rgb(255, 153, 153));
                break;
            case 30:
                timeout_30.setBackgroundColor(Color.rgb(255, 153, 153));
                break;
        }

        newDelay = delay;

        timeout_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newDelay = 2;
                timeout_2.setBackgroundColor(Color.rgb(255, 153, 153));
                timeout_3.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_5.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_10.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_20.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_30.setBackgroundColor(Color.rgb(255, 230, 230));
            }
        });

        timeout_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newDelay = 3;
                timeout_2.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_3.setBackgroundColor(Color.rgb(255, 153, 153));
                timeout_5.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_10.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_20.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_30.setBackgroundColor(Color.rgb(255, 230, 230));
            }
        });

        timeout_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newDelay = 5;
                timeout_2.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_3.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_5.setBackgroundColor(Color.rgb(255, 153, 153));
                timeout_10.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_20.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_30.setBackgroundColor(Color.rgb(255, 230, 230));
            }
        });
        timeout_10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newDelay = 10;
                timeout_2.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_3.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_5.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_10.setBackgroundColor(Color.rgb(255, 153, 153));
                timeout_20.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_30.setBackgroundColor(Color.rgb(255, 230, 230));
            }
        });
        timeout_20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newDelay = 20;
                timeout_2.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_3.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_5.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_10.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_20.setBackgroundColor(Color.rgb(255, 153, 153));
                timeout_30.setBackgroundColor(Color.rgb(255, 230, 230));
            }
        });
        timeout_30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newDelay = 30;
                timeout_2.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_3.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_5.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_10.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_20.setBackgroundColor(Color.rgb(255, 230, 230));
                timeout_30.setBackgroundColor(Color.rgb(255, 153, 153));
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent eventsList = new Intent(getApplicationContext(), EventsListActivity.class);
                eventsList.putExtra("userId", userId);
                eventsList.putExtra("token", token);
                eventsList.putExtra("delay", newDelay);
                startActivity(eventsList);
                finish();
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
