#
# Environment variables for overriding default behavior.
#

ifndef ANDROID_NDK_HOME
ANDROID_NDK_HOME = $(abspath $(dir $(realpath $(shell which ndk-build))))
endif

SCALA_VERSION=2.11
DJINNI_VERSION=0.1-SNAPSHOT
OUTPUT_JAR=src/target/scala-$(SCALA_VERSION)/djinni-assembly-$(DJINNI_VERSION).jar

#
# Global targets.
#

all: djinni example_ios example_android example_localhost test

clean: djinni_jar_clean
	-ndk-build -C example/android/app/ clean
	-xcodebuild -workspace example/objc/TextSort.xcworkspace -scheme TextSort -configuration 'Debug' -sdk iphonesimulator clean
	-rm -rf libs/
	-rm -rf obj/
	-rm -rf build/
	-rm -rf build_ios/
	-rm -f GypAndroid.mk

# rule to lazily clone gyp
# freeze gyp at the last version with android support
./deps/gyp:
	git clone https://chromium.googlesource.com/external/gyp.git ./deps/gyp
	cd deps/gyp && git checkout -q 0bb67471bca068996e15b56738fa4824dfa19de0

djinni:
	cd src && ./build

$(OUTPUT_JAR):
	cd src && sbt assembly

djinni_jar: $(OUTPUT_JAR)

djinni_jar_clean:
	cd src && sbt clean

# we specify a root target for android to prevent all of the targets from spidering out
GypAndroid.mk: ./deps/gyp example/libtextsort.gyp support-lib/support_lib.gyp example/example.djinni
	./example/run_djinni.sh
	ANDROID_BUILD_TOP=$(ANDROID_NDK_HOME) deps/gyp/gyp --depth=. -f android -DOS=android -Icommon.gypi example/libtextsort.gyp --root-target=libtextsort_jni

# we specify a root target for android to prevent all of the targets from spidering out
./build_ios/example/libtextsort.xcodeproj: ./deps/gyp example/libtextsort.gyp support-lib/support_lib.gyp example/example.djinni
	./example/run_djinni.sh
	deps/gyp/gyp --depth=. -f xcode -DOS=ios --generator-output ./build_ios -Icommon.gypi example/libtextsort.gyp

example_ios: ./build_ios/example/libtextsort.xcodeproj
	xcodebuild -workspace example/objc/TextSort.xcworkspace \
           -scheme TextSort \
           -configuration 'Debug' \
           -sdk iphonesimulator

# this target implicitly depends on GypAndroid.mk since gradle will try to make it
example_android: GypAndroid.mk
	cd example/android/ && ./gradlew app:assembleDebug
	@echo "Apks produced at:"
	@python example/glob.py example/ '*.apk'

example_localhost: ./deps/java
	cd example && make localhost

test: ./deps/java
	make -C test-suite

.PHONY: example_android example_ios example_localhost test djinni clean all dinni_jar
