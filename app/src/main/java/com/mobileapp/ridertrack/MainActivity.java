package com.mobileapp.ridertrack;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;

/**
 * MainActivity is the activity called by the launcher every time Ridertrack application is opened.
 * It displays the "Login" button, which redirects user to LoginActivity.
 */
public class MainActivity extends AppCompatActivity  {

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
}