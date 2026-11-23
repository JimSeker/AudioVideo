package edu.cs4730.imagelabelingvideox;

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
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import edu.cs4730.imagelabelingvideox.databinding.ActivityMainBinding;


/**
 * This is the Image Labling using cameraX and the MlKitAnalyzer from camerax
 * works pretty well for the image labeling.  code is pretty simple
 * get the labeler and process and display the text and confidence level on the screen.
 */

public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<String[]> rpl;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    Executor executor;
    CameraSelector lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;
    ActivityMainBinding binding;
    String TAG = "MainActivity";
    ImageLabeler labeler;

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

        // To use default options:
        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        // Or, to set the minimum confidence required:
//        ImageLabelerOptions options =
//            new ImageLabelerOptions.Builder()
//                .setConfidenceThreshold(0.7f)
//                .build();
//        labeler = ImageLabeling.getClient(options);
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
                        Collections.singletonList(labeler),
                        ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                        executor,
                        new Consumer<MlKitAnalyzer.Result>() {
                            @Override
                            public void accept(MlKitAnalyzer.Result result) {
                                List<ImageLabel> labels = result.getValue(labeler);
                                binding.Logger.setText("");
                                for (ImageLabel label : labels) {
                                    String text = label.getText();
                                    float confidence = label.getConfidence();
                                    int index = label.getIndex();
                                    text += " " + confidence + " " + index;
                                    binding.Logger.append(text + "\n");
                                    logthis(text);
                                }
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