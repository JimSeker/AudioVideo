package edu.cs4730.videocapture2_1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


public class HomeFragment extends Fragment {
    private final static String TAG = "HelpFragment";
    TextView logger;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_home, container, false);
        logger = myView.findViewById(R.id.loggerh);
        myView.findViewById(R.id.btn_perm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkpermissions();
            }
        });
        checkpermissions();
        return myView;
    }

    void checkpermissions() {
        //needs fine location for API 28+ or coarse location below 28 for the discovery only.
        if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            logthis("asking for permissions");
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                MainActivity.REQUEST_ACCESS);
            logthis("We don't have all 4 permissions ");
        } else {
            logthis("We have permission to read and write storage, camera, and the mic.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MainActivity.REQUEST_ACCESS) {
            for (int i = 0; i < permissions.length; i++)
                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    logthis("permission granted to write external storage");
                } else if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    logthis("permission granted to read external storage");
                } else if (permissions[i].equals(Manifest.permission.CAMERA)
                    && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    logthis("permission granted to camera");
                } else if (permissions[i].equals(Manifest.permission.RECORD_AUDIO)
                    && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    logthis("permission granted to the mic.");
                }
        }
    }

    public void logthis(String msg) {
        logger.append(msg + "\n");
        Log.d(TAG, msg);
    }
}