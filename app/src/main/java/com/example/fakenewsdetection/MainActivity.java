package com.example.fakenewsdetection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.LocationRequest;
public class MainActivity extends AppCompatActivity implements CustomAdapter.onItemClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    //Test pushing from android studio local to remote master1



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
    public static final String LOCATION_LAT_KEY = "lat_key";
    public static final String LOCATION_LONG_KEY = "long_key";
    //google api client for Location
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location location;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private String latitude,longitude;
    public Double latCall,longCall;
    private int LOCATION_PERMISSION_CODE=1;

    //variables for on click Events
    public static final String EXTRA_EVENT_ID = "id" ;
    public static final String EXTRA_IMAGE_URL = "image" ;
    public static final String EXTRA_Description = "description" ;
    public static final String EXTRA_LOCATION = "location" ;
    public static final String EXTRA_TIMESTAMP = "timestamp" ;
    public static final String EXTRA_TRUSTWORTHINESS  = "trustworthiness" ;



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
        }
        else {
            //Call the location activity request
            Intent allowLocationAccess = new Intent(MainActivity.this, LocationPermissionRequest.class);
            startActivityForResult(allowLocationAccess, LOCATION_ACCESS_REQUEST);
        }



//        //Getting Location
//        googleApiClient =  new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//     //   locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
////


        getLocationpdates();

        latitude = prefs.getString(MainActivity.LOCATION_LAT_KEY, "");
        longitude= prefs.getString(MainActivity.LOCATION_LONG_KEY, "");
        Log.d("Mainactivity", "From Shared Preference" + latitude + longitude);


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

        if (latitude == null && longitude == null ){
            Toast.makeText(MainActivity.this, "Unable to Fetch the current Location!", Toast.LENGTH_SHORT).show();
        }
        else {
            queryChainCode(Double.parseDouble(latitude),Double.parseDouble(longitude),new VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JSONArray array = new JSONArray(result);
                        JSONArray sortedJsonArray = new JSONArray();
                        //sorting the data based on the time
                        //if(array.length()>1) {
                        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
                        for (int i = 0; i < array.length(); i++) {
                            jsonValues.add(array.getJSONObject(i));
                        }
                        Collections.sort( jsonValues, new Comparator<JSONObject>() {
                            //You can change "Name" with "ID" if you want to sort by ID
                            private static final String KEY_NAME = "timestamp";
                            @SuppressLint("NewApi")
                            @Override
                            public int compare(JSONObject a, JSONObject b) {
                                int valA,valB,compare=0;
                                try {
                                    valA = Integer.parseInt((a.getString(KEY_NAME)));
                                    valB = Integer.parseInt((b.getString(KEY_NAME)));
                                    compare = Integer.compare(valB, valA);
                                    Log.d("sorting json", "ValA VALB " + valA +"/"+ valB ) ;
                                }
                                catch (JSONException e) {
                                    //do something
                                }
                                return compare;
                            }
                        });

                        for (int i = 0; i < array.length(); i++) {
                            sortedJsonArray.put(jsonValues.get(i));
                        }


                        Log.d("queryChaincode", "Sorted json array" + sortedJsonArray) ;

                        for (int i = 0; i < sortedJsonArray.length(); i++) {
                            JSONObject row = sortedJsonArray.getJSONObject(i);
                            String image_url= row.getString("image");
                            String id= row.getString("id");
                            JSONObject location= row.getJSONObject("location");
                            double latitude = Double.parseDouble(location.getString("latitude"));
                            double longitude = Double.parseDouble(location.getString("longitude"));
                            String cityName=null;
                            String timestamp =row.getString("timestamp");
                            double trustworthiness=Double.parseDouble(row.getString("trustworthiness"));

                            //processing the time stamp and convert it to a readable date.
                            long ts= Long.parseLong(timestamp);
                            Date d = new Date(ts * 1000);
                            DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss zzz");
                            String mDate= (df.format(d));

                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                            List<Address> addresses = null;
                            try {
                                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (addresses.size() > 0){
                                cityName=addresses.get(0).getLocality();
                                Log.d("querychaincode", "City name:"+ addresses.get(0).getLocality() ) ;
                            }



                            String description= row.getString("description");
                            Log.d("querychaincode", "row " + i + ":" + id + description+image_url+latitude+longitude+timestamp+trustworthiness);
                            data_list.add(new MyData(id,description,image_url,latitude,longitude,cityName,mDate,trustworthiness)) ;
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

        }


        floatingActionButton = findViewById(R.id.fab);
        bottomAppBar = findViewById(R.id.bottomAppbar);

        //for handling fab
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //Adding New Event
                Intent addEvent = new Intent(MainActivity.this, addEvent.class);
                SharedPreferences prefs = getSharedPreferences(MY_GLOBAL_PREFS, MODE_PRIVATE);
                latitude = prefs.getString(MainActivity.LOCATION_LAT_KEY, "");
                longitude= prefs.getString(MainActivity.LOCATION_LONG_KEY, "");
                if(latitude !=  null  && longitude != null ) {
                    addEvent.putExtra("lat", latitude);
                    addEvent.putExtra("long", longitude);
                    startActivityForResult(addEvent, ADDEVENT_REQUEST);
                }
                else {
                    Toast.makeText(MainActivity.this, "Unable to Fetch the current Location!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Handling Options menu
        setSupportActionBar(bottomAppBar);
    }



    public void queryChainCode(final Double latitude, final Double longitude, final VolleyCallback callback) {
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
                        callback.onSuccess(String.valueOf(response));
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

        request.setRetryPolicy(new DefaultRetryPolicy(30 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(MainActivity.this).add(request);
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
                SharedPreferences prefs = getSharedPreferences(MY_GLOBAL_PREFS, MODE_PRIVATE);
                latitude = prefs.getString(MainActivity.LOCATION_LAT_KEY, "");
                longitude= prefs.getString(MainActivity.LOCATION_LONG_KEY, "");
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

            case  R.id.bottom_app_home:
                finish();
                startActivity(getIntent());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //Location Handling Methods

    @SuppressLint("MissingPermission")
    void getLocationpdates(){


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           return ;
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this) ;
        fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(),
                getLocationCallback(),
                Looper.myLooper());
    }

    private LocationCallback getLocationCallback() {
        LocationCallback callback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
               Location location= locationResult.getLastLocation();
               latCall = location.getLatitude();
               longCall= location.getLongitude();
               SharedPreferences.Editor editor =
                        getSharedPreferences(MY_GLOBAL_PREFS,MODE_PRIVATE).edit();
                editor.putString(MainActivity.LOCATION_LAT_KEY, String.valueOf(latCall));
                editor.putString(MainActivity.LOCATION_LONG_KEY, String.valueOf(longCall));
                editor.apply();
                Log.d("Mainactivity", "lat longcall>>" + latCall+">>" + longCall);
            }
        };
        return callback;
    }

    private LocationRequest getLocationRequest() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(20000);
        request.setFastestInterval(5000);
        return request;
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Log.d("Mainactivity", "onstart ");
//        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
//        if (googleApiClient.isConnected()){
//            googleApiClient.disconnect();
//        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d("Mainactivity", "inside onConnected:" );
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
                            Log.d("Mainactivity", "inside fusedlocation:" + latitude +longitude );
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
    }
//    @Override
@Override
public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

}

    @Override
    public void onLocationChanged(Location location) {

    }
//    public void onConnectionSuspended(int i) {
//        Log.d("Mainactivity", "Connection Suspended ");
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.d("Mainactivity", "Connection Failed");
//    }
//
//
//    @Override
//    public void onLocationChanged(Location location) {
//
//    }
//
//
    //Handling per click events.
    @Override
    public void onItemClick(int position) {
        Intent detailedEvent = new Intent(this, DetailedEvent.class);
        MyData clickedEvent = data_list.get(position) ;
        detailedEvent.putExtra(EXTRA_IMAGE_URL, clickedEvent.getImage_url()) ;
        detailedEvent.putExtra(EXTRA_Description, clickedEvent.getDescription()) ;
        detailedEvent.putExtra(EXTRA_EVENT_ID, clickedEvent.getId()) ;
        detailedEvent.putExtra(EXTRA_LOCATION, clickedEvent.getCityName()) ;
        detailedEvent.putExtra(EXTRA_TIMESTAMP, clickedEvent.getTimestamp()) ;
        detailedEvent.putExtra(EXTRA_TRUSTWORTHINESS,clickedEvent.getTrustworthiness()) ;

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
