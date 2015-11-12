#! /usr/bin/env bash
set -eux

# Build docker container
docker build -t djinni-demo .

djinni_root=`cd ../../; pwd`

# Run!
if [ $# -eq 0 ]; then
  docker run --rm -it -v $djinni_root:/opt/djinni -w /opt/djinni djinni-demo bash
else
  docker run --rm -v $djinni_root:/opt/djinni -w /opt/djinni/example/localhost djinni-demo $@
fi

