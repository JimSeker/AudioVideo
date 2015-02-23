package edu.cs4730.videocapture;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends Activity {
	int REQUEST_VIDEO_CAPTURE = 1;
	VideoView mVideoView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mVideoView = (VideoView)findViewById(R.id.videoView1);
		mVideoView.setMediaController(new MediaController(this));
		
		//setup the button take a video.
		Button btn1 = (Button)findViewById(R.id.button1);
        btn1.setOnClickListener( new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				//create an intent to have the default video record take a video.
			    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			    if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
			        startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
			    }

			}
        });
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
	        Uri videoUri = data.getData();
	        Log.v("return", "Video saved to: " + data.getData());

	        mVideoView.setVideoURI(videoUri);
	        mVideoView.start();
	        
	    }
	}
}
