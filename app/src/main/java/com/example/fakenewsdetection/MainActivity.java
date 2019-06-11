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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

public class MainActivity extends AppCompatActivity implements CustomAdapter.onItemClickListener,ConnectionCallbacks, OnConnectionFailedListener {

    //Referencing UI elements
    private FloatingActionButton floatingActionButton;
    private BottomAppBar bottomAppBar;

    //private GridLayoutManager gridLayoutManager ;
    private RecyclerView recyclerView;
    private CustomAdapter adapter ;
    private ArrayList<MyData> data_list ;


    //Variables for activity request in order to track the status for every activity individually and
    //Track it's exit status
    private static final int SIGNIN_REQUEST = 1001;
    private static final int ADDEVENT_REQUEST = 1003;
    private static final int LOCATION_ACCESS_REQUEST = 1004;

    //creating a global shared preferences
    public static final String MY_GLOBAL_PREFS = "my_global_prefs" ;
    public static final String JWT= "token";
    String email;
    //google api client for Location
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String latitude,longitude;
    private int LOCATION_PERMISSION_CODE=1;

    //variables for on click Events
    public static final String EXTRA_URL = "imageUrl" ;
    public static final String EXTRA_Description = "description" ;



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
        email = prefs.getString(activity_login.EMAIL_KEY, "");
        //if user didn't authenticated before direct to login screen
        if (TextUtils.isEmpty(email)) {
            Intent login = new Intent(MainActivity.this, activity_login.class);
            startActivityForResult(login, SIGNIN_REQUEST);
        }




        //checking Location permissions.
        //checking on Location permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(MainActivity.this, "Location Access already been granted! ", Toast.LENGTH_SHORT).show();
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


        //Getting JWT and Enroll user
        // Checking JWT
        final String JWT = prefs.getString(MainActivity.JWT, "");
        if (JWT.isEmpty()){
            Log.d("enrolluser", "NO jwt found enrolling user");
            enrollUser(email, new VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JSONObject myJson = new JSONObject(result);
                        Log.d("enrolluser", "before parsing " + result );
                        String jwt= myJson.getString("token");
                        Log.d("enrolluser", "after parsing " + jwt );
                        //  Toast.makeText(MainActivity.this,"JWT:"+jwt, Toast.LENGTH_LONG).show();
                        SharedPreferences.Editor editor =
                                getSharedPreferences(MY_GLOBAL_PREFS,MODE_PRIVATE).edit();
                        editor.putString(MainActivity.JWT,jwt);
                        editor.apply();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        //Home Feed //
        recyclerView = findViewById(R.id.recyclerView) ;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        data_list =  new ArrayList<>() ;


//        double latitudeDouble = 0, longitudeouble=0;
//        latitude = Double.parseDouble(String.valueOf(latitude));
//        longitude= Double.parseDouble(String.valueOf(longitude));
//

        queryChainCode(52,10,new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONArray array = new JSONArray(result);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject row = array.getJSONObject(i);
                        String image_url= row.getString("image");
                        String id= row.getString("id");
                        JSONObject location= row.getJSONObject("location");

                        double latitude = Double.parseDouble(location.getString("latitude"));
                        double longitude = Double.parseDouble(location.getString("longitude"));
                        String timestamp =row.getString("timestamp");
                        double trustworthiness=Double.parseDouble(row.getString("trustworthiness"));



                        String description= row.getString("description");
                        Log.d("queryChaincode", "row " + i + ":" + id + description+image_url+latitude+longitude+timestamp+trustworthiness);
                        data_list.add(new MyData(id,description,image_url,latitude,longitude,timestamp,trustworthiness)) ;
                    }
                    adapter = new CustomAdapter(MainActivity.this, data_list) ;
                    recyclerView.setAdapter(adapter);
                    // Implementing per click
                    adapter.setOnItemClickListener(MainActivity.this);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        floatingActionButton = findViewById(R.id.fab);
        bottomAppBar = findViewById(R.id.bottomAppbar);


        //for handling fab
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //Adding New Event
                Intent addEvent = new Intent(MainActivity.this, addEvent.class);
                addEvent.putExtra("lat", latitude) ;
                addEvent.putExtra("long", longitude) ;
                startActivityForResult(addEvent, ADDEVENT_REQUEST);
            }
        });

        //Handling Options menu
        setSupportActionBar(bottomAppBar);
    }


    //Enroll user and getting JWT
    public void queryChainCode(final double latitude, final double longitude, final VolleyCallback callback) {
        SharedPreferences prefs = getSharedPreferences(MY_GLOBAL_PREFS, MODE_PRIVATE);
        final String JWT = prefs.getString(MainActivity.JWT, "");
        Log.d("querychaincode", "location: " + latitude + "/" + longitude ) ;


        double latGte,latLte,longLte,longGte;
        latGte=latitude-1;
        latLte=latitude+1 ;
        longGte=longitude-1;
        longLte=longitude+1;



        String selector="{ \"selector\":{ \"docType\":\"event\", \"location.latitude\":{ \"$gte\":"+latGte+", \"$lte\":"+latLte+" }, \"location.longitude\":{ \"$gte\":"+longGte+", \"$lte\":"+longLte+"} } }\n" ;
        Log.d("querychaincode", "selector: " + selector ) ;

        String url = null;
        
        try {
            url = "http://192.168.3.103:4000/channels/mychannel/chaincodes/mycc?peer=peer0.org1.example.com&fcn=queryEvents&args=" + URLEncoder.encode(selector, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d("querychaincode", "url encoded: " + url ) ;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("queryChaincode", "success to querychaincode:" + String.valueOf(response) ) ;
                        callback.onSuccess(String.valueOf(response.replaceAll("\\s+","")));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //callback.onSuccess(String.valueOf(error));
                error.printStackTrace();
                Log.d("queryChaincode", "Fail to querychaincode:" + String.valueOf(error) ) ;
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers= new HashMap<String, String>();
                String authorization= "Bearer " + JWT ;
                headers.put("Content-type","application/json");
                headers.put("Accept-Encoding", "utf-8");
                headers.put("authorization", authorization);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(MainActivity.this).add(request);
    }

    private void updateLocation() {
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
                Toast.makeText(MainActivity.this, "Location is: " +latitude + "/" + longitude  , Toast.LENGTH_SHORT).show();
                return true;

            case  R.id.bottom_app_refresh:
                //Refreshing the JWT
                //Getting JWT and Enroll user
                enrollUser(email, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject myJson = new JSONObject(result);
                            Log.d("enrolluser", "before parsing " + result );
                            String jwt= myJson.getString("token");
                            Log.d("enrolluser", "after parsing " + jwt );
                            //  Toast.makeText(MainActivity.this,"JWT:"+jwt, Toast.LENGTH_LONG).show();
                            SharedPreferences.Editor editor =
                                    getSharedPreferences(MY_GLOBAL_PREFS,MODE_PRIVATE).edit();
                            editor.putString(MainActivity.JWT,jwt);
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return true;

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
            //Toast.makeText(MainActivity.this, "Location Access already been granted! ", Toast.LENGTH_SHORT).show();
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


    //Handling per click events.
    @Override
    public void onItemClick(int position) {
        Intent detailedEvent = new Intent(this, DetailedEvent.class);
        MyData clickedEvent = data_list.get(position) ;
        detailedEvent.putExtra(EXTRA_URL, clickedEvent.getImage_url()) ;
        detailedEvent.putExtra(EXTRA_Description, clickedEvent.getDescription()) ;
        startActivity(detailedEvent);
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
            SnackBarMessage(R.string.addevent_success,getResources().getColor(R.color.colorGreen));
           // Toast.makeText(MainActivity.this,"Your Story is online!", Toast.LENGTH_LONG).show();
        }
        if (resultCode == RESULT_CANCELED && requestCode == ADDEVENT_REQUEST) {
            SnackBarMessage(R.string.error,getResources().getColor(R.color.colorOrange));
        }


    }

    //Method for Snackbar
    private void SnackBarMessage(int message,int  Color ) {
        //showing a snackbar message to the user
        Snackbar bar = Snackbar.make(findViewById(android.R.id.content),message, Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.colorWhite))
                .setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle user action
                    }
                });
        View snackBarView = bar.getView();
        snackBarView.setBackgroundColor(Color);
        TextView tv = (TextView) bar.getView().findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(getResources().getColor(R.color.colorWhite));
        bar.show();
    }
}
