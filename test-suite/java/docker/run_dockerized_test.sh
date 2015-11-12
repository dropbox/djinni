#! /usr/bin/env bash
set -eux

djinni_root=`pwd`

if [ ! -e $djinni_root/test-suite/java/docker/run_dockerized_test.sh ]; then
  echo "Please run this script from the root djinni directory"
  exit 1
fi

# Which Docker images are we to build & run ?
img_dirs=()
if [ $# -eq 0 ]; then
  for f in ./test-suite/java/docker/*; do
    [[ -d $f ]] && img_dirs+=("$f")
  done
else
  img_dirs+=("$@")
fi

for img_dir in "${img_dirs[@]}"; do
  if [ ! -e $img_dir/Dockerfile ]; then
    continue
  fi

  img_name="djinni_test."`basename $img_dir`
  (
    cd $img_dir && docker build -t $img_name .
    docker run --rm -v $djinni_root:/opt/djinni $img_name
  )
done

