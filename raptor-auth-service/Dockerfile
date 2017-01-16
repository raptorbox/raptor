# FROM dockerfile/oracle-java8

FROM anapsix/alpine-java

MAINTAINER Luca Capra <lcapra@fbk.eu>
VOLUME ["/etc/raptor"]
EXPOSE 8090

COPY target/raptor-auth-service-1.0.jar /raptor-auth-service.jar

CMD ["java","-jar","/raptor-auth-service.jar", "--spring.config.location=file:/etc/raptor/auth-service.yml"]
