package com.mobileapp.ridertrack;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

        timeout_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delay = 2;
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
                delay = 3;
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
                delay = 5;
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
                delay = 10;
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
                delay = 20;
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
                delay = 30;
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
                eventsList.putExtra("delay", delay);
                startActivity(eventsList);
                finish();
            }
        });
    }
}
