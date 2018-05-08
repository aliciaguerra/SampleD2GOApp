package com.digital2go.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
public class ForgotActivity extends AppCompatActivity {
    //Client configuration (default)
    ClientConfiguration clientConfiguration = new ClientConfiguration();

    //User pool object
    CognitoUserPool userPool;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        userPool = new CognitoUserPool(getApplicationContext(), getString(R.string.POOL_ID), getString(R.string.POOL_CLIENT_ID), getString(R.string.POOL_CLIENT_SECRET), clientConfiguration); //call for the user pool
    }

    public void back(View view){
        onBackPressed();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void recoverPass(View view){
        EditText username = findViewById(R.id.username);
        if  (!TextUtils.isEmpty(username.getText().toString())){
            userPool.getUser(username.getText().toString()).forgotPasswordInBackground(new ForgotPasswordHandler() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ForgotActivity.this, R.string.pass_restore, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void getResetCode(ForgotPasswordContinuation continuation) {

                }

                @Override
                public void onFailure(Exception exception) {
                    Toast.makeText(ForgotActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else Toast.makeText(this, R.string.username_needed, Toast.LENGTH_SHORT).show();
    }
}
