package edu.cs4730.videocapture;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;


/**
 * simple of example to use an intent to record video via the default video recorder.
 */
public class MainFragment extends Fragment {

    VideoView mVideoView;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_main, container, false);
        mVideoView = myView.findViewById(R.id.videoView1);
        mVideoView.setMediaController(new MediaController(getActivity()));

        //setup the button take a video.
        Button btn1 = myView.findViewById(R.id.button1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create an intent to have the default video record take a video.
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, MainActivity.REQUEST_VIDEO_CAPTURE);
                }
            }
        });

        return myView;
    }

    // method to allow the activity send the fragment the video and start it playing.
    void startVideo(Uri videoUri) {
        mVideoView.setVideoURI(videoUri);
        mVideoView.start();
    }

}
