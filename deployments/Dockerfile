FROM eclipse-temurin:17-jre-alpine

COPY target/*.jar app.jar

COPY deployments/certs/java/keycloak.crt .
COPY deployments/certs/java/keystore.p12 .

RUN keytool -importcert -v -trustcacerts -alias keycloak -file keycloak.crt -keypass secret -cacerts -noprompt -storepass changeit;

VOLUME /home/essrecette/ess

EXPOSE 8080
EXPOSE 8445

ENTRYPOINT ["java","-jar","/app.jar"]