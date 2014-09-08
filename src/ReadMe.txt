------------------------------------------------------
Djinni

Parses an interface definition file and generates:
- C++ implementations of types (enums, records)
- Java implementations of types
- Objective-C implementations of types
- C++ code to convert between C++ and Java over JNI
- Objective-C++ code to convert between C++ and Objective-C.

This tool is written in Scala and has been tested in JDK 6 and 7.

------------------------------------------------------
Building

To build once:

    % ./build

If you're building frequently, load up the build tool's REPL and run
the "compile" command every time you want to compile it.

    % ./support/sbt
    > compile

------------------------------------------------------
Running

    # ./run
          --idl input.djinni
          --cpp-out out/cpp
          --java-out out/java/src
          --jni-out out/java/jni
          --objc-out out/objc

    # ./run --help  # to show all options

------------------------------------------------------
IntelliJ

- There's a ".idea/" project folder.
- Use IntelliJ 13
