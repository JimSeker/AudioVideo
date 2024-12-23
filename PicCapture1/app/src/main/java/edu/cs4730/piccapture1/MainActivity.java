package edu.cs4730.piccapture1;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.exifinterface.media.ExifInterface;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.cs4730.piccapture1.databinding.ActivityMainBinding;

/**
 * using the camera2 (api 21+) methods to capture a picture and store in either the sdcard or local to the app.
 * This example just takes the picture. then calls a dialog to display the picture.
 * the logcat and toast show the files has been stored.
 * <p>
 * honesty, the new androidX cameraX methods are simpler.  and should be used, unless you are doing something more specific.
 * <p>
 * android 15 has broken/changed the way  the writer works?  the file doesn't exist when it is supposed
 * to so the example was dieing on a file not found.   it seems the file write is now delayed
 * so a new button was added and android15 check is decides if the displayfragment is called automatically.
 */

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private String[] REQUIRED_PERMISSIONS;
    ActivityResultLauncher<String[]> rpl;
    static int MEDIA_TYPE_IMAGE = 1;
    static int MEDIA_TYPE_VIDEO = 2;
    static int MEDIA_TYPE_AUDIO = 3;

    ActivityMainBinding binding;
    SurfaceHolder surfaceHolder;
    String TAG = "MainActivity";

    //used for the camera
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    //needed for take picture
    private Size[] jpegSizes;
    int width = 640;
    int height = 480;
    CameraCharacteristics characteristics;
    ImageReader reader;
    Handler backgroudHandler;
    CaptureRequest.Builder captureBuilder;
    List<Surface> outputSurfaces;
    //File file;
    Uri imageFileUri;

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
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  //For API 33+
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
        } else //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {  //For API 29+ (q) to 32,
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
        //} else  // for 26 to 28.
        // REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> isGranted) {
                if (allPermissionsGranted()) {
                    openCamera(); //start camera if permission has been granted by user
                } else {
                    Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        //setup the preview for the camera.
        surfaceHolder = binding.CameraView2.getHolder();
        surfaceHolder.addCallback(this);

        binding.buttonlocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create a file in the local app pictures directory.
                imageFileUri = getOutputMediaFile(MEDIA_TYPE_IMAGE, true);
                try {
                    mCameraDevice.createCaptureSession(outputSurfaces, mCaptureStateCallback, backgroudHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });


        binding.buttonsd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //this get a unique file name with .jpg in the media pictures directory.
                imageFileUri = getOutputMediaFile(MEDIA_TYPE_IMAGE, false);
                try {
                    mCameraDevice.createCaptureSession(outputSurfaces, mCaptureStateCallback, backgroudHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        binding.display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayPicFragment newDialog = DisplayPicFragment.newInstance(imageFileUri);
                newDialog.show(getSupportFragmentManager(), "displayPic");
            }
        });
    }

    /**
     * Now these open and start up the camera.   with all the callbacks as well.
     */
    @SuppressLint("MissingPermission")
    private void openCamera() {

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "openCamera E");
        try {
            String cameraId = manager.getCameraIdList()[0];  //assuming back.

            //this line is what permission check in studio is bitching about, even though I have done permission checking and this method.
            manager.openCamera(cameraId, mStateCallback, null);

            //setup for taking the picture here, so we only do it once, instead at "take picture" time.
            characteristics = manager.getCameraCharacteristics(cameraId);
            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

            //setup the width and height size, assuming camera knows it or use default 640x480.
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());


            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            backgroudHandler = new Handler(thread.getLooper());
            reader.setOnImageAvailableListener(readerListener, backgroudHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }


    /*
  This is the callback necessary for the manager.openCamera Call back needed above.
 */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {

            Log.e(TAG, "onOpened");
            mCameraDevice = camera;
            //setup the capture of the current surface.
            startPreview();

            //is the setup to take the picture, now the mCameraDevice is initialized.
            //configure the catureBuilder, which is built in listener later on.
            try {
                captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            //int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            int deviceorientation = getResources().getConfiguration().orientation;
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(characteristics, deviceorientation));

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

            Log.e(TAG, "onDisconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

            Log.e(TAG, "onError");
        }

    };

    private int getJpegOrientation(CameraCharacteristics c, int deviceOrientation) {
        if (deviceOrientation == OrientationEventListener.ORIENTATION_UNKNOWN)
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


    protected void startPreview() {

        if (null == mCameraDevice) {
            Log.e(TAG, "startPreview fail, return");
            return;
        }

        //get the surface, so I can added to varing places...
        Surface surface = surfaceHolder.getSurface();

        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {

            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {

                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                    Toast.makeText(getApplicationContext(), "onConfigureFailed", Toast.LENGTH_LONG).show();
                }
            }, null);
        } catch (CameraAccessException e) {

            e.printStackTrace();
        }
    }

    protected void updatePreview() {

        if (null == mCameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }

        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        Handler backgroundHandler = new Handler(thread.getLooper());

        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {

            e.printStackTrace();
        }
    }

    //setup for actually taking the picture.

    ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {

            try (Image image = reader.acquireLatestImage()) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                save(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void save(byte[] bytes) throws IOException {
            try (OutputStream output = getContentResolver().openOutputStream(imageFileUri)) {
                output.write(bytes);

            }
        }

    };

    CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {

            super.onCaptureCompleted(session, request, result);
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(imageFileUri, filePathColumn, null, null, null);
            String file;
            if (cursor != null) {  //sdcard
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                file = cursor.getString(columnIndex);
                cursor.close();
            } else { //local
                file = imageFileUri.toString();
            }
            Toast.makeText(getApplicationContext(), "Saved:" + file, Toast.LENGTH_SHORT).show();
            Log.v(TAG, "Saved:" + file);
            //android 15 seems to have delayed the file writes and this fails.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DisplayPicFragment newDialog = DisplayPicFragment.newInstance(imageFileUri);
                        newDialog.show(getSupportFragmentManager(), "displayPic");

                    }
                });
            startPreview();
        }
    };

    CameraCaptureSession.StateCallback mCaptureStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {

            try {
                session.capture(captureBuilder.build(), captureListener, backgroudHandler);
            } catch (CameraAccessException e) {

                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.v(TAG, "OnConfigureFailed");
        }
    };

    /**
     * These are the 3 required methods for a surfaceview holder.
     */
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        Log.e(TAG, "Surfaceview Created");
        if (allPermissionsGranted()) {
            openCamera(); //start camera if permission has been granted by user
        } else {
            rpl.launch(REQUIRED_PERMISSIONS);
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //ignoring screen rotates.
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
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
// test code, to see if android 15 or my file save was broken.  it's 15
//                File[] list  = getExternalMediaDirs();
//                if (list[0] != null) {
//                    storageDir = list[0] ;
//                } else {
//                    storageDir = getFilesDir();
//                }
                storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                mediaFile = new File(storageDir, File.separator + "IMG_" + timeStamp + ".jpg");
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
}