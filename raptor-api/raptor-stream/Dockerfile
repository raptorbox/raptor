FROM anapsix/alpine-java

MAINTAINER Luca Capra <lcapra@fbk.eu>
VOLUME ["/etc/raptor"]
EXPOSE 8080

COPY target/raptor-stream-1.0.jar /raptor-stream.jar

CMD ["java","-jar","/raptor-stream.jar", "--spring.config.location=file:/etc/raptor/stream-service.yml"]
