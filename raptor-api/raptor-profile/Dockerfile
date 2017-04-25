FROM anapsix/alpine-java

MAINTAINER Luca Capra <lcapra@fbk.eu>
VOLUME ["/etc/raptor"]
EXPOSE 8070

COPY target/raptor-profile-1.0.jar /raptor-profile.jar

CMD ["java","-jar","/raptor-profile.jar", "--spring.config.location=file:/etc/raptor/profile-service.yml"]
