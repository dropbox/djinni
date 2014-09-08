.PHONY: all objc java

FORCE_DJINNI := $(shell ./run_djinni.sh >&2)

all: objc java

objc:
	cd objc; xcodebuild -sdk iphonesimulator -project DjinniObjcTest.xcodeproj -scheme DjinniObjcTest test

java:
	cd java; ant test
