# Access Gateway Test

Very simple application that regulates resources by access level.

Users can only access resources once their access level is elevated to appropriate level.

For example accessing any content behind the URI "/Level1/**" will be blocked without the appropriate session and access level. 

### Current Tech Stack

* Spring Boot
* Apache Freemarker
* Spring Security

### todo
* Complete and improve all unit tests in GateControllerIT.java
* Complete as many todo's as you can. Search the code for them.
* Add Unit tests where you see fit.
* Improve any code as you wish.

NB Required:
* Document all changes that you have made and provide motivation for the changes.

### Getting Started
run the following commands in your console:
* mvn clean install -DskipTests
* mvn spring-boot:run

Open your browser on "http://localhost:8080/home"

### Access levels Supported

* Low is set to numeric password
* High is alphanumeric password

### Access resource

POST "http://localhost:8080/access"

Required Headers
* Authorization
* XSRF

Required Cookies
* CSRF Cookie required (given when GET request is done to /home)

Header Input Format
* Authorization: Basic {requested_level}#{username}:{password} encoded base64
* XSRF: alphanumeric (CSRF cookie value)

Authorization Header Example

* Encoded request "Authorization: Basic MSNib2I6MTIzNDE="
* Decoded "Authorization: Basic 1#bob:12341"

Successful Authentication Response

```
{"authenticated":true}
```

Error Authentication Response
Bad Credentials:
```
403
```
Bad CSRF:
```
400
```


on successful authentication responses

* session cookie
* new CSRF cookie

### Hosted static-resources

GET "http://localhost:8080/resources"

Response

```
{ 
   "resources":[ 
      "/Level1/low/access.txt",
      "/Level1/low_access.txt",
      "/Level2/high_access.txt",
      "/Level2/what/am/I/access.txt",
      "/css/main.css",
      "/js/main.js"
   ]
}
```

will give a list of all static resources hosted by the server

#### Usage

* Resources can be accessed directly "http://localhost:8080/level1/low_access.txt"
* Resources require session and appropriate access level to be requested

#### Violations

If no appropriate session and csrf token is received on resource request, an access(403) violation is
returned.

```
{"requiredAccess":"Level1","message":"invalid access level"}
```

#### Basic CSRF protection

* A get request to "/home" screen will receive new csrf token, if no csrf token is available.
* All successful authentication request will receive new csrf token.
* All POST requests require the CSRF token present

