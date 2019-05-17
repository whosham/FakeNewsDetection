package com.example.fakenewsdetection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    //Test pushing from android studio local to remote master1


    //Referencing UI elements
    private FloatingActionButton floatingActionButton;
    private BottomAppBar bottomAppBar;
    private Button querycc;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager ;
    private CustomAdapter adapter ;
    private List<MyData> data_list ;


    //Variables for activity request in order to track the status for every activity individually and
    //Track it's exit status
    private static final int SIGNIN_REQUEST = 1001;
    private static final int ADDEVENT_REQUEST = 1003;
    private static final int LOCATION_ACCESS_REQUEST = 1004;
    //creating a global shared preferences
    public static final String MY_GLOBAL_PREFS = "my_global_prefs" ;

    //google api client for Location
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String latitude,longitude;
    private int LOCATION_PERMISSION_CODE=1;


    //Trying Background update of activity
    private FusedLocationProviderClient fusedLocationClient1;


    static MainActivity instance ;
    public static MainActivity getInstance(){
        return instance ;
    }




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




        //checking Location permissions.
        //checking on Location permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Location Access already been granted! ", Toast.LENGTH_SHORT).show();
           // updateLocation();
        }
        else {
            //Call the location activity request
            Intent allowLocationAccess = new Intent(MainActivity.this, LocationRequest.class);
            startActivityForResult(allowLocationAccess, LOCATION_ACCESS_REQUEST);
        }

        // using background for location
        instance= this;





        //Getting Location
        googleApiClient =  new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this) ;


        //Home Feed //
//        recyclerView = findViewById(R.id.recyclerView) ;
//        data_list =  new ArrayList<>() ;
//        loadDataFromServer(0) ;
//        //om response add
//        adapter.notifyDataSetChanged();
//
//        gridLayoutManager = new GridLayoutManager(this,2);
//        recyclerView.setLayoutManager(gridLayoutManager);
//        //bind data to the recyclerview it self
//        adapter = new CustomAdapter (this, data_list) ;
//        recyclerView.setAdapter(adapter);
//
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                if (gridLayoutManager.findLastCompletelyVisibleItemPosition() == data_list.size()-1){
//                    loadDataFromServer(data_list.get(data_list.size()-1).getId());
//                }
//            }
//        });









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

    private void updateLocation() {
    }

    private void loadDataFromServer(int i) {

        //doing the network request fetch json data and
        // for (int i = - ; i <array.length() ; i++ )  { loiop add to list
        //Mydata data =  new Mydata(id,desc,imageLink )
        //data_list.add(data)
    }

    //for handling menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_app_bar,menu);
        return true;
    }

    @SuppressLint("MissingPermission")
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

            case  R.id.bottom_app_location:
//                fusedLocationClient1 = LocationServices.getFusedLocationProviderClient(this);
//                fusedLocationClient1.getLastLocation()
//                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                            @Override
//                            public void onSuccess(Location location) {
////                                if(location != null){
//                                    latitude=String.valueOf(location.getLatitude());
//                                    longitude=String.valueOf(location.getLongitude());
                                    Toast.makeText(MainActivity.this, "Location is: " +latitude + "/" + longitude  , Toast.LENGTH_SHORT).show();
//                                }
//
//                            }
//                        });


        }

        return super.onOptionsItemSelected(item);
    }


    //Location Handling Methods

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();


    }

    @Override
    protected void onStop() {
        if (googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        //checking on Location permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Location Access already been granted! ", Toast.LENGTH_SHORT).show();
        }
        else {
            requestLocationPermissions();
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null){
                            latitude=String.valueOf(location.getLatitude());
                            longitude=String.valueOf(location.getLongitude());
                        }

                    }
                });
    }


    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Mainactivity", "Connection Suspended ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Mainactivity", "Connection Failed");
    }
//
//
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
