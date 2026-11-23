package edu.cs4730.posevideoxdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.LifecycleCameraController;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.util.Consumer;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import edu.cs4730.posevideoxdemo.databinding.ActivityMainBinding;


/**
 * This is the pose detection without needed the extra pieces.  except it does completely work.
 * Not sure if there is an error mlkit for pose, it works perfect for barcode and face.  but
 * pose seems to be off, by a scale of 2.x for x and y direction.
 *
 * I used the graphic overlay from the Google api version with the same result.  assuming pose isn't
 * supported correctly yet.  not sure.
 */

public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<String[]> rpl;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    Executor executor;
    CameraSelector lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;
    ActivityMainBinding binding;
    String TAG = "MainActivity";
    PoseDetector poseDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
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
        PoseDetectorOptions options =
            new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
                .build();

        poseDetector = PoseDetection.getClient(options);
        lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
        executor = ContextCompat.getMainExecutor(this);


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
                Collections.singletonList(poseDetector),
                ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                executor,
                new Consumer<MlKitAnalyzer.Result>() {
                    @Override
                    public void accept(MlKitAnalyzer.Result result) {

                        Pose pose = result.getValue(poseDetector);
                        List<PoseLandmark> allPoseLandmarks = pose.getAllPoseLandmarks();
                        if (!allPoseLandmarks.isEmpty()) {
                            //logthis("We have landmarks\n");
                            binding.graphicOverlay.setPose(pose,lensFacing,true);
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

    public void logthis(String item) {
        Log.d(TAG, item);
    }
}