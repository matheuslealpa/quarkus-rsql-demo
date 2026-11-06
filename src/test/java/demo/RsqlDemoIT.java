package demo;

import io.quarkus.test.junit.QuarkusIntegrationTest;

/** Runs {@link RsqlDemoTest} against the packaged application, or the native executable with -Dnative. */
@QuarkusIntegrationTest
class RsqlDemoIT extends RsqlDemoTest {
}
