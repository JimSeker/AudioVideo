package edu.cs4730.camerapreview;


import android.content.Context;
import android.database.Cursor;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import androidx.exifinterface.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * This class is designed to only have the methods needed to capture a picture
 * The camera2Preview class deals with surface.
 */
public class Camera2CapturePic {
    //where all the camrea info is located.
    Camera2Preview camera2Preview;
    Context context;
    String TAG = "Camera2Capture";

    //variables needed to take a picture

    //needed for take picture
    private Size[] jpegSizes;
    int width = 640;
    int height = 480;
    CameraCharacteristics characteristics;
    ImageReader reader;
    Handler backgroudHandler;
    CaptureRequest.Builder captureBuilder;
    List<Surface> outputSurfaces;
    Uri mediaFileUri;
    int deviceorientation = ORIENTATION_PORTRAIT;


    public Camera2CapturePic(Context context, Camera2Preview camera2Preview) {
        this.camera2Preview = camera2Preview;
        this.context = context;

        if (camera2Preview.mCameraDevice == null) {  //camera must be setup first!
            Log.e(TAG, "mCameraDevice is null!!");
            return;
        }

        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        //setup for taking the picture here, so we only do it once, instead at "take picture" time.
        try {
            characteristics = manager.getCameraCharacteristics(camera2Preview.cameraId);
            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());


            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            backgroudHandler = new Handler(thread.getLooper());
            reader.setOnImageAvailableListener(readerListener, backgroudHandler);

            //is the setup to take the picture, now the mCameraDevice is initialized.
            //configure the catureBuilder, which is built in listener later on.
            captureBuilder = camera2Preview.mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            //int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            deviceorientation = context.getResources().getConfiguration().orientation;
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(characteristics, deviceorientation));


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    /**
     * This is the one to call to take a picture.
     */
    public void TakePicture(Uri fileUri) {
        mediaFileUri = fileUri;
        try {
            camera2Preview.mCameraDevice.createCaptureSession(outputSurfaces, mCaptureStateCallback, backgroudHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //helper method so set the picture orientation correctly.  This doesn't set the header in jpeg
    // instead it just makes sure the picture is the same way as the phone is when it was taken.
    private int getJpegOrientation(CameraCharacteristics c, int deviceOrientation) {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN)
            return 0;
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;

        return jpegOrientation;
    }

    //setup for actually taking the picture.

    ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {

            Image image = null;
            try {
                image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                save(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (image != null) {
                    image.close();
                }
            }
        }

        private void save(byte[] bytes) throws IOException {
            OutputStream output = null;
            try {
                output = context.getContentResolver().openOutputStream(mediaFileUri);
                output.write(bytes);
                //orientation, I don't think this code is working anymore.
                if (deviceorientation == ORIENTATION_LANDSCAPE) {
                    ExifInterface exifInterface = new ExifInterface(mediaFileUri.getPath());
                    exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION,
                        String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
                    exifInterface.saveAttributes();
                }
            } finally {
                if (null != output) {
                    output.close();
                }
            }
        }

    };

    CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session,
                                       CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //this line is for images only, because this is for picture, not video.  the main code may think both though.
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(mediaFileUri, filePathColumn, null, null, null);
            String file;
            if (cursor != null) {  //sdcard
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                file = cursor.getString(columnIndex);
                cursor.close();
            } else { //local
                file = mediaFileUri.toString();
            }
            Toast.makeText(context, "Saved:" + file, Toast.LENGTH_SHORT).show();
            Log.v(TAG, "Saved:" + file);
            camera2Preview.startPreview();
        }

    };

    CameraCaptureSession.StateCallback mCaptureStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                session.capture(captureBuilder.build(), captureListener, backgroudHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };


}
