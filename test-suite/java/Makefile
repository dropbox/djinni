SUPPORT_DIR := ../../support-lib/jni

DYLIB := libDjinniTestNative.dylib

# Hacky - dummies cancel out .. to make sure objs are all under obj/
OBJ_DIR := obj/dummy/dummy

CPP_SRCS := $(SUPPORT_DIR)/djinni_support.cpp \
            $(SUPPORT_DIR)/djinni_main.cpp \
            $(wildcard ../generated-src/jni/*.cpp) \
            $(wildcard ../generated-src/cpp/*.cpp) \
            $(wildcard ../handwritten-src/cpp/*.cpp) \

CPP_OBJS := $(patsubst %,$(OBJ_DIR)/%,$(CPP_SRCS:.cpp=.o))

CPPFLAGS := -std=c++1y -I../generated-src/{jni,cpp} -I$(SUPPORT_DIR) -I/System/Library/Frameworks/JavaVM.framework/Headers -I../handwritten-src/cpp -g -Wall -Werror

$(OBJ_DIR)/%.o: %.cpp
	@mkdir -p $(dir $@)
	$(CXX) -MMD $(CPPFLAGS) -c $< -o $@
	@./fixdep.sh $(@:.o=.d) > $(@:.o=.P)

default:
	@echo "Use 'ant' (or 'ant clean') instead of invoking make directly."
	@exit 1

$(DYLIB): $(CPP_OBJS)
	clang++ $(CPPFLAGS) $(CPP_OBJS) -dynamiclib -o $@

clean:
	rm -rf obj $(CPP_OBJS) $(CPP_OBJS:.o=.d) $(CPP_OBJS:.o=.P) $(DYLIB)*

-include $(CPP_OBJS:.o=.P)
