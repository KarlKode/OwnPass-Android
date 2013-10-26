package com.ownpass.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.net.ssl.HostnameVerifier;

/**
 * Created by leo on 10/26/13.
 */
public class DeviceActivationActivity extends Activity {
    private static final String TAG = "DeviceActivationActivity";

    private TextView mLoginStatusMessageView;

    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mActivationCodeView;
    private ActivationTask mActivationTask;


    private String mActivationCode;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_activation);

        mActivationCodeView = (TextView) findViewById(R.id.activation_key);

        mLoginFormView = findViewById(R.id.activation_form);
        mLoginStatusView = findViewById(R.id.activation_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.activation_status_message);

        mActivationCodeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.activate || id == EditorInfo.IME_NULL) {
                    attemptActivate();
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.activate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptActivate();
            }
        });

    }

    //194725

    public void attemptActivate() {
        if (mActivationTask != null) {
            return;
        }

        // Reset errors.
        mActivationCodeView.setError(null);

        // Store values at the time of the login attempt.
        mActivationCode = mActivationCodeView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mActivationCode)) {
            mActivationCodeView.setError(getString(R.string.error_field_required));
            focusView = mActivationCodeView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mActivationTask = new ActivationTask();
            mActivationTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class ActivationTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean successful = false;

            SharedPreferences prefs = getApplicationContext().getSharedPreferences("OwnPass", MODE_PRIVATE);
            String device_id = prefs.getString("device_id", null);

            HttpPut httpput = new HttpPut("https://ownpass.marcg.ch/devices/" + device_id);
            HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

            /* http://stackoverflow.com/questions/2012497/accepting-a-certificate-for-https-on-android/3904473#3904473 */
            DefaultHttpClient client = new DefaultHttpClient();

            SchemeRegistry registry = new SchemeRegistry();
            SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            registry.register(new Scheme("https", socketFactory, 443));
            SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
            DefaultHttpClient httpclient = new DefaultHttpClient(mgr, client.getParams());

            try {
                JSONObject json = new JSONObject();
                json.put("device", prefs.getString("device", null));
                json.put("active", "true");
                json.put("code", mActivationCode);

                StringEntity se = new StringEntity(json.toString());

                se.setContentEncoding("UTF-8");
                se.setContentType("application/json");

                String credentials = prefs.getString("email", null) + ":" + Utilities.generateSHAHash(prefs.getString("password", null));
                String base64creds = Utilities.toBase64(credentials);

                httpput.addHeader("Authorization", "Basic " + base64creds);
                httpput.setEntity(se);

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httpput);
                String responseString = Utilities.convertStreamToString(response.getEntity().getContent());
                Log.i(TAG, responseString);

                if(response.getStatusLine().getStatusCode() == 200){
                    JSONObject o = new JSONObject(responseString);
                    successful = true;
                }else{

                }
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // TODO: register the new account here.
            return successful;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mActivationTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(DeviceActivationActivity.this, CredentialsListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                mActivationCodeView.setError(getString(R.string.error_activation_not_successful));
            }
        }

        @Override
        protected void onCancelled() {
            mActivationTask = null;
            showProgress(false);
        }
    }
}