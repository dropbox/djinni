#! /bin/bash
set -eu

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

need_to_build=0
dependencies=("build.sbt" "project/plugins.sbt" "source")
launcher="target/start"

if [ $# -ne 0 ]; then
    echo "Error: No command-line arguments allowed.  Got: $@" > /dev/stderr
    exit 1
fi

# Run the build if necessary.
if [ ! -f "$base_dir/$launcher" ]; then
    # If the launcher file doesn't exist, the build was never even run.
    need_to_build=1
else
    # If the launcher file isn't newer than all the build dependencies, the build needs to be rerun.
    dependencies_full_path=("$loc")
    for dependency in "${dependencies[@]}"; do
        dependencies_full_path+=("$base_dir/$dependency")
    done
    newer=$(find "${dependencies_full_path[@]}" -newer "$base_dir/$launcher" | head | wc -l)
    if [ "$newer" -gt 0 ]; then
        need_to_build=1
    fi
fi

if [ "$need_to_build" -eq 1 ]; then
    echo "Building Djinni..."
    cd "$base_dir"
    ./support/sbt compile start-script
else
    echo "Already up to date: Djinni"
fi
