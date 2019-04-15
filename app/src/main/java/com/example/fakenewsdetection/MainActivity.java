package com.example.fakenewsdetection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //Referencing UI elements
    FloatingActionButton floatingActionButton;
    BottomAppBar bottomAppBar;



    //Variables for activity request in order to track the status for every activity individually and
    //Track it's exit status
    private static final int SIGNIN_REQUEST = 1001;
    //creating a global shared preferences
    public static final String MY_GLOBAL_PREFS = "my_global_prefs" ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Checking if user already logged in
        SharedPreferences prefs = getSharedPreferences(MainActivity.MY_GLOBAL_PREFS, MODE_PRIVATE);
        String email = prefs.getString(activity_login.EMAIL_KEY, "");
        //if user didn't authenticated before direct to login screen
        if (TextUtils.isEmpty(email)) {
            Intent login = new Intent(MainActivity.this, activity_login.class);
            startActivityForResult(login, SIGNIN_REQUEST);
        }


        floatingActionButton = findViewById(R.id.fab);
        bottomAppBar = findViewById(R.id.bottomAppbar);


        //for handling fab
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               //Adding New Story
            }
        });

        //Handling Options menu
        setSupportActionBar(bottomAppBar);



    }
    //for handling menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_app_bar,menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //checking the result from login from login activity
        if (resultCode == RESULT_OK && requestCode == SIGNIN_REQUEST) {
            Toast.makeText(MainActivity.this,"Welcome!", Toast.LENGTH_LONG).show();
        }



    }
}
