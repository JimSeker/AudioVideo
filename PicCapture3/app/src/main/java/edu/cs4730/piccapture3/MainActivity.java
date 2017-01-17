package edu.cs4730.piccapture3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/*
 * A simple example of using an intent to take a picture and display it on the screen
 * This example is based on  http://www.tutorialspoint.com/android/android_camera.htm
 * The code for the intent in the fragment.  Since it returns via OnActivityResult, the
 * code to caught the picture is in here and then the fragment is called to display the image.
*/

public class MainActivity extends AppCompatActivity {
    MainFragment mf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (mf == null) {
            mf = new MainFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mf).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //the picture is stored in the intent in the data key.
        //get the picture and show it in an the imagview.
        //Note the picture is not stored on the filesystem, so this is the only "copy" of the picture.
        Bundle extras = data.getExtras();
        if (extras != null) {
            //if you know for a fact there will be a bundle, you can use  data.getExtras().get("Data");  but we don't know.
            Bitmap bp = (Bitmap) extras.get("data");
            mf.setPic(bp);
        } else {
            Toast.makeText(this, "No picture was returned", Toast.LENGTH_SHORT).show();
        }
    }
}
