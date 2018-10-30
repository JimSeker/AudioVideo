package edu.cs4730.AudioPlay;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * A simple app to demo how to play/pause/restart playing an audio
 * file from different places.  See the fragment for actually code.
 */

public class MainActivity extends AppCompatActivity {

    MainFragment myFrag;

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

    /**
     * Make sure we clean up and release the media player in on pause (and onDestroy too)
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onPause()
     */
    @Override
    protected void onPause() {
        myFrag.KillMediaPlayer();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        myFrag.KillMediaPlayer();
        super.onDestroy();
    }

}
