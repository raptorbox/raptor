# FROM dockerfile/oracle-java8

FROM anapsix/alpine-java

MAINTAINER Luca Capra <lcapra@fbk.eu>
VOLUME ["/etc/raptor"]
EXPOSE 8090

COPY target/raptor-auth-1.0.jar /raptor.jar

CMD ["java","-jar","/raptor.jar"]
