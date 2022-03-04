package edu.cs4730.cameraxvideodemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {


    String TAG = "MainActivity";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private VideoCapture videoCapture;
    private File outputDirectory;

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};
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
                takePhoto();
            }
        });

        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @SuppressLint("RestrictedApi")
    private void startCamera() {

        ListenableFuture cameraProviderFuture = ProcessCameraProvider.getInstance(this);


        cameraProviderFuture.addListener(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                        Preview preview = (new Preview.Builder()).build();
                        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                        VideoCapture.Builder vc = new VideoCapture.Builder()
                            .setCameraSelector(cameraSelector)
                            .setTargetRotation(viewFinder.getDisplay().getRotation());

                        videoCapture = vc.build();


                        // Unbind use cases before rebinding
                        cameraProvider.unbindAll();

                        // Bind use cases to camera
                        cameraProvider.bindToLifecycle(
                            MainActivity.this, cameraSelector, preview, videoCapture);


                    } catch (Exception e) {
                        Log.e(TAG, "Use case binding failed", e);
                    }
                }
            }, ContextCompat.getMainExecutor(this)
        );
    }


    @SuppressLint({"RestrictedApi", "MissingPermission"})
    public final void takePhoto() {

        if (videoCapture == null) return;
        if (executor == null) return;

        if (recording) {  //ie already started.
            videoCapture.stopRecording();
            recording = false;
            take_photo.setText("Start Rec");
        } else {

            File photoFile = new File(getOutputDirectory(),
                (new SimpleDateFormat(FILENAME_FORMAT, Locale.US)).format(System.currentTimeMillis()) + ".mp4");
            VideoCapture.OutputFileOptions outputOptions = new VideoCapture.OutputFileOptions.Builder(photoFile).build();

            videoCapture.startRecording(outputOptions, executor, new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = Uri.fromFile(photoFile);
                        String msg = "Video capture succeeded: " + savedUri;
                        runOnUiThread(() -> Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show());
                        Log.d(TAG, msg);
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        Log.e(TAG, "Video capture failed: " + message);
                    }
                }

            );

            recording = true;
            take_photo.setText("Stop Rec");
        }

    }

    File getOutputDirectory() {
        File[] list = getExternalMediaDirs();
        File mediadir = null;
        if (list[0] != null) {
            mediadir = new File(list[0], getResources().getString(R.string.app_name));
            mediadir.mkdirs();
        }
        if (mediadir != null && mediadir.exists())
            return mediadir;
        else
            return getFilesDir();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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