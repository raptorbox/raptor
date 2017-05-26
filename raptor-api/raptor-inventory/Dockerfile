FROM anapsix/alpine-java

MAINTAINER Luca Capra <lcapra@fbk.eu>
VOLUME ["/etc/raptor"]
EXPOSE 8060

COPY target/raptor-inventory-1.0.jar /raptor.jar

CMD ["java","-jar","/raptor.jar"]
