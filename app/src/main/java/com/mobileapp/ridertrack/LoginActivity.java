package com.mobileapp.ridertrack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final String TAG = "LoginActivity";
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    /**
     * UI references.
     */
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /*
         * Setting up the login form.
         */
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mLoginFormView = findViewById(R.id.email_login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * PopulateAutoComplete method allows automatic completion of a field
     */
    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempting to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        /*
         * Resetting errors.
         */
        mEmailView.setError(null);
        mPasswordView.setError(null);

        /*
         * Storing values at the time of the login attempt.
         */
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        /*
         * Checking for a valid password, if the user entered one.
         */
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        /*
         * Checking for a valid email address.
         */
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            /*
             * There was an error: don't attempt login and focus the first
             * form field with an error.
             */
            focusView.requestFocus();
        } else {
            /*
             * Showing a progress spinner, and kicking off a background task to
             * perform the user login attempt.
             */
            showProgress(true);
            new UserLoginTask().execute(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        /*
         * On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
         * for very easy animations. If available, use these APIs to fade-in
         * the progress spinner.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            /*
             * The ViewPropertyAnimator APIs are not available, so simply show
             * and hide the relevant UI components.
             */
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                /*
                 * Retrieving data rows for the device user's 'profile' contact.
                 */
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                /*
                 * Selecting only email addresses.
                 */
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                /* Showing primary email addresses first. Note that there won't be
                 * a primary email address if the user hasn't specified one.
                 */
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
    }

    /**
     * UserLoginTask is an asynchronous login task used to authenticate the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, Boolean> {

        /**
         * The method performs a POST request to the server, in order to check if email and password inserted
         * by user are already in the database and, therefore, valid.
         * No params needed.
         * @param params: two String params needed: email, password
         * @return Boolean: true if the request ends correctly, false otherwise
         */
        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("https://rider-track-dev.herokuapp.com/api/auth/login");
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("email", params[0]);
                postDataParams.put("password", params[1]);
                /*
                 * Creating connection
                 */
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                /*
                 * Setting the request method to GET
                 */
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                 /*
                 * Writing out data to output stream
                 */
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                 /*
                 * If the response code is 200, the POST request has concluded successfully
                 */
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    /*
                     * Creating input stream
                     */
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";
                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }
                    /*
                     * Handling response: converting String Buffer to String, splitting it and extracting "userId"
                     * and "token" fields
                     */
                    String CurrentString = sb.toString();
                    String[] separated = CurrentString.split(",");
                    ArrayList<String> fieldsList = new ArrayList<>();
                    for (String string : separated) {
                        String[] fields = string.split(":");
                        fieldsList.add(fields[1]);
                    }
                    String userId = fieldsList.get(0).replace("\"", "");
                    String token = fieldsList.get(2).replace("\"", "").replace(" ", "");
                    /*
                     * Closing input stream
                     */
                    in.close();
                    /*
                     * Saving data about the user in Shared Preferences
                     */
                    SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
                    SharedPreferences.Editor Ed = sp.edit();
                    Ed.putString("userId", userId);
                    Ed.putString("token", token);
                    Ed.commit();
                    /*
                     * Login completed successfully: redirecting user to EventsListActivity
                     */
                    Intent eventsList = new Intent(getApplicationContext(), EventsListActivity.class);
                    eventsList.putExtra("userId", userId);
                    eventsList.putExtra("token", token);
                    startActivity(eventsList);
                    finish();
                    return true;
                } else {
                    /*
                     * Creating error stream
                     */
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    Log.e(TAG, String.valueOf(responseCode));

                    /*
                     * Redirecting user to ErrorActivity
                     */
                    Intent error = new Intent(getApplicationContext(), ErrorActivity.class);
                    startActivity(error);
                    return false;
                }
            } catch (Exception e) {
                Log.e("Exception: ", e.getMessage());
                return false;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * GetPostDataString method decodes a JSONObject and returns the related String.
     *
     * @param params: JSONObject to be decoded
     * @return String variable, "traslation" to String of the param object
     * @throws JSONException: Thrown to indicate a problem with the JSON API. Exception shown as log message.
     * @throws UnsupportedEncodingException: Exception thrown when the Character Encoding is not supported.
     */
    public String getPostDataString(JSONObject params) throws JSONException, UnsupportedEncodingException {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
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
