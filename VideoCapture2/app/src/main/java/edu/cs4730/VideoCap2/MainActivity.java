package edu.cs4730.VideoCap2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;

import java.io.File;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    boolean recording = false;
    CaptureSurface cameraView = null;
    String outputFile = "/sdcard/videoexample.mp4";
    private static final String Tag = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        cameraView = (CaptureSurface) findViewById(R.id.CameraView);
        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (recording) {
            Log.d(Tag, "Calling stopRecording in CaptureSurface");
            cameraView.stopRecording();
            recording = false;
            Log.d(Tag, "finished, now calling native viewer");

            File videoFile = new File(outputFile);
            if (videoFile.exists()) {
                Uri fileUri = Uri.fromFile(videoFile);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, URLConnection.guessContentTypeFromName(fileUri.toString()));
                Log.d(Tag, "Native viewer should be playing, we are done.");
                startActivity(intent);
            } else {
                Log.d(Tag, "File not found!");
            }
            finish();
        } else {
            recording = true;
            Log.d(Tag, "Calling startRecording in CaptureSurface");
            cameraView.startRecording();
        }
    }

}