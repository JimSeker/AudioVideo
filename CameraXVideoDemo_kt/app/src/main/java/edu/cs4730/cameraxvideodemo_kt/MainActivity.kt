package edu.cs4730.cameraxvideodemo_kt

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoRecordEvent.Finalize
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * based on https://codelabs.developers.google.com/codelabs/camerax-getting-started#0
 */

class MainActivity : AppCompatActivity() {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null
    lateinit var rpl: ActivityResultLauncher<Array<String>>
    private lateinit var cameraExecutor: ExecutorService
    var recording: Boolean = false
    lateinit var camera_capture_button : Button
    lateinit var viewFinder: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {  //For API 29+ (q), for 26 to 28.
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            } else {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                )
            }
        rpl = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            rpl.launch(REQUIRED_PERMISSIONS)
        }

        viewFinder = findViewById(R.id.viewFinder)
        // Set up the listener for take photo button
        camera_capture_button = findViewById(R.id.camera_capture_button)
        camera_capture_button.setOnClickListener { takeVideo() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @SuppressLint("RestrictedApi", "MissingPermission")
    private fun takeVideo() {
        Log.d(TAG, "start")
        // Get a stable reference of the modifiable image capture use case
        val videoCapture = videoCapture ?: return

        if (recording) {
            //ie already started.
            currentRecording?.stop()
            recording = false
            camera_capture_button.text = "Start Rec"
        } else {

            val name = "CameraX-" + SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".mp4"
            val cv = ContentValues()
            cv.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            cv.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                cv.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }

            val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
                contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
                .setContentValues(cv)
                .build()
            currentRecording = videoCapture.output
                .prepareRecording(this@MainActivity, mediaStoreOutputOptions)
                .withAudioEnabled()
                .start(
                    cameraExecutor
                ) { videoRecordEvent ->
                    if (videoRecordEvent is Finalize) {
                        val savedUri = videoRecordEvent.outputResults.outputUri
                        //convert uri to useful name.
                        var cursor: Cursor? = null
                        var path: String
                        try {
                            cursor = contentResolver.query(
                                savedUri,
                                arrayOf(MediaStore.MediaColumns.DATA),
                                null,
                                null,
                                null
                            )
                            cursor!!.moveToFirst()
                            path =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                        } finally {
                            cursor!!.close()
                        }
                        Log.wtf(TAG, path)
                        if (path == "") {
                            path = savedUri.toString()
                        }
                        val msg = "Video capture succeeded: $path"
                        runOnUiThread {
                            Toast.makeText(
                                baseContext,
                                msg,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        Log.d(TAG, msg)
                        currentRecording = null
                    }
                }

            recording = true
            camera_capture_button.text = "Stop Rec"
        }
    }


    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                try {
                    val cameraProvider = cameraProviderFuture.get() as ProcessCameraProvider
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(viewFinder.surfaceProvider)
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    val recorder = Recorder.Builder()
                        .setQualitySelector(
                            QualitySelector.from(
                                Quality.HIGHEST,
                                FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                            )
                        )
                        .build()
                    videoCapture = VideoCapture.withOutput(recorder)
                    val imageCatpure = ImageCapture.Builder().build()
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        this@MainActivity, cameraSelector, preview, imageCatpure, videoCapture
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(this)
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private lateinit var REQUIRED_PERMISSIONS: Array<String>
    }


}
