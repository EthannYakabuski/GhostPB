# GhostPB

The notion of a PB (personal best) in timed sports is that you are constantly trying to improve your own time,
effectively being in a mental race against yourself. 

GhostPB allows users to track and time their runs using google maps API, and save them to the app so that they can "race" against
themselves at a later date. The app will prompt the user if they are beating or losing to previous trials of the same track. 

(MUST READ) BEFORE YOU START, BUT AFTER YOU CLONE THE REPOSITORY YOU MUST;
FIRST: Obtain your own google maps API KEY; AND
SECOND: in app/src/debug/res/values/google_maps_api.xml replace KEY_HERE with your new google maps API KEY

This app is being developed in android studio. Instructions for set up and installation are here: 
https://developer.android.com/studio/
after installation you can import this git project

With developer options enabled on your phone (https://www.greenbot.com/article/2457986/how-to-enable-developer-options-on-your-android-phone-or-tablet.html)
you can plug your phone into your computer and run the app directly on your android device 

OR

You can run phone emulators via the AVD manager of Android Studio


MapsActivity.java is the main activity for the application, and where most of the functionality is

Activity_maps.xml is the layout file for the main activity of the application

