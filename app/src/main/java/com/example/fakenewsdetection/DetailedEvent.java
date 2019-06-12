package com.example.fakenewsdetection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

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

import static com.example.fakenewsdetection.MainActivity.EXTRA_Description;
import static com.example.fakenewsdetection.MainActivity.EXTRA_EVENT_ID;
import static com.example.fakenewsdetection.MainActivity.EXTRA_IMAGE_URL;
import static com.example.fakenewsdetection.MainActivity.EXTRA_LOCATION;
import static com.example.fakenewsdetection.MainActivity.EXTRA_TIMESTAMP;
import static com.example.fakenewsdetection.MainActivity.EXTRA_TRUSTWORTHINESS;
import static com.example.fakenewsdetection.MainActivity.MY_GLOBAL_PREFS;

public class DetailedEvent extends AppCompatActivity {

    private static final int VOTING_REQUEST =1006;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_event);

        Intent intent = getIntent() ;
        String imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL) ;
        String description = intent.getStringExtra(EXTRA_Description) ;
        String eventId = intent.getStringExtra(EXTRA_EVENT_ID) ;
        String timestamp = intent.getStringExtra(EXTRA_TIMESTAMP) ;
        String location = intent.getStringExtra(EXTRA_LOCATION) ;
        String trustworthiness = intent.getStringExtra(EXTRA_TRUSTWORTHINESS) ;
        Log.d("detailedevent", "event id" + eventId + description + trustworthiness + location ) ;

        //Displaying the detailed event


        ImageView imageUrlIv = findViewById(R.id.detailed_image_iv) ;
        TextView eventIdTv= findViewById(R.id.detailed_eventid_tv) ;
        TextView trustworthinessTv= findViewById(R.id.detailed_rank_tv) ;
        TextView locationTV= findViewById(R.id.detailed_location_iv) ;
        TextView timestampTv= findViewById(R.id.detailed_timestamp_tv) ;
        TextView desctiptionTv= findViewById(R.id.detailed_description_tv) ;
        Glide.with(this).load("http://192.168.3.103/data/"+imageUrl+".jpg").into(imageUrlIv);
        eventIdTv.setText(eventId);
        locationTV.setText(location);
        timestampTv.setText(timestamp);
        desctiptionTv.setText(description);
        trustworthinessTv.setText(trustworthiness);






//        //Query specific event to get ssessments
//        try {
//            querySpecificEvent(eventId,new VolleyCallback() {
//                @Override
//                public void onSuccess(String result) {
//                    try {
//                        JSONArray array = new JSONArray(result);
//                        for (int i = 0; i < array.length(); i++) {
//                            JSONObject row = array.getJSONObject(i);
//                            String image_url= row.getString("image");
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        final ImageView upvote_button = findViewById(R.id.event_upvote_button_iv) ;
        final ImageView downvote_button = findViewById(R.id.event_downvote_button_iv) ;









        upvote_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetailedEvent.this,"Upvoted!", Toast.LENGTH_LONG).show();
                //upvote_button.setColorFilter(getResources().getColor(R.color.colorGreen)) ;
                //Call the voting activity
                Intent voting = new Intent(DetailedEvent.this, Voting.class);
                voting.putExtra("votetype", "Upvote!") ;
                startActivityForResult(voting,VOTING_REQUEST);
            }
        });

        downvote_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetailedEvent.this,"downvoted!", Toast.LENGTH_LONG).show();
               // downvote_button.setColorFilter(getResources().getColor(R.color.colorRed)) ;
                //Call the voting activity
                Intent voting = new Intent(DetailedEvent.this, Voting.class);
                voting.putExtra("votetype", "Downvote!") ;
                startActivityForResult(voting,VOTING_REQUEST);
            }
        });

    }


    //Volley call back interface
    public interface VolleyCallback{
        void onSuccess(String result);
    }


    public void querySpecificEvent(final String eventId, final DetailedEvent.VolleyCallback callback) throws UnsupportedEncodingException {


        SharedPreferences prefs = getSharedPreferences(MY_GLOBAL_PREFS, MODE_PRIVATE);
        final String JWT = prefs.getString(MainActivity.JWT, "");
        Log.d("querySpecificEvent", "JWT: " + JWT ) ;


        String url = null;

        url = "http://192.168.3.103:4000/channels/mychannel/chaincodes/mycc?peer=peer0.org1.example.com&fcn=getFullEvent&args=" +eventId;
        Log.d("querySpecificEvent", "url encoded: " + url ) ;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("querySpecificEvent", "success to querychaincode:" + String.valueOf(response) ) ;
                        callback.onSuccess(String.valueOf(response));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //callback.onSuccess(String.valueOf(error));
                error.printStackTrace();
                Log.d("querySpecificEvent", "Fail to querychaincode:" + String.valueOf(error) ) ;
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
        Volley.newRequestQueue(DetailedEvent.this).add(request);
    }

}
