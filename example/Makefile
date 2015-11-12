all: android ios localhost py

android: example.djinni
	@make -C .. GypAndroid.mk

ios: example.djinni
	@make -C .. ./build_ios/example/libtextsort.xcodeproj

localhost: example.djinni
	cd localhost && ant compile jar run

py: example.djinni
	@make -C .. ./build_py/Makefile

.PHONY: android ios localhost py
