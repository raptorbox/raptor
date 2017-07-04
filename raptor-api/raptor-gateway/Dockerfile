FROM anapsix/alpine-java
VOLUME ["/etc/raptor"]
COPY target/raptor-gateway-1.0.jar /raptor.jar
CMD ["java","-jar","/raptor.jar"]
