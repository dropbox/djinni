#! /usr/bin/env bash
set -eu
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
djinni_dir="$base_dir/../src"

temp_out="$base_dir/djinni-output-temp"

djinnifile=example.djinni
in="$base_dir/$djinnifile"

cpp_out="$base_dir/generated-src/cpp"
jni_out="$base_dir/generated-src/jni"
java_out="$base_dir/generated-src/java/com/dropbox/textsort"
objc_out="$base_dir/generated-src/objc"
py_out="$base_dir/generated-src/python"
cffi_out="$base_dir/generated-src/cffi"
cwrapper_out="$base_dir/generated-src/cwrapper"

java_package="com.dropbox.textsort"

deps_file="$temp_out/$djinnifile.deps"

if [ $# -eq 0 ]; then
    # Normal build.
    true
elif [ $# -eq 1 ]; then
    command="$1"; shift
    if [ "$command" != "clean" ]; then
        echo "Unexpected argument: \"$command\"." 1>&2
        exit 1
    fi
    for dir in "$temp_out" "$cpp_out" "$jni_out" "$java_out" "$objc_out" "$py_out" "$cffi_out" "$cwrapper_out"; do
        if [ -e "$dir" ]; then
            echo "Deleting \"$dir\"..."
            rm -r "$dir"
        fi
    done
    exit
fi

# Build djinni
"$base_dir/../src/build"

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

    "$base_dir/../src/run-assume-built" \
        --java-out "$temp_out/java" \
        --java-package $java_package \
        --java-nullable-annotation "javax.annotation.CheckForNull" \
        --java-nonnull-annotation "javax.annotation.Nonnull" \
        --ident-java-field mFooBar \
        \
        --cpp-out "$temp_out/cpp" \
        --cpp-namespace textsort \
        --ident-cpp-enum-type foo_bar \
        --cpp-optional-template std::experimental::optional \
        --cpp-optional-header "<experimental/optional>" \
        \
        --jni-out "$temp_out/jni" \
        --ident-jni-class NativeFooBar \
        --ident-jni-file NativeFooBar \
        \
        --objc-out "$temp_out/objc" \
        --objcpp-out "$temp_out/objc" \
        --objc-type-prefix TXS \
        \
        --py-out "$temp_out/python" \
        --pycffi-package-name PyCFFIlib \
        --pycffi-dynamic-lib-list textsort_py \
        --pycffi-out "$temp_out/cffi" \
        --c-wrapper-out "$temp_out/cwrapper" \
        \
        --idl "$in" \
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
mirror "cpp" "$temp_out/cpp" "$cpp_out"
mirror "java" "$temp_out/java" "$java_out"
mirror "jni" "$temp_out/jni" "$jni_out"
mirror "objc" "$temp_out/objc" "$objc_out"
mirror "py" "$temp_out/python" "$py_out"
mirror "cffi" "$temp_out/cffi" "$cffi_out"
mirror "cwrapper" "$temp_out/cwrapper" "$cwrapper_out"

# Take new dependencies file only after all build steps have completed.
if [ -f "$deps_file.tmp" ] && [ "$deps_file.tmp" -nt "$deps_file" ]; then
    mv "$deps_file.tmp" "$deps_file"
fi

echo "djinni completed."
