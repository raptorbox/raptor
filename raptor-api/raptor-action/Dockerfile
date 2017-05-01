FROM anapsix/alpine-java

MAINTAINER Luca Capra <lcapra@fbk.eu>
VOLUME ["/etc/raptor"]
EXPOSE 8050

COPY target/raptor-action-1.0.jar /raptor-action.jar

CMD ["java","-jar","/raptor-action.jar", "--spring.config.location=file:/etc/raptor/action-service.yml"]
