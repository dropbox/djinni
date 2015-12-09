FROM ubuntu:vivid

RUN apt-get update
RUN apt-get -y install git build-essential clang cmake

RUN apt-get install -y openjdk-8-jdk ant

ENV CXX clang++

# Select Java 8
RUN update-java-alternatives -s java-1.8.0-openjdk-amd64
RUN rm /usr/lib/jvm/default-java
RUN ln -s /usr/lib/jvm/java-8-openjdk-amd64 /usr/lib/jvm/default-java

