Android Audio and video Examples
===========

eclipse/ has the examples in eclipse project format, no longer updated.  Otherwise the examples are for android studio.

<b>AudioPlay</b> demos how to play a resouce audio file and an audio file from any other location (ie filesystem, internet, etc)

<b>AudioRecordTest</b>  will record via the mic and then you can play it back.

<b>CameraPreview</b> uses Api21 and only the Camera2 APIs.  The code is broken up into a Preview class that just shows the camera preview
in a surfaceview.  A capturePic class (that required the preview class) to take a picture.  At at some point a CaptureVid that will capture video (not even started).

<b>PicCapture1</b> shows how to write code to take a picture with either camera and camera2 apis.

<b>PicCapture2</b> hides most of the camera code in a cameraXpreview.  It has code for both camera and camera2 apis.

<b>PicCapture3</b> shows examples of how to take a picture with the intent.

<b>VideoCapture1</b> Uses an intent to record a video via the native recorder and then plays it in the app.

<b>VideoCapture2</b> shows how to record video using the media recorder (and older camera APIs) and plays it back.  Doesn't work well on API 21+ video maybe upside down. (no permission checking for API23+ either) 

<b>VideoCapture3</b> shows how to record video several videos in a row.  works ok on API 21, but 23+ video is upsidedown  (no permission checking for API23+ either)

<b>VideoPlayA</b> shows and to play a video with a video view.

<b>YouTubeDemo</b> Shows how to use the youtube API and key.  This demo is still a work in progress.


These are example code for University of Wyoming, Cosc 4730 Mobile Programming course.
All examples are for Android.