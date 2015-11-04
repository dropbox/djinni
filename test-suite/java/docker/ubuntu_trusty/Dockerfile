FROM ubuntu:trusty

RUN apt-get update
RUN apt-get -y install build-essential clang-3.5 cmake

RUN apt-get -y install software-properties-common

# djinni requires some libstdc++ 4.9 features, e.g. experimental/optional
RUN add-apt-repository ppa:ubuntu-toolchain-r/test && apt-get update
RUN apt-get install -y libstdc++-4.9-dev

# TODO: replace with official openjdk-8-jdk once they post the backport
RUN add-apt-repository ppa:openjdk-r/ppa && apt-get update
RUN apt-get install -y openjdk-8-jdk ant

# Select Java 8
RUN update-java-alternatives -s java-1.8.0-openjdk-amd64
RUN rm /usr/lib/jvm/default-java
RUN ln -s /usr/lib/jvm/java-8-openjdk-amd64 /usr/lib/jvm/default-java

ENV CXX clang++
RUN ln -s /usr/bin/clang++-3.5 /usr/bin/clang++

VOLUME /opt/djinni
CMD /opt/djinni/test-suite/java/docker/build_and_run_tests.sh

