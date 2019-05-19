package com.example.fakenewsdetection;

import android.Manifest;
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
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fakenewsdetection.Utilities.Hashing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.example.fakenewsdetection.MainActivity.MY_GLOBAL_PREFS;

public class addEvent extends AppCompatActivity {

    private int STORAGE_PERMISSION_CODE = 1;
    private ImageButton addImage;
    private static final int IMAGE_REQUEST = 1005;
    private Bitmap bitmap;
    private ImageView selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        //checking on Storage permission
        if (ContextCompat.checkSelfPermission(addEvent.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission aleady Granted", Toast.LENGTH_SHORT).show();

        } else {
            requestStoragePermissions();
        }

        //Adding an image
        addImage = findViewById(R.id.add_image) ;
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
        if (requestCode == STORAGE_PERMISSION_CODE ){
            if ( grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED ){
                Toast.makeText(this, "Permission successfully granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Permission wasn't granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void selectImage(){
        //TODO loading bar and updating
        Log.d("addEvent", "2 Inside Image selection done" ) ;
        Intent intent =  new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }

    private String  imageToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] imgBytes= byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes,Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {

            Log.d("AddImage", "3 Image selection done");
            Uri path = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                selectedImage =  findViewById(R.id.selected_image);
                selectedImage.setImageBitmap(bitmap);

                //Uploading the image to the server
                //Loading email from shared preferences
                SharedPreferences prefs = getSharedPreferences(MY_GLOBAL_PREFS, MODE_PRIVATE);
                final String username = prefs.getString(activity_login.EMAIL_KEY, "");

                Bundle extras = getIntent().getExtras();
                String latitude,longitude ;
                if (extras != null) {
                    latitude  = extras.getString("lat");
                    longitude = extras.getString("long");
                    String image = imageToString(bitmap) ;
                    Log.d("AddImage", "Lat/Long" + latitude + "/" + longitude);
                    Log.d("AddImage", "Image string" + image);
                    Log.d("AddImage", "Hashed image" + Hashing.hashPassword( image,Hashing.SALT));
                   //  Toast.makeText(this, "Lat/Long" + latitude + "/" + longitude+ ">>>>>" + image , Toast.LENGTH_SHORT).show();
                }



//                uploadImage(username, imageToString(bitmap), new VolleyCallback() {
//                    @Override
//                    public void onSuccess(String result) {
//                        //All good
//                        Log.d("profile", "Photo Uploaded to the server:" + result);
//                        ImageView imageView = (ImageView) findViewById(R.id.profile_image);
//                        imageView.setImageBitmap(bitmap);
//                        Log.d("profile", "Setting the image view");
//                        //Dismissing the progress bar
//                        progressBar.setVisibility(View.INVISIBLE);
//                        SnackBarMessage(R.string.profile_updated_successfully, getResources().getColor(R.color.colorGreen));
//                    }
//                });


            } catch (IOException e) {
                e.printStackTrace();
            }
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