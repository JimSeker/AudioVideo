package edu.cs4730.AudioRecord;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Almost nothing interesting here.  It deals with the permission request for the file system and audio record access.
 */

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private MainFragment myFrag;
    static int MEDIA_TYPE_IMAGE = 1;
    static int MEDIA_TYPE_VIDEO = 2;
    static int MEDIA_TYPE_AUDIO = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            myFrag = new MainFragment();
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, myFrag).commit();
        }
    }


}
