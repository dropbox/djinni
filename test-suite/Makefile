.PHONY: all objc java linux

FORCE_DJINNI := $(shell ./run_djinni.sh >&2)

all: objc java linux

objc:
	cd objc; xcodebuild -sdk iphonesimulator -project DjinniObjcTest.xcodeproj -scheme DjinniObjcTest test

java:
	cd java && ant compile test

linux:
	cd ..; ./test-suite/java/docker/run_dockerized_test.sh

