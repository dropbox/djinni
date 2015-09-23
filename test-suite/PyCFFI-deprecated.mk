# get all object files needed for the library

.PHONY: all $(lib) clean

# could pass this as argument to script
support_lib=$(base_dir)/../support-lib/c
cwrapper_out=$(base_dir)/generated-src/cwrapper
cpp_hw=$(base_dir)/handwritten-src/pycpp
cpp_out=$(base_dir)/generated-src/pycpp

all: $(lib)

fnameimpl=$(fname)_impl
cwfname=cw$(fname)

# cpp compiled to .o for 32 and 64 bit architectures, as position independent code (fpic)
$(fnameimpl).o: $(cpp_hw)/$(fnameimpl).cpp $(cpp_hw)/$(fnameimpl).hpp $(cpp_out)/$(fname).hpp
	$(CXX) -arch i386 -arch x86_64 -fPIC -std=c++14 -I$(support_lib) -I$(cpp_hw) -I$(cpp_out) -c $(cpp_hw)/$(fnameimpl).cpp

wrapper_marshal.o: $(support_lib)/wrapper_marshal.cpp $(support_lib)/wrapper_marshal.hpp
	gcc -arch i386 -arch x86_64 -fPIC -std=c++14 -c $(support_lib)/wrapper_marshal.cpp 

$(cwfname).o: $(cwrapper_out)/$(cwfname).cpp $(cwrapper_out)/$(cwfname).h 
	gcc -arch i386 -arch x86_64 -fPIC -std=c++14 -I$(support_lib) -I$(cpp_hw) -I$(cpp_out) -c $(cwrapper_out)/$(cwfname).cpp

$(lib): $(fnameimpl).o wrapper_marshal.o $(cwfname).o
	echo "Library objects compiled for "$(fname)

# gcc -arch i386 -arch x86_64 -fPIC -c $(cpp_hw)/utility.cpp -std=c++14
clean:
	-rm $(pyb)/*
