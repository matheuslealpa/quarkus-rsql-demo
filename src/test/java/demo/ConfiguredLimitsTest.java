package demo;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

/** The limits are runtime properties, so each application can tighten them. */
@QuarkusTest
@TestProfile(ConfiguredLimitsTest.TightLimits.class)
class ConfiguredLimitsTest {

    public static class TightLimits implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.rsql.max-filter-length", "40",
                    "quarkus.rsql.max-comparisons", "2",
                    "quarkus.rsql.max-in-values", "3",
                    "quarkus.rsql.max-depth", "2");
        }
    }

    @Test
    void tighterLimits() {
        LimitsTest.filter("color==RED;weight=gt=10").statusCode(200);
        LimitsTest.filter("color==RED;weight=gt=10;price<3").statusCode(400);
        LimitsTest.filter("color=in=(RED,GREEN,YELLOW)").statusCode(200);
        LimitsTest.filter("color=in=(RED,GREEN,YELLOW,ORANGE)").statusCode(400);
        LimitsTest.filter("(color=in=(RED))").statusCode(200);
        LimitsTest.filter("((color=in=(RED)))").statusCode(400);
        LimitsTest.filter("name==" + "a".repeat(35)).statusCode(400);
    }

}
