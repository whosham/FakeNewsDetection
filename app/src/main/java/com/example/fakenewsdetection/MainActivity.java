package com.example.fakenewsdetection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {


    //Variables for activity request in order to track the status for every activity individually and
    //Track it's exit status
    private static final int SIGNIN_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Test initial Commit
        Intent login = new Intent(MainActivity.this, activity_login.class);
        startActivityForResult(login, SIGNIN_REQUEST);
    }
}
