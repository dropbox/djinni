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
base_dir=$(cd `dirname "$loc"` && pwd)

temp_out="$base_dir/djinni-output-temp"

in="$base_dir/djinni/all.djinni"

cpp_out="$base_dir/generated-src/cpp"
jni_out="$base_dir/generated-src/jni"
objc_out="$base_dir/generated-src/objc"
java_out="$base_dir/generated-src/java/com/dropbox/djinni/test"

java_package="com.dropbox.djinni.test"

gen_stamp="$temp_out/gen.stamp"

if [ $# -eq 0 ]; then
    # Normal build.
    true
elif [ $# -eq 1 ]; then
    command="$1"; shift
    if [ "$command" != "clean" ]; then
        echo "Unexpected arguemnt: \"$command\"." 1>&2
        exit 1
    fi
    for dir in "$temp_out" "$cpp_out" "$jni_out" "$java_out"; do
        if [ -e "$dir" ]; then
            echo "Deleting \"$dir\"..."
            rm -r "$dir"
        fi
    done
    exit
fi

# Build Djinni.
"$base_dir/../src/build"

[ ! -e "$temp_out" ] || rm -r "$temp_out"
$base_dir/../src/run-assume-built \
    --java-out "$temp_out/java" \
    --java-package $java_package \
    --ident-java-field mFooBar \
    \
    --cpp-out "$temp_out/cpp" \
	--cpp-namespace "djinni_generated" \
    --ident-cpp-enum-type foo_bar \
    --cpp-optional-template "std::experimental::optional" \
    --cpp-optional-header "<experimental/optional>" \
    \
    --jni-out "$temp_out/jni" \
	--jni-namespace "djinni_generated::jni" \
    --ident-jni-class NativeFooBar \
    --ident-jni-file NativeFooBar \
    \
    --objc-out "$temp_out/objc" \
    --objcpp-namespace "djinni_generated::objc" \
    --objc-type-prefix DB \
    \
    --idl "$in"

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

date > "$gen_stamp"

echo "Djinni completed."
