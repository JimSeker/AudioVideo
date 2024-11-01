// Copyright 2014 Google Inc. All Rights Reserved.

package edu.cs4730.youtubedemo;

/**
 * Static container class for holding a reference to your YouTube Developer Key.
 */
public class DeveloperKey {

  /**
   * Please replace this with a valid API key which is enabled for the
   * YouTube Data API v3 service. Go to the
   * <a href="https://console.developers.google.com/">Google Developers Console</a>
   * to register a new developer key.
   */
  public static final String DEVELOPER_KEY = "AIzaSyCYgp_QQ3D3yn1ODNcBS40GSO0Gch4lmig";

}

/*  from http://www.sitepoint.com/using-the-youtube-api-to-embed-video-in-an-android-app/

    Go to the Google Developers Console
    Create a new project. I named mine VideoTube.
    On the page that appears after project creation, expand APIs & auth on the left sidebar.
      Next, click APIs. In the list of APIs, click on YouTube Data API and enable the Youtube Data
      API v3 on the page that follows.
    In the sidebar on the left, select Credentials. For credentials, the API supports OAuth 2.0,
      the use of an API key and of a Service account. We'll use the API key option.
    Select API key from the Add Credentials dropdown menu. A popup will appear for you to specify
    the key type. Select Android Key. Next select Add package name and fingerprint and add the
    Android app's package name (Mine is com.echessa.videotube) and then run the following command
     in Terminal to get the SHA-1 certificate fingerprint.

    keytool -list -v -keystore ~/.android/debug.keystore
 */