package demo;

import io.quarkus.test.junit.QuarkusIntegrationTest;

/** Runs {@link LimitsTest} against the packaged application, or the native executable with -Dnative. */
@QuarkusIntegrationTest
class LimitsIT extends LimitsTest {
}
