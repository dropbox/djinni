# always force this build to re-run its dependencies
FORCE_GYP := $(shell make -C ../../../ GypAndroid.mk)
include ../../../GypAndroid.mk
