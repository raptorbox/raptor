FROM anapsix/alpine-java
VOLUME ["/etc/raptor"]
COPY target/raptor-auth-1.0.jar /raptor.jar
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x entrypoint.sh
CMD [ "/bin/sh", "/entrypoint.sh" ]
