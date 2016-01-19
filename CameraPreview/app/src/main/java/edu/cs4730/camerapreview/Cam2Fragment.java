package edu.cs4730.camerapreview;


import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class Cam2Fragment extends Fragment {
    String TAG = "Cam2Fragment";
    static int MEDIA_TYPE_IMAGE = 1;
    static int MEDIA_TYPE_VIDEO = 2;


    Camera2Preview mPreview;
    FrameLayout preview;
    Button btn_takepicture, btn_takevideo;

    //for taking a picture.
    Camera2CapturePic mCapture;

    //for take a video
    Camera2CaptureVid mVideo;
    //Camera2CaptureVid2 mVideo;
    boolean mIsRecordingVideo = false;


    public Cam2Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_cam2, container, false);

        preview = (FrameLayout) myView.findViewById(R.id.camera2_preview);

        //we have to pass the camera id that we want to use to the surfaceview
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            mPreview = new Camera2Preview(getActivity().getApplicationContext(), cameraId);
            preview.addView(mPreview);

        } catch (CameraAccessException e) {
            Log.v(TAG, "Failed to get a camera ID!");
            e.printStackTrace();
        }


        // Add a listener to the Capture button
        btn_takepicture = (Button) myView.findViewById(R.id.btn_takepicture);
        btn_takepicture.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCapture == null) // While I would like the declare this earlier, the camara is not setup yet, so wait until now.
                            mCapture = new Camera2CapturePic(getActivity().getApplicationContext(), mPreview);

                        // get an image from the camera
                        if (mCapture.reader != null) {  //I'm sure it's setup correctly if reader is not null.
                            mCapture.TakePicture(getOutputMediaFile(MEDIA_TYPE_IMAGE));
                        }

                    }
                }
        );
        btn_takevideo = (Button) myView.findViewById(R.id.btn_takevideo);
        btn_takevideo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mVideo == null) // While I would like the declare this earlier, the camara is not setup yet, so wait until now.
                            mVideo = new Camera2CaptureVid((AppCompatActivity)getActivity(), mPreview);


                        if (mIsRecordingVideo == false) {  //about to take a video
                            mIsRecordingVideo = true;
                            btn_takevideo.setText("Stop Recording");
                            mVideo.startRecordingVideo(getOutputMediaFile(MEDIA_TYPE_VIDEO));
                        } else {
                            mVideo.stopRecordingVideo();
                            mIsRecordingVideo = false;
                            btn_takevideo.setText("Start Recording");
                        }


                    }
                }
        );
        //and add video when I know how to do it.
        return myView;
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        //creates a directory in pictures.
        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");

        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

}
