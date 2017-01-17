package edu.cs4730.AudioRecord;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

/**
 * Shows how to setup and record audio.
 *
 * The permission request still calls back to MainActivity, so there is some code there to
 * deal with it, which calls methods here.
 *
 */
public class MainFragment extends Fragment {

    String TAG = "MainFragment";
    private static String mFileName = null;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    Boolean recording, playing;
    Button btn_play, btn_record;

    public MainFragment() {
        recording = true;
        playing = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_main, container, false);

        //setup the filename to record to / play from.  Assumes /sdcard exists I think.
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

        btn_play = (Button) myView.findViewById(R.id.btn_play);
        btn_play.setOnClickListener(mlistener);
        btn_record = (Button) myView.findViewById(R.id.btn_record);
        btn_record.setOnClickListener(mlistener);
        return myView;
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


    /*
     * A very simple implementation of get the get the player,
     * set the data source, prepare and then start playing.
     *    call stopPlaying to stop if before end audio.
     */
    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    /*
     * simple piece of code to stop and release the player.
     *   if you want to continue it later, don't release, so audio play example.
     */
    private void stopPlaying() {
        mPlayer.stop();  //maybe not be needed, since just releasing anyway.
        mPlayer.release();
        mPlayer = null;
    }

    /*
     * start to record from the mic, in the mobile format (not mp3), 3gg format.  may need vlc to play on windows if you copy the file
     * over.
     *
     * Note, you have to set a file name that it will start into, since you can't store just in memory.
     *
     */

    public void startRecording() {
        if ((ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)) {
            //I'm on not explaining why, just asking for permission.
            Log.v(TAG, "asking for permissions");
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                    MainActivity.REQUEST_PERM_ACCESS);

        } else {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(mFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e(TAG, "prepare() failed");
            }
            mRecorder.start();
        }
    }

    /*
     * stop the recording and clears up.
     *   Note if you want to stop and then continue again, you would need to stop, but not release (and then only start() again).
     */
    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }


	/*
     * clean up and make sure I have released the player and recorder.
	 *
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onPause()
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

}
