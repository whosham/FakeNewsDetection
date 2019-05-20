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
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.fakenewsdetection.Utilities.Hashing;

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

            Log.d("AddImage", "3 Image selection done");
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
                //Adding Progress bar
                progressBar =  (ProgressBar) findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);

                Log.d("Add event Menu ", "Done pressed");
                //Uploading the event to the server
                //Loading email from shared preferences
                SharedPreferences prefs = getSharedPreferences(MY_GLOBAL_PREFS, MODE_PRIVATE);
                final String email = prefs.getString(activity_login.EMAIL_KEY, "");
                Bundle extras = getIntent().getExtras();
                final String latitude, longitude;
                if (extras != null) {
                    latitude = extras.getString("lat");
                    longitude = extras.getString("long");
                    final String image = imageToString(bitmap);
                    eventDescriptionEditText= findViewById(R.id.editText);
                    final String description = eventDescriptionEditText.getText().toString();
                    Log.d("AddImage", "Lat/Long" + latitude + "/" + longitude);
                    Log.d("AddImage", "Image string" + image);
                    Log.d("AddImage", "Hashed image" + Hashing.hashPassword(image, Hashing.SALT));
                    //  Toast.makeText(this, "Lat/Long" + latitude + "/" + longitude+ ">>>>>" + image , Toast.LENGTH_SHORT).show();
                    uploadingImages(image,Hashing.hashPassword(image, Hashing.SALT) ,new VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            //All good
                            Log.d("uploadingImage", "image uploaded to the server :" + result);
                            //Dismissing the progress bar
                            Toast.makeText(addEvent.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);

                            setResult(RESULT_OK, getIntent());
                            finish();
//                            String image_url ="http://192.168.3.103/data/"+Hashing.hashPassword(image, Hashing.SALT) ;
//                            //Adding the event data to the blockchain
//                            addingEvent(email, description, image_url, latitude, longitude, new VolleyCallback() {
//                                @Override
//                                public void onSuccess(String result) {
//
//                                }
//                            });


                        }
                    });
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public interface VolleyCallback{
        void onSuccess(String result);
    }


    private void uploadingImages(final String image,  final String image_url , final VolleyCallback callback){
        Log.d("uploadingimage ", "inside uplaod image username/image" +"/"+ image + ">image_url " + image_url) ;
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.3.103/images_upload.php",

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d("TRACKCREATEGROUP", "3 calling groupdata") ;
                        try {
                            Log.d("uploadingimage", "Success:" + String.valueOf(response) ) ;
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
                Log.d("uplaodingimage", "Fail to upload:" + String.valueOf(error) ) ;
                setResult(RESULT_CANCELED, getIntent());
                finish();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Log.d("uploadingimage", "Sending to server:" + "/"+ image + "/" + image_url) ;
                params.put("image", image);
                params.put("image_url", image_url);
                params.put("action", "upload_image" );
                return params;
            }
        };

        Volley.newRequestQueue(addEvent.this).add(request);
    }



    private void addingEvent(final String email, final String description , final String image_url , final String latitude , final String longitude,final VolleyCallback callback){
        Log.d("addingEvent", "inside addingevent email url location " +"/"+ email + "/") ;
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.3.103/images_upload.php",

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d("TRACKCREATEGROUP", "3 calling groupdata") ;
                        try {
                            Log.d("addingEvent", "Success:" + String.valueOf(response) ) ;
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
                Log.d("addingEvent", "Fail to add Event:" + String.valueOf(error) ) ;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Log.d("addingEvent", "Sending event to server:" + "/"+"/" + image_url) ;
                params.put("email", email);
                params.put("description", description);
                params.put("image_url", image_url);
                params.put("latitude", latitude);
                params.put("longitude", longitude);
                params.put("action", "add_event" );
                return params;
            }
        };

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