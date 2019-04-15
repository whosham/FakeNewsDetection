package com.example.fakenewsdetection;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.MenuItem;
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
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.bottom_item_logout:
                //Logout setting will delete all the shared preferences so the user must relogin
                Log.d("MainActivity", getString(R.string.Logout));
                //confirm if the user wanted to logout or not
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.Logout))
                        .setMessage(getString(R.string.logout_confirmation))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //User confirmed loging out
                                Toast.makeText(MainActivity.this, getString(R.string.Logout), Toast.LENGTH_SHORT).show();
                                //Delete all his shared preference.
                                SharedPreferences.Editor editor =
                                        getSharedPreferences(MY_GLOBAL_PREFS,MODE_PRIVATE).edit();
                                // Getting Email data from login activity
                                editor.remove(activity_login.EMAIL_KEY) ;
                                editor.apply();

                                // Restarting app in case of logout
                                Intent restart  = getBaseContext().getPackageManager()
                                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                                restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(restart);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                return true;

        }

        return super.onOptionsItemSelected(item);
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
