TextSort
--------
This folder contains an example project using djinni. The application contains a multiline text
view, and when the button "Sort" is hit, sorts the lines in that view.

Interface Stucture
------------------
Two interfaces are defined: SortItems and TextboxListener. SortItems is implemented in C++; its
method sort() takes a list of strings (wrapped in a record), sort it, and passes the sorted list to
TextboxListener.update() . TextboxListener is implemented in Java / Objective-C, and will update the
text area on the UI when update() is called. You can check the source tree for implemetation
details.

Getting Started
---------------
**Quick Start Android**
```
cd djinni_root_dir;
make example_android
```

**Quick Start iOS**
```
cd djinni_root_dir;
make example_ios
```

**Details**
This example proejct utilizes [gyp](https://code.google.com/p/gyp/) to generate project files for
each platform. So, before running any of the example code the first time you will need to run `make ios`
or `make android` depending on the platform you wish you run.  Additionally, those commands will need
to be run each time the djinni file has been changed.  Gyp will automatically detect the new files
and include them into each platform's build.

Android Version
---------------
The folder android/ contains an Android Studio project. The main handwritten logic is at
com.dropbox.textsort.MainActivity and NativeMainActivity.java. Use Android Studio / Gradle to build
the project. More simply, just run:
```
cd djinni_root_dir/example/android;
./gradlew app:assembleDebug
```

iOS Version
-----------
The iOS project is in objc/ . Note that the interface layout is only tested under 4-inch iPhone.
The main handwritten logic is at TXSViewController.mm. Please open TextSort.**xcworkspace** (not the
xcodeproj file) This program can be built using default setting in Xcode. Or more simply:
```
cd djinni_root_rit/example;
xcodebuild -workspace objc/TextSort.xcworkspace -scheme TextSort -configuration 'Debug' -sdk iphoneos
```

Making Changes to Djinni file
-----------------------------
The Djinni file used in this example is `example.djinni`. After modifying this file, you can run
either `make android` or `make ios` to generate the source code and new project files.


Troubleshooting
---------------

### Android build fails &mdash; NDK not configured

If you're getting this error:

> NDK is not configured. Make sure there is a local.properties file with an ndk.dir entry in the directory

then gradle can not find your NDK installation. First make sure that you've
installed the NDK. On Mac OS X, you can install easily via
[homebrew](http://brew.sh/): `brew install android-ndk`

After that, you have to tell gradle where the location of the NDK location.
This can happen by either setting the `ANDROID_NDK_HOME` environment variable
to the path where the NDK was installed, or by creating a `local.properties`
file in the project directory.

This file must contain the `ndk.dir=<path-to-ndk>` setting. A
[`local.properties.sample`](local.properties.sample) file is included with this
example. In order to use it, you have to rename it to `local.properties` and
replace `<path-to-ndk>` with the real path to your NDK installation.
