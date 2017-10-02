FROM debian

# auto validate license
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections

# update repos
RUN echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list
RUN echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list

RUN apt-get update
RUN apt-get install -y gnupg

RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886

# install java
RUN apt-get install oracle-java8-installer -y --allow-unauthenticated
RUN apt-get install -y libaio-dev

RUN apt-get clean

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

COPY target/raptor-broker-1.0-exec.jar /raptor.jar
CMD ["java","-jar","/raptor.jar"]
