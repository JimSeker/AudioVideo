package edu.cs4730.piccapture2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * This example based on android's example on there pages
 * http://developer.android.com/guide/topics/media/camera.html
 * but it has been changed a whole lot, plus bug fixes from android... dumb ones too.
 *
 * The user presses a button in order to capture the picture and the surfaceview is a separate class.
 *
 * most of the code is in the Cam1Fragment and/or the surfaceView.  The code here is for the onPause
 * and onResume events, so we don't hold the camera while the app is paused.
 *
 *
 * Note this does not follow the new scoped permission rules and has a legacy tag in the manifest file.
 *
 */
public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener {

    public static final int REQUEST_PERM_ACCESS = 1;
    String TAG = "MainActivity";
    MainFragment myFrag;
    Cam1Fragment mf;


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

    @Override
    public void onFragmentInteraction(int which) {
        if (which == 1) {
            mf = new Cam1Fragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mf).addToBackStack(null).commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new Cam2Fragment()).addToBackStack(null).commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();     //call the super first, then our stuff.
        if (mf != null)
            mf.releaseCamera();   // release the camera immediately on pause event
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if (mf != null)
            mf.reinitCamera(); //reinitialize the camera if coming from an onPause event.
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v(TAG, "onRequest result called.");
        boolean file = false, cam = false;

        switch (requestCode) {
            case REQUEST_PERM_ACCESS:
                //received result for GPS access
                Log.v(TAG, "Received response for permissions request.");
                for (int i = 0; i < grantResults.length; i++) {
                    if ((permissions[i].compareTo(Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0) &&
                        (grantResults[i] == PackageManager.PERMISSION_GRANTED))
                        file = true;
                    else if ((permissions[i].compareTo(Manifest.permission.CAMERA) == 0) &&
                        (grantResults[i] == PackageManager.PERMISSION_GRANTED))
                        cam = true;
                }
                myFrag.setPerm(cam, file);
                return;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
