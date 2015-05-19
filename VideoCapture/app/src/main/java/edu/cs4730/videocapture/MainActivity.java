package edu.cs4730.videocapture;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

// The main code is in the fragment.  But the fragment calls the default video recorder
// and the response comes back her and then is sent to be fragment to play the recorded video

public class MainActivity extends AppCompatActivity {

    final static int REQUEST_VIDEO_CAPTURE = 1;
    MainFragment mf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mf = new MainFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mf).commit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            Log.v("return", "Video saved to: " + data.getData());
            mf.startVideo(videoUri);
        } else {
            Log.v("return", "video failed?");
        }
    }
}
