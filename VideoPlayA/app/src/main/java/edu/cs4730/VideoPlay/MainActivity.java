package edu.cs4730.VideoPlay;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;


/*
 * A very simple example to play a video (local and via the web).
 * The local example assumes the video is stored on the sdcard in the root directory.
 */


public class MainActivity extends AppCompatActivity {

    VideoView vv;

  @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
      //Get the ViewView
      vv = (VideoView) findViewById( R.id.videoView);
      //add media controls to it.  //note, in a fragment, this dies... don't know why.  basecontext is wrong.  dies here too.
      vv.setMediaController(new MediaController(this));
      //Setup where the file to play is

      //on the SDcard in the root directory.  Not you need permissions to read the external storage.
      //Uri videoUri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/the-empire.3gp");
      //Log.v("path",Environment.getExternalStorageDirectory().getPath() );

      //via the web.  Note you need Internet permissions.
      Uri videoUri = Uri.parse("http://www.cs.uwyo.edu/~seker/courses/4730/example/the-empire.3gp");
      vv.setVideoURI(videoUri);
      //play the video
      vv.start();
    }
}