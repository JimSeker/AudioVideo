package edu.cs4730.videocaptureintent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * These example show to call the default camera (video recorder) for the phone and store in
 * one of three places.
 * <p>
 * 1st is default, no filename provided, likely goes into the default directory, which is camera (at the time of testing).
 * 2nd is into the app directory, where you need a provider, so the camera can store the file in your app directory.
 * 3rd is on the sdcard in the "movie" directory.  it could be any of the external directories.
 * <p>
 * Note, these is a media file (video, audio, or pic), you don't need any permissions in the extra storage, since the media store do all this for you.
 */

public class MainActivity extends AppCompatActivity {

    VideoView mVideoView;
    ActivityResultLauncher<Intent> ActivityResultAll;
    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVideoView = findViewById(R.id.videoView);
        mVideoView.setMediaController(new MediaController(this));

        //only need one result return, since the file and path is return with the data.
        ActivityResultAll = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent data = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (data != null) {
                        Uri videoUri = data.getData();
                        Log.v("return", "Video saved to: " + data.getData());
                        mVideoView.setVideoURI(videoUri);
                        mVideoView.start();
                    } else {
                        Toast.makeText(getApplicationContext(), "No Data returned?!.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Request was canceled.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Setup video no no parameters, should go into the camera directory, I think.
        Button btnany = findViewById(R.id.buttonany);
        btnany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create an intent to have the default video record take a video.
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                //starting in API 30, it will only target native apps and you can't look for it either.  this is an explicit intent now.
                ActivityResultAll.launch(intent);
            }
        });

        //Setup to start the video in our local directories, /video I hope.
        findViewById(R.id.buttonlocal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                File mediaFile = new File(storageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
                Uri videoURI = FileProvider.getUriForFile(getApplicationContext(),
                    "edu.cs4730.videocaptureintent.fileprovider",
                    mediaFile);

                //so I can print out the location, otherwise, these 2 lines are not needed.
                String imagefile = mediaFile.getAbsolutePath();
                Log.wtf("File", imagefile);

                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                ActivityResultAll.launch(intent);
            }
        });

        //Setup to start the video in our local dictories, /video I hope.
        findViewById(R.id.buttonsd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.TITLE, "VID_" + timeStamp + ".mp4");  //not needed?
                values.put(MediaStore.Video.Media.DISPLAY_NAME, "VID_" + timeStamp + ".mp4");  //file name.
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");

                Uri mediaURI = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

                //these two lines are just print out the file name.  which is just the content id number.
                String imagefile = mediaURI.toString();
                Log.wtf("SDcard", imagefile);

                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaURI);
                ActivityResultAll.launch(intent);
            }
        });
    }

}