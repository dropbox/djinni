.PHONY: all djinni objc java linux_docker python python2 python3 clean clean_objc clean_java clean_python

all: objc java python

djinni:
	./run_djinni.sh

objc: djinni
	cd objc && xcodebuild -sdk iphonesimulator -project DjinniObjcTest.xcodeproj -scheme DjinniObjcTest test -destination 'platform=iOS Simulator,name=iPhone 6s,OS=9.2'

clean_objc:
	cd objc && xcodebuild -sdk iphonesimulator -project DjinniObjcTest.xcodeproj -scheme DjinniObjcTest clean

java: djinni
	cd java && ant compile test

clean_java:
	cd java && ant clean

linux_docker: djinni
	cd .. && ./test-suite/java/docker/run_dockerized_test.sh

python2:
	./test_all_djinni4python.sh python

python3:
	./test_all_djinni4python.sh python3

python: python2 python3

clean_python:
	./test_all_djinni4python.sh clean

clean: clean_objc clean_java clean_python
