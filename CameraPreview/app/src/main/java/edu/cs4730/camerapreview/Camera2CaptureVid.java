package edu.cs4730.camerapreview;

import android.content.ContentValues;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This works great with API 23 and not well on 21.  Also, the rotation for landscape shows correctly
 * but when you use a player, it's sideways.
 * 
 * or video, currently look at https://github.com/googlesamples/android-Camera2Video
 */
public class Camera2CaptureVid {
    //where all the camrea info is located.
    Camera2Preview camera2Preview;
    Context context;
    AppCompatActivity activity;
    String TAG = "Camera2Video";
    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;
    CaptureRequest.Builder captureBuilder;
    private MediaRecorder mMediaRecorder;
    CameraCharacteristics characteristics;
    List<Surface> outputSurfaces;
    Handler backgroundHandler;
    File file;
    CameraCaptureSession mSession;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public Camera2CaptureVid(AppCompatActivity A, Camera2Preview camera2Preview) {
        this.activity = A;
        this.camera2Preview = camera2Preview;
        context = A.getApplicationContext();


    }

    public void setup() {
        if (camera2Preview.mCameraDevice == null) {  //camera must be setup first!
            Log.e(TAG, "mCameraDevice is null!!");
            return;
        }

        mMediaRecorder = new MediaRecorder();

        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        //setup for video recording.
        try {
            characteristics = manager.getCameraCharacteristics(camera2Preview.cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));

            try {
                setUpMediaRecorder();
            } catch (IOException e) {
                e.printStackTrace();
            }

            captureBuilder = camera2Preview.mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(mMediaRecorder.getSurface());

            outputSurfaces.add(camera2Preview.mHolder.getSurface());

            HandlerThread thread = new HandlerThread("CameraVideo");
            thread.start();
            backgroundHandler = new Handler(thread.getLooper());


            captureBuilder.addTarget(mMediaRecorder.getSurface());
            captureBuilder.addTarget(camera2Preview.mHolder.getSurface());

            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            camera2Preview.mCameraDevice.createCaptureSession(outputSurfaces, mCaptureStateCallback, backgroundHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    public void startRecordingVideo(File f) {
        file = f;
        try {
            camera2Preview.mPreviewSession.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        setup();


        //start recording
        mMediaRecorder.start();
    }

    public void stopRecordingVideo() {
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();

        try {
            mSession.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.MediaColumns.DATA, file.toString());
        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        Toast.makeText(context, "Video saved: " + file, Toast.LENGTH_SHORT).show();
        //Log.v(TAG, "Video saved: " + getVideoFile(context));
        Log.v(TAG, "Video saved: " + file);
        //reset the preview screen.
        camera2Preview.startPreview();
    }


    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        //Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }


    private void setUpMediaRecorder() throws IOException {

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int orientation = ORIENTATIONS.get(rotation);
        mMediaRecorder.setOrientationHint(orientation);

        int deviceorientation = context.getResources().getConfiguration().orientation;
        mMediaRecorder.setOrientationHint(getJpegOrientation(characteristics, deviceorientation));
        mMediaRecorder.prepare();
    }

    private File getVideoFile(Context context) {
        return new File(context.getExternalFilesDir(null), "video.mp4");
    }


    CameraCaptureSession.StateCallback mCaptureStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession session) {

            try {
                mSession = session;
                //null for capture listener, because mrecoder does the work here!
                session.setRepeatingRequest(captureBuilder.build(), null, backgroundHandler);

            } catch (CameraAccessException e) {

                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    //helper method so set the picture orientation correctly.  This doesn't set the header in jpeg
    // instead it just makes sure the picture is the same way as the phone is when it was taken.
    private int getJpegOrientation(CameraCharacteristics c, int deviceOrientation) {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN)
            return 0;
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;

        return jpegOrientation;
    }

}
