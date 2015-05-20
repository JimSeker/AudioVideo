package edu.cs4730.piccapture2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/*
 * This example based on android's example on there pages
 * http://developer.android.com/guide/topics/media/camera.html
 * but it has been changed a whole lot, plus bug fixes from android... dumb ones too.
 *
 * The user presses a button in order to capture the picture and the surfaceview is a seperate class.
 *
 * most of the code is in the MainFragment and/or the surfaceView.  The code here is for the onPause
 * and onResume events, so we don't hold the camera while the app is paused.
 *
 */
public class MainActivity extends AppCompatActivity {

    MainFragment mf;

    String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (mf == null) {
            mf = new MainFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mf).commit();
        }
    }


    @Override
    public void onPause() {
        super.onPause();     //call the super first, then our stuff.
        mf.releaseCamera();   // release the camera immediately on pause event
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        mf.reinitCamera(); //reinitialize the camera if coming from an onPause event.
    }

}
