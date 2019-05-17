package com.example.fakenewsdetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class LocationRequest extends AppCompatActivity {


   private Button allowLocationAccess ;
   private int LOCATION_PERMISSION_CODE=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_request);

        //checking on Location permission
        if (ContextCompat.checkSelfPermission(LocationRequest.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setResult(RESULT_OK, getIntent());
            finish();
        }
        else {
            //Requesting Permissions
            requestLocationPermissions();
        }

        allowLocationAccess = findViewById(R.id.allow_location_access_button) ;
        allowLocationAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocationPermissions();
                if (ContextCompat.checkSelfPermission(LocationRequest.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED)
                {
                    setResult(RESULT_OK, getIntent());
                    finish();
                }
            }
        });
    }


    //To override the UP bar press and Back Press and make the same behavior
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
    }


    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(LocationRequest.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE){
            if (ContextCompat.checkSelfPermission(LocationRequest.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                setResult(RESULT_OK, getIntent());
                finish();
            }
        }
    }


}
