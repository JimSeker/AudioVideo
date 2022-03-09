Android Audio and video Examples legacy/
===========

These examples are no longer updated and should be concerned abandoned. 

<b>camaera2te</b> unknown

<b>VideoCapture</b> Uses an intent to record a video via the native recorder and then plays it in the app.  it's badly out of date and doesn't use the newer media store.

<b>VideoCapture2</b> shows how to record video using the media recorder (and older camera APIs) and plays it back.  This example is set to target API 20, but will run on higher APIs. It doesn't work well on API 21+ video maybe upside down.   This example is broken completely in API 24+, I commented out a startActivity, so it at least it would not crash anymore.

<b>VideoCapture4</b> unknown
 
`PicCapture` shows how to write code to take a picture with either camera and camera2 apis.  as it set to API 26, most of the camera1 has stopped working correctly.

`PicCapture2` hides most of the camera code in a camera1preview or camera2preview.  It has code for both camera and camera2 apis. 

`PicCapture3` shows examples of how to take a picture with the intent.  older code and kind of a mess too.

`VideoCapture3` shows how to record video with either Camera or Camera2 (API21+).  Note that using Camera in 21+ causes odd results like the video maybe upside down.

These were example code for University of Wyoming, Cosc 4730 Mobile Programming course.
All examples are for Android.