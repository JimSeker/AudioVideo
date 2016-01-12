package edu.cs4730.PicCapture;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

//Nothing to see here, see the Cam1Fragment for all the camera code.

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener{
    public static final int REQUEST_PERM_ACCESS = 1;
    String TAG = "MainActivity";
    MainFragment myFrag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new Cam1Fragment()).addToBackStack(null).commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new Cam2Fragment()).addToBackStack(null).commit();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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