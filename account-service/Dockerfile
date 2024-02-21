FROM container-registry.oracle.com/java/openjdk:21.0.2
WORKDIR /helidon

COPY target/account-service.jar ./
COPY target/libs ./libs

#CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "account-service.jar"]
CMD ["java", "-jar", "account-service.jar"]