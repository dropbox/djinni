.PHONY: all djinni objc java linux_docker

all: objc java

djinni:
	./run_djinni.sh

objc: djinni
	cd objc; xcodebuild -sdk iphonesimulator -project DjinniObjcTest.xcodeproj -scheme DjinniObjcTest test -destination 'platform=iOS Simulator,name=iPhone 6s,OS=10.1'

java: djinni
	cd java && ant compile test

linux_docker: djinni
	cd ..; ./test-suite/java/docker/run_dockerized_test.sh
