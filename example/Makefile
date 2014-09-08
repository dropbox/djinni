all: android ios

android: example.djinni
	@make -C .. GypAndroid.mk

ios: example.djinni
	@make -C .. ./build_ios/example/libtextsort.xcodeproj

.PHONY: android ios
