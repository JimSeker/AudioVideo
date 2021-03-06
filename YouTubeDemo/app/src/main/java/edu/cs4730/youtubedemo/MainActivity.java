package edu.cs4730.youtubedemo;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

/**
 * Youtube demo
 * <p>
 * Helpful references:
 * https://developers.google.com/youtube/android/player/
 * as a note, this is currently use the 1.2.2 jar files which was the most recent as of Oct 30, 2018
 * <p>
 * a nice tutorial of how to implement it.
 * http://www.sitepoint.com/using-the-youtube-api-to-embed-video-in-an-android-app/
 * <p>
 * <p>
 * To get this example to work see the DeveloperKey.java file to change the API key.
 *
 *
 * This fails in API 30.  
 */


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //First method,  A basic example to get a youtube widget up.
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), BasicYTActivity.class));

            }
        });

        //Second method,  An advanced example with more control and feedback.
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AdvancedYTActivity.class));

            }
        });


    }


}
