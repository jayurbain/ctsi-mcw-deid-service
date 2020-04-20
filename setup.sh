#!/bin/bash
  
cat /usr/local/tomcat/conf/server.xml |sed 's/\(port="8080"\)/relaxedQueryChars="[]|{}" \1/g' > /usr/local/tomcat/conf/server2.xml
mv /usr/local/tomcat/conf/server2.xml /usr/local/tomcat/conf/server.xml

catalina.sh run &
