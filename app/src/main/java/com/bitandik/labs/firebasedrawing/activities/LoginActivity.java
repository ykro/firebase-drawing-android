package com.bitandik.labs.firebasedrawing.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import com.bitandik.labs.firebasedrawing.FirebaseDrawingApplication;
import com.bitandik.labs.firebasedrawing.R;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {
    @Bind(R.id.twitter_login_button)
    TwitterLoginButton loginButton;
    @Bind(R.id.editTxtEmail)
    EditText editTextEmail;
    @Bind(R.id.editTxtPassword) EditText editTextPassWord;

    private Firebase.AuthResultHandler authResultHandler;
    private Firebase dataReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        FirebaseDrawingApplication app = (FirebaseDrawingApplication)getApplicationContext();
        dataReference = app.getFirebase();

        authResultHandler = new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                doLogin();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                String msgError = String.format(getString(R.string.msg_error_login), firebaseError.getMessage());
                Toast.makeText(getApplicationContext(), msgError, Toast.LENGTH_LONG).show();
            }
        };

        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Map<String, String> options = new HashMap<String, String>();

                options.put("oauth_token", result.data.getAuthToken().token);
                options.put("oauth_token_secret", result.data.getAuthToken().secret);
                options.put("user_id", String.valueOf(result.data.getUserId()));
                dataReference.authWithOAuthToken("twitter", options, authResultHandler);
            }

            @Override
            public void failure(TwitterException exception) {
                String msgError = String.format(getString(R.string.msg_error_login), exception.getMessage());
                Toast.makeText(getApplicationContext(), msgError, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        doLogin();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.btnLogin)
    public void emailLogin(){
        dataReference.authWithPassword(editTextEmail.getText().toString(), editTextPassWord.getText().toString(), authResultHandler);
    }

    private void doLogin() {
        AuthData authData = dataReference.getAuth();

        if (authData != null) {
            String username = null;
            Map<String,Object> providerData = authData.getProviderData();
            if (authData.getProvider().equals("twitter")) {
                username = "@" + providerData.get("username").toString();
            } else if (authData.getProvider().equals("password")) {
                username = providerData.get("email").toString();
            }
            SharedPreferences prefs = getApplication().getSharedPreferences("LEDPrefs", 0);
            prefs.edit().putString("username", username).commit();

            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }
}
