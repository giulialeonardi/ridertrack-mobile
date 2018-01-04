package com.mobileapp.ridertrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * ErrorActivity is called when an error occurs, during the login phase.
 */
public class ErrorActivity extends AppCompatActivity {

    private static final String TAG = "ErrorActivity";

    /**
     * Binds the activity to its layout.
     * When users click on the button, it redirects them to the MainActivity, to allow them retry the login.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        Button tryAgain = findViewById(R.id.try_again_button);
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main);
                finish();
            }
        });
    }
}
