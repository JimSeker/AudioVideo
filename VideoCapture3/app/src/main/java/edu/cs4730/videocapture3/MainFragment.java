package edu.cs4730.videocapture3;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import java.io.IOException;

/**
 *  this is another simple example that records video.  Again, click the screen to record and
 *  again to stop recording.   It will then setup again, so you can record another one.
 *  the last file on the sdcard will not play, because it is empty, because of the way this setups.
 *
 */
public class MainFragment extends Fragment  implements View.OnClickListener, SurfaceHolder.Callback{

    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;
    int num = 0;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_main, container, false);
        recorder = new MediaRecorder();
        initRecorder();
        SurfaceView cameraView = (SurfaceView) myView.findViewById(R.id.cameraView);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);
        return myView;
    }


    private void initRecorder() {
        // recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);
        num++; //so it doesn't overwrite the previous file.
        recorder.setOutputFile(Environment.getExternalStorageDirectory().getPath()+"/videocapture_example"+num+".mp4");
        recorder.setMaxDuration(50000); // 50 seconds
        recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
    }

    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
        }  catch (IOException e) {
            e.printStackTrace();
            getActivity().finish();
        }
    }

    public void onClick(View v) {
        if (recording) {
            recorder.stop();
            recording = false;

            // Let's initRecorder so we can record again
            initRecorder();
            prepareRecorder();
        } else {
            recording = true;
            recorder.start();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        prepareRecorder();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        getActivity().finish();
    }
}
