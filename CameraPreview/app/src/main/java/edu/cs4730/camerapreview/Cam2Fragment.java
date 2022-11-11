package edu.cs4730.camerapreview;

import android.content.ContentValues;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This fragment to take a picture.  It uses the Camera2X classes to do most of the work.
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


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_cam2, container, false);

        preview = myView.findViewById(R.id.camera2_preview);

        //we have to pass the camera id that we want to use to the surfaceview
        CameraManager manager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics cc = manager.getCameraCharacteristics(cameraId);
            //int[] map = cc.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
            //its 3 on a pixel and I can't find what that actually means....  and a pixel4a dies on array bounds error.
//            Log.e("CameraDepth value", "Value is " + map[CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT]);
            mPreview = new Camera2Preview(requireContext(), cameraId);
            preview.addView(mPreview);

        } catch (CameraAccessException e) {
            Log.v(TAG, "Failed to get a camera ID!");
            e.printStackTrace();
        }

        // Add a listener to the Capture button
        btn_takepicture = myView.findViewById(R.id.btn_takepicture);
        btn_takepicture.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCapture == null) // While I would like the declare this earlier, the camera is not setup yet, so wait until now.
                        mCapture = new Camera2CapturePic(requireContext(), mPreview);

                    // get an image from the camera
                    if (mCapture.reader != null) {  //I'm sure it's setup correctly if reader is not null.
                        mCapture.TakePicture(getOutputMediaFile(MEDIA_TYPE_IMAGE, false));
                    }

                }
            }
        );
        btn_takevideo = myView.findViewById(R.id.btn_takevideo);
        btn_takevideo.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mVideo == null) // While I would like the declare this earlier, the camara is not setup yet, so wait until now.
                        mVideo = new Camera2CaptureVid((AppCompatActivity) requireActivity(), mPreview);

                    if (!mIsRecordingVideo) {  //about to take a video
                        mIsRecordingVideo = true;
                        btn_takevideo.setText("Stop Recording");
                        mVideo.startRecordingVideo(getOutputMediaFile(MEDIA_TYPE_VIDEO, false));
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
    private Uri getOutputMediaFile(int type, boolean local) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        ContentValues values = new ContentValues();
        File mediaFile;
        File storageDir;
        Uri returnUri = null;

        if (type == MEDIA_TYPE_IMAGE) {

            if (local) {
                storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                mediaFile = new File(storageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
                returnUri = Uri.fromFile(mediaFile);

            } else { //onto the sdcard
                //values.put(MediaStore.Images.Media.TITLE, "IMG_" + timeStamp + ".jpg");  //not needed?
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + timeStamp + ".jpg");  //file name.
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                returnUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        } else if (type == MEDIA_TYPE_VIDEO) {
            if (local) {
                storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                mediaFile = new File(storageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
                returnUri = Uri.fromFile(mediaFile);
            } else {
                //values.put(MediaStore.Images.Media.TITLE, "VID_" + timeStamp + ".mp4");  //not needed?
                values.put(MediaStore.Video.Media.DISPLAY_NAME, "VID_" + timeStamp + ".mp4");  //file name.
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                returnUri = requireContext().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            }
        }
        return returnUri;
    }

}
