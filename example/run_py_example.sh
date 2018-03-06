#! /usr/bin/env bash
set -eux

# Locate the script file.  Cross symlinks if necessary.
loc="$0"
while [ -h "$loc" ]; do
    ls=`ls -ld "$loc"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        loc="$link"  # Absolute link
    else
        loc="`dirname "$loc"`/$link"  # Relative link
    fi
done
base_dir=$(cd "`dirname "$loc"`" && pwd)

python_cmd=$1

export PYTHONPATH=${PYTHONPATH:-""}
export PYTHONPATH="$base_dir/../support-lib/py:$base_dir/../build_py/cffi:$base_dir/generated-src/python:$PYTHONPATH"
# The use of install_name_tool below is a hack to force MacOS to know where to load libtextsort_py.dylib,
# needed because DYLD_LIBRARY_PATH is now ignored by System Integrity Protection.  A real installed app which
# used a dylib like this would need a cleaner solution based on @rpath or somesuch thing.  The file existence
# checks before the commands look for the two different .so names produced for CPython 2 or 3.
#export DYLD_LIBRARY_PATH=$base_dir/../build_py/cffi:$DYLD_LIBRARY_PATH
cffi_dir=$(cd "$base_dir/../build_py/cffi" && pwd)
(cd "$cffi_dir" \
        && "$python_cmd" ../../example/generated-src/cffi/pycffi_lib_build.py ../../support-lib/cwrapper/wrapper_marshal.h $(ls ../../example/generated-src/cwrapper/*.h) \
        && echo && ([ ! -f PyCFFIlib_cffi.so ] || install_name_tool -change /usr/local/lib/libtextsort_py.dylib "$cffi_dir/libtextsort_py.dylib" PyCFFIlib_cffi.so) \
        && echo && ([ ! -f PyCFFIlib_cffi.*.so ] || install_name_tool -change /usr/local/lib/libtextsort_py.dylib "$cffi_dir/libtextsort_py.dylib" PyCFFIlib_cffi.*.so) \
        && echo && "$python_cmd" ../../example/python/textsort.py)
