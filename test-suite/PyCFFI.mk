# Work in progress
# get all object files needed for the library

.PHONY: all $(lib) clean
FLAGS=-arch i386 -arch x86_64 -fPIC -std=c++14

# could pass this as argument to script
support_lib=$(base_dir)/../support-lib/cwrapper
cwrapper_out=$(base_dir)/generated-src/cwrapper
py_cpp_hw=$(base_dir)/handwritten-src/pycpp
py_cpp_out=$(base_dir)/generated-src/pycpp
cpp_hw=$(base_dir)/handwritten-src/cpp
cpp_out=$(base_dir)/generated-src/cpp
c_hw=$(base_dir)/handwritten-src/cwrapper
pybuild=$(base_dir)/pybuild

all: $(lib)

# cpp compiled to .o for 32 and 64 bit architectures, as position independent code (fpic)
$(pybuild)/%.o : $(py_cpp_hw)/%.cpp
	$(CXX) $(FLAGS) -I$(support_lib) -I$(py_cpp_hw) -I$(py_cpp_out) -c $< -o $@

$(pybuild)/%.o : $(cpp_hw)/%.cpp
	$(CXX) $(FLAGS) -I$(support_lib) -I$(cpp_hw)  -I$(cpp_out) -c $< -o $@

# c support for limts testing
$(pybuild)/%.o : $(c_hw)/%.cpp
	$(CXX) $(FLAGS) -c $< -o $@

$(pybuild)/%.o : $(support_lib)/%.cpp
	$(CXX) -c $(FLAGS) $< -o $@

$(pybuild)/%.o : $(cwrapper_out)/%.cpp
	$(CXX) -c $(FLAGS) -I$(support_lib) -I$(py_cpp_hw) -I$(cpp_hw) -I$(py_cpp_out) -I$(cpp_out) $< -o $@

sources := $(wildcard $(c_hw)/*.cpp) $(wildcard $(py_cpp_hw)/*.cpp) $(wildcard $(cpp_hw)/*.cpp) $(wildcard $(support_lib)/*.cpp) $(wildcard $(cwrapper_out)/*.cpp)
objects := $(addprefix $(pybuild)/,$(addsuffix .o,$(basename $(notdir $(sources)))))

$(lib): $(objects)
	$(CXX) $(FLAGS) -dynamiclib -lc $^ -o $@

clean:
	-rm $(pyb)/*
