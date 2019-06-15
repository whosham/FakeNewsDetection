package com.example.fakenewsdetection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
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

public class DetailedEvent extends AppCompatActivity  implements AssessmentAdapter.onItemClickListener {

    //private GridLayoutManager gridLayoutManager ;
    private RecyclerView recyclerView;
    private  AssessmentAdapter adapter ;
    private ArrayList<AssessmentData> data_list ;

    //Adapter


    private static final int VOTING_REQUEST =1006;
    String eventId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_event);

        //Adding assessment using recycler view
        //Home Feed //
        recyclerView = findViewById(R.id.assessment_recyclerView) ;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        data_list =  new ArrayList<>() ;



        Intent intent = getIntent() ;
        String imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL) ;
        String description = intent.getStringExtra(EXTRA_Description) ;
        eventId = intent.getStringExtra(EXTRA_EVENT_ID) ;
        String timestamp = intent.getStringExtra(EXTRA_TIMESTAMP) ;
        String location = intent.getStringExtra(EXTRA_LOCATION) ;
        String trustworthiness = intent.getStringExtra(EXTRA_TRUSTWORTHINESS) ;
        Log.d("detailedevent", "event id" + eventId + description + trustworthiness + location ) ;

        //Displaying the detailed event

        RatingBar ratingBar = findViewById(R.id.detailed_ratingBar);
        ImageView imageUrlIv = findViewById(R.id.detailed_image_iv) ;
        TextView eventIdTv= findViewById(R.id.detailed_eventid_tv) ;
        TextView trustworthinessTv= findViewById(R.id.detailed_rank_tv) ;
        TextView locationTV= findViewById(R.id.detailed_location_iv) ;
        TextView timestampTv= findViewById(R.id.detailed_timestamp_tv) ;
        TextView desctiptionTv= findViewById(R.id.detailed_description_tv) ;
       // final TextView assessmentTv= findViewById(R.id.detailed_assessment_tv) ;
        Glide.with(this).load("http://192.168.3.103/data/"+imageUrl+".jpg").into(imageUrlIv);
        eventIdTv.setText(eventId);
        locationTV.setText(location);
        timestampTv.setText("Posted: " +timestamp);
        desctiptionTv.setText(description);
        trustworthinessTv.setText("Trustorthiness: " + trustworthiness);

                //Rating bar
                float rating = Float.parseFloat(trustworthiness);
                if (rating <= 0) {
                    //0star
                    ratingBar.setRating(0);
                }
                else if ( rating > 0 && rating <5 ) {
                    //one star
                    ratingBar.setRating(1);
                }
                else if (rating >= 5 && rating <10 ) {
                    //two stars
                    ratingBar.setRating(2);
                }
                else if (rating >= 10 && rating <15 ) {
                    //3 stars
                    ratingBar.setRating(3);
                }
                else if (rating >= 15 && rating < 20 ) {
                    //4 stars
                    ratingBar.setRating(4);
                }
                else if (rating >= 20 ) {
                    //5 stars
                    ratingBar.setRating(5);
                }



        //Query specific event to get ssessments
        try {
            querySpecificEvent(eventId,new VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    try {
                       // JSONArray array = new JSONArray(result);
                        JSONObject jsonResult = new JSONObject(result) ;
                        Log.d("querySpecificEvent", "jsonResult>>" + jsonResult) ;
                        JSONArray assessmentsArray = jsonResult.getJSONArray("assessments");
                        Log.d("querySpecificEvent", "assessmentsarray>>" + assessmentsArray) ;
                        for (int i = 0; i < assessmentsArray.length(); i++) {
                            JSONObject row = assessmentsArray.getJSONObject(i);
                            String id = row.getString("id");
                            String timestamp = row.getString("timestamp");
                            String rating = row.getString("rating");
                            String trustworthiness = row.getString("trustworthiness");
                            String description = row.getString("description");
                            String image = row.getString("image");
                            Log.d("querySpecificEvent", "assessment " + i + ": " + id + timestamp + rating + trustworthiness + description + image) ;

                           // assessmentTv.setText(assessments);
                            data_list.add(new AssessmentData(id,description,image,trustworthiness,timestamp,rating)) ;
                            adapter = new AssessmentAdapter(DetailedEvent.this, data_list) ;
                            recyclerView.setAdapter(adapter);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

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
                voting.putExtra("eventid", eventId) ;
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
                voting.putExtra("eventid", eventId) ;
                startActivityForResult(voting,VOTING_REQUEST);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //checking the Result from Add Event
        if (resultCode == RESULT_OK && requestCode == VOTING_REQUEST) {
            SnackBarMessage(R.string.votingResult,getResources().getColor(R.color.colorGreen));
            finish();
            startActivity(getIntent());
            // Toast.makeText(MainActivity.this,"Your Story is online!", Toast.LENGTH_LONG).show();
        }
        if (resultCode == RESULT_CANCELED && requestCode == VOTING_REQUEST) {
            SnackBarMessage(R.string.error,getResources().getColor(R.color.colorOrange));
        }
    }

    @Override
    public void onItemClick(int position) {

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
