package edu.cs4730.audioplay_kt

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.cs4730.audioplay_kt.databinding.ActivityMainBinding

/**
 * A simple app to demo how to play/pause/restart playing an audio
 * file from different places.
 * note cleartext is turned on in the manifest files.
 */

class MainActivity : AppCompatActivity() {

    //private val AUDIO_PATH: String = "http://www.eecs.uwyo.edu/~seker/courses/4730/example/MEEPMEEP.WAV"
    //https://www.kozco.com/tech/soundtests.html
    private val AUDIO_PATH: String = "https://www.kozco.com/tech/piano2-CoolEdit.mp3"

    //"/sdcard/file.mp3"
    private var mediaPlayer: MediaPlayer? = null
    private var playbackPosition: Int = 0
    private var localfile: Boolean = false
    private val TAG: String = "MainActivity"
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(binding.main.id)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnPlayLocal.setOnClickListener { playAudioResource() }
        // play button, using remote file.
        binding.btnPlay.setOnClickListener { playAudio(AUDIO_PATH) }
        //pause the audio.
        binding.btnPause.setOnClickListener { pauseAudio() }
        //restart the audio.
        binding.btnRestart.setOnClickListener { restartAudio() }
    }

    /**
     * simple code to pause the playback and store the current position.
     */
    fun pauseAudio() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            playbackPosition = mediaPlayer!!.currentPosition
            mediaPlayer!!.pause()
            logthis("Media paused")
        }
    }

    /**
     * Load a file from the res/raw directory and play it.
     */
    fun playAudioResource() {
        if (mediaPlayer == null || !localfile) {
            mediaPlayer = MediaPlayer.create(this, R.raw.hmscream)
            localfile = true
        } else { //play it again sam
            mediaPlayer!!.seekTo(0)
        }
        if (mediaPlayer!!.isPlaying) {  //duh don't start it again.
            logthis("I'm playing already")
            return
        }
        //finally play!
        logthis("Started local")
        mediaPlayer!!.start()
    }

    /**
     * load a file from some url (including the internet) and play it.
     */
    fun playAudio(url: String) {
        if (mediaPlayer == null || localfile) { //first time or not the right file.
            localfile = false
            mediaPlayer = MediaPlayer()
            try {
                mediaPlayer!!.setDataSource(url)
                mediaPlayer!!.prepare()
            } catch (e: Exception) {
                logthis("Exception, not playing")
                e.printStackTrace()
                return
            }
        } else if (mediaPlayer!!.isPlaying) {  //duh don't start it again.
            logthis("I'm playing already")
            return
        } else {  //play it at least one, reset and play again.
            mediaPlayer!!.seekTo(0)
        }
        logthis("Started from internet/url")
        mediaPlayer!!.start()
    }

    /**
     * should be called after the pauseAudio(), but setup has position set to zero so should work
     * anyway.
     */
    fun restartAudio() {
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(playbackPosition)
            mediaPlayer!!.start()
            logthis("Media restarted")
        }
    }

    /**
     * make sure the player has been released.
     */
    fun KillMediaPlayer() {
        if (mediaPlayer != null) mediaPlayer!!.release()
        mediaPlayer = null
    }

    /**
     * Make sure we clean up and release the media player in on pause (and onDestroy too)
     */
    override fun onPause() {
        KillMediaPlayer()
        super.onPause()
    }

    override fun onDestroy() {
        KillMediaPlayer()
        super.onDestroy()
    }

    //A simple method to append data to the logger textview.
    fun logthis(msg: String) {
        binding.logger.append(msg + "\n")
        Log.d(TAG, msg)
    }
}