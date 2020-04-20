FROM maven:3.5.2-jdk-8-alpine AS MAVEN_TOOL_CHAIN
COPY pom.xml /usr/local/
WORKDIR /usr/local/
RUN mvn clean install
FROM tomcat:8.5-jre8
COPY ./setup.sh /
RUN bash /setup.sh
CMD ["catalina.sh", "run"]
