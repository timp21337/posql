Plain Old SQL servlet
Place a properties file in /etc/posql/posql.properties

dbBaseUrl=jdbc:mysql://localhost:3306/                                                                                            
driver=com.mysql.jdbc.Driver                                                                                                      
user=root
password=admin

mvn jetty:run 

access at http://localhost:8080/posql/posql