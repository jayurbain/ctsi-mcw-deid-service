FROM tomcat:8.5-jre8
COPY ./target/ctsi-mcw-deid-service.war /usr/local/tomcat/webapps/ctsi-mcw-deid-service.war
COPY ./setup.sh /
RUN bash /setup.sh
CMD ["catalina.sh", "run"]
