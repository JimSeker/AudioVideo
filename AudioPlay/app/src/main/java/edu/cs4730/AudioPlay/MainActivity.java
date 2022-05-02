package edu.cs4730.AudioPlay;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

/**
 * A simple app to demo how to play/pause/restart playing an audio
 * file from different places.
 */

public class MainActivity extends AppCompatActivity {

    static final String AUDIO_PATH = "http://www.cs.uwyo.edu/~seker/courses/4730/example/MEEPMEEP.WAV";
    //"http://java.sun.com/products/java-media/mma/media/test-wav.wav";  //doesn't exist anymore.
    //"/sdcard/file.mp3"
    MediaPlayer mediaPlayer;
    int playbackPosition = 0;
    boolean localfile = false;
    TextView logger;
    static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logger = findViewById(R.id.logger);

        findViewById(R.id.btnPlayLocal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudioResource();
            }
        });
        // play button, using remote file.
        findViewById(R.id.btnPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudio(AUDIO_PATH);
            }
        });
        //pause the audio.
        findViewById(R.id.btnPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseAudio();
            }
        });
        //restart the audio.
        findViewById(R.id.btnRestart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartAudio();
            }
        });
    }

    /**
     * simple code to pause the playback and store the current position.
     */
    void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            playbackPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            logthis("Media paused");
        }
    }

    /**
     * Load a file from the res/raw directory and play it.
     */
    void playAudioResource() {
        if (mediaPlayer == null || !localfile) {
            mediaPlayer = MediaPlayer.create(this, R.raw.hmscream);
            localfile = true;
        } else { //play it again sam
            mediaPlayer.seekTo(0);
        }
        if (mediaPlayer.isPlaying()) {  //duh don't start it again.
           logthis("I'm playing already");
            return;
        }
        //finally play!
        logthis("Started local");
        mediaPlayer.start();
    }

    /**
     * load a file from some url (including the internet) and play it.
     */
    void playAudio(String url) {
        if (mediaPlayer == null || localfile) {//first time or not the right file.
            localfile = false;
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare();
            } catch (Exception e) {
                logthis("Exception, not playing");
                e.printStackTrace();
                return;
            }
        } else if (mediaPlayer.isPlaying()) {  //duh don't start it again.
            logthis( "I'm playing already");
            return;
        } else {  //play it at least one, reset and play again.
            mediaPlayer.seekTo(0);
        }
        logthis("Started from internet/url");
        mediaPlayer.start();

    }

    /**
     * should be called after the pauseAudio(), but setup has position set to zero so should work
     * anyway.
     */
    void restartAudio() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(playbackPosition);
            mediaPlayer.start();
            logthis("Media restarted");
        }
    }

    /**
     * make sure the player has been released.
     */
    void KillMediaPlayer() {
        if (mediaPlayer != null)
            mediaPlayer.release();
        mediaPlayer = null;
    }

    /**
     * Make sure we clean up and release the media player in on pause (and onDestroy too)
     */
    @Override
    protected void onPause() {
        KillMediaPlayer();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        KillMediaPlayer();
        super.onDestroy();
    }

    //A simple method to append data to the logger textview.
    public void logthis(String msg) {
        logger.append(msg + "\n");
        Log.d(TAG, msg);
    }


}
