package com.example.fakenewsdetection;

import android.content.Intent;
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

public class activity_login extends AppCompatActivity {


    private static final int REGISTER_REQUEST=1002;
    //UI Reference
    private TextInputEditText loginEmail;
    private TextInputEditText loginPassword;
    private TextView signup;
    //Avoid multi click
    private long mLastClickTime = 0;




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

              //  attemptLogin();
            }
        });

        //if user clicked signup
        //Forget password
        signup=findViewById(R.id.tv_login_signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup = new Intent(activity_login.this,register.class) ;
                startActivityForResult(signup,REGISTER_REQUEST);
            }
        });

    }


//    // Method for handling login
//    private void attemptLogin() {
//
//        Log.d("attempLogin", "login key pressed");
//        // Reset errors.
//        loginEmail.setError(null);
//        loginPassword.setError(null);
//
//        // Store values at the time of the login attempt.
//        String email = loginEmail.getText().toString();
//        String password = loginPassword.getText().toString();
//
//        //Variables to make ure that email and password are ok then
//        boolean cancel = false;
//        View focusView = null;
//
//        // Check for a valid password, if the user entered one.
//        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
//            loginPassword.setError(getString(R.string.error_invalid_password));
//            focusView = loginPassword;
//            cancel = true;
//        }
//
//        // Check for a valid email address.
//        if (TextUtils.isEmpty(email)) {
//            loginEmail.setError(getString(R.string.error_field_required));
//            focusView = loginEmail;
//            cancel = true;
//        }
//
//
//
//        // in case of cancel==True focusing on mail or password textbox to reneter
//        if (cancel) {
//            focusView.requestFocus();
//        }
//        // data entered correctly Packaging back the data to the onActivityResult at the main activity
//        else {
//            //Saving email to the activity so that we can use it at the return statues.
//            //as key value pair EMAIL_KEY -> email(that the user entered)
//            // Now calling method to login check mail and pw in the database
//
//            //Log.d("checkLogin", "flag:"+flag);
//            if ( checkLogin(email.toLowerCase(),password)) {
//                Log.d("checkLogin", "Returning Back to the main");
//                //  getIntent().putExtra(EMAIL_KEY, email.toLowerCase());
//
//                //Reloading data service
//                Intent loadingData = new Intent(login.this, LoadingDataService.class);
//                startActivity(loadingData);
//
//                setResult(RESULT_OK, getIntent());
//                finish();
//            }
//
//            else {
//                Log.d("checkLogin", "Login failed or Back pressed");
//                setResult(RESULT_CANCELED, getIntent());
//                finish();
//            }
//
//
//        }
//
//    }
    private boolean isPasswordValid  (String password){
        return password.length() > 7;
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
}
