FROM container-registry.oracle.com/java/openjdk:22
WORKDIR /helidon

COPY target/libs ./libs
COPY target/transaction-service.jar ./

#CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "transaction-service.jar"]
CMD ["java", "-jar", "transaction-service.jar"]
