package edu.cs4730.videocapture3;

import android.content.ContentValues;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * this is another simple example that records video.  Again, click the button to record and
 * again to stop recording.   It will then setup again, so you can record another one.
 * the last file on the sdcard will not play, because it is empty, because of the way this setups.
 */
public class CamFragment extends Fragment implements View.OnClickListener, SurfaceHolder.Callback {

    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;
    String TAG = "CamFrag";
    File VideoFile;
    Button btn_takevideo;

    public CamFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_cam, container, false);
        recorder = new MediaRecorder();
        initRecorder();
        SurfaceView cameraView = (SurfaceView) myView.findViewById(R.id.camera_preview);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //setup the button.
        btn_takevideo = (Button) myView.findViewById(R.id.btn1_takevideo);
        btn_takevideo.setOnClickListener(this);
        return myView;
    }


    private void initRecorder() {
        Log.d(TAG, "initRecorder");
        // recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);
        VideoFile = MainActivity.getOutputMediaFile(MainActivity.MEDIA_TYPE_VIDEO);
        Log.d(TAG, "File is " + VideoFile.toString());
        recorder.setOutputFile(VideoFile.toString());

        //if you wanted to limit the video size, you can use one of these.
        //     recorder.setMaxDuration(50000); // 50 seconds
        // recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
    }

    private void prepareRecorder() {
        Log.d(TAG, "prepareRecorder");
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            getActivity().finish();
        }
    }

    public void onClick(View v) {
        if (recording) {
            recorder.stop();
            recording = false;
            Log.d(TAG, "Recording Stop");
            btn_takevideo.setText("Start Recording");

            //so the media viewer (likely photos?) can find the file.
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.MediaColumns.DATA, VideoFile.toString());
            getContext().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            Toast.makeText(getContext(), "Video saved: " + VideoFile.toString(), Toast.LENGTH_SHORT).show();


            // Let's initRecorder so we can record again
            initRecorder();
            prepareRecorder();
        } else {
            recording = true;
            btn_takevideo.setText("Stop Recording");
            recorder.start();
            Log.d(TAG, "Recording Started");
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "Surfaceview Created");
        prepareRecorder();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "Surfaceview Changed");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "Surfaceview Destroyed");
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        getActivity().finish();
    }
}
