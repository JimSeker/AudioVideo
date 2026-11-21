package edu.cs4730.facemeshvideoxdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.facemesh.FaceMesh;
import com.google.mlkit.vision.facemesh.FaceMeshDetection;
import com.google.mlkit.vision.facemesh.FaceMeshDetector;
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import edu.cs4730.facemeshvideoxdemo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    String TAG = "MainActivity";
    ActivityResultLauncher<String[]> rpl;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    Executor executor;
    CameraSelector lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;

    FaceMeshDetector detector;
    ActivityMainBinding binding;

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

        lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
        executor = ContextCompat.getMainExecutor(this);
        //FACE_MESH (default option): Provides a bounding box and additional face mesh info (468 3D points
        // and triangle info). When compared to the BOUNDING_BOX_ONLY use case, latency increases by ~15%,
        // as measured on Pixel 3.
        FaceMeshDetectorOptions.Builder optionsBuilder = new FaceMeshDetectorOptions.Builder();
        detector = FaceMeshDetection.getClient(optionsBuilder.build());

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
                Collections.singletonList(detector),
                ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                executor,
                new Consumer<MlKitAnalyzer.Result>() {
                    @Override
                    public void accept(MlKitAnalyzer.Result result) {
                        List<FaceMesh> faces = result.getValue(detector);
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