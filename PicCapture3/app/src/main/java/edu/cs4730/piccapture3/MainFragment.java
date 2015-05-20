package edu.cs4730.piccapture3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/*
 * A simple example of using an intent to take a picture and display it on the screen
 * This example is based on  http://www.tutorialspoint.com/android/android_camera.htm
 * The code to start the default camera app is here.  Since it returns via OnActivityResult, the
 * code to caught the picture is in the Activity and then the fragment is called to display the image.
*/
public class MainFragment extends Fragment {
    ImageView iv;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_main, container, false);
        iv = (ImageView) myView.findViewById(R.id.imageView1);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open();
            }
        });
        return myView;
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

    public void setPic(Bitmap bp) {
        //Note the picture is not stored on the filesystem, so this is the only "copy" of the picture.
        iv.setImageBitmap(bp);
        iv.invalidate();  //likely not needed, but just in case this will cause the imageview to redraw.
    }

}
