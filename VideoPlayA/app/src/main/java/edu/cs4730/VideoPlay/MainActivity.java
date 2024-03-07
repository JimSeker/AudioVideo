package edu.cs4730.VideoPlay;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.MediaController;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Map;

import edu.cs4730.VideoPlay.databinding.MainActivityBinding;

/**
 * A very simple example to play a video (local and via the web).
 * The local example assumes the video is stored on the sdcard in the root directory.
 * <p>
 * Note, since using a website (not https), added allowcleartext flag in manifest file.
 */

public class MainActivity extends AppCompatActivity {
    MainActivityBinding binding;
    String file;
    private String[] REQUIRED_PERMISSIONS;
    ActivityResultLauncher<String[]> rpl;
    private final static String TAG = "MainActivity";
    Uri videoUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //It's not context, actually needs the activity, so it can display the controls to the screen.
        binding.videoView.setMediaController(new MediaController(this));
        //Setup where the file to play is

        //on the SDcard in the root directory.  You need permissions to read the external storage or media_access
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //api 33+
            REQUIRED_PERMISSIONS = new String[]{android.Manifest.permission.ACCESS_MEDIA_LOCATION, android.Manifest.permission.READ_MEDIA_VIDEO};
        } else {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        //it's commented out, since you also need to copy the file to the sdcard first.
        /*
        videoUri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/the-empire.3gp");
        logthis(Environment.getExternalStorageDirectory().getPath() );
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> isGranted) {
                        boolean granted = true;
                        for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {
                            logthis(x.getKey() + " is " + x.getValue());
                            granted = x.getValue();
                        }
                        if (granted) {
                            binding.videoView.setVideoURI(videoUri);
                            binding.videoView.start();
                        }
                    }
                }
        );
        if (!allPermissionsGranted())
            rpl.launch(REQUIRED_PERMISSIONS);
        else {
            logthis("All permissions have been granted already.");
            binding.videoView.setVideoURI(videoUri);
            binding.videoView.start();
        }
        */

        //via the web.  Note you need Internet permissions.
        //usesCleartextTraffic has been added to the manifest file.
        //local to me, bad quality, but good sound.
        file = "http://www.cs.uwyo.edu/~seker/courses/4730/example/the-empire.3gp";
        //no audio, good picture from https://standaloneinstaller.com/blog/big-list-of-sample-videos-for-testers-124.html
        file = "http://mirrors.standaloneinstaller.com/video-sample/jellyfish-25-mbps-hd-hevc.3gp";
        //about 3 minutes, sound and video. from https://sample-videos.com/
        //file = "https://sample-videos.com/video123/3gp/240/big_buck_bunny_240p_10mb.3gp";
        file = "https://sample-videos.com/video321/mp4/360/big_buck_bunny_360p_5mb.mp4";
        videoUri = Uri.parse(file);
        binding.videoView.setVideoURI(videoUri);

        //play the video
        binding.videoView.start();

    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void logthis(String msg) {
        Log.d(TAG, msg);
    }
}