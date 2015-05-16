package edu.cs4730.AudioPlay;

import android.support.v4.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


/*
 * A simple app to demo how to play/pause/restart playing an audio
 * file from different places.   There is a fragment at the bottom of the code.
 */

public class MainActivity extends AppCompatActivity {

    static final String AUDIO_PATH = "http://www.cs.uwyo.edu/~seker/courses/4730/example/MEEPMEEP.WAV";
    //"http://java.sun.com/products/java-media/mma/media/test-wav.wav";  //doesn't exist anymore.
    //"/sdcard/file.mp3"
    MediaPlayer mediaPlayer;
    int playbackPosition = 0;
    boolean localfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment()).commit();
        }
    }

    /*
     *  Make sure we clean up and release the media player in on pause (and onDestory too)
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onPause()
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

    /*
     * simple code to pause the playback and store the current position.
     */
    void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            playbackPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        }
    }

	
	/*
	 * Load a file from the res/raw directory and play it.
	 */

    void playAudioResource() {
        if (mediaPlayer == null || !localfile) {
            mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.hmscream);
            localfile = true;
        } else { //play it again sam
            mediaPlayer.seekTo(0);
        }
        if (mediaPlayer.isPlaying()) {  //duh don't start it again.
            Toast.makeText(getBaseContext(), "I'm playing already", Toast.LENGTH_SHORT).show();
            return;
        }
        //finally play!
        mediaPlayer.start();
    }

    /*
     * load a file from somewhere (including the internet) and play it.
     */
    void playAudio(String url) {
        if (mediaPlayer == null || localfile) {//firsttime or not the right file.
            localfile = false;
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Exception, not playing",
                        Toast.LENGTH_SHORT).show();
                System.out.println("Player exception is " + e.getMessage());
                return;
            }
        } else if (mediaPlayer.isPlaying()) {  //duh don't start it again.
            Toast.makeText(getBaseContext(), "I'm playing already", Toast.LENGTH_SHORT).show();
            return;
        } else {  //play it at least one, reset and play again.
            mediaPlayer.seekTo(0);
        }
        mediaPlayer.start();

    }

    /*
     * should be called after the pauseAudio(), but setup has position set to zero so should work
     * anyway.
     */
    void restartAudio() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(playbackPosition);
            mediaPlayer.start();
        }

    }

    /*
     * make sure the player has been released.
     */
    void KillMediaPlayer() {
        if (mediaPlayer != null)
            mediaPlayer.release();
        mediaPlayer = null;
    }

    /**
     * this just setups the buttons, the content of the code is above to play
     * and pause/restart audio.
     */
    public class MainFragment extends Fragment {


        Button btnStartl, btnStart, btnPause, btnRestart;


        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View myView = inflater.inflate(R.layout.fragment_main, container, false);
            // play button, using local file.
            btnStartl = (Button) myView.findViewById(R.id.btnPlayLocal);
            btnStartl.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    playAudioResource();
                }
            });
            // play button, using remote file.
            btnStart = (Button) myView.findViewById(R.id.btnPlay);
            btnStart.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    playAudio(AUDIO_PATH);
                }
            });
            //pause the audio.
            btnPause = (Button) myView.findViewById(R.id.btnPause);
            btnPause.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    pauseAudio();
                }
            });
            //restart the audio.
            btnRestart = (Button) myView.findViewById(R.id.btnRestart);
            btnRestart.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    restartAudio();
                }
            });

            return myView;
        }
    }


}
