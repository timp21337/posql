Plain Old SQL servlet

Place a properties file in /etc/posql/posql.properties

tomcat.user=admin
tomcat.password=
tomcat.url=http://localhost:8080/manager

mvn jetty:run 

access at http://localhost:8080/posql/selection

For every database you want to allow access to create a file
/etc/posql/<dbname>.properties

dbBaseUrl=jdbc:mysql://localhost:3306/                                                                                            
driver=com.mysql.jdbc.Driver
user=root                                                                                                     
password=pwd 