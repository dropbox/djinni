all: android ios py

android: example.djinni
	@make -C .. GypAndroid.mk

ios: example.djinni
	@make -C .. ./build_ios/example/libtextsort.xcodeproj

py: example.djinni
	@make -C .. ./build_py/Makefile

.PHONY: android ios py
