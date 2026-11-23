package edu.cs4730.mlkitfacevideoxdemo;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.view.LifecycleCameraController;
import androidx.core.content.ContextCompat;
import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.core.graphics.Insets;
import androidx.core.util.Consumer;
import androidx.camera.core.ImageAnalysis;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import edu.cs4730.mlkitfacevideoxdemo.databinding.ActivityMainBinding;


/**
 * This demos the camera ML Kit pieces. The analyzer provides coordinates in the view's
 * coordinate system; the overlay orders landmark coordinates independently of camera facing.
 */


public class MainActivity extends AppCompatActivity {


        String TAG = "MainActivity";
        ActivityResultLauncher<String[]> rpl;
        private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
        Executor executor;
        CameraSelector lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;

        FaceDetector faceDetector;
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

        lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
        executor = ContextCompat.getMainExecutor(this);
        //create the faceDetector
        //most accurate one
        FaceDetectorOptions highAccuracyOpts =
            new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)  //PERFORMANCE_MODE_FAST
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL) //eyes, ears, nose, cheeks, mouth, etc.
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .enableTracking()//smiling and eyes open
                .build();
        //Fastest one
        FaceDetectorOptions fastOpts =
            new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL) //eyes, ears, nose, cheeks, mouth, etc.
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)  //smiling and eyes open
                .enableTracking()
                .build();
        faceDetector = FaceDetection.getClient(fastOpts);

        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            rpl.launch(REQUIRED_PERMISSIONS);
        }

    }


    private void startCamera() {
        LifecycleCameraController cameraController = new LifecycleCameraController(this);
        cameraController.setImageAnalysisAnalyzer(
            executor,
            new MlKitAnalyzer(
                Collections.singletonList(faceDetector),
                ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                executor,
                new Consumer<MlKitAnalyzer.Result>() {
                    @Override
                    public void accept(MlKitAnalyzer.Result result) {
                        List<Face> faces = result.getValue(faceDetector);
                        if (faces != null) {
                            binding.graphicOverlay.setFaces(faces, lensFacing);
                        }
                        binding.graphicOverlay.postInvalidate();
                    }
                }
            )
        );

        binding.fab.setOnClickListener(v -> {
            if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
                lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
            } else {
                lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;
            }
            cameraController.setCameraSelector(lensFacing);
        });

        cameraController.setCameraSelector(lensFacing);
        cameraController.bindToLifecycle(this);
        binding.viewFinder.setController(cameraController);
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