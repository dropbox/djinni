.PHONY: all objc java linux_docker

FORCE_DJINNI := $(shell ./run_djinni.sh >&2)

all: objc java

objc:
	cd objc; xcodebuild -sdk iphonesimulator -project DjinniObjcTest.xcodeproj -scheme DjinniObjcTest test

java:
	cd java && ant compile test

linux_docker:
	cd ..; ./test-suite/java/docker/run_dockerized_test.sh
