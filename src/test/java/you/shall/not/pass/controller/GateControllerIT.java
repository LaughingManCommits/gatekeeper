package you.shall.not.pass.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GateControllerIT {

    @LocalServerPort
    private int port;

    @BeforeAll
    public static void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("Validate resources are loaded")
    void test1()  {
        throw new RuntimeException("todo create tests for me");
    }

    @Test
    @DisplayName("Integration test to check incorrect login : with Auth Header and incorrect credentials: 403 expected")
    void test2() {
        throw new RuntimeException("todo create tests for me");
    }

    @Test
    @DisplayName("Anonymous user accessing static content: 200 expected")
    void test3()  {
        throw new RuntimeException("todo create tests for me");
    }

    @Test
    @DisplayName("Anonymous user accessing level 1 content : 403 expected")
    void test4()  {
        throw new RuntimeException("todo create tests for me");
    }

    @Test
    @DisplayName("level 1 authenticated user accessing level 1 resources: 200 expected")
    void test5() {
        throw new RuntimeException("todo create tests for me");
    }

    @Test
    @DisplayName("level 1 authenticated user accessing level 2 resources: 403 expected")
    void test6() {
        throw new RuntimeException("todo create tests for me");
    }

    @Test
    @DisplayName("level 2 authenticated user accessing level 1 resources: 200 expected")
    void test7() {
        Response homeResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .get(createURLWithPort("/home"))
                .then()
                .extract().response();

        assertThat(HttpStatus.OK.value()).isEqualTo(homeResponse.getStatusCode());

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
                .post(createURLWithPort("/access"))
                .then()
                .extract().response();

        assertThat(HttpStatus.OK.value()).isEqualTo(authenticationResponse.getStatusCode());

        String level2SessionCookie = authenticationResponse.getCookie("GRANT");

        Response resourceAccess = given()
                .contentType(ContentType.JSON)
                .when()
                .cookie("GRANT", level2SessionCookie)
                .get(createURLWithPort("/Level1/low/access.html"))
                .then()
                .extract().response();

        assertThat(HttpStatus.OK.value()).isEqualTo(resourceAccess.getStatusCode());
        assertThat(resourceAccess.getBody().asString()).contains("<p><a href=\"https://www.youtube.com/watch?v=BhSEXdQHwBE\">try me</a></p>");
    }

    @Test
    @DisplayName("User tries to authenticate with bad csrf token: 400 expected")
    void test8() {
        throw new RuntimeException("todo create tests for me");
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}
