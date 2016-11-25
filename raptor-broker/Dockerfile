# FROM dockerfile/oracle-java8

FROM anapsix/alpine-java

MAINTAINER Luca Capra <lcapra@create-net.org>
VOLUME ["/etc/raptor"]
EXPOSE 1883

COPY target/raptor-broker-1.0-jar-with-dependencies.jar /raptor-broker.jar

CMD ["java","-jar","/raptor-broker.jar"]
