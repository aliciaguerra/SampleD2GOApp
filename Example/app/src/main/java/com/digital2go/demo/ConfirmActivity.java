package d2go.sahuarolabs.com.testfacebook;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler;

import d2go.sahuarolabs.com.testfacebook.utils.Preferences;

public class ConfirmActivity extends AppCompatActivity {

    EditText code;

    //Client configuration (default)
    ClientConfiguration clientConfiguration = new ClientConfiguration();

    //User pool object
    CognitoUserPool userPool;

    private Preferences preferences;

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        preferences = Preferences.getInstance(getApplicationContext()); //retrieve preferences instance

        userPool = new CognitoUserPool(getApplicationContext(), getString(R.string.POOL_ID), getString(R.string.POOL_CLIENT_ID), getString(R.string.POOL_CLIENT_SECRET), clientConfiguration); //call for the user pool

        username = getIntent().getStringExtra("username");
        code = findViewById(R.id.etx_code);
    }


    public void back(View view){
        onBackPressed();
    }


    public void confirm(View view){

        if (TextUtils.isEmpty(code.getText().toString())){
            Toast.makeText(this, "Please fill the confirmation code", Toast.LENGTH_SHORT).show();
            return;
        }

        GenericHandler confirmationCallback = new GenericHandler() {

            @Override
            public void onSuccess() {
                // User was successfully confirmed
                Toast.makeText(ConfirmActivity.this, "User confirmed", Toast.LENGTH_SHORT).show();
                signIn();
            }

            @Override
            public void onFailure(Exception exception) {
                // User confirmation failed. Check exception for the cause.
                String error = exception.getMessage();
                if (error.contains("Invalid code")) {
                    Toast.makeText(ConfirmActivity.this, "Resend code again", Toast.LENGTH_SHORT).show();
                    resendConfirmation();
                }
            }
        };

        userPool.getUser(username).confirmSignUpInBackground(code.getText().toString(), false, confirmationCallback);

    }

    void resendConfirmation(){
        userPool.getUser(username).resendConfirmationCodeInBackground(new VerificationHandler() {
            @Override
            public void onSuccess(CognitoUserCodeDeliveryDetails verificationCodeDeliveryMedium) {
                Toast.makeText(ConfirmActivity.this, "Confirmation sent", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(ConfirmActivity.this, "Error sending confirmation: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void signIn(){
        preferences.setLogged(true);
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), MainActivityNew.class);
        startActivity(intent);
        overridePendingTransition(R.anim.to_up, R.anim.fade_out);
        finish();
    }

}
