package com.mobileapp.ridertrack;

import android.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity  {

    private Button emailButton;
    private Button facebookButton;
    private Button googleButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        facebookButton = (Button) findViewById(R.id.facebook_sign_in_button);
        googleButton = (Button) findViewById(R.id.google_sign_in_button);
        emailButton = (Button) findViewById(R.id.email_sign_in_button);
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailLogin = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(emailLogin);
            }
        });

        //temporary:just to see error screen
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailLogin = new Intent(getApplicationContext(), ErrorActivity.class);
                startActivity(emailLogin);
            }
        });
        //temporary:just to see race screen
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailLogin = new Intent(getApplicationContext(), RaceActivity.class);
                startActivity(emailLogin);
            }
        });
    }
}