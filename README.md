## Ticket Service 

Spring Boot REST API Service to query, hold and reserve tickets.

Send the ticket hold and reservation information to pb-ticket-db-service (Not Implemented yet).

## Prequisite

Java 8

SQL Express 2017 with SQL Server authentication enabled

Eclipse

Maven

## Setup

1. Create a SQL Server login (like login name = ticketservice, password = Ticketservice123)
2. Provide read/write permission to SQL Server user
3. Run pb-ticket-db-service to setup the database.

## Build

Got to the pb-ticket-service folder and run the following command

mvn package


## Start the service

If SQL Server is on local local machine and TCP/IP enabled and Database name is TicketService run the following command

### java -Ddb.user.name={USER_NAME} -Ddb.user.password={PASSWORD} -jar pb-ticket-service-0.0.1-SNAPSHOT.jar

Otherwise run the following command 

### java -Ddb.server.name=127.0.0.1\\SQLEXPRESS -Ddb.database.name=TicketService -Ddb.user.name={USER_NAME} -Ddb.user.password={PASSWORD} -jar pb-ticket-service-0.0.1-SNAPSHOT.jar

Alternately application.properties file can be updated with all these values. Then just run the following command.
 
### java -jar pb-ticket-service-0.0.1-SNAPSHOT.jar

## Swagger UI

Application is Swagger2 enabled. To start Swagger UI go to the following URL.

http://localhost:8080/swagger-ui.html

Click ticket-service-controller (: Ticket Service Controller) to test the methods.



