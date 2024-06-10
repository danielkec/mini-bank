FROM container-registry.oracle.com/java/openjdk:22
WORKDIR /helidon

COPY target/libs ./libs
COPY target/account-service.jar ./

#CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "account-service.jar"]
CMD ["java", "-jar", "account-service.jar"]