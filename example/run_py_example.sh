#! /usr/bin/env bash
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
base_dir=$(cd "`dirname "$loc"`" && pwd)

python_cmd=$1

export PYTHONPATH="$base_dir/../support-lib/py:$base_dir/../build_py/cffi:$base_dir/generated-src/python:$PYTHONPATH"
export DYLD_LIBRARY_PATH=$base_dir/../build_py/cffi:$DYLD_LIBRARY_PATH
(cd "$base_dir/../build_py/cffi" \
        && "$python_cmd" ../../example/generated-src/cffi/pycffi_lib_build.py ../../support-lib/cwrapper/wrapper_marshal.h $(ls ../../example/generated-src/cwrapper/*.h) \
        && echo && "$python_cmd" ../../example/python/textsort.py)
