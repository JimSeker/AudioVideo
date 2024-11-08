package edu.cs4730.videocapture1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.cs4730.videocapture1.databinding.ActivityMainBinding;

/**
 * This shows how to setup and record a video, while storing in the sdcard (easily switched to local)
 * <p>
 * This example does have a problem though.  It creates the file to store the data into before the record button is pressed.
 * And it assumes you will record another video, once you have stopped.  There is no good way to prevent a
 * corrupted final file.   Instead there should be a preveiew screen and when the user presses record, it should do
 * all this work and start the recording.  Then finally press stop and it falls back to a preview instead of a record_template.
 * Note, this should setup a preview screen first. when the record button is pushed, then changes to a record sessions
 * (which this example does by default.).  see VideoCapture2 for the preview plus, capture.
 * <p>
 * A final piece is needed, that starts the default player, so you can see what you recorded.  that is still missing.
 */

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private String[] REQUIRED_PERMISSIONS;
    final static String TAG = "MainActivity";
    boolean mIsRecordingVideo = false;
    static int MEDIA_TYPE_IMAGE = 1;
    static int MEDIA_TYPE_VIDEO = 2;
    static int MEDIA_TYPE_AUDIO = 3;

    ActivityMainBinding binding;
    public SurfaceHolder mHolder;

    String cameraId;
    public CameraDevice mCameraDevice;
    Uri mediaFile;

    private Size mVideoSize;
    CaptureRequest.Builder captureBuilder;
    private MediaRecorder mMediaRecorder;
    CameraCharacteristics characteristics;
    List<Surface> outputSurfaces;
    Handler backgroundHandler;
    CameraCaptureSession mSession;
    ActivityResultLauncher<String[]> rpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_MEDIA_LOCATION, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //api 33+
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_MEDIA_LOCATION, Manifest.permission.READ_MEDIA_VIDEO};
        } else  //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {  //For API 29+ (q),
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_MEDIA_LOCATION};
//        } else   //for 26 to 28.
//            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = binding.camera2Preview.getHolder();
        mHolder.addCallback(this);

        //Use this to check permissions.
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    if (allPermissionsGranted()) {
                        openCamera(); //start camera if permission has been granted by user
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        );


        binding.btnTakevideo.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mIsRecordingVideo) {  //about to take a video
                        mIsRecordingVideo = true;
                        binding.btnTakevideo.setText("Stop Recording");
                        startRecordingVideo();
                    } else {
                        stopRecordingVideo();
                        mIsRecordingVideo = false;
                        binding.btnTakevideo.setText("Start Recording");
                    }
                }
            }
        );

    }

    @Override
    public void onPause() {
        if (mIsRecordingVideo) {
            stopRecordingVideo();
            mIsRecordingVideo = false;
        }
        super.onPause();
    }

    /**
     * These two are helper methods, to start recording and stop.  could be done in the buttons themselves.
     */
    //start recording
    public void startRecordingVideo() {
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

        String[] filePathColumn = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(mediaFile, filePathColumn, null, null, null);
        String file;
        if (cursor != null) {  //sdcard
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            file = cursor.getString(columnIndex);
            cursor.close();
        } else { //local
            file = mediaFile.toString();
        }
        Toast.makeText(getApplicationContext(), "Saved:" + file, Toast.LENGTH_SHORT).show();

        //Log.v(TAG, "Video saved: " + getVideoFile(context));
        Log.v(TAG, "Video saved: " + file);
        //reset the preview screen.
        //startPreview();

        setup2Record();

    }

    /**
     * Actual methods to do the camera and video.
     */

//now setup the preview code
//setup the camera objects
    @SuppressLint("MissingPermission") //we are checking permissions, but lint can't tell.
    private void openCamera() {

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        Log.d(TAG, "openCamera Start");
        if (allPermissionsGranted()) {

            try {
                cameraId = manager.getCameraIdList()[0];

                // setup the camera perview.  should wrap this in a checkpermissions, which studio is bitching about
                // except it has been done before this fragment is called.
                manager.openCamera(cameraId, mStateCallback, null);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Asking for permissions, then  openCamera again!");
            rpl.launch(REQUIRED_PERMISSIONS);
        }
        Log.d(TAG, "openCamera End");
    }


    /*
     * all the listeners, callbacks that are needed here.
     */


    /*
      This is the callback necessary for the manager.openCamera Call back needed above.
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "onOpened");
            mCameraDevice = camera;
            setup2Record();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "onDisconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "onError from mStateCallback from opencamera listener.");
        }
    };

    ///////////////////////

    /// Now the methods to take a video
    /// /////////////////


    //everything to setup to record a video
    public void setup2Record() {
        if (mCameraDevice == null) {  //camera must be setup first!
            Log.e(TAG, "mCameraDevice is null!!");
            return;
        }

        mMediaRecorder = new MediaRecorder();

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        //setup for video recording.
        try {
            characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));

            try {
                setUpMediaRecorder();
            } catch (IOException e) {
                Log.e(TAG, "Failed to get a MediaRecorder setup. done now");
                e.printStackTrace();
                return;
            }
            try {
                captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to get a TEMPLAE_RECORD. done now");
                e.printStackTrace();
                return;
            }


            outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(mMediaRecorder.getSurface());

            outputSurfaces.add(mHolder.getSurface());

            HandlerThread thread = new HandlerThread("CameraVideo");
            thread.start();
            backgroundHandler = new Handler(thread.getLooper());


            captureBuilder.addTarget(mMediaRecorder.getSurface());
            captureBuilder.addTarget(mHolder.getSurface());

            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            mCameraDevice.createCaptureSession(outputSurfaces, mCaptureStateCallback, backgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Well something failed in setup record");
            e.printStackTrace();
        }
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
        //Log.d(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }


    private void setUpMediaRecorder() throws IOException {

        mediaFile = getOutputMediaFile(MainActivity.MEDIA_TYPE_VIDEO, false); //setup a new filename for every record.
        ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(mediaFile, "w");
        mMediaRecorder.setOutputFile(pfd.getFileDescriptor());

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        int deviceOrientation = getResources().getConfiguration().orientation;
        mMediaRecorder.setOrientationHint(getJpegOrientation(characteristics, deviceOrientation));
        mMediaRecorder.prepare();
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
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "onConfigureFailed");
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
        return (sensorOrientation + deviceOrientation + 360) % 360;

    }

    /**
     * These are the 3 required methods for a surfaceview holder.
     */

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "Surfaceview Created");
        openCamera();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "Surfaceview Changed");
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "Surfaceview Destroyed");
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }

    /**
     * Create a File for saving an image or video
     */
    public Uri getOutputMediaFile(int type, boolean local) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        ContentValues values = new ContentValues();
        File mediaFile;
        File storageDir;
        Uri returnUri = null;

        if (type == MainActivity.MEDIA_TYPE_IMAGE) {

            if (local) {
                storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                mediaFile = new File(storageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
                returnUri = Uri.fromFile(mediaFile);

            } else { //onto the sdcard
                //values.put(MediaStore.Images.Media.TITLE, "IMG_" + timeStamp + ".jpg");  //not needed?
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + timeStamp + ".jpg");  //file name.
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                returnUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        } else if (type == MainActivity.MEDIA_TYPE_VIDEO) {
            if (local) {
                storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                mediaFile = new File(storageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
                returnUri = Uri.fromFile(mediaFile);
            } else {
                //values.put(MediaStore.Images.Media.TITLE, "VID_" + timeStamp + ".mp4");  //not needed?
                values.put(MediaStore.Video.Media.DISPLAY_NAME, "VID_" + timeStamp + ".mp4");  //file name.
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                returnUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            }
        } else if (type == MainActivity.MEDIA_TYPE_AUDIO) {
            if (local) {
                storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                mediaFile = new File(storageDir.getPath() + File.separator + "AUD_" + timeStamp + ".mp4");
                returnUri = Uri.fromFile(mediaFile);
            } else {
                values.put(MediaStore.Audio.Media.DISPLAY_NAME, "AUD_" + timeStamp + ".mp3");  //file name.
                values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3");
                returnUri = getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
            }
        }
        return returnUri;
    }


    /**
     * This a helper method to check for the permissions.
     */


    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


}
