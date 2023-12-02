# Gatekeeper Test

Gatekeeper Test is a straightforward application designed to regulate resource access based on user access levels. Users can retrieve or access resources only when their session access level matches the required access level for the requested content.

For instance, when attempting to access content behind the URI "/Level1/**," the request undergoes validation. If the attached session lacks the necessary access level, the request is blocked.
Current Tech Stack

    Spring Boot
    Apache Freemarker
    Spring Security

### To-Do

    Enhance and complete unit tests in ControllerIntegrationTests.java.
    Enhance and complete unit tests in UserInteractionTests.java.
    Address all existing "todo" items in the code.
    Add unit tests where appropriate.
    Make necessary code improvements.

NB Required:

    Document all changes made, providing motivation for each modification.

### Getting Started

Run the following commands in your console:

bash

mvn clean install -DskipTests
mvn spring-boot:run

Open your browser at "http://localhost:8080/home."
Access Levels Supported

    0: No specific access
    1: Numeric password required
    2: Alphanumeric password required

### Access Endpoint

POST "http://localhost:8080/authenticate"

Required Headers:

    Authorization
    XSRF

Required Cookies:

    CSRF Cookie (obtained from a GET request to /home)

Header Input Format:

    Authorization: Basic {requested_level}#{username}:{password} (base64 encoded)
    XSRF: alphanumeric (CSRF cookie value)

Authorization Header Example:

    Encoded request: "Authorization: Basic MSNib2I6MTIzNDE="
    Decoded: "Authorization: Basic 1#bob:12341"

Successful Authentication Response:

json
```
{"authenticated":true}
```
Error Authentication Response:

    Bad Credentials: 403
    Bad CSRF: 400

On successful authentication responses:

    Session cookie
    New CSRF cookie

Resources Endpoint

GET "http://localhost:8080/resources"

Response:

json
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
This provides a list of all static resources hosted by the server.

#### Resource Access Violations

If no appropriate session is received on a resource request, an access (403) violation is returned.

json
```
{"requiredAccess":"Level1","message":"invalid accessLevel level"}
```
### Basic CSRF Protection

    A GET request to "/home" screen will receive a new CSRF token when no CSRF token is present.
    Any successful authentication request will receive a new CSRF token.
    Any POST request requires a CSRF token to be present.

Provide git patch files containing your changes. Ensure large formatting changes are excluded from the patch files.
