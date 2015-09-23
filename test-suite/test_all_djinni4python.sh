#!/bin/bash
# set -x
set -e

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
export base_dir=$(cd "`dirname "$loc"`" && pwd)

python_cmd="$1"
support_lib="$base_dir/../support-lib"

in="$base_dir/djinni/py_all.djinni"

export python_out="$base_dir/generated-src/python"
export cffi_out="$base_dir/generated-src/cffi"
export py_hw="$base_dir/handwritten-src/python"
export wrapper_marshal="../../support-lib/cwrapper/wrapper_marshal.h"
export limits_helper="$base_dir/handwritten-src/cwrapper/limits_helper.h"

export PYTHONPATH="$support_lib/py:$base_dir/pybuild:$base_dir/generated-src/python:$PYTHONPATH"
# echo $PYTHONPATH

export lib="libmylib.dylib"
mkdir -p "$base_dir/pybuild"
export pyb=$base_dir/pybuild

# Clean command
if [ "$python_cmd" == "clean" ]; then
    ./run_py_djinni.sh clean
    (cd $pyb && make -f ../PyCFFI.mk clean)
    exit
fi

./run_py_djinni.sh

# Generate the dynamic library
(export fname=foo_interface && cd $pyb && make -f ../PyCFFI.mk $lib)

# Build and test CFFI module
"$python_cmd" --version
(cd $pyb \
        && cffi_dep=($(ls ../generated-src/cwrapper/*.h)) \
        && cffi_dep+=($limits_helper) \
        && "$python_cmd" $cffi_out/pycffi_lib_build.py $wrapper_marshal ${cffi_dep[*]} \
        && "$python_cmd" -m pytest -s  $py_hw)
