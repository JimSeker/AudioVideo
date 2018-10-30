package edu.cs4730.AudioPlay;


import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


/**
 * this is a simple demo to play an audio file from either the internet or from the local raw directory.
 * to play from the sdcard, this app would need read permissions for the filesystem.
 * <p>
 * Note usesCleartextTraffic="true" is the manifest, because of this downloads audio file.
 * Google... seriously....
 */

public class MainFragment extends Fragment {

    static final String AUDIO_PATH = "http://www.cs.uwyo.edu/~seker/courses/4730/example/MEEPMEEP.WAV";
    //"http://java.sun.com/products/java-media/mma/media/test-wav.wav";  //doesn't exist anymore.
    //"/sdcard/file.mp3"
    MediaPlayer mediaPlayer;
    int playbackPosition = 0;
    boolean localfile = false;


    Button btnStartl, btnStart, btnPause, btnRestart;


    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_main, container, false);
        // play button, using local file.
        btnStartl = myView.findViewById(R.id.btnPlayLocal);
        btnStartl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudioResource();
            }
        });
        // play button, using remote file.
        btnStart = myView.findViewById(R.id.btnPlay);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudio(AUDIO_PATH);
            }
        });
        //pause the audio.
        btnPause = myView.findViewById(R.id.btnPause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseAudio();
            }
        });
        //restart the audio.
        btnRestart = myView.findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartAudio();
            }
        });

        return myView;
    }


    /**
     * simple code to pause the playback and store the current position.
     */
    void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            playbackPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        }
    }


    /**
     * Load a file from the res/raw directory and play it.
     */
    void playAudioResource() {
        if (mediaPlayer == null || !localfile) {
            mediaPlayer = MediaPlayer.create(getContext(), R.raw.hmscream);
            localfile = true;
        } else { //play it again sam
            mediaPlayer.seekTo(0);
        }
        if (mediaPlayer.isPlaying()) {  //duh don't start it again.
            Toast.makeText(getContext(), "I'm playing already", Toast.LENGTH_SHORT).show();
            return;
        }
        //finally play!
        mediaPlayer.start();
    }

    /*
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
                Toast.makeText(getContext(), "Exception, not playing",
                    Toast.LENGTH_SHORT).show();
                System.out.println("Player exception is " + e.getMessage());
                return;
            }
        } else if (mediaPlayer.isPlaying()) {  //duh don't start it again.
            Toast.makeText(getContext(), "I'm playing already", Toast.LENGTH_SHORT).show();
            return;
        } else {  //play it at least one, reset and play again.
            mediaPlayer.seekTo(0);
        }
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

}
