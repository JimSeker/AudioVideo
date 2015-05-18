package edu.cs4730.VideoPlay;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;


/*
 * A very simple example to play a video (local and via the web).
 * The local example assumes the video is stored on the sdcard in the root directory.
 */

public class MainFragment extends Fragment {

    VideoView vv;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_main, container, false);
        //Get the ViewView
        vv = (VideoView) myView.findViewById( R.id.videoView);
        //add media controls to it.  //note, in a fragment, this dies... don't know why.
        // in both frag and activity these are are wrong: getApplicationContext() and getBaseContext()
        //It's not context, actually needs the activity, so it can display the controls to the screen.
        vv.setMediaController(new MediaController(getActivity()));
        //Setup where the file to play is

        //on the SDcard in the root directory.  Not you need permissions to read the external storage.
        //Uri videoUri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/the-empire.3gp");
        //Log.v("path",Environment.getExternalStorageDirectory().getPath() );

        //via the web.  Note you need Internet permissions.
        Uri videoUri = Uri.parse("http://www.cs.uwyo.edu/~seker/courses/4730/example/the-empire.3gp");
        vv.setVideoURI(videoUri);
        //play the video
        vv.start();
        return myView;
    }


}
