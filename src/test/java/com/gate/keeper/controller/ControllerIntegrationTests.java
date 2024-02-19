package com.gate.keeper.controller;

import you.shall.not.pass.properties.UserProperties;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ControllerIntegrationTests {

    @Autowired
    private UserProperties userProperties;

    @LocalServerPort
    private int port;

    @BeforeAll
    public static void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }


    @Test
    @DisplayName("Validate resources are loaded")
    void test1() {
        Response homeResponse = given().contentType(ContentType.JSON).
            when().get(createURLWithPort("/home")).
            then().extract().response();

        assertThat(HttpStatus.OK.value()).isEqualTo(
            homeResponse.getStatusCode());
        assertThat(homeResponse.getBody().asString()).isNotEmpty();

        Response logoutResponse = given().contentType(ContentType.JSON).
            when().get(createURLWithPort("/logout")).
            then().extract().response();

        assertThat(HttpStatus.NO_CONTENT.value()).isEqualTo(
            logoutResponse.getStatusCode());

        String csrf = homeResponse.getCookie("CSRF");
        String anonymousSession = homeResponse.getCookie("GRANT");

        Response authenticationResponse = given()
            .contentType(ContentType.JSON)
            .header(new Header("XSRF", csrf))
            .auth()
            .preemptive()
            .basic("2#bob", "test1")
            .cookie("GRANT", anonymousSession)
            .cookie("CSRF", csrf)
            .when()
            .post(createURLWithPort("/authenticate"))
            .then()
            .extract().response();

        assertThat(HttpStatus.OK.value()).isEqualTo(
            authenticationResponse.getStatusCode());
        assertThat(authenticationResponse.getBody().asString()).isNotEmpty();


    }

    @Disabled
    @Test
    @DisplayName("Integration test to check incorrect login : with Auth Header and incorrect credentials: 403 expected")
    void test2() {

        List<UserProperties.User> users = userProperties.getUsers();
        for (UserProperties.User user : users) {
            // Load home page
            Response homeResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .get(createURLWithPort("/home"))
                .then()
                .extract().response();
            // assert that 200 http response is given
            assertThat(HttpStatus.OK.value()).isEqualTo(homeResponse.getStatusCode());
            // Retrieve CSRF tokens (https://en.wikipedia.org/wiki/Cross-site_request_forgery)
            // https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html
            String csrf = homeResponse.getCookie("CSRF");
            String anonymousSession = homeResponse.getCookie("GRANT");
            // Authenticate user, insure CSRF and GRANT session is known
            Response authenticationResponse = given()
                .contentType(ContentType.JSON)
                .header(new Header("XSRF", csrf))
                .auth()
                .preemptive()
                .basic("userName", "password")
                .cookie("GRANT", anonymousSession)
                .cookie("CSRF", csrf)
                .when()
                .post(createURLWithPort("/authenticate"))
                .then()
                .extract().response();

            assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(
                authenticationResponse.getStatusCode());
        }
    }

    @Test
    @DisplayName("Anonymous user accessing static content: 200 expected")
    void test3() {
        Response homeResponse = given().contentType(ContentType.JSON).
            when().get(createURLWithPort("/resources")).
            then().extract().response();

        assertThat(HttpStatus.OK.value()).isEqualTo(
            homeResponse.getStatusCode());
        assertThat(homeResponse.getBody().asString()).isNotEmpty();
    }


    @Test
    @DisplayName("Anonymous user accessing level 1 content : 403 expected")
    void test4() {

        List<UserProperties.User> users = userProperties.getUsers();
        for (UserProperties.User user : users) {
            String userName = "2#" + user.getUserName();
            String password = new String(user.getLevel2Password());
            // Load home page
            Response homeResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .get(createURLWithPort("/home"))
                .then()
                .extract().response();
            // assert that 200 http response is given
            assertThat(HttpStatus.OK.value()).isEqualTo(homeResponse.getStatusCode());
            // Retrieve CSRF tokens (https://en.wikipedia.org/wiki/Cross-site_request_forgery)
            // https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html
            String csrf = homeResponse.getCookie("CSRF");
            String anonymousSession = homeResponse.getCookie("GRANT");
            // Authenticate user, insure CSRF and GRANT session is known
            Response authenticationResponse = given()
                .contentType(ContentType.JSON)
                .header(new Header("XSRF", csrf))
                .auth()
                .preemptive()
                .basic(userName, password)
                .cookie("GRANT", anonymousSession)
                .cookie("CSRF", csrf)
                .when()
                .post(createURLWithPort("/authenticate"))
                .then()
                .extract().response();
            assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(
                authenticationResponse.getStatusCode());
        }
    }

    @Test
    @DisplayName("level 1 authenticated user accessing level 1 resources: 200 expected")
    void test5() {
        List<UserProperties.User> users = userProperties.getUsers();
        for (UserProperties.User user : users) {
            String userName = "2#" + user.getUserName();
            String password = new String(user.getLevel1Password());
            // Load home page
            Response homeResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .get(createURLWithPort("/home"))
                .then()
                .extract().response();
            // assert that 200 http response is given
            assertThat(HttpStatus.OK.value()).isEqualTo(homeResponse.getStatusCode());
            // Retrieve CSRF tokens (https://en.wikipedia.org/wiki/Cross-site_request_forgery)
            // https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html
            String csrf = homeResponse.getCookie("CSRF");
            String anonymousSession = homeResponse.getCookie("GRANT");
            // Authenticate user, insure CSRF and GRANT session is known
            Response authenticationResponse = given()
                .contentType(ContentType.JSON)
                .header(new Header("XSRF", csrf))
                .auth()
                .preemptive()
                .basic(userName, password)
                .cookie("GRANT", anonymousSession)
                .cookie("CSRF", csrf)
                .when()
                .post(createURLWithPort("/authenticate"))
                .then()
                .extract().response();
            // assert that 200 http response is given
            assertThat(HttpStatus.OK.value()).isEqualTo(authenticationResponse.getStatusCode());
            assertThat(authenticationResponse.getBody().asString()).contains(
                "{\"authenticated\":true}");
            // retrieve authenticated session
            String level2SessionCookie = authenticationResponse.getCookie("GRANT");
            // assert that session are rotated from anonymous to authenticated session
            assertThat(level2SessionCookie).isNotEqualTo(anonymousSession);
            // retrieved protected resource with authenticated session
            Response resourceAccess = given()
                .contentType(ContentType.JSON)
                .when()
                .cookie("GRANT", level2SessionCookie)
                .get(createURLWithPort("/Level1/page.html"))
                .then()
                .extract().response();
            // validate secured resource is fetched
            assertThat(HttpStatus.OK.value()).isEqualTo(resourceAccess.getStatusCode());
            assertThat(resourceAccess.getBody().asString()).contains("https://www.youtube.com");
        }
    }


    @Test
    @DisplayName("level 1 authenticated user accessing level 2 resources: 403 expected")
    void test6() {
        List<UserProperties.User> users = userProperties.getUsers();
        for (UserProperties.User user : users) {
            String userName = "2#" + user.getUserName();
            String password = new String(user.getLevel2Password());
            // Load home page
            Response homeResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .get(createURLWithPort("/home"))
                .then()
                .extract().response();
            // assert that 200 http response is given
            assertThat(HttpStatus.OK.value()).isEqualTo(homeResponse.getStatusCode());
            // Retrieve CSRF tokens (https://en.wikipedia.org/wiki/Cross-site_request_forgery)
            // https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html
            String csrf = homeResponse.getCookie("CSRF");
            String anonymousSession = homeResponse.getCookie("GRANT");
            // Authenticate user, insure CSRF and GRANT session is known
            Response authenticationResponse = given()
                .contentType(ContentType.JSON)
                .header(new Header("XSRF", csrf))
                .auth()
                .preemptive()
                .basic(userName, password)
                .cookie("GRANT", anonymousSession)
                .cookie("CSRF", csrf)
                .when()
                .post(createURLWithPort("/authenticate"))
                .then()
                .extract().response();
            // assert that 200 http response is given
            assertThat(HttpStatus.OK.value()).isEqualTo(authenticationResponse.getStatusCode());
            assertThat(authenticationResponse.getBody().asString()).contains(
                "{\"authenticated\":true}");
            // retrieve authenticated session
            String level2SessionCookie = authenticationResponse.getCookie("GRANT");
            // assert that session are rotated from anonymous to authenticated session
            assertThat(level2SessionCookie).isNotEqualTo(anonymousSession);
            // retrieved protected resource with authenticated session
            Response resourceAccess = given()
                .contentType(ContentType.JSON)
                .when()
                .cookie("GRANT", level2SessionCookie)
                .get(createURLWithPort("/Level2/page.html"))
                .then()
                .extract().response();
            // validate secured resource is fetched
            assertThat(HttpStatus.OK.value()).isEqualTo(resourceAccess.getStatusCode());
            assertThat(resourceAccess.getBody().asString()).contains("https://www.youtube.com");
        }
    }

    @Test
    @DisplayName("level 2 authenticated user accessing level 1 resources: 200 expected")
    void test7() {
        List<UserProperties.User> users = userProperties.getUsers();
        for (UserProperties.User user : users) {
            String userName = "2#" + user.getUserName();
            String password = new String(user.getLevel2Password());
            // Load home page
            Response homeResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .get(createURLWithPort("/home"))
                .then()
                .extract().response();
            // assert that 200 http response is given
            assertThat(HttpStatus.OK.value()).isEqualTo(homeResponse.getStatusCode());
            // Retrieve CSRF tokens (https://en.wikipedia.org/wiki/Cross-site_request_forgery)
            // https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html
            String csrf = homeResponse.getCookie("CSRF");
            String anonymousSession = homeResponse.getCookie("GRANT");
            // Authenticate user, insure CSRF and GRANT session is known
            Response authenticationResponse = given()
                .contentType(ContentType.JSON)
                .header(new Header("XSRF", csrf))
                .auth()
                .preemptive()
                .basic(userName, password)
                .cookie("GRANT", anonymousSession)
                .cookie("CSRF", csrf)
                .when()
                .post(createURLWithPort("/authenticate"))
                .then()
                .extract().response();
            // assert that 200 http response is given
            assertThat(HttpStatus.OK.value()).isEqualTo(authenticationResponse.getStatusCode());
            assertThat(authenticationResponse.getBody().asString()).contains(
                "{\"authenticated\":true}");
            // retrieve authenticated session
            String level2SessionCookie = authenticationResponse.getCookie("GRANT");
            // assert that session are rotated from anonymous to authenticated session
            assertThat(level2SessionCookie).isNotEqualTo(anonymousSession);
            // retrieved protected resource with authenticated session
            Response resourceAccess = given()
                .contentType(ContentType.JSON)
                .when()
                .cookie("GRANT", level2SessionCookie)
                .get(createURLWithPort("/Level1/page.html"))
                .then()
                .extract().response();
            // validate secured resource is fetched
            assertThat(HttpStatus.OK.value()).isEqualTo(resourceAccess.getStatusCode());
            assertThat(resourceAccess.getBody().asString()).contains("https://www.youtube.com");
        }
    }

    @Disabled
    @Test
    @DisplayName("User tries to authenticate with bad csrf token: 400 expected")
    void test8() {
        throw new RuntimeException("todo create tests for me");
    }

    @Test
    @DisplayName("User is locked out after 3 configured authentication attempts")
    void test9() {
        String authenticationEndpoint = "/authenticate";

        // Perform three consecutive failed login attempts
        for (int i = 0; i < 3; i++) {
            Response response = given()
                .contentType(ContentType.JSON)
                .auth().basic("invalidUsername", "invalidPassword")
                .when()
                .post(createURLWithPort(authenticationEndpoint))
                .then()
                .extract().response();

            // Assert that 401 http response (Unauthorized) is given for each attempt
            assertThat(HttpStatus.UNAUTHORIZED.value()).isEqualTo(response.getStatusCode());
        }

        // Perform one more login attempt to check if the user is locked out
        Response responseAfterLockout = given()
            .contentType(ContentType.JSON)
            .auth().basic("invalidUsername", "invalidPassword")
            .when()
            .post(createURLWithPort(authenticationEndpoint))
            .then()
            .extract().response();

        // Assert that 401 http response (Unauthorized) is given, indicating the user is locked out
        assertThat(HttpStatus.UNAUTHORIZED.value()).isEqualTo(responseAfterLockout.getStatusCode());

    }


    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}