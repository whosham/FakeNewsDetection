package com.example.fakenewsdetection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //Referencing UI elements
    private FloatingActionButton floatingActionButton;
    private BottomAppBar bottomAppBar;
    private Button querycc;


    //Variables for activity request in order to track the status for every activity individually and
    //Track it's exit status
    private static final int SIGNIN_REQUEST = 1001;
    private static final int ADDEVENT_REQUEST = 1003;
    //creating a global shared preferences
    public static final String MY_GLOBAL_PREFS = "my_global_prefs" ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Checking if user already logged in
        SharedPreferences prefs = getSharedPreferences(MainActivity.MY_GLOBAL_PREFS, MODE_PRIVATE);
        final String email = prefs.getString(activity_login.EMAIL_KEY, "");
        //if user didn't authenticated before direct to login screen
        if (TextUtils.isEmpty(email)) {
            Intent login = new Intent(MainActivity.this, activity_login.class);
            startActivityForResult(login, SIGNIN_REQUEST);
        }




        floatingActionButton = findViewById(R.id.fab);
        bottomAppBar = findViewById(R.id.bottomAppbar);







        //for handling fab
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               //Adding New Event
                Intent addEvent = new Intent(MainActivity.this, addEvent.class);
                startActivityForResult(addEvent, ADDEVENT_REQUEST);

            }
        });




        //enroll user
        querycc= findViewById(R.id.main_query);
        querycc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("enroluserbutton", "enroll user button pressed");
                enrollUser(email, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject myJson = new JSONObject(result);
                            String jwt= myJson.getString("token");
                            Toast.makeText(MainActivity.this,"JWT:"+jwt, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        //Handling Options menu
        setSupportActionBar(bottomAppBar);




    }
    //for handling menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_app_bar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.bottom_item_logout:
                //Logout setting will delete all the shared preferences so the user must relogin
                Log.d("MainActivity", getString(R.string.Logout));
                //confirm if the user wanted to logout or not
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.Logout))
                        .setMessage(getString(R.string.logout_confirmation))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //User confirmed loging out
                                Toast.makeText(MainActivity.this, getString(R.string.Logout), Toast.LENGTH_SHORT).show();
                                //Delete all his shared preference.
                                SharedPreferences.Editor editor =
                                        getSharedPreferences(MY_GLOBAL_PREFS,MODE_PRIVATE).edit();
                                // Getting Email data from login activity
                                editor.remove(activity_login.EMAIL_KEY) ;
                                editor.apply();

                                // Restarting app in case of logout
                                Intent restart  = getBaseContext().getPackageManager()
                                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                                restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(restart);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


    //Volley call back interface
    public interface VolleyCallback{
        void onSuccess(String result);
    }

    //Enroll user and getting JWT
    public void enrollUser(final String email, final MainActivity.VolleyCallback callback) {
        Log.d("enrolluser", "USERNAME: " + email ) ;
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.3.103:4000/users",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("enrolluser", "check JWT" + String.valueOf(response) ) ;
                        callback.onSuccess(String.valueOf(response.replaceAll("\\s+","")));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //callback.onSuccess(String.valueOf(error));
                error.printStackTrace();
                Log.d("enrolluser", "Fail:" + String.valueOf(error) ) ;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("username" , email);
                params.put("orgName", "Org1" );
               params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Accept", "application/json");
                params.put("Accept-Encoding", "utf-8");
                return params;
            }
        };

        Volley.newRequestQueue(MainActivity.this).add(request);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //checking the result from login from login activity
        if (resultCode == RESULT_OK && requestCode == SIGNIN_REQUEST) {
            Toast.makeText(MainActivity.this,"Welcome!", Toast.LENGTH_LONG).show();
        }

        //checking the Result from Add Event
        if (resultCode == RESULT_OK && requestCode == ADDEVENT_REQUEST) {
            Toast.makeText(MainActivity.this,"Your Story is online!", Toast.LENGTH_LONG).show();
        }




    }
}
