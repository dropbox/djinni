FROM centos:6

# Get Java 8 (64-bit)
RUN yum install -y java-1.8.0-openjdk-devel

# Get other build utils
RUN yum install -y cmake wget tar

# djinni requires llvm with libstdc++ 4.9 features, 
# e.g. experimental/optional, so we need a modern
# compiler.  Let's get gcc 4.9 from Scientific Linux Cern
RUN yum install -y wget
WORKDIR /etc/yum.repos.d
RUN wget http://linuxsoft.cern.ch/cern/scl/slc6-scl.repo
RUN yum -y --nogpgcheck install devtoolset-3-gcc devtoolset-3-gcc-c++

# Make devtoolset's gcc accessible
ENV PATH /opt/rh/devtoolset-3/root/usr/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# Select Java 8
RUN echo 1 | update-alternatives --config java
RUN echo 1 | update-alternatives --config javac

# Get modern ant
WORKDIR /opt
RUN wget http://archive.apache.org/dist/ant/binaries/apache-ant-1.9.3-bin.tar.gz
RUN tar xvfvz apache-ant-1.9.3-bin.tar.gz -C /opt
ENV ANT_HOME /opt/apache-ant-1.9.3/bin/ant
RUN ln -s /opt/apache-ant-1.9.3/bin/ant /usr/bin/ant
ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.51-1.b16.el6_7.x86_64

VOLUME /opt/djinni
CMD /opt/djinni/test-suite/java/docker/build_and_run_tests.sh

