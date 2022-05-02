package edu.cs4730.piccapture2;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * The user presses a button in order to capture the picture and the surfaceview is a separate class.
 * <p>
 * most of the code is in the CCamera2Preview for the actual camera.  This just calls a method to
 * take a picture and set the filename/Uri.
 */
public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<String[]> rpl;
    private String[] REQUIRED_PERMISSIONS;
    String TAG = "MainActivity";

    static int MEDIA_TYPE_IMAGE = 1;
    static int MEDIA_TYPE_VIDEO = 2;
    static int MEDIA_TYPE_AUDIO = 3;
    Camera2Preview mPreview;
    FrameLayout preview;

    Uri mediaFileUri;
    //for threading and communication,
    protected Handler handler;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {  //For API 29+ (q), for 26 to 28.
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
        } else {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    if (allPermissionsGranted()) {
                        startCamera(); //start camera if permission has been granted by user
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        );

        preview = findViewById(R.id.camera2_preview);

        // Button SDcard capture
        findViewById(R.id.buttonsd).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get an image from the camera
                    if (mPreview != null) {
                        mediaFileUri = getOutputMediaFile(MEDIA_TYPE_IMAGE, false); //to the sdcard.
                        mPreview.TakePicture(mediaFileUri);
                    }

                }
            }
        );
        // Button  local capture
        findViewById(R.id.buttonlocal).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get an image from the camera
                    if (mPreview != null) {
                        mediaFileUri = getOutputMediaFile(MEDIA_TYPE_IMAGE, true); //to the sdcard.
                        mPreview.TakePicture(mediaFileUri);
                    }

                }
            }
        );
        startCamera();
    }

    public void startCamera() {

        if (allPermissionsGranted()) {
            //we have to pass the camera id that we want to use to the surfaceview
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                String cameraId = manager.getCameraIdList()[0];
                mPreview = new Camera2Preview(getApplicationContext(), cameraId);
                mPreview.setOnPicListener(new Camera2Preview.OnPicCallback() {
                    @Override
                    public void onPic(Uri fileUri) {
                        Log.wtf("dialog", fileUri.toString());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //note this doesn't always work.  running this on a thread is a bad idea, but when I do
                                //all "correctly" on a thread and then launch, it doesn't work.  odd.
                                DisplayPicFragment newDialog = DisplayPicFragment.newInstance(fileUri);
                                newDialog.show(getSupportFragmentManager(), "displayPic");
                            }
                        }).start();
                    }
                });
                preview.addView(mPreview);
            } catch (CameraAccessException e) {
                Log.v(TAG, "Failed to get a camera ID!");
                e.printStackTrace();
            }
        } else {
            rpl.launch(REQUIRED_PERMISSIONS);
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


}
