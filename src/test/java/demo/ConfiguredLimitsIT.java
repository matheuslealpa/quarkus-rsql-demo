package demo;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;

/** Runs {@link ConfiguredLimitsTest} against the packaged application, or the native executable with -Dnative. */
@QuarkusIntegrationTest
@TestProfile(ConfiguredLimitsTest.TightLimits.class)
class ConfiguredLimitsIT extends ConfiguredLimitsTest {
}
