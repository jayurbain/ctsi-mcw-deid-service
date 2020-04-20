
FROM maven:3.5.2-jdk-8-alpine AS MAVEN_TOOL_CHAIN
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn clean install

FROM tomcat:9.0-jre8-alpine
COPY --from=MAVEN_TOOL_CHAIN /tmp/target/wizard*.war $CATALINA_HOME/webapps/wizard.war

COPY ./setup.sh /
RUN bash /setup.sh
CMD ["catalina.sh", "run"]
