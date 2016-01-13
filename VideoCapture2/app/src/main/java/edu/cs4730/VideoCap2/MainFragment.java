package edu.cs4730.VideoCap2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.net.URLConnection;

/**
 * This is a simple example that records (touch the blank screen) a video and stores it on
 * the "sdcard".  It will then start the default video player and the app will exit.
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    boolean recording = false;
    CaptureSurface cameraView = null;
    //not supposed to put "/sdcard/", instead ask for where the sdcard is and use that.
    // may look something like this: /storage/emulated/0/videoexample.mp4
    String outputFile = Environment.getExternalStorageDirectory().getPath() + "/videoexample.mp4";
    private static final String Tag = "MainActivity";

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_main, container, false);
        Log.d(Tag, "Running?");
        cameraView = (CaptureSurface) myView.findViewById(R.id.CameraView);
        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);
        Log.d(Tag, "Running End");
        return myView;
    }

    @Override
    public void onClick(View v) {
        Log.d(Tag, "onClick called?");
        if (recording) {
            Log.d(Tag, "Calling stopRecording in CaptureSurface");
            cameraView.stopRecording();
            recording = false;
            Log.d(Tag, "finished, now calling native viewer");

            File videoFile = new File(outputFile);
            if (videoFile.exists()) {
                Uri fileUri = Uri.fromFile(videoFile);
                Log.d(Tag, "file: " + outputFile);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, URLConnection.guessContentTypeFromName(fileUri.toString()));
                Log.d(Tag, "Native viewer should be playing, we are done.");
                startActivity(intent);
            } else {
                Log.d(Tag, "File not found!");
            }
            getActivity().finish();
        } else {
            recording = true;
            Log.d(Tag, "Calling startRecording in CaptureSurface");
            cameraView.startRecording();
        }
    }


}
