FROM container-registry.oracle.com/java/openjdk:21.0.2
WORKDIR /helidon

COPY target/mini-bank.jar ./
COPY target/libs ./libs

CMD ["java", "-jar", "mini-bank.jar"]