FROM anapsix/alpine-java

MAINTAINER Luca Capra <lcapra@fbk.eu>
VOLUME ["/etc/raptor"]
EXPOSE 9090

COPY target/raptor-gateway-1.0.jar /raptor.jar

CMD ["java","-jar","/raptor.jar"]
