Android Audio and video Examples
===========

`eclipse/` has the examples in eclipse project format, no longer updated.  Otherwise the examples are for android studio.

`legacy/` are now abandoned examples and are no longer updated.

---

**Audio Examples:**

`AudioPlay` demos how to play a resource audio file and an audio file from any other location (ie filesystem, Internet, etc)

`AudioRecordTest` will record via the mic and then you can play it back.

 There also a `fileSystemMediaStoreRecAudioDemo` example using the sdcard and mediastore methods in [saveData repo](https://github.com/JimSeker/saveData)

---

**Picture Examples:**

`PicCapture1` shows how to write code to take a picture with either camera and camera2 apis.

`PicCapture2` hides most of the camera code in a cameraXpreview.  It has code for both camera and camera2 apis.

`PicCapture3` shows examples of how to take a picture with the intent.

`PicCaptureIntent` uses intents to open the camera app, and return just bytes, put a picture in the local directory (with providers), or store onto the sdcard using the mediastore.  So no permissions are necessary for API 28+

`CameraPreview` only the Camera2 APIs and targets API 21+.  The code is broken up into a Preview class that just shows the camera preview in a surfaceview.  A capturePic class (that required the preview class) to take a picture. And there is a VideoClass as well.  This example is far from perfect, but works to show the separation in the classes.

`CameraXdemo` is a java version of the new cameraX from the androidX libraries.  Note, still beta in some places.

`CameraXdemo_kt` is a kotlin version of the new CameraX androidX libraries.  Note, still beta in some places.

---

**Video Examples:**


`CameraXVideoDemo` is using java with the new CameraX for video recording.   The video version of the cameraX APIs are beta, So they may change without notice or simply stop working.

`CameraXVideoDemo_kt` is using kotlin with the new CameraX for video recording. The video version of the cameraX APIs are beta, So they may change without notice or simply stop working

`videoCapture2_1` Uses the camera2 to record videos and storages in public movie directory on the sdcard.  It has bottomNavView so you can switch to a player and see the videos you have recorded this session.

`VideoCapture3` shows how to record video with either Camera or Camera2 (API21+).  Note that using Camera in 21+ causes odd results like the video maybe upside down.

`VideoCaptureIntent` shows how to ask the video recorder (likely camera) to record for you and store in default directory, inside your app, or onto the sdcard using the mediastore.  So no permissions are necessary.

`VideoPlayA` shows and to play a video with a video view.


---

**Other A/V Examples:**

`YouTubeDemo` Shows how to use the youtube API and key.  The youtube libraries are badly out of date and this example doesn't work well since API 29.  https://developers.google.com/youtube/android/player/ 

`QRdemo` shows to to create and scan QR codes.  This uses zxing app and some of there code (included in project).
A link to where I got the code is https://github.com/zxing/zxing/tree/master/android-integration/src/main/java/com/google/zxing/integration/android 


---

These are example code for University of Wyoming, Cosc 4730 Mobile Programming course and cosc 4735 Advance Mobile Programing course. 
All examples are for Android.