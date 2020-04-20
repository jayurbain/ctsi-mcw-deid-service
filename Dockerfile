FROM maven:3.5.2-jdk-8-alpine AS MAVEN_TOOL_CHAIN
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn clean install
COPY ./target/ctsi-mcw-deid-service.war /usr/local/tomcat/webapps/ctsi-mcw-deid-service.war
FROM tomcat:8.5-jre8
COPY ./setup.sh /
RUN bash /setup.sh
CMD ["catalina.sh", "run"]
