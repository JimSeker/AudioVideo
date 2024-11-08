package edu.cs4730.cameraxdemo;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.cs4730.cameraxdemo.databinding.ActivityMainBinding;


/**
 * This is based off the CameraX code lab, except it's in java.  This works as is.
 * with help from https://www.journaldev.com/30132/android-camerax-overview which is alpha02 and doesn't work in 06.
 */

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private ImageCapture imageCapture;
    ActivityResultLauncher<String[]> rpl;
    private String[] REQUIRED_PERMISSIONS;
    //PreviewView viewFinder;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    ActivityMainBinding binding;

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
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {  //For API 29+ (q), for 26 to 28.
        REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
        // } else {
        //    REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //}
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


        binding.cameraCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            rpl.launch(REQUIRED_PERMISSIONS);
        }
    }

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);


        cameraProviderFuture.addListener(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                        Preview preview = (new Preview.Builder()).build();
                        preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

                        imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build();

                        CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();

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
                runOnUiThread(() -> {
                    Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                    DisplayPicFragment newDialog = DisplayPicFragment.newInstance(savedUri);
                    newDialog.show(getSupportFragmentManager(), "displayPic");
                });
                Log.d(TAG, msg);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage());
            }
        });


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

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public Bitmap loadImage(Uri mediaURI) {
        Bitmap bitmap = null;
        try {
            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), mediaURI));

            Log.e(TAG, "success loaded file.");
        } catch (IOException e) {
            Log.e(TAG, "failed to load file.");
            e.printStackTrace();
        }
        return bitmap;
    }
}
