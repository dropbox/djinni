.PHONY: all objc java linux python python2 python3 clean clean_objc clean_java clean_python

FORCE_DJINNI := $(shell ./run_djinni.sh >&2)

all: objc java linux python

objc:
	cd objc && xcodebuild -sdk iphonesimulator -project DjinniObjcTest.xcodeproj -scheme DjinniObjcTest test

clean_objc:
	cd objc && xcodebuild -sdk iphonesimulator -project DjinniObjcTest.xcodeproj -scheme DjinniObjcTest clean

java:
	cd java && ant compile test

clean_java:
	cd java && ant clean

linux:
	cd ..; ./test-suite/java/docker/run_dockerized_test.sh

python2:
	./test_all_djinni4python.sh python

python3:
	./test_all_djinni4python.sh python3

python: python2 python3

clean_python:
	./test_all_djinni4python.sh clean

clean: clean_objc clean_java clean_python
