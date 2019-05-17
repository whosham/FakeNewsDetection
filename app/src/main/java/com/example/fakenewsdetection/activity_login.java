package com.example.fakenewsdetection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.fakenewsdetection.Utilities.Hashing;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import static com.example.fakenewsdetection.MainActivity.MY_GLOBAL_PREFS;

public class activity_login extends AppCompatActivity {


    private static final int REGISTER_REQUEST=1002;
    //UI Reference
    private TextInputEditText loginEmail;
    private TextInputEditText loginPassword;
    private TextView signup;

    //Avoid multi click
    private long mLastClickTime = 0;
    //to save the user login in the shared preferences
    public static final String EMAIL_KEY = "email_key";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Referencing  UI Elements
        loginEmail = findViewById(R.id.tipet_login_email);
        loginPassword = findViewById(R.id.tipet_login_password);
        final Button login = findViewById(R.id.login_submit_button);

        // Handling event when user press the login key using attemptLogin Method
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //avoid multi click
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                //Validating user input
                final String email = loginEmail.getText().toString().toLowerCase();
                final String password = loginPassword.getText().toString();
                //Toast.makeText(register.this,"Register "+ email + " "+ password, Toast.LENGTH_LONG).show();
                loginEmail.setError(null);
                loginPassword.setError(null);

                //Variables to validate user input
                boolean cancel = false;
                View focusView = null;
                //Validating Email
                if (TextUtils.isEmpty(email)) {
                    loginEmail.setError(getString(R.string.error_field_required));
                    focusView = loginEmail;
                    cancel = true;
                }
                if (TextUtils.isEmpty(password)) {
                    loginPassword.setError(getString(R.string.error_field_required));
                    focusView = loginPassword;
                    cancel = true;
                }

                //Check if the user enter a white space between username
                if ((email).contains(" ")) {
                    loginEmail.setError(getString(R.string.error_invalid_email));
                    focusView = loginEmail;
                    cancel = true;
                }

                //Data is not valid recheck
                if (cancel){
                    focusView.requestFocus();
                }
                //valid data sending network request to check login
                else {
                        loginAttempt(email, Hashing.hashPassword( password,Hashing.SALT), new VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {

                                JSONArray jsonArray = null;
                                try {
                                    jsonArray = new JSONArray(result);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("check user", "Array Length"+ jsonArray.length()) ;
                                //checking if the user email and password are correct.
                                if(jsonArray.length() == 0 ){
                                    Toast.makeText(activity_login.this,"Wrong Email or Password", Toast.LENGTH_LONG).show();
                                }

                                //Credential are correct save the login mail in the shared pref and direct user to home
                                else{
                                    Toast.makeText(activity_login.this,"Login success!", Toast.LENGTH_LONG).show();
                                    //Saving login email  as shared preferences since user successfully authenticated
                                    SharedPreferences.Editor editor =
                                            getSharedPreferences(MY_GLOBAL_PREFS,MODE_PRIVATE).edit();
                                    editor.putString(activity_login.EMAIL_KEY,email);
                                    editor.apply();
                                    setResult(RESULT_OK, getIntent());
                                    finish();
                                }

                            }
                        });
                }

            }
        });

        //if user clicked signup
        signup=findViewById(R.id.tv_login_signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup = new Intent(activity_login.this,register.class) ;
                startActivityForResult(signup,REGISTER_REQUEST);
            }
        });




    }

    private boolean isPasswordValid  (String password){
        return password.length() > 7;
    }


    //To override the UP bar press and Back Press and make the same behavior
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
    }

    //Method to handle the result from activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REGISTER_REQUEST){
            Log.d("Register", "SUCCESS");
            Toast.makeText(this, getString(R.string.registeration_successfull ) , Toast.LENGTH_LONG).show();
        }
        else if (resultCode == RESULT_CANCELED && requestCode == REGISTER_REQUEST){
            Log.d("Register", "FAILED");
            Toast.makeText(this, getString(R.string.registeration_failed ) , Toast.LENGTH_LONG).show();
        }
    }

    public interface VolleyCallback{
        void onSuccess(String result);
    }

    //checking user credential
    public void loginAttempt(final String email, final String password,  final activity_login.VolleyCallback callback) {
        Log.d("activity_login", "email password " + email +"/"+ password) ;
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.3.103/login.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("activity_login", "chekc username and password" + String.valueOf(response) ) ;
                        callback.onSuccess(String.valueOf(response.replaceAll("\\s+","")));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //callback.onSuccess(String.valueOf(error));
                error.printStackTrace();
                Log.d("activity login", "Fail:" + String.valueOf(error) ) ;

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email" , email);
                params.put("password" , password);
                params.put("action", "check_login" );
                return params;
            }
        };

        Volley.newRequestQueue(activity_login.this).add(request);
    }
}
