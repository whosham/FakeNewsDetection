package com.example.fakenewsdetection;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import static com.example.fakenewsdetection.MainActivity.EXTRA_Description;
import static com.example.fakenewsdetection.MainActivity.EXTRA_URL;

public class DetailedEvent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_event);

        Intent intent = getIntent() ;
        String imageUrl = intent.getStringExtra(EXTRA_URL) ;
        String description = intent.getStringExtra(EXTRA_Description) ;

        ImageView imageview = findViewById(R.id.iv_detail) ;
        TextView textView= findViewById(R.id.tv_description_detail) ;

        Glide.with(this).load(imageUrl).into(imageview);
        textView.setText(description);

        final ImageView upvote_button = findViewById(R.id.event_upvote_button_iv) ;
        ImageView downvote_button = findViewById(R.id.event_downvote_button_iv) ;

        upvote_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetailedEvent.this,"Upvoted!", Toast.LENGTH_LONG).show();
                upvote_button.setColorFilter(getResources().getColor(R.color.colorGreen)) ;
            }
        });

    }
}
