package com.example.fakenewsdetection;

import android.Manifest;
import android.content.Intent;
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

import static com.example.fakenewsdetection.MainActivity.EXTRA_Description;
import static com.example.fakenewsdetection.MainActivity.EXTRA_URL;

public class Voting extends AppCompatActivity {

    private int STORAGE_PERMISSION_CODE = 1;
    private ImageButton addImage;
    private Button votingConfirm ;
    private static final int IMAGE_REQUEST = 1007;
    private Bitmap bitmap;
    private ImageView selectedImage;

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
        voteTypeTV = findViewById(R.id.tv_voting_type) ;
        voteTypeTV.setText(voteType);
        if (voteType.equals("Upvote!")){
            voteTypeTV.setTextColor(getResources().getColor(R.color.colorGreen));
        }
        else {
            voteTypeTV.setTextColor(getResources().getColor(R.color.colorRed));
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
                final String description = votingDescriptionEditText.getText().toString();
                //Checking if user entered text or Image
                if (description != null && bitmap != null ){

                    //if there is an image
                    if (bitmap != null) {
                        final String image = imageToString(bitmap);
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
                            }
                        });
                    }
                    // Adding the vote to the blockchain



                }
                else{
                    Toast.makeText(Voting.this, "Please Enter Text or Upload an image!!", Toast.LENGTH_SHORT).show();
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
}
