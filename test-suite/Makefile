.PHONY: all djinni objc clean_objc java clean_java linux_docker clean

all: objc java

djinni:
	./run_djinni.sh

objc: djinni
	cd objc; xcodebuild -sdk iphonesimulator -project DjinniObjcTest.xcodeproj -scheme DjinniObjcTest test -destination 'platform=iOS Simulator,name=iPhone 7,OS=latest'

clean_objc:
	cd objc && xcodebuild -sdk iphonesimulator -project DjinniObjcTest.xcodeproj -scheme DjinniObjcTest clean

java: djinni
	cd java && ant compile test

clean_java:
	cd java && ant clean

linux_docker: djinni
	cd ..; ./test-suite/java/docker/run_dockerized_test.sh

clean: clean_objc clean_java
