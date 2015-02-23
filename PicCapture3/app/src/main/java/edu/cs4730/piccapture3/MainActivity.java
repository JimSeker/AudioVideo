package edu.cs4730.piccapture3;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.view.View.OnClickListener;

/*
 * A simple example of using an intent to take a picture and display it on the screen
 * This example is based on  http://www.tutorialspoint.com/android/android_camera.htm
*/

public class MainActivity extends Activity {
	ImageView iv;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	      iv = (ImageView)findViewById(R.id.imageView1);
	      iv.setOnClickListener(new OnClickListener() {
	         @Override
	         public void onClick(View v) {
	            open();
	         }
	      });
	   }
	   public void open(){
		   /*
		    * android.provider.MediaStore.ACTION_IMAGE_CAPTURE_SECURE
		    * This is an interesting intent, but what does it really do?  
		    *   does the screen have to be locked into to take a picture? 
		    *   http://developer.android.com/reference/android/provider/MediaStore.html#ACTION_IMAGE_CAPTURE_SECURE  
			* android.provider.MediaStore.ACTION_IMAGE_CAPTURE_SECURE
			*It returns the image captured from the camera , when the device is secured
			*/
		   
		   //create an intent to have the default camera app take a picture and return the picture.
	      Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	      startActivityForResult(intent, 0);
	   }

	   @Override
	   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	      super.onActivityResult(requestCode, resultCode, data);
	      //the picture is stored in the intent in the data key. 
	      //get the picture and show it in an the imagview.
	      Bitmap bp = (Bitmap) data.getExtras().get("data");
	      iv.setImageBitmap(bp);
	   }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
