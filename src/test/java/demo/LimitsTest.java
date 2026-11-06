package demo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;

/**
 * Default limits: 2048 characters, 64 comparisons, 256 values in an =in= and a depth of 32.
 * Each limit is tested at its exact value, which is accepted, and one above, which is rejected.
 */
@QuarkusTest
class LimitsTest {

    static ValidatableResponse filter(String filter) {
        return given().queryParam("filter", filter).when().get("/fruits").then();
    }

    static String comparisons(int n) {
        return IntStream.range(0, n).mapToObj(i -> "weight=gt=" + i).collect(Collectors.joining(";"));
    }

    static String inValues(int n) {
        return IntStream.range(0, n).mapToObj(i -> "v" + i).collect(Collectors.joining(",", "name=in=(", ")"));
    }

    static String nested(int depth) {
        return "(".repeat(depth) + "name==Apple" + ")".repeat(depth);
    }

    @Test
    void filterLength() {
        String prefix = "name==";
        filter(prefix + "a".repeat(2048 - prefix.length())).statusCode(200);
        filter(prefix + "a".repeat(2049 - prefix.length())).statusCode(400).body(containsString("2048"));
    }

    @Test
    void sortLength() {
        String sort = "name," + "id,".repeat(700) + "id";
        given().queryParam("sort", sort).when().get("/fruits").then().statusCode(400);
    }

    @Test
    void numberOfComparisons() {
        filter(comparisons(64)).statusCode(200);
        filter(comparisons(65)).statusCode(400).body(containsString("64"));
    }

    @Test
    void inValues() {
        filter(inValues(256)).statusCode(200);
        filter(inValues(257)).statusCode(400).body(containsString("256"));
        filter(inValues(257).replace("=in=", "=out=")).statusCode(400);
    }

    @Test
    void depth() {
        filter(nested(32)).statusCode(200);
        filter(nested(33)).statusCode(400).body(containsString("32"));
        // the parentheses of an =in= count too
        filter("(".repeat(31) + "color=in=(RED)" + ")".repeat(31)).statusCode(200);
        filter("(".repeat(32) + "color=in=(RED)" + ")".repeat(32)).statusCode(400);
    }

    @Test
    void costlyFilterIsRejectedBeforeReachingTheDatabase() {
        // 64 "or" through a collection are 64 exists subqueries, still accepted
        String accepted = IntStream.range(0, 64).mapToObj(i -> "tags==t" + i).collect(Collectors.joining(","));
        filter(accepted).statusCode(200);
        filter(accepted + ",tags==extra").statusCode(400);
    }

}
