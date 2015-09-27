# Nounours for Android

Source code for the "Nounours" (aka "Noonoors") app:
https://play.google.com/store/apps/details?id=ca.rmen.nounours

## What is this?
This app includes a series of images of a teddy bear.  By dragging his "features" (paws, ears,
etc), the user changes the image, thereby moving the bear around.  Some features include:
* list of preset animations: each animation is a sequence of some of the bundled
  images, and may be accompanied by audio.
* gesture detection: if a fling gesture is detected, this triggers an animation
* orientation detection: when the device is tilted at certain angles, this triggers the
  display of certain images. The bear effectively "looks" in the direction the device
  is tilted.
* themes: each theme is a set of images, animations, sounds, and gesture and orientation 
  configuration. Only the default theme resources are included in the app.  The list of
  themes is also included in the app, but the other theme resources (images, audio files,
  configuration files) are hosted on an external server and downloaded when the user
  first selects a given theme from the settings screen.

## History
I created this app in 2009 as a way to introduce myself to Android development.  At the time,
it was simpler for me to first develop a prototype app using Java Swing.  I extracted the
non-platform-specific code (basically everything but UI logic) from the prototype app into a
library, libnounours (https://github.com/caarmen/libnounours), which I then included in this Android
app.


The project was closed-source until 2015, when I decided to publish it on GitHub.  The legacy code
from 2009 was not too clean, as I was just discovering Android development at the time: The project
had unusual naming conventions, unusual logging, messy hairs added to the code as I tried to resolve
memory issues when loading bitmaps...

#### Trivia
This app was selected as one of 10 "Prix du Public" finalists in the "SFR Jeunes Talents Développeurs" competition
in Paris on May 13, 2009.  http://www.pointgphone.com/resultats-concours-android-sfr-jeunes-talents-developpeurs-2636/

## Modernization
I have tried to clean up the project a bit.  I've:
* moved the build system from ant to gradle.
* fixed some bugs.
* removed the feature to check for new themes on the server: in the six years since this app was
  published on the Play Store, I have not released any new themes on the server. I've decided, if
  I ever do decide to make new themes, I will just publish a new version of the app.
* updated the code to use more standard naming conventions and logging methods.
* moved settings out of custom menus and dialogs into a ```PreferenceActivity```.
* added native action bar support for API level 11+.
* changed the license from no license (all rights reserved) to GPLv3.

Nevertheless, the original 2009 design and implementation of the project is still visible, and
this project is not the first one I would show off to prospective employers or clients :)

## Legacy support
Android development was pretty new itself at the time this app was created: Android 1.5 Cupcake (API
level 3) was released while this app was being developed.  Mostly for nostalgic reasons, this app
still supports API level 3.  As a result, this project has a few unusual attributes compared to a
typical modern Android project:
* A ```compat``` package exists, with classes such as ```Api8Helper```.
  Prior to Android 2.0 Eclair, app code calling framework methods at higher API levels could not be
  loaded, even if proper checks on ```Build.VERSION``` ensured it would never be executed. The
  result would be a ```AndroidRuntime: java.lang.VerifyError: com.mycompany.MyClass```. To prevent
  this, code for higher API levels must be completely isolated into separate classes.
* The check for the API level is done on the String ```Build.VERSION.SDK``` instead of the int
  ```Build.VERSION.SDK_INT```
* No support library is included.  The only library included in this project is libnounours.

