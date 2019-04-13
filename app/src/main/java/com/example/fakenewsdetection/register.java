package com.example.fakenewsdetection;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.fakenewsdetection.Utilities.Hashing;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class register extends AppCompatActivity {


    private TextInputEditText registerEmail ;
    private TextInputEditText registerPassword ;
    private Button registerSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);



        registerEmail= findViewById(R.id.tipet_register_email) ;
        registerPassword=findViewById(R.id.tipet_register_password) ;
        registerSubmit=findViewById(R.id.register_submit_button);



        registerSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = registerEmail.getText().toString();
                final String password = registerPassword.getText().toString();
                //Toast.makeText(register.this,"Register "+ email + " "+ password, Toast.LENGTH_LONG).show();
                registerEmail.setError(null);
                registerPassword.setError(null);

                //Variables to validate user input
                boolean cancel = false;
                View focusView = null;
                //Validating Email
                if (TextUtils.isEmpty(email)) {
                    registerEmail.setError(getString(R.string.error_field_required));
                    focusView = registerEmail;
                    cancel = true;
                }

                if (! isEmailValid(email)) {
                    registerEmail.setError(getString(R.string.error_invalid_email));
                    focusView = registerEmail;
                    cancel = true;

                }
                //Check if the user enter a white space between username
                if ((email).contains(" ")) {
                    registerEmail.setError(getString(R.string.error_invalid_email));
                    focusView = registerEmail;
                    cancel = true;
                }
                //Data is not valid recheck
                if (cancel){
                    focusView.requestFocus();
                }
                //Data is ready to submit
                else {


                     registerUser(email.toLowerCase(),Hashing.hashPassword( password,Hashing.SALT), new VolleyCallback() {
                         @Override
                         public void onSuccess(String result) {
                             Log.d("Register Result", "Server said" +result) ;
                             Toast.makeText(register.this,"Successfully registered", Toast.LENGTH_LONG).show();
                         }
                     });
                }

            }
        });


    }

    //Methods for Validating user input
    private boolean isEmailValid(String email){

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        Log.d("Email Valid", String.valueOf(pat.matcher(email).matches()));
        return pat.matcher(email).matches();
        //return email.contains("@");
    }
    private boolean isPasswordValid  (String password){
        return password.length() > 7;
    }


    public interface VolleyCallback{
        void onSuccess(String result);
    }


    //method for registering user and sending requests over network
    public void registerUser(final String email,final String password,  final register.VolleyCallback callback) {
        Log.d("register", "email Password: " + email +"/"+ password ) ;
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.2.103/register.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("register", "Success:" + String.valueOf(response) ) ;
                        callback.onSuccess(String.valueOf(response.replaceAll("\\s+","")));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //callback.onSuccess(String.valueOf(error));
                error.printStackTrace();
                Log.d("register", "Fail:" + String.valueOf(error) ) ;

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email" , email) ;
                params.put("password" , password) ;
                params.put("action", "register" );
                return params;
            }
        };

        Volley.newRequestQueue(register.this).add(request);
    }
}
