FROM anapsix/alpine-java

MAINTAINER Luca Capra <lcapra@fbk.eu>
VOLUME ["/etc/raptor"]
EXPOSE 8060

COPY target/raptor-inventory-1.0.jar /raptor-inventory.jar

CMD ["java","-jar","/raptor-inventory.jar", "--spring.config.location=file:/etc/raptor/inventory-service.yml"]
