package edu.cs4730.cameraxdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;

import androidx.camera.core.UseCase;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
  This is based off the CameraX code lab, except it's in java.  This works as is.
  But change to alpha 07+ and it won't oompile.  There has been some series api breaking changes.

  with help from https://www.journaldev.com/30132/android-camerax-overview which is alpha02 and doesn't work in 06.
 */

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private ImageCapture imageCapture;
    private File outputDirectory;

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    PreviewView viewFinder;
    Button take_photo;
    ExecutorService executor = Executors.newSingleThreadExecutor();


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

    private void startCamera() {

        ListenableFuture cameraProviderFuture = ProcessCameraProvider.getInstance(this);


        cameraProviderFuture.addListener(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                        Preview preview = (new Preview.Builder()).build();
                        preview.setSurfaceProvider( viewFinder.getSurfaceProvider());

                        imageCapture = (new ImageCapture.Builder()).build();
                        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                        // Unbind use cases before rebinding
                        cameraProvider.unbindAll();

                        // Bind use cases to camera
                        cameraProvider.bindToLifecycle(
                            MainActivity.this, cameraSelector, preview, imageCapture);


                    } catch (Exception e) {
                        Log.e(TAG, "Use case binding failed", e);
                    }
                }
            }, ContextCompat.getMainExecutor(this)
        );
    }



    public final void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getOutputDirectory(),
            (new SimpleDateFormat(FILENAME_FORMAT, Locale.US)).format(System.currentTimeMillis()) + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();


        imageCapture.takePicture(outputOptions, executor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri savedUri = Uri.fromFile(photoFile);
                String msg = "Photo capture succeeded: " + savedUri;
                runOnUiThread(() -> Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show());
                 Log.d(TAG, msg);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: "+ exception.getMessage());
            }
        });


    }

    File getOutputDirectory() {
        File [] list = getExternalMediaDirs();
        File mediadir = null;
        if(list[0] != null) {
            mediadir = new File (list[0], getResources().getString(R.string.app_name));
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
