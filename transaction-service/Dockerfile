FROM container-registry.oracle.com/java/openjdk:21.0.2
WORKDIR /helidon

COPY target/transaction-service.jar ./
COPY target/libs ./libs

#CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "transaction-service.jar"]
CMD ["java", "-jar", "transaction-service.jar"]
