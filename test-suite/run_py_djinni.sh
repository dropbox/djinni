#! /usr/bin/env bash
set -eu
# set -x
shopt -s nullglob

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

temp_out="$base_dir/djinni-output-temp-py"

djinnifile=py_all.djinni
in="$base_dir/djinni/$djinnifile"

python_out="$base_dir/generated-src/python"
cffi_out="$base_dir/generated-src/cffi"
cwrapper_out="$base_dir/generated-src/cwrapper"
py_cpp_out="$base_dir/generated-src/pycpp" # cpp files used in djinni4python

djinni_dir="$base_dir/../src"

deps_file="$temp_out/$djinnifile.deps"

if [ $# -eq 0 ]; then
    # Normal build.
    true
elif [ $# -eq 1 ]; then
    command="$1"; shift
    if [ "$command" != "clean" ]; then
        echo "Unexpected arguemnt: \"$command\"." 1>&2
        exit 1
    fi
    for dir in "$temp_out" "$cwrapper_out" "$python_out" "$cffi_out" "$py_cpp_out"; do
        if [ -e "$dir" ]; then
            echo "Deleting \"$dir\"..."
            rm -r "$dir"
        fi
    done
    exit
fi

# Build Djinni.
"$djinni_dir/build"

already_generated=0

# $deps_file contains the list of dependencies from a prior run on the same input file.
# There is nothing to do when:
# (1) $deps_file is newer than the Djinni build
# (2) $deps_file is newer than this script
# (3) $deps_file is newer than the input file
# (4) $deps_file is newer than all of the dependencies listed in $deps_file.
if [ "$deps_file" -nt "$djinni_dir/target/start" ] && [ "$deps_file" -nt "$loc" ] && [ "$deps_file" -nt "$in" ]; then
    found_new_file=0
    while read one_dep_file; do
        if [ "$deps_file" -ot "$one_dep_file" ]; then
            found_new_file=1
            break
        fi
    done < "$deps_file"
    if [ $found_new_file -eq 0 ]; then
        echo "Already up to date: generated code for \"$in\"."
        already_generated=1
    fi
fi

if [ $already_generated -eq 0 ]; then
    [ ! -e "$temp_out" ] || rm -r "$temp_out"

    # Note that /pycpp is used for getting cpp files meant for use in python
    "$djinni_dir/run-assume-built" \
        --idl "$in" \
        --py-out "$temp_out/python" \
        --pycffi-package-name PyCFFIlib \
        --pycffi-dynamic-lib-list mylib \
        --pycffi-out "$temp_out/cffi" \
        --c-wrapper-out "$temp_out/cwrapper" \
        --cpp-out "$temp_out/pycpp" \
        --cpp-namespace testsuite \
        --ident-cpp-enum-type foo_bar \
        --cpp-optional-template std::experimental::optional \
        --cpp-optional-header "<experimental/optional>" \
        --list-in-files "$deps_file.tmp"
fi

# Copy changes from "$temp_output" to final dir.

mirror() {
    local prefix="$1" ; shift
    local src="$1" ; shift
    local dest="$1" ; shift
    mkdir -p "$dest"
    rsync -a --delete --checksum --itemize-changes "$src"/ "$dest" | grep -v '^\.' | sed "s/^/[$prefix]/"
}

echo "Copying generated code to final directories..."
mirror "python" "$temp_out/python" "$python_out"
mirror "cffi" "$temp_out/cffi" "$cffi_out"
mirror "cwrapper" "$temp_out/cwrapper" "$cwrapper_out"
mirror "pycpp" "$temp_out/pycpp" "$py_cpp_out"

# Take new dependencies file only after all build steps have completed.
if [ -f "$deps_file.tmp" ] && [ "$deps_file.tmp" -nt "$deps_file" ]; then
    mv "$deps_file.tmp" "$deps_file"
fi

echo "Djinni 4 Python completed."
