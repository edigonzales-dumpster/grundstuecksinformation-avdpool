package ch.so.agi.grundstuecksinformation;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class MainControllerTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/ping")
          .then()
             .statusCode(200)
             .body(is("grundstuecksinformation-avdpool"));
    }

}