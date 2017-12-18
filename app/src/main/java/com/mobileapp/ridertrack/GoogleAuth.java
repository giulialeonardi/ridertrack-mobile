package com.mobileapp.ridertrack;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


import javax.net.ssl.HttpsURLConnection;

public class GoogleAuth extends AppCompatActivity {

    private static final int RC_SIGN_IN = 5;
    private GoogleSignInAccount account;

    private com.google.android.gms.auth.api.signin.GoogleSignInClient mGoogleSignInClient;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Log.e("[GoogleAuth]", "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        account = GoogleSignIn.getLastSignedInAccount(this);
        Log.e("[GoogleAuth]", "onStart");
        updateUI(account);

    }
    public void startSignIn(){
        Log.e("[GoogleAuth]", "startSignIn");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("[GoogleAuth]", "onActivityResult");

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            new SendToServer().execute(idToken);
        } catch (ApiException e) {
            Log.w("[Google Auth]", "handleSignInResult:error", e);
            updateUI(null);
        }
    }

    public class SendToServer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String blank = "";
            URL url = null; // here is your URL path
            try {
                url = new URL("https://rider-track-dev.herokuapp.com/api/auth/login/google?access_token="+params[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            try {
                conn.setRequestMethod("POST");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = null;
            try {
                os = conn.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                writer.write(blank);
                writer.flush();
                writer.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int responseCode = 0;
            try {
                responseCode = conn.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                Log.i("[Google Auth]", "Successfully signed in as: " /*+ user name*/);
                updateUI(account);
                return "true";
            } else {
                Log.e("[Google Auth]", "Error sending ID token to backend:" + String.valueOf(responseCode));
                Intent error = new Intent(getApplicationContext(), ErrorActivity.class);
                startActivity(error);
                return "false";
            }
        }
    }
    private void updateUI(GoogleSignInAccount account){
        if (account != null) {
            Intent startRace = new Intent(getApplicationContext(), RaceActivity.class);
            startActivity(startRace);
        }else{
            startSignIn();
        }
    }

}
