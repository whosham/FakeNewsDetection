package com.example.fakenewsdetection;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.example.fakenewsdetection.MainActivity.MY_GLOBAL_PREFS;

public class addEvent extends AppCompatActivity {

    private int STORAGE_PERMISSION_CODE = 1;
    private ImageButton addImage;
    private static final int IMAGE_REQUEST = 1005;
    private Bitmap bitmap;
    private ImageView selectedImage;
    private EditText eventDescriptionEditText ;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        //checking on Storage permission
        if (ContextCompat.checkSelfPermission(addEvent.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
         //   Toast.makeText(this, "Permission aleady Granted", Toast.LENGTH_SHORT).show();

        } else {
            requestStoragePermissions();
        }

        //Adding an image
        addImage = findViewById(R.id.add_image);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
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
        //TODO loading bar and updating
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
                selectedImage = findViewById(R.id.selected_image);
                selectedImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //for handling menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_event_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.done_button:

                Log.d("addEvent", "Done pressed");
                //Uploading the event to the server
                //Loading email from shared preferences
                SharedPreferences prefs = getSharedPreferences(MY_GLOBAL_PREFS, MODE_PRIVATE);
                final String email = prefs.getString(activity_login.EMAIL_KEY, "");
                final String JWT = prefs.getString(MainActivity.JWT, "");
                Bundle extras = getIntent().getExtras();


                eventDescriptionEditText= findViewById(R.id.editText);
                final String description = eventDescriptionEditText.getText().toString();
                    if (extras != null && bitmap != null ) {
                        final String image;
                        double latitude = 0, longitude=0;
                        Log.d("addEvent", "Bitmap>>>>" + bitmap);
                        image = imageToString(bitmap);
                        try {
                            latitude = Double.parseDouble(extras.getString("lat"));
                            longitude = Double.parseDouble(extras.getString("long"));
                            Log.d("addEvent", "Lat/Long" + latitude + "/" + longitude);
                            Log.d("addEvent", "Image string" + image);
                            Log.d("addEvent", "Hashed image" + Hashing.hashPassword(image, Hashing.SALT));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        //  Toast.makeText(this, "Lat/Long" + latitude + "/" + longitude+ ">>>>>" + image , Toast.LENGTH_SHORT).show();
                        //Adding Progress bar
                        progressBar =  (ProgressBar) findViewById(R.id.progressbar);
                        progressBar.setVisibility(View.VISIBLE);
                        final double finalLongitude = longitude;
                        final double finalLatitude = latitude;
                        uploadingImages(image,Hashing.hashPassword(image, Hashing.SALT) ,new VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                //All good
                                Log.d("addEvent", "image uploaded to the server :" + result);
                                //Dismissing the progress bar
                                Toast.makeText(addEvent.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();

                                progressBar.setVisibility(View.INVISIBLE);

                                String image_hash =Hashing.hashPassword(image, Hashing.SALT) ;
                                //Adding the event data to the blockchain
                                //Adding Progress bar
                                progressBar =  (ProgressBar) findViewById(R.id.progressbar);
                                progressBar.setVisibility(View.VISIBLE);

                                addingEvent(email, description, image_hash, finalLatitude, finalLongitude,JWT, new VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        Log.d("addEvent", "Response:" + result);
                                        try {

                                            progressBar.setVisibility(View.INVISIBLE);

                                            JSONObject jsonResult = new JSONObject(result) ;
                                            String stringResult = String.valueOf(jsonResult.get("success"));
                                            Log.d("addEvent", "string:" + stringResult );
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
                        Toast.makeText(addEvent.this, "Please upload a media file!", Toast.LENGTH_SHORT).show();
                    }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public interface VolleyCallback{
        void onSuccess(String result);
    }


    public void uploadingImages(final String image,  final String image_url , final VolleyCallback callback){
        Log.d("addEvent", "inside uplaod image username/image" +"/"+ image + ">image_url " + image_url) ;
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.3.103/images_upload.php",

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d("TRACKCREATEGROUP", "3 calling groupdata") ;
                        try {
                            Log.d("addEvent", "Success:" + String.valueOf(response) ) ;
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
                Log.d("addEvent", "Fail to upload:" + String.valueOf(error) ) ;
                setResult(RESULT_CANCELED, getIntent());
                finish();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Log.d("addEvent", "Sending to server:" + "/"+ image + "/" + image_url) ;
                params.put("image", image);
                params.put("image_url", image_url);
                params.put("action", "upload_image" );
                return params;
            }
        };

        Volley.newRequestQueue(addEvent.this).add(request);
    }



    private void addingEvent(final String email, final String description , final String image_hash , final Double latitude , final Double longitude, final String JWT, final VolleyCallback callback){
        Log.d("addEvent", "inside addingevent email url location " +"/"+ email + description +"/" + image_hash + "/" + latitude + "/"
                + longitude + "/" + JWT) ;


        final JSONObject jsonWholeObject  = new JSONObject();
        JSONObject jsonLocation = new JSONObject();
        JSONObject jsonEvent   = new JSONObject();
        try {

            jsonEvent.put("title", "fooandroid");



            //make sure that latitude and longitude as float
            jsonLocation.put("latitude",  latitude);
            jsonLocation.put("longitude", longitude) ;

            jsonEvent.put("location",jsonLocation) ;

            jsonEvent.put("description", description);

            jsonEvent.put("image", image_hash) ;

            jsonWholeObject.put("args", jsonEvent );
            Log.d("addEvent", "jsonEvent: " + String.valueOf(jsonWholeObject.get("args")) ) ;



            jsonWholeObject.put("fcn","addEvent") ;
            jsonWholeObject.put("peers","[\"peer0.org1.example.com\",\"peer0.org2.example.com\"]");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("addEvent", "jsonWholeObject : " + jsonWholeObject + "Types:" + jsonWholeObject.getClass().getName() ) ;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,"http://192.168.3.103:4000/channels/mychannel/chaincodes/mycc",
                jsonWholeObject , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess(String.valueOf(response));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("addEvent", "Fail to add Event on BC:" + String.valueOf(error) ) ;
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
        Volley.newRequestQueue(addEvent.this).add(request);
    }


    //To override the UP bar press and Back Press and make the same behavior
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_FIRST_USER);
        super.onBackPressed();

    }

}