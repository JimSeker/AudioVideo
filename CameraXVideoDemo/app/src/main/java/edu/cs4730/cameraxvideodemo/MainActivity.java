package edu.cs4730.cameraxvideodemo;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {


    String TAG = "MainActivity";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private VideoCapture<Recorder> videoCapture;
    private Recording currentRecording;

    ActivityResultLauncher<String[]> rpl;
    private String[] REQUIRED_PERMISSIONS;
    PreviewView viewFinder;
    Button take_photo;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewFinder = findViewById(R.id.viewFinder);
        take_photo = findViewById(R.id.camera_capture_button);
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeVideo();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {  //For API 29+ (q), for 26 to 28.
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        } else {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
        }
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    if (allPermissionsGranted()) {
                        startCamera();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        );
        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            rpl.launch(REQUIRED_PERMISSIONS);
        }
    }

    @SuppressLint("RestrictedApi")
    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);


        cameraProviderFuture.addListener(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                        Preview preview = (new Preview.Builder()).build();
                        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                        Recorder recorder = new Recorder.Builder()
                            .setQualitySelector(QualitySelector.from(Quality.HIGHEST,
                                FallbackStrategy.higherQualityOrLowerThan(Quality.SD)))
                            .build();
                        videoCapture = VideoCapture.withOutput(recorder);
                        ImageCapture imageCatpure = new ImageCapture.Builder().build();
                        // Unbind use cases before rebinding
                        cameraProvider.unbindAll();

                        // Bind use cases to camera
                        cameraProvider.bindToLifecycle(
                            MainActivity.this, cameraSelector, preview, imageCatpure, videoCapture);


                    } catch (Exception e) {
                        Log.e(TAG, "Use case binding failed", e);
                    }
                }
            }, ContextCompat.getMainExecutor(this)
        );
    }


    @SuppressLint({"RestrictedApi", "MissingPermission"})
    public final void takeVideo() {

        if (videoCapture == null) return;
        if (executor == null) return;

        if (recording) {  //ie already started.
            currentRecording.stop();
            recording = false;
            take_photo.setText("Start Rec");
        } else {
            String name = "CameraX-" + (new SimpleDateFormat(FILENAME_FORMAT, Locale.US)).format(System.currentTimeMillis()) + ".mp4";
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            cv.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                cv.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");
            }

            MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions.Builder(
                getContentResolver(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(cv)
                .build();

            currentRecording = ((Recorder) videoCapture.getOutput())
                .prepareRecording(MainActivity.this, mediaStoreOutputOptions)
                .withAudioEnabled()
                .start(executor, new Consumer<VideoRecordEvent>() {
                    @Override
                    public void accept(VideoRecordEvent videoRecordEvent) {
                        if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                            Uri savedUri = ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                            //convert uri to useful name.
                            Cursor cursor = null;
                            String path = "";
                            try {
                                cursor = getContentResolver().query(savedUri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);
                                cursor.moveToFirst();
                                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                            } finally {
                                cursor.close();
                            }
                            Log.wtf(TAG, path);
                            if (path.equals("")) {
                                path = savedUri.toString();
                            }
                            String msg = "Video capture succeeded: " + path;
                            runOnUiThread(() -> Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show());
                            Log.d(TAG, msg);
                            currentRecording = null;
                        }
                    }
                });

            recording = true;
            take_photo.setText("Stop Rec");
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
}