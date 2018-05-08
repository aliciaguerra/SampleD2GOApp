package com.digital2go.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;

import org.w3c.dom.Text;

public class SignUpActivity extends AppCompatActivity {

    EditText username, password, phone, email;

    //Client configuration (default)
    ClientConfiguration clientConfiguration = new ClientConfiguration();

    //User pool object
    CognitoUserPool userPool;

    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        preferences = Preferences.getInstance(getApplicationContext()); //retrieve preferences instance
        userPool = new CognitoUserPool(getApplicationContext(), getString(R.string.POOL_ID), getString(R.string.POOL_CLIENT_ID), getString(R.string.POOL_CLIENT_SECRET), clientConfiguration); //call for the user pool
        configUI();
    }

    private void configUI() {
        username = findViewById(R.id.etx_username);
        password = findViewById(R.id.etx_password);
        phone = findViewById(R.id.etx_phone);
        email = findViewById(R.id.etx_email);
    }

    public void sign_up(View view){
        if (TextUtils.isEmpty(username.getText().toString())){
            Toast.makeText(this, R.string.sign_up_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password.getText().toString())){
            Toast.makeText(this, R.string.sign_up_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phone.getText().toString())){
            Toast.makeText(this, R.string.sign_up_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email.getText().toString())){
            Toast.makeText(this, R.string.sign_up_error, Toast.LENGTH_SHORT).show();
            return;
        }

        signUpUser();
    }

    private void signUpUser() {
        final String username = this.username.getText().toString(), password = this.password.getText().toString(), phone = this.phone.getText().toString(), email = this.email.getText().toString();

        SignUpHandler signupCallback = new SignUpHandler() {

            @Override
            public void onSuccess(CognitoUser cognitoUser, boolean userConfirmed, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                // Sign-up was successful

                // Check if this user (cognitoUser) needs to be confirmed
                if (!userConfirmed) {
                    // This user must be confirmed and a confirmation code was sent to the user
                    // cognitoUserCodeDeliveryDetails will indicate where the confirmation code was sent
                    // Get the confirmation code from user
                    confirm(username);
                    Toast.makeText(SignUpActivity.this, R.string.confirm_request, Toast.LENGTH_SHORT).show();
                } else {
                    // The user has already been confirmed
                    Toast.makeText(SignUpActivity.this, "User alreaady confirmed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception exception) {
                // Sign-up failed, check exception for the cause
                Toast.makeText(SignUpActivity.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        //Cognito user attributes
        CognitoUserAttributes userAttributes = new CognitoUserAttributes();

        //Adding user's email
        userAttributes.addAttribute("email", email);
        // Note that the key is "given_name" which is the OIDC claim for given name
        userAttributes.addAttribute("given_name", username);
        // Adding user's phone number
        userAttributes.addAttribute("phone_number", phone);

        userPool.signUpInBackground(username, password, userAttributes, null, signupCallback);
    }

    void confirm(String username){
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), ConfirmActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);

        overridePendingTransition(R.anim.to_left, R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
