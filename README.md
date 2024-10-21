Android Audio and video Examples
===========

`eclipse/` has the examples in eclipse project format, no longer updated.  Otherwise the examples are for android studio.

`legacy/` are now abandoned examples and are no longer updated.

---

**Audio Examples:**

`AudioPlay` (java) demos how to play a resource audio file and an audio file from any other location (ie filesystem, Internet, etc)

`AudioPlay` (kotlin) demos how to play a resource audio file and an audio file from any other location (ie filesystem, Internet, etc)

`AudioRecordDemo` will record via the mic and then you can play it back.

 There also a `fileSystemMediaStoreRecAudioDemo` example using the sdcard and mediastore methods in [saveData repo](https://github.com/JimSeker/saveData)

---

**Picture Examples:**

`PicCapture1` shows how to write code to take a picture (with a surfaceview) with camera2 apis, saving to local or the sdcard.

`PicCapture2` hides most of the camera code in a camera2preview.  This only uses the camera2 apis.  (see legacy for camera v1)

`PicCaptureIntent` uses intents to open the camera app, and return just bytes, put a picture in the local directory (with providers), or store onto the sdcard using the mediastore.  So no permissions are necessary for API 28+

`CameraPreview` More complex example, that has a Preview class,  capturePic class (that required the preview class) to take a picture, and VideoClass to take video.  This example is far from perfect, but works to show the separation in the classes.  This example only take/records.  It doesn't do any playback.

`CameraXdemo` is a java version of the new cameraX from the androidX libraries.  Note, still beta in some places.

`CameraXdemo_kt` is a kotlin version of the new CameraX androidX libraries.  Note, still beta in some places.

---

**Video Examples:**


`CameraXVideoDemo` is using java with the new CameraX for video recording.   The video version of the cameraX APIs are beta, So they may change without notice or simply stop working.

`CameraXVideoDemo_kt` is using kotlin with the new CameraX for video recording. The video version of the cameraX APIs are beta, So they may change without notice or simply stop working

`VideoCapture1` shows how to record video with Camera2 (API21+).  there is no playback of the video.

`videoCapture2` Uses the camera2 to record videos and storages in public movie directory on the sdcard.  It has bottomNavView so you can switch to a player and see the videos you have recorded this session.



`VideoCaptureIntent` shows how to ask the video recorder (likely camera) to record for you and store in default directory, inside your app, or onto the sdcard using the mediastore.  So no permissions are necessary.

`VideoPlayA` shows and to play a video with a video view.


---

**Other A/V Examples:**

`YouTubeDemo` Shows how to use the youtube API and key.  The youtube libraries are badly out of date and this example doesn't work well since API 29.  https://developers.google.com/youtube/android/player/ 

`QRdemo` shows to to create and scan QR codes.  This uses zxing app and some of there code (included in project).  Note zxing states their code is in maintenance mode only and not actively being developed.  the code still work in api 33, but it will likely fail sometime soon, this can be done via the googleapi in mlkit/vision as well.
A link to where I got the code is https://github.com/zxing/zxing/tree/master/android-integration/src/main/java/com/google/zxing/integration/android 


---

These are example code for University of Wyoming, Cosc 4730 Mobile Programming course and cosc 4735 Advance Mobile Programing course. 
All examples are for Android.