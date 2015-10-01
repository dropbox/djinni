Localhost Demo
--------------
This demo simply sorts some random strings and prints
the results to the terminal as so:

```
$ ant compile jar run
...
Sep 07, 2015 6:12:10 PM com.dropbox.djinni.NativeLibLoader loadLibrary
INFO: Loaded /var/folders/zj/w8zq8xmd4m9f__rdl4rmqhyr0000gn/T/libTextSortNative.dylib3424746480331627703dylib
Sep 07, 2015 6:12:10 PM com.dropbox.textsort.SortTest main
INFO: Input strings:
96901
70751
93099
10224
21918
Sep 07, 2015 6:12:10 PM com.dropbox.textsort.SortTest main
INFO: Output strings:
10224
21918
70751
93099
96901
```

This demo also serves to document how to build a native library using djinni
and ship it inside a jar (and include djinni's small set of dependencies).


Running locally
---------------
Run `ant compile jar run` to compile and run.


Running in Linux via Docker
---------------------------
To demo running in linux, `run_in_docker.sh ant clean compile jar run` to run inside
a Docker container, or just `run_in_docker.sh` to drop into a Dockerized
shell.  The root of this djinni repo will be mounted at `/opt/djinni`.


Build Details
-------------
This demo packages the native library in the application jar and uses the 
djinni support class `com.dropbox.djinni.NativeLibLoader` to unpack and 
load the library at runtime.  This approach should work well for the
majority of use cases but has several known caveats:
 * The app might not have access to temp space at runtime.  In this case,
     you must install the library locally and can use the system
	 property `djinni.native_libs_dirs` to tell djinni where to find your
	 library.
 * The native library might have many other system dependencies.  In this
     case, you must either include those shared libraries in the jar,
	 link/compile them into the app native library, or somehow
	 ensure the dependencies exist on the target system (e.g. run your
	 app in a container).
 * `NativeLibLoader` does not currently support filtering libraries by
     host architecture.  In this scenario, you might consider building
	 a fat library using `lipo` and `libtool`.

Note that `NativeLibLoader` is an entirely optional dependency.  You can
omit it from your app and build and include only djinni generated code.
However, you will need to invoke `System.load()` or `System.loadLibrary()`
manually.

