# FROM dockerfile/oracle-java8

FROM anapsix/alpine-java

MAINTAINER Luca Capra <lcapra@create-net.org>
VOLUME ["/etc/raptor"]
EXPOSE 8080

COPY target/raptor-http-api-1.0-jar-with-dependencies.jar /home/raptor-http-api.jar

CMD ["java","-jar","/home/raptor-http-api.jar"]
