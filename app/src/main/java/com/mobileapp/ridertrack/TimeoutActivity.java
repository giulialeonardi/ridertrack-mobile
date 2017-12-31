package com.mobileapp.ridertrack;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TimeoutActivity extends AppCompatActivity {

    private int delay;
    private Button timeout_2;
    private Button timeout_3;
    private Button timeout_5;
    private Button timeout_10;
    private Button timeout_20;
    private Button timeout_30;
    private Button done;
    private String userId;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeout);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        token = intent.getStringExtra("token");

        timeout_2 = (Button) findViewById(R.id.timeout_2);
        timeout_3 = (Button) findViewById(R.id.timeout_3);
        timeout_5 = (Button) findViewById(R.id.timeout_5);
        timeout_10 = (Button) findViewById(R.id.timeout_10);
        timeout_20 = (Button) findViewById(R.id.timeout_20);
        timeout_30 = (Button) findViewById(R.id.timeout_30);
        done = (Button) findViewById(R.id.done);




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
                Log.e("[Timeout Activity]", String.valueOf(delay));
                startActivity(eventsList);
                finish();
            }
        });
    }
}
