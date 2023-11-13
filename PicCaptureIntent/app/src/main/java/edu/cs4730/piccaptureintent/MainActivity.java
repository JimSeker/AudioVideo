package edu.cs4730.piccaptureintent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
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
import java.util.Locale;

import edu.cs4730.piccaptureintent.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    //needed for when we spec the file, , but why?? inconsistent with video.
    String imagefile;
    Uri mediaURI;

    ActivityResultLauncher<Intent> ActivityResultNoPic, ActivityResultlocal, ActivityResultSD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityResultNoPic = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
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
                        binding.imageView.setImageBitmap(bp);
                        binding.imageView.invalidate();  //likely not needed, but just in case this will cause the imageview to redraw.
                    } else {
                        Toast.makeText(getApplicationContext(), "No picture was returned", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Request was canceled.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.buttonnofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create an intent to have the default camera app take a picture and return the picture, but no file.
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                ActivityResultNoPic.launch(intent);
            }
        });

        //for the local picture returns.
        ActivityResultlocal = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {
                    //Intent data = result.getData();  //since we provided a filename, it will come back null.  don't use.
                    // but with video, we do use it.  weird android... very weird and inconsistent.
                    binding.imageView.setImageBitmap(loadAndRotateImage(imagefile));
                } else {
                    Toast.makeText(getApplicationContext(), "Request was canceled.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //take a picture and have it stored in our local space.  with need providers so the camera can access our space.
        binding.buttonlocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File mediaFile = new File(storageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
                Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                    "edu.cs4730.piccaptureintent.fileprovider",
                    mediaFile);

                imagefile = mediaFile.getAbsolutePath();  //we need to store value to use on the return.
                Log.wtf("File", imagefile);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                ActivityResultlocal.launch(intent);
            }
        });

        //Setup to start the video in our local directories

        //only need one result return, since the file and path is return with the data.
        ActivityResultSD = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {
                    //Intent data = result.getData();  //zero.  useless, like local
                    Bitmap bitmap;
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            bitmap = ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(getContentResolver(), mediaURI)
                            );
                        } else {
                            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mediaURI));
                        }
                        binding.imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Request was canceled.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.buttonsd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "IMG_" + timeStamp + ".jpg");  //not needed?
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + timeStamp + ".jpg");  //file name.
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");

                mediaURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                imagefile = mediaURI.toString();
                Log.wtf("SDcard", imagefile);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaURI);
                ActivityResultSD.launch(intent);
            }
        });

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