Djinni Test Suite
-----------------
The test suite provides a toolkit to test the behavior of Djinni generated code. It can be
run under Java (standalone JUnit runner) and iOS (via XCode iPhone simulator).

Djinni Files
----------
The djinni files are located in djinni/ . all.djinni is a list of import for all files you would
like to be generated. After input files are changed, run ./run_djinni.sh to regenerate files.

Testing
-------
Run 'make java' or 'make objc' to test the given environment.

You may need to have Xcode open for the simulator portion of the objc
tests to complete successfully.  Try opening the app if you see a
failure connecting to the simulator.