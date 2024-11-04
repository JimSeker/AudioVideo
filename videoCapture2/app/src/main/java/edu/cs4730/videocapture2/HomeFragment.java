package edu.cs4730.videocapture2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import edu.cs4730.videocapture2.databinding.FragmentHomeBinding;


public class HomeFragment extends Fragment {
    private final static String TAG = "HelpFragment";
   FragmentHomeBinding binding;
    private String[] REQUIRED_PERMISSIONS;
    ActivityResultLauncher<String[]> rpl;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_MEDIA_LOCATION, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //api 33+
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_MEDIA_LOCATION, Manifest.permission.READ_MEDIA_VIDEO};
        } else { // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {  //For API 29+ (q),
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_MEDIA_LOCATION};
        } //else {  //for 26 to 28.
        //    REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //}

       binding.btnPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rpl.launch(REQUIRED_PERMISSIONS);
            }
        });

        //this allows us to check in the fragment instead of doing it all in the activity.
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    boolean granted = true;
                    for (Map.Entry<String, Boolean> x : isGranted.entrySet())
                        logthis(x.getKey() + " is " + x.getValue());
                }
            }
        );
        if (!allPermissionsGranted())
            rpl.launch(REQUIRED_PERMISSIONS);
        else
            logthis("All permissions have been granted already.");

        return binding.getRoot();
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void logthis(String msg) {
        binding.loggerh.append(msg + "\n");
        Log.d(TAG, msg);
    }
}