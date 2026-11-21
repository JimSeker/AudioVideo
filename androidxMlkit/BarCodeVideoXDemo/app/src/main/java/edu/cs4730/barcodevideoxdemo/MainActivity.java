package edu.cs4730.barcodevideoxdemo;

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
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import edu.cs4730.barcodevideoxdemo.databinding.ActivityMainBinding;


/**
 * This barcode example is very simple to the mlkit barcode example, except is video
 * the main thing is using the COORDINATE_SYSTEM_VIEW_REFERENCED  below, we don't need
 * any translation x or y.  since the coordinates are already the view coordinates.
 * So the code is much simpler and easier to follow in the BarcodeGraphic view.
 *
 */



public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<String[]> rpl;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    Executor executor;
    CameraSelector lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;

    //BarcodeScannerOptions options;
    BarcodeScanner scanner;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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
        //if we only want to recognize a few
        BarcodeScannerOptions options =new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(   //default is all formats.
                    // Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_ALL_FORMATS)
                // Barcode.FORMAT_UPC_A)
                .build();
        scanner = BarcodeScanning.getClient( options);
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
                Collections.singletonList(scanner),
                ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                executor,
                new Consumer<MlKitAnalyzer.Result>() {
                    @Override
                    public void accept(MlKitAnalyzer.Result result) {
                        List<Barcode> barcodes = result.getValue(scanner);
                        if (barcodes != null) {
                            binding.graphicOverlay.setBarCodes(barcodes);
                            binding.graphicOverlay.postInvalidate();
                        }

                    }
                }
            )
        );

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