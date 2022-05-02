package edu.cs4730.AudioRecord;


import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Shows how to setup and record audio.
 * <p>
 * The permission request still calls back to MainActivity, so there is some code there to
 * deal with it, which calls methods here.
 */
public class MainFragment extends Fragment {

    String TAG = "MainFragment";
    private String[] REQUIRED_PERMISSIONS;
    ActivityResultLauncher<String[]> rpl;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    Uri mFileUri;
    TextView logger;
    Boolean recording, playing;
    Button btn_play, btn_record;

    public MainFragment() {
        recording = true;
        playing = true;
        //Use this to check permissions.
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    boolean granted = true;
                    for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {
                        logthis(x.getKey() + " is " + x.getValue());
                        if (!x.getValue()) granted = false;
                    }
                    if (granted) startRecording();
                }
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_main, container, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {  //For API 29+ (q), for 26 to 28.
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO};
        } else {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
        logger = myView.findViewById(R.id.logger);
        btn_play = myView.findViewById(R.id.btn_play);
        btn_play.setOnClickListener(mlistener);
        btn_record = myView.findViewById(R.id.btn_record);
        btn_record.setOnClickListener(mlistener);
        return myView;
    }

    //A simple method to append data to the logger textview.
    public void logthis(String msg) {
        logger.append(msg + "\n");
        Log.d(TAG, msg);
    }

    View.OnClickListener mlistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == btn_play) {
                if (playing) { // start playing the sound
                    btn_play.setText("Stop playing");
                    startPlaying();
                } else {
                    btn_play.setText("Start playing");
                    stopPlaying();
                }
                playing = !playing;
            } else if (v == btn_record) {
                if (recording) { // start recording
                    btn_record.setText("Stop recording");
                    startRecording();
                } else {
                    stopRecording();
                    btn_record.setText("Start recording");
                }
                recording = !recording;
            }
        }
    };


    /**
     * A very simple implementation of get the get the player,
     * set the data source, prepare and then start playing.
     * call stopPlaying to stop if before end audio.
     */
    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            logthis("start playing.");
            mPlayer.setDataSource(requireContext(), mFileUri);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            logthis("prepare() failed");
        }
    }

    /**
     * simple piece of code to stop and release the player.
     * if you want to continue it later, don't release, so audio play example.
     */
    private void stopPlaying() {
        logthis("stop playing.");
        mPlayer.stop();  //maybe not be needed, since just releasing anyway.
        mPlayer.release();
        mPlayer = null;
    }

    /**
     * start to record from the mic, in the mobile format (not mp3), 3gg format.  may need vlc to play on windows if you copy the file
     * over.
     * Note, you have to set a file name that it will start into, since you can't store just in memory.
     */

    public void startRecording() {
        if (!allPermissionsGranted()) {
            rpl.launch(REQUIRED_PERMISSIONS);
        } else {
            logthis("Start recording");
            mRecorder = new MediaRecorder();
            //Setting this to record the NOT local, ie the sd card directory for audio.
            //if set to true.  no write permissions are needed in any api version.
            mFileUri = getOutputMediaFile(MainActivity.MEDIA_TYPE_AUDIO, true);

            //this code is just to print the real filename, instead of the contentresolver number name.
            //logthis("file name is " + mFileUri.toString());
            String[] filePathColumn = {MediaStore.Audio.Media.DATA};
            Cursor cursor = requireActivity().getContentResolver().query(mFileUri, filePathColumn, null, null, null);
            String file;
            if (cursor != null) {  //sdcard
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                file = cursor.getString(columnIndex);
                cursor.close();
            } else { //local
                file = mFileUri.toString();
            }
            logthis("File name is " + file);

            //now open the file and set to write.
            try {
                ParcelFileDescriptor pfd = requireActivity().getContentResolver().openFileDescriptor(mFileUri, "w");
                mRecorder.setOutputFile(pfd.getFileDescriptor());
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            } catch (FileNotFoundException e) {
                logthis("openFileDescriptor failed");
            }

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                logthis("prepare() failed");
            }
            mRecorder.start();
        }
    }

    /**
     * stop the recording and clears up.
     * Note if you want to stop and then continue again, you would need to stop, but not release (and then only start() again).
     */
    private void stopRecording() {
        logthis("Stop recording");
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }


    /**
     * clean up and make sure I have released the player and recorder.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }


    /**
     * Create a File for saving an image or video
     */
    public Uri getOutputMediaFile(int type, boolean local) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        ContentValues values = new ContentValues();
        File mediaFile;
        File storageDir;
        Uri returnUri = null;

        if (type == MainActivity.MEDIA_TYPE_IMAGE) {

            if (local) {
                storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                mediaFile = new File(storageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
                returnUri = Uri.fromFile(mediaFile);

            } else { //onto the sdcard
                //values.put(MediaStore.Images.Media.TITLE, "IMG_" + timeStamp + ".jpg");  //not needed?
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + timeStamp + ".jpg");  //file name.
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                returnUri = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        } else if (type == MainActivity.MEDIA_TYPE_VIDEO) {
            if (local) {
                storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                mediaFile = new File(storageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
                returnUri = Uri.fromFile(mediaFile);
            } else {
                //values.put(MediaStore.Images.Media.TITLE, "VID_" + timeStamp + ".mp4");  //not needed?
                values.put(MediaStore.Video.Media.DISPLAY_NAME, "VID_" + timeStamp + ".mp4");  //file name.
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                returnUri = requireActivity().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            }
        } else if (type == MainActivity.MEDIA_TYPE_AUDIO) {
            if (local) {
                storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                mediaFile = new File(storageDir.getPath() + File.separator + "AUD_" + timeStamp + ".mp4");
                returnUri = Uri.fromFile(mediaFile);
            } else {
                values.put(MediaStore.Audio.Media.DISPLAY_NAME, "AUD_" + timeStamp + ".mp3");  //file name.
                values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3");
                returnUri = requireActivity().getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
            }
        }
        return returnUri;
    }

    /**
     * This a helper method to check for the permissions.
     */
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
