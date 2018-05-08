package com.digital2go.demo.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.digital2go.sdk.D2GOSDK;
import com.digital2go.sdk.exceptions.LoginException;
import com.digital2go.sdk.exceptions.SDKException;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.digital2go.demo.MainActivityNew;
import com.digital2go.demo.R;
import com.digital2go.demo.MainActivityNew;
import com.digital2go.demo.models.Profile;
import com.digital2go.demo.utils.Preferences;


/**
 * Created by Ulrick on 10/10/2016.
 */
public class FragmentSettings extends Fragment implements OnClickListener, OnFocusChangeListener{

    private Button save;
    private EditText first_name, last_name, age, city, state, zipcode, email;
    private Switch console_switch;
    private TextView version;
    private RadioButton male,female;
    private Switch alert, sound;
    private Button logout;
    private static Context context;
    private static View view2;
    private String m_Text = "";

    public FragmentSettings() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view2 = view;
        configUI(view);

        Profile profile = Preferences.getInstance(getContext()).getProfile();
        if (profile != null)
            restoreUI(profile);
    }

    public static void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);

        if (view2 == null) return;

        imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void restoreUI(Profile profile) {
        first_name.setText(profile.getFirst_name());
        last_name.setText(profile.getLast_name());
        if (!(profile.getSex() == ' ')){
            switch (profile.getSex()){
                case 'm':
                    male.setChecked(true);
                    female.setChecked(false);
                    break;

                case 'f':
                    male.setChecked(false);
                    female.setChecked(true);
                    break;
            }
        }
        String str_age = String.valueOf(profile.getAge());
        if (!str_age.equals("0")) age.setText(String.valueOf(profile.getAge()));
        city.setText(profile.getCity());
        state.setText(profile.getState());
        if (!String.valueOf(profile.getZipcode()).equals("0"))
            zipcode.setText(String.valueOf(profile.getZipcode()));
        email.setText(profile.getEmail());


        if (profile.isAlert()) alert.setChecked(true);
        else alert.setChecked(false);

        if (profile.isSound()) sound.setChecked(true);
        else sound.setChecked(false);
    }

    private void configUI(final View view) {
        save = (Button) view.findViewById(R.id.save);
        logout = (Button) view.findViewById(R.id.logout);
        first_name = (EditText) view.findViewById(R.id.first_name);
        last_name = (EditText) view.findViewById(R.id.last_name);
        male = (RadioButton) view.findViewById(R.id.male);
        female = (RadioButton) view.findViewById(R.id.female);
        age = (EditText) view.findViewById(R.id.age);
        city = (EditText) view.findViewById(R.id.city);
        state = (EditText) view.findViewById(R.id.state);
        zipcode = (EditText) view.findViewById(R.id.zipcode);
        email = (EditText) view.findViewById(R.id.email);
        version = (TextView) view.findViewById(R.id.version);
        console_switch = (Switch) view.findViewById(R.id.console_switch);
        setVersion();

        alert = (Switch) view.findViewById(R.id.switch_alert);
        sound = (Switch) view.findViewById(R.id.switch_sound);

        logout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityNew.logout();
                D2GOSDK.stop();
            }
        });
        save.setOnClickListener(this);

        console_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean status) {
                if (status){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    final InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    builder.setTitle(R.string.debug_password);

                    // Set up the input
                    final EditText input = new EditText(getContext());
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    input.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_Text = input.getText().toString();
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);


                            if (m_Text.equals("d1g1t4l2g0")){
                                MainActivityNew.setConsole(true);
                            }else {
                                MainActivityNew.setConsole(false);
                                console_switch.setChecked(false);
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                            console_switch.setChecked(false);
                            dialog.cancel();
                        }
                    });

                    builder.show();
                } else MainActivityNew.setConsole(false);

            }
        });

        first_name.setOnFocusChangeListener(this);
        last_name.setOnFocusChangeListener(this);
        age.setOnFocusChangeListener(this);
        city.setOnFocusChangeListener(this);
        state.setOnFocusChangeListener(this);
        zipcode.setOnFocusChangeListener(this);
        email.setOnFocusChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        Profile profile = new Profile();
        if (!first_name.getText().toString().isEmpty() && !last_name.getText().toString().isEmpty() && !age.getText().toString().isEmpty() && male.isChecked() || female.isChecked() && !city.getText().toString().isEmpty() && !email.getText().toString().isEmpty()){
            profile.setAge(Integer.parseInt(age.getText().toString()));
            profile.setCity(city.getText().toString());
            profile.setState(state.getText().toString());
            profile.setZipcode(Integer.parseInt(zipcode.getText().toString()));
            profile.setEmail(email.getText().toString());

            profile.setFirst_name(first_name.getText().toString());
            profile.setLast_name(last_name.getText().toString());

            if (male.isChecked()) profile.setSex('m');
            if (female.isChecked()) profile.setSex('f');

            if (alert.isChecked()) profile.setAlert(true);
            else profile.setAlert(false);

            if (sound.isChecked()) profile.setSound(true);
            else profile.setSound(false);

            Preferences.getInstance(getContext()).saveProfile(profile);
            Toast.makeText(getContext(), R.string.settigs_saved, Toast.LENGTH_SHORT).show();

            String gender = null;

            switch (profile.getSex()){
                case 'm':
                    gender = "male";
                    break;

                case 'f':
                    gender = "female";
                    break;
            }

            JSONArray array = new JSONArray();
            try {
                JSONObject demographics = new JSONObject();
                demographics.put("age", profile.getAge());
                demographics.put("gender", gender);
                demographics.put("city", profile.getCity());

                array.put(demographics);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            D2GOSDK.updateDemographics(array, getContext());
            if(!D2GOSDK.isConnected(getContext())) D2GOSDK.init(getContext(), Preferences.getInstance(getContext()).getFirebaseToken());
            MainActivityNew.viewPager.setCurrentItem(0, true);
        }else{
            Toast.makeText(getContext(), R.string.fields_need, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onFocusChange(View view, boolean b) {
        if (!b){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void setVersion(){
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version.setText("Version: "+pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}