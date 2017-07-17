FROM anapsix/alpine-java

MAINTAINER Luca Capra <lcapra@fbk.eu>
VOLUME ["/etc/raptor"]
EXPOSE 1883

COPY target/raptor-broker-1.0.jar /raptor.jar

CMD ["java","-jar","/raptor.jar"]
