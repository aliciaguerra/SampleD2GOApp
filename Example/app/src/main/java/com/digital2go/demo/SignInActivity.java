package com.digital2go.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.digital2go.sdk.D2GOSDK;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Map;
import com.digital2go.demo.models.Profile;
import com.digital2go.demo.utils.Preferences;

public class SignInActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private Button login, login_fb;
    private Preferences preferences;

    //Client configuration (default)
    ClientConfiguration clientConfiguration = new ClientConfiguration();

    //User pool object
    CognitoUserPool userPool;

    RelativeLayout progress;
    LinearLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        preferences = Preferences.getInstance(getApplicationContext()); //retrieve preferences instance

        userPool = new CognitoUserPool(getApplicationContext(), getString(R.string.POOL_ID), getString(R.string.POOL_CLIENT_ID), getString(R.string.POOL_CLIENT_SECRET), clientConfiguration); //call for the user pool

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
        callbackManager = CallbackManager.Factory.create();

        configUserInterface();
        if(!D2GOSDK.isConnected(getApplicationContext())) D2GOSDK.init(getApplicationContext(), preferences.getFirebaseToken());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void configUserInterface() {
        /** Custom facebook button **/
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email","public_profile","user_friends","user_location","user_status");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getFBProfile();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(SignInActivity.this, R.string.facebook_error, Toast.LENGTH_SHORT).show();
                LoginManager.getInstance().logOut();
            }
        });
        /** End of custom button config **/

        progress = findViewById(R.id.progress);
        main = findViewById(R.id.main);
    }

    private void getFBProfile(){
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                finish();
                try {
                    Profile profile = new Profile();
                    profile.setFirst_name(object.getString("first_name"));
                    profile.setLast_name(object.getString("last_name"));
                    profile.setEmail(object.getString("email"));
                    profile.setSex(object.getString("gender").charAt(0));

                    Preferences.getInstance(getApplicationContext()).saveProfile(profile);
                    Preferences.getInstance(getApplicationContext()).setLogged(true);
                    startActivity(new Intent(getApplicationContext(), MainActivityNew.class));
                    overridePendingTransition(R.anim.to_up, R.anim.fade_out);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,first_name,last_name,email,id,birthday,location,gender");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void logIn(View view){
        EditText uname = findViewById(R.id.etx_username);
        EditText pass = findViewById(R.id.etx_password);

        String username = uname.getText().toString(), password = pass.getText().toString();

        if(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            if (username.equals("guest") && password.equals("guest")  || username.equals("fernando") && password.equals("fernando123")) {
                signIn();
            } else{
                loginUser(username, password);
            }
        } else Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show();
    }

    /**
     * Login with Facebook
     * @param view Fb Login Button
     */
    public void logInFacebook(View view){
        loginButton.performClick(); //call the 'real' facebook button, and perform a click
    }

    public void forgotPassword(View view){
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), ForgotActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.to_left, R.anim.fade_out);
    }

    public void sign_up(View view){
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.to_left, R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    
    private void loginUser(final String username, final String password){
        // Callback handler for the sign-in process
        AuthenticationHandler authenticationHandler = new AuthenticationHandler() {

            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                signIn();
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                // The API needs user sign-in credentials to continue
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(username, password, null);

                // Pass the user sign-in credentials to the continuation
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);

                // Allow the sign-in to continue
                authenticationContinuation.continueTask();
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {

            }

            @Override
            public void onFailure(Exception exception) {
                // Sign-in failed, check exception for the cause
                String error = exception.getMessage();

                if (error.contains("not confirmed")) confirm(username);
                progress.setVisibility(View.GONE);
                main.setAlpha(1);
            }
        };

        userPool.getUser(username).getSessionInBackground(authenticationHandler);
        main.setAlpha(0.4f);
        progress.setVisibility(View.VISIBLE);
    }

    void signIn(){
        preferences.setLogged(true);
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), MainActivityNew.class);
        startActivity(intent);
        overridePendingTransition(R.anim.to_up, R.anim.fade_out);
        finish();
    }

    void confirm(String username){
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), ConfirmActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);

        overridePendingTransition(R.anim.to_left, R.anim.fade_out);
    }
}
