package edu.cs4730.VideoPlay;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * A very simple example to play a video (local and via the web).
 * The local example assumes the video is stored on the sdcard in the root directory.
 *
 * Note, since using a website (not https), added allowcleartext flag in manifest file.
 *
 */

public class MainActivity extends AppCompatActivity {
    VideoView vv;
    String file;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        vv = findViewById(R.id.videoView);
        //add media controls to it.  //note, in a fragment, this dies... don't know why.
        // in both frag and activity these are are wrong: getApplicationContext() and getBaseContext()
        //It's not context, actually needs the activity, so it can display the controls to the screen.
        vv.setMediaController(new MediaController(this));
        //Setup where the file to play is

        //on the SDcard in the root directory.  Now you need permissions to read the external storage.
        //Uri videoUri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/the-empire.3gp");
        //Log.v("path",Environment.getExternalStorageDirectory().getPath() );

        //via the web.  Note you need Internet permissions.
        //usesCleartextTraffic has been added to the manifest file.
        //local to me, bad quality, but good sound.
        file = "http://www.cs.uwyo.edu/~seker/courses/4730/example/the-empire.3gp";
        //no audio, good picture from https://standaloneinstaller.com/blog/big-list-of-sample-videos-for-testers-124.html
        file = "http://mirrors.standaloneinstaller.com/video-sample/jellyfish-25-mbps-hd-hevc.3gp";
        //about 3 minutes, sound and video. from https://sample-videos.com/
        file = "https://sample-videos.com/video123/3gp/240/big_buck_bunny_240p_10mb.3gp";
        Uri videoUri = Uri.parse(file);
        vv.setVideoURI(videoUri);
        //play the video
        vv.start();


    }
}