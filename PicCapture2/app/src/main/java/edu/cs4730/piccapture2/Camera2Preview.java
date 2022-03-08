package edu.cs4730.piccapture2;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a helper class, where everything is buried in this class, so
 * that it the code is very simple calling it.
 *
 * Construct it with a context and camera id number.
 *
 * call takepicture with a file (full path) and it will write it out.
 *
 */

public class Camera2Preview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    String TAG = "Camera2Preview";
    Context context;
    //used for the camera
    String cameraId;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    //needed for take picture
    private Size[] jpegSizes;
    int width = 640;
    int height = 480;
    CameraCharacteristics characteristics;
    ImageReader reader;
    Handler backgroudHandler;
    CaptureRequest.Builder captureBuilder;
    List<Surface> outputSurfaces;
   // File file;
   Uri mediaFileUri;

    public Camera2Preview(Context context, String CameraID) {
        super(context);
        this.context = context;
        //set the Camera to be used
        cameraId = CameraID;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);

    }

    /*
    This is the one to call to take a picture.
     */
    public void TakePicture(Uri fileUri) {
        mediaFileUri = fileUri;
        try {
            mCameraDevice.createCaptureSession(outputSurfaces, mCaptureStateCallback, backgroudHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    //Methods for the SurfaceView
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "Surfaceview Created");
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }
    //setup the camera objects

    private void openCamera() {

        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "openCamera Start");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                //String cameraId = manager.getCameraIdList()[0];

                // setup the camera perview.  should wrap this in a checkpermissions, which studio is bitching about
                // except it has been done before this fragment is called.
                manager.openCamera(cameraId, mStateCallback, null);


                //setup for taking the picture here, so we only do it once, instead at "take picture" time.
                characteristics = manager.getCameraCharacteristics(cameraId);
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

                //setup the width and height size, assuming camera knows it or use default 640x480.
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

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Don't have permission to camera!");
        }
        Log.e(TAG, "openCamera End");
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


    protected void startPreview() {

        if (null == mCameraDevice) {
            Log.e(TAG, "startPreview fail, return");
            return;
        }


        //get the surface, so I can added to varing places...
        Surface surface = mHolder.getSurface();

        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {

            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {

                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                    Toast.makeText(context, "onConfigureFailed", Toast.LENGTH_LONG).show();
                }
            }, null);
        } catch (CameraAccessException e) {

            e.printStackTrace();
        }
    }

    protected void updatePreview() {

        if (null == mCameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }

        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        Handler backgroundHandler = new Handler(thread.getLooper());

        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {

            e.printStackTrace();
        }
    }


    /**
     * all the listeners, callbacks that are needed here.
     */


    /*
      This is the callback necessary for the manager.openCamera Call back needed above.
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {

            Log.e(TAG, "onOpened");
            mCameraDevice = camera;
            //setup the capture of the current surface.
            startPreview();

            //is the setup to take the picture, now the mCameraDevice is initialized.
            //configure the catureBuilder, which is built in listener later on.
            try {
                captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            //int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            int deviceorientation = getResources().getConfiguration().orientation;
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(characteristics, deviceorientation));

        }


        @Override
        public void onDisconnected(CameraDevice camera) {

            Log.e(TAG, "onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {

            Log.e(TAG, "onError");
        }

    };
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
            if (listener != null) {
                listener.onPic(mediaFileUri);
            }
            startPreview();
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

    /**
     * simple callback to tell the main code the picture is taken.
     */
    // Define listener member variable
    private OnPicCallback listener = null;
    // Define the listener interface
    public interface OnPicCallback {
        void onPic(Uri fileUri);  //honesty the main sends this, but just case, return the file info.
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnPicListener(OnPicCallback listener) {
        this.listener = listener;
    }

}
