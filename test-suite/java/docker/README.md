Djinni Linux Tests
------------------

This directory contains a suite of tools for testing djinni (JNI only)
on Linux via Docker.  The suite helps ensure the portability of djinni and 
(self-)document compatible platforms.

Quickstart
----------

You need to have [Docker](https://docker.com) installed.  To execute all
tests, run `./test-suite/java/docker/run_dockerized_test.sh` from the root of the
djinni repository.

Adding a Platform
-----------------

 1. Create a subdirectory named after the platform (e.g. `my_platform`).
 2. If you can create a Docker image for your platform,
    simply create a `Dockerfile` in the subdirectory that:
    * Builds an image with djinni dependencies (e.g. Java, a C++ compiler)
    * Expects djinni in `/opt/djinni/`
    * Has a `CMD` to runs the test script:
	    `/opt/djinni/test-suite/java/docker/build_and_run_tests.sh`
 3. You can test the new platform using:
     `./test-suite/java/docker/run_dockerized_test.sh ./test-suite/java/docker/my_platform`

