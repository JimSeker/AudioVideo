package edu.cs4730.piccapture3;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple example of using an intent to take a picture and display it on the screen
 * This example is based on  http://www.tutorialspoint.com/android/android_camera.htm
 * The code for the intent in the fragment.  Since it returns via OnActivityResult, the
 * code to caught the picture is in here and then the fragment is called to display the image.
 * <p>
 * Note the picture is taken via an intent may not be stored.  So no permissions are listed or asked for
 * if withFile = true, then it will create a file, but everything is stored in the app directory, so no permission needed.
 */

public class MainActivity extends AppCompatActivity {
    ImageView iv;

    final static int CAPTURE_FILE = 2;
    final static int CAPTURE_NO_FILE = 1;
    boolean withFile = true;
   String imagefile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = findViewById(R.id.imageView1);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open();
            }
        });
    }


    /**
     * android.provider.MediaStore.ACTION_IMAGE_CAPTURE_SECURE
     * This is an interesting intent, but what does it really do?
     * does the screen have to be locked into to take a picture?
     * http://developer.android.com/reference/android/provider/MediaStore.html#ACTION_IMAGE_CAPTURE_SECURE
     * android.provider.MediaStore.ACTION_IMAGE_CAPTURE_SECURE
     * It returns the image captured from the camera , when the device is secured
     */
    public void open() {

        if (!withFile) {
            //create an intent to have the default camera app take a picture and return the picture, but no file.
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAPTURE_NO_FILE);
        } else {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File mediaFile = new File(storageDir.getPath() +File.separator + "IMG_" + timeStamp+ ".jpg");
            Uri photoURI = FileProvider.getUriForFile(this,
                    "edu.cs4730.piccapture3.fileprovider",
                    mediaFile);

            imagefile = mediaFile.getAbsolutePath();
            Log.wtf("File", imagefile);
           // Uri photoURI = getUriForFile(this, "edu.cs4730.piccapture3.fileprovider",mediaFile);
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(intent, CAPTURE_FILE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //the picture is stored in the intent in the data key.
        //get the picture and show it in an the imagview.

        if (requestCode == CAPTURE_NO_FILE) {
            Log.w("NOFILE", "onactivityresult no file");
            Bundle extras = data.getExtras();
            if (extras != null) {
                //if you know for a fact there will be a bundle, you can use  data.getExtras().get("Data");  but we don't know.
                Bitmap bp = (Bitmap) extras.get("data");

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    //rotate the image
                    Matrix matrix = new Matrix();
                    matrix.preRotate(90);
                    bp = Bitmap.createBitmap(bp, 0, 0, bp.getWidth(), bp.getHeight(), matrix, true);
                }

                //Note the picture is not stored on the filesystem, so this is the only "copy" of the picture.
                iv.setImageBitmap(bp);
                iv.invalidate();  //likely not needed, but just in case this will cause the imageview to redraw.
            } else {
                Toast.makeText(this, "No picture was returned", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAPTURE_FILE) {
            Log.wtf("CAPTURE FILE", "we got a file?");
            iv.setImageBitmap( loadAndRotateImage(imagefile));
        }
    }


    /**
     * loads and rotates a file as needed, based on the orientation found in the file
     */

    public Bitmap loadAndRotateImage(String path) {
        int rotate = 0;
        ExifInterface exif;

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
    }

}

