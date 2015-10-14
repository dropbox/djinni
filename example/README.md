TextSort
--------
This folder contains an example project using djinni. The application contains a multiline text
view, and when the button "Sort" is hit, sorts the lines in that view.  There is also a
command-line version of the demo.

Interface Structure
------------------
Two interfaces are defined: SortItems and TextboxListener. SortItems is implemented in C++; its
method sort() takes a list of strings (wrapped in a record), sorts it, and passes the sorted list to
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

**Quick Start Command-line**
```
cd djinni_root_dir;
make example_localhost
```

**Details**
This example project utilizes [gyp](https://code.google.com/p/gyp/) to generate project files for
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
xcodeproj file) This program can be built using the default settings in Xcode. Or more simply:
```
cd djinni_root_rit/example;
xcodebuild -workspace objc/TextSort.xcworkspace -scheme TextSort -configuration 'Debug' -sdk iphoneos
```

Command-line / Localhost Version
--------------------------------
See the [Localhost README](localhost/README.md)

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

After that, you have to tell gradle the location of the NDK.
This can happen by either setting the `ANDROID_NDK_HOME` environment variable
to the path where the NDK was installed, or by creating a `local.properties`
file in the project directory.

This file must contain the `ndk.dir=<path-to-ndk>` setting. A
[`local.properties.sample`](local.properties.sample) file is included with this
example. In order to use it, you have to rename it to `local.properties` and
replace `<path-to-ndk>` with the real path to your NDK installation.

### Android build fails &mdash; Ambiguous method overloading for method java.io.File\#\<init\>.

If you're getting this error when running `make example_android`:

```
Ambiguous method overloading for method java.io.File#<init>.
Cannot resolve which method to invoke for [null, class java.lang.String] due to overlapping prototypes between:
      [class java.lang.String, class java.lang.String]
      [class java.io.File, class java.lang.String]
```

Adding the ANDROID_HOME environment variable may fix this error, as mentioned in the following issue: [https://github.com/dropbox/djinni/issues/44](https://github.com/dropbox/djinni/issues/44)

Try adding the following line to `~/.bash_profile` (below is the homebrew SDK path, your path may be different):

    export ANDROID_HOME=/usr/local/Cellar/android-sdk/24.3.2

    
### Android Studio project fails to load &mdash; Gradle DSL method not found: 'runProguard'

If you're getting this error when you open the Android project in Android Studio:

```
Error:(16, 0) Gradle DSL method not found: 'runProguard()'
Possible causes:
* The project 'android' may be using a version of Gradle that does not contain the method. *Gradle settings*
* The build file may be missing a Gradle plugin. *Apply Gradle plugin*
```

Try replacing line 16 of `djinni_root_dir/example/android/app/build.gradle` with the following:

```
minifyEnabled false
```

More details in this SO answer: [http://stackoverflow.com/a/27266373/2490989](http://stackoverflow.com/a/27266373/2490989)


### Android Studio project fails to load &mdash; No such property: ndkFolder

If you're getting this error when you open the Android project in Android Studio:

> Error:(36, 0) No such property: ndkFolder for class:com.android.build.gradle.AppPlugin

Try replacing line 36 of `djinni_root_dir/example/android/app/build.gradle` with the following:

```
File ndkDir = project.getPlugins().getPlugin('android').sdkHandler.getNdkFolder()
```

More details in this SO answer: [http://stackoverflow.com/a/28700250/2490989](http://stackoverflow.com/a/28700250/2490989)
