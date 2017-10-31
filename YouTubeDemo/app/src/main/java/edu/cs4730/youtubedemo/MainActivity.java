package edu.cs4730.youtubedemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/*
 * Youtube demo
 *
 * Helpful references:
 * https://developers.google.com/youtube/android/player/
 *   as a note, this is currently use the 1.2.2 jar files which was the most recent as of jan 17, 2017
 *
 * a nice tutorial of how to implement it.
 * http://www.sitepoint.com/using-the-youtube-api-to-embed-video-in-an-android-app/
 *
 *
 * To get this example to work see the DeveloperKey.java file to change the API key.
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

        //Second method,  An advanced example with more control and feedback.
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //dies.
            //   getSupportFragmentManager().beginTransaction().add(android.R.id.content, new MainFragment()).commit();

        }
    });

}


}
