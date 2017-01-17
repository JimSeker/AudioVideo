Android Audio and video Examples
===========

eclipse/ has the examples in eclipse project format, no longer updated.  Otherwise the examples are for android studio.

<b>AudioPlay</b> demos how to play a resouce audio file and an audio file from any other location (ie filesystem, internet, etc)

<b>AudioRecordTest</b> will record via the mic and then you can play it back.

<b>CameraPreview</b> only the Camera2 APIs and targets API 21+.  The code is broken up into a Preview class that just shows the camera preview in a surfaceview.  A capturePic class (that required the preview class) to take a picture. And there is a VideoClass as well.  This example is far from perfect, but works to show the seperation in the classes.

<b>PicCapture1</b> shows how to write code to take a picture with either camera and camera2 apis.

<b>PicCapture2</b> hides most of the camera code in a cameraXpreview.  It has code for both camera and camera2 apis.

<b>PicCapture3</b> shows examples of how to take a picture with the intent.

<b>VideoCapture1</b> Uses an intent to record a video via the native recorder and then plays it in the app.

<b>VideoCapture2</b> shows how to record video using the media recorder (and older camera APIs) and plays it back.  This example is set to target API 20, but will run on higher APIs. It doesn't work well on API 21+ video maybe upside down. 

<b>VideoCapture3</b> shows how to record video with either Camera or Camera2 (API21+).  Note that using Camera in 21+ causes odd results like the video maybe upside down.

<b>VideoPlayA</b> shows and to play a video with a video view.

<b>YouTubeDemo</b> Shows how to use the youtube API and key.  This demo is still a work in progress.

<b>legacy/</b> are now abandoned examples and are no longer updated.

These are example code for University of Wyoming, Cosc 4730 Mobile Programming course.
All examples are for Android.