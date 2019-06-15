package com.example.fakenewsdetection;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.fakenewsdetection.Utilities.Hashing;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.example.fakenewsdetection.MainActivity.EXTRA_Description;
import static com.example.fakenewsdetection.MainActivity.MY_GLOBAL_PREFS;


public class Voting extends AppCompatActivity {

    private int STORAGE_PERMISSION_CODE = 1;
    private ImageButton addImage;
    private Button votingConfirm ;
    private static final int IMAGE_REQUEST = 1007;
    private Bitmap bitmap;
    private ImageView selectedImage;
    private float rating;
    private String eventId,description,image,image_hash="";
    private TextView voteTypeTV ;
    private EditText votingDescriptionEditText ;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        //getting if user making up/downvote
        Bundle extras = getIntent().getExtras();
        String voteType = extras.getString("votetype") ;
        eventId= extras.getString("eventid");
        voteTypeTV = findViewById(R.id.tv_voting_type) ;
        voteTypeTV.setText(voteType);
        if (voteType.equals("Upvote!")){
            voteTypeTV.setTextColor(getResources().getColor(R.color.colorGreen));
            rating=1;
        }
        else {
            voteTypeTV.setTextColor(getResources().getColor(R.color.colorRed));
            rating=-1;
        }



        //checking on Storage permission
        if (ContextCompat.checkSelfPermission(Voting.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        } else {
            requestStoragePermissions();
        }
        //Adding an image
        addImage = findViewById(R.id.ib_voting_add_image);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        // Handling Confirmation
        votingConfirm = findViewById(R.id.ib_voting_confirm) ;
        votingConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                votingDescriptionEditText= findViewById(R.id.et_voting_description);
                description = votingDescriptionEditText.getText().toString();
                Log.d("Voting", "checking :" + eventId + " " + rating + description +"/"+ image);
                //Checking if user entered text or Image
                if (description.isEmpty() && bitmap == null ){
                    Toast.makeText(Voting.this, "Please Enter Text or Upload an image!!", Toast.LENGTH_SHORT).show();
                }
                else{
                    //if there is an image
                    if (bitmap != null) {
                        image = imageToString(bitmap);
                        progressBar =  (ProgressBar) findViewById(R.id.progressbar);
                        progressBar.setVisibility(View.VISIBLE);
                        uploadingImages(image,Hashing.hashPassword(image, Hashing.SALT) ,new Voting.VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                //All good
                                Log.d("Voting", "image uploaded to the server :" + result);
                                //Dismissing the progress bar
                                Toast.makeText(Voting.this, "images uploaded Successfully", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);

                                // Adding the vote to the blockchain
                                SharedPreferences prefs = getSharedPreferences(MY_GLOBAL_PREFS, MODE_PRIVATE);
                                String JWT = prefs.getString(MainActivity.JWT, "");
                                Log.d("Voting", "Data is ready With Image:" + eventId + " " + rating + description +"/"+ image_hash + ">>>>>>>" +JWT);
                                progressBar =  (ProgressBar) findViewById(R.id.progressbar);
                                progressBar.setVisibility(View.VISIBLE);
                                image_hash=Hashing.hashPassword(image, Hashing.SALT);
                                assessingEvent(eventId, description,image_hash, rating ,JWT, new VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        Log.d("Voting", "Response:" + result);
                                        try {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            JSONObject jsonResult = new JSONObject(result) ;
                                            String stringResult = String.valueOf(jsonResult.get("success"));
                                            Log.d("Voting", "string:" + stringResult );
                                            if(stringResult.equals("true")){
                                                setResult(RESULT_OK, getIntent());
                                                finish();
                                            }
                                            else {
                                                setResult(RESULT_CANCELED, getIntent());
                                                finish();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });
                            }
                        });
                    }
                    else {
                        // No image only description
                        // Adding the vote to the blockchain
                        SharedPreferences prefs = getSharedPreferences(MY_GLOBAL_PREFS, MODE_PRIVATE);
                        String JWT = prefs.getString(MainActivity.JWT, "");
                        Log.d("Voting", "Data is ready No image :" + eventId + " " + rating + description +"/"+ image_hash + ">>>>>>>" +JWT);
                        progressBar =  (ProgressBar) findViewById(R.id.progressbar);
                        progressBar.setVisibility(View.VISIBLE);
                        assessingEvent(eventId, description,image_hash, rating ,JWT, new VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                Log.d("Voting", "Response:" + result);
                                try {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    JSONObject jsonResult = new JSONObject(result) ;
                                    String stringResult = String.valueOf(jsonResult.get("success"));
                                    Log.d("Voting", "string:" + stringResult );
                                    if(stringResult.equals("true")){
                                        setResult(RESULT_OK, getIntent());
                                        finish();
                                    }
                                    else {
                                        setResult(RESULT_CANCELED, getIntent());
                                        finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });

                    }



                }

            }
        });


    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Toast.makeText(this, "Permission successfully granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission wasn't granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void selectImage() {

        Log.d("addEvent", "2 Inside Image selection done");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {

            Log.d("addEvent", "3 Image selection done");
            Uri path = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                selectedImage = findViewById(R.id.iv_voting_selected_image);
                selectedImage.setImageBitmap(bitmap);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public interface VolleyCallback{
        void onSuccess(String result);
    }


    public void uploadingImages(final String image,  final String image_url , final Voting.VolleyCallback callback){
        Log.d("Voting", "inside uplaod image username/image" +"/"+ image + ">image_url " + image_url) ;
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.3.103/images_upload.php",

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d("TRACKCREATEGROUP", "3 calling groupdata") ;
                        try {
                            Log.d("Voting", "Success:" + String.valueOf(response) ) ;
                            JSONObject jsonObject =  new JSONObject(response);
                            String Response= jsonObject.getString("response");
                            callback.onSuccess(Response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Voting", "Fail to upload:" + String.valueOf(error) ) ;
                setResult(RESULT_CANCELED, getIntent());
                finish();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Log.d("Voting", "Sending to server:" + "/"+ image + "/" + image_url) ;
                params.put("image", image);
                params.put("image_url", image_url);
                params.put("action", "upload_image" );
                return params;
            }
        };

        Volley.newRequestQueue(Voting.this).add(request);
    }


    //Assessment
    private void assessingEvent(final String eventID, final String description , final String image_hash , final float rating , final String JWT, final Voting.VolleyCallback callback){
        Log.d("Voting", "inside assessing event" +"/"+ eventID + "/"+ rating + "/" + description +"/" + image_hash+ "/" + JWT) ;

        final JSONObject jsonWholeObject  = new JSONObject();
        JSONObject jsonEvent   = new JSONObject();
        try {
            jsonEvent.put("event", eventID);
            jsonEvent.put("rating",rating) ;
            jsonEvent.put("description", description);
            jsonEvent.put("image", image_hash) ;

            jsonWholeObject.put("args", jsonEvent );
            Log.d("Voting", "jsonEvent: " + String.valueOf(jsonWholeObject.get("args")) ) ;

            jsonWholeObject.put("fcn","judgeEvent") ;
            jsonWholeObject.put("peers","[\"peer0.org1.example.com\",\"peer0.org2.example.com\"]");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("Voting", "jsonWholeObject : " + jsonWholeObject + "Types:" + jsonWholeObject.getClass().getName() ) ;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,"http://192.168.3.103:4000/channels/mychannel/chaincodes/mycc",
                jsonWholeObject , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess(String.valueOf(response));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Voting", "Fail to add Event on BC:" + String.valueOf(error) ) ;
                setResult(RESULT_CANCELED, getIntent());
                finish();
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
        Volley.newRequestQueue(Voting.this).add(request);
    }



}
