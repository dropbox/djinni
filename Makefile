all: djinni example_ios example_android example_localhost example_python test

clean:
	-ndk-build -C example/android/app/ clean
	-xcodebuild -workspace example/objc/TextSort.xcworkspace -scheme TextSort -configuration 'Debug' -sdk iphonesimulator clean
	-xcodebuild -project ./build_py/example/libtextsort.py.xcodeproj -target libtextsort_py -configuration 'Debug' clean
	-rm -rf libs/
	-rm -rf obj/
	-rm -rf build/
	-rm -rf example/build/
	-rm -rf build_ios/
	-rm -rf build_by/
	-rm -f GypAndroid.mk

#
# Pre-generation rules
#

# rule to lazily clone gyp
# freeze gyp at the last version with android support
./deps/gyp:
	git clone https://chromium.googlesource.com/external/gyp.git ./deps/gyp
	cd deps/gyp && git checkout -q 0bb67471bca068996e15b56738fa4824dfa19de0

djinni:
	cd src && ./build

run_example_djinni:
	./example/run_djinni.sh

#
# Android example rules
#

# we specify a root target for android to prevent all of the targets from spidering out
GypAndroid.mk: run_example_djinni ./deps/gyp example/libtextsort.gyp support-lib/support_lib.gyp example/example.djinni
	ANDROID_BUILD_TOP=$(shell dirname `which ndk-build`) deps/gyp/gyp --depth=. -f android -DOS=android -Icommon.gypi example/libtextsort.gyp --root-target=libtextsort_jni

# this target implicitly depends on GypAndroid.mk since gradle will try to make it
example_android: GypAndroid.mk
	cd example/android/ && ./gradlew app:assembleDebug
	@echo "Apks produced at:"
	@python example/glob.py example/ '*.apk'

#
# iOS example rules
#

./build_ios/example/libtextsort.xcodeproj/project.pbxproj: run_example_djinni ./deps/gyp example/libtextsort.gyp support-lib/support_lib.gyp example/example.djinni
	deps/gyp/gyp --depth=. -f xcode -DOS=ios --generator-output ./build_ios -Icommon.gypi example/libtextsort.gyp

example_ios: ./build_ios/example/libtextsort.xcodeproj/project.pbxproj
	xcodebuild -workspace example/objc/TextSort.xcworkspace \
           -scheme TextSort \
           -configuration 'Debug' \
           -sdk iphonesimulator \
	   -destination 'platform=iOS Simulator,name=iPhone 6s,OS=9.2'

#
# Python example rules
# (These only work on Mac, due to the use of Xcode and dylib.  For other platforms, a makefile
#  for a .so would be better.  On Mac, gyp can't generate a Makefile for a multi-arch binary.)
#

./build_py/example/libtextsort.py.xcodeproj/project.pbxproj: run_example_djinni ./deps/gyp example/libtextsort.gyp support-lib/support_lib.gyp example/example.djinni
	deps/gyp/gyp --depth=. -f xcode -DOS=mac --generator-output ./build_py --suffix=.py -Icommon.gypi example/libtextsort.gyp

./build_py/cffi/libtextsort_py.dylib: ./build_py/example/libtextsort.py.xcodeproj/project.pbxproj
	xcodebuild -project ./build_py/example/libtextsort.py.xcodeproj \
           -target libtextsort_py \
           -configuration 'Debug' \
           ONLY_ACTIVE_ARCH=NO
	cp example/build/Debug/libtextsort_py.dylib ./build_py/cffi/libtextsort_py.dylib

example_python2: ./build_py/cffi/libtextsort_py.dylib
	./example/run_py_example.sh python

example_python3: ./build_py/cffi/libtextsort_py.dylib
	./example/run_py_example.sh python3

example_python: example_python2 example_python3

example_localhost: ./deps/java
	cd example && make localhost

#
# Test-Suite rules
#

test: ./deps/java
	make -C test-suite

.PHONY: run_example_djinni example_android example_ios example_localhost example_python example_python2 example_python3 test djinni clean all
