package edu.cs4730.youtubedemo;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

/*
 * This is the simplist amount of code to launch a youtube video in your app.
 *
 * note: This activity is set to landscape in the manifest file.
 */

public class BasicYTActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {
    private static final int RECOVERY_REQUEST = 1;
    private YouTubePlayerView youTubeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_yt);
        //get the widget and then initialize with our api key.  See the developerkey.java to set your key.
        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubeView.initialize(DeveloperKey.DEVELOPER_KEY, this);
    }

    //two necessary methods for the OnInitializedListener

    //once we have scuessfully initialzied the youtube widget, now we can play the video.
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            player.cueVideo("fhWaJi1Hsfo"); // Plays https://www.youtube.com/watch?v=fhWaJi1Hsfo
        }
    }

    //We have failed to initialized the widget, if recoverable, then there will be a dialog box
    //for the user to fix the problem, else if just fails.
    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorResult) {
        if (errorResult.isUserRecoverableError()) {
            errorResult.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format(getString(R.string.player_error), errorResult.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

}
