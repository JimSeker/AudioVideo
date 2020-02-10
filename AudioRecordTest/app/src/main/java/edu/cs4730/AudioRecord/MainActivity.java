package edu.cs4730.AudioRecord;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Almost nothing interesting here.  It deals with the permission request for the file system and audio record access.
 */

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    private MainFragment myFrag;
    public static final int REQUEST_PERM_ACCESS = 1;

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
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v(TAG, "onRequest result called.");
        boolean file = false, mic = false;

        switch (requestCode) {
            case REQUEST_PERM_ACCESS:
                //received result for GPS access
                Log.v(TAG, "Received response for permissions request.");
                for (int i = 0; i < grantResults.length; i++) {
                    if ((permissions[i].compareTo(Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0) &&
                        (grantResults[i] == PackageManager.PERMISSION_GRANTED))
                        file = true;
                    else if ((permissions[i].compareTo(Manifest.permission.RECORD_AUDIO) == 0) &&
                        (grantResults[i] == PackageManager.PERMISSION_GRANTED))
                        mic = true;
                }
                if (mic && file) {
                    // permission was granted
                    Log.v(TAG, "Both permissions has now been granted. Starting Demo.");
                    myFrag.startRecording();
                } else {
                    // permission denied,    Disable this feature or close the app.
                    Log.v(TAG, "both permissions were NOT granted.");
                    Toast.makeText(this, "File and Mic access NOT granted", Toast.LENGTH_SHORT).show();
                    //clean up interface.
                    myFrag.recording = true;
                    myFrag.btn_record.setText("Start recording");
                }

                return;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
