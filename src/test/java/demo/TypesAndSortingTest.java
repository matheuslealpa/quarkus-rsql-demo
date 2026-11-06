package demo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;

/** UUID, Instant and OffsetDateTime values, and sorting with nulls, on PostgreSQL. */
@QuarkusTest
class TypesAndSortingTest {

    private static ValidatableResponse get(String filter, String sort) {
        var request = given();
        if (filter != null) {
            request.queryParam("filter", filter);
        }
        if (sort != null) {
            request.queryParam("sort", sort);
        }
        return request.when().get("/fruits").then();
    }

    private static ValidatableResponse fruits(String filter) {
        return get(filter, null).statusCode(200);
    }

    // UUID

    @Test
    void uuidEqualityAndIn() {
        fruits("code==00000000-0000-0000-0000-000000000003").body("name", contains("Banana"));
        fruits("code=in=(00000000-0000-0000-0000-000000000001,00000000-0000-0000-0000-000000000006)")
                .body("name", containsInAnyOrder("Apple", "Strawberry"));
        fruits("code!=00000000-0000-0000-0000-000000000003").body("$", hasSize(5));
    }

    @Test
    void invalidUuid() {
        get("code==not-a-uuid", null).statusCode(400);
    }

    // Instant: always UTC, in ISO format with a Z

    @Test
    void instantRange() {
        fruits("createdAt=ge=2026-10-15T00:00:00Z").body("name", containsInAnyOrder("Orange", "Lime", "Strawberry"));
        fruits("createdAt=lt=2026-10-01T08:00:00Z").body("name", contains("Banana"));
        fruits("createdAt==2026-10-01T08:00:00Z").body("name", contains("Apple"));
    }

    @Test
    void instantWithoutTimeZoneIsInvalid() {
        get("createdAt=ge=2026-10-15", null).statusCode(400);
        get("createdAt=ge=2026-10-15T00:00:00", null).statusCode(400);
    }

    // OffsetDateTime: PostgreSQL stores a timestamptz, so the instant is compared, not the text

    @Test
    void offsetDateTimeComparesTheInstant() {
        // 2026-10-02T12:00-03:00 is 15:00Z, and Apple was delivered at 13:00Z
        fruits("deliveredAt=gt=2026-10-02T12:00:00-03:00").body("name", containsInAnyOrder("Green Apple", "Strawberry"));
        // Apple was delivered at 10:00-03:00, and the same instant matches in any other offset
        fruits("deliveredAt==2026-10-02T10:00:00-03:00").body("name", contains("Apple"));
        fruits("deliveredAt==2026-10-02T13:00:00Z").body("name", contains("Apple"));
        fruits("deliveredAt==2026-10-02T15:00:00+02:00").body("name", contains("Apple"));
    }

    @Test
    void offsetDateTimeNulls() {
        fruits("deliveredAt=null=").body("name", containsInAnyOrder("Orange", "Lime"));
        fruits("deliveredAt=notnull=;deliveredAt=lt=2026-10-01T00:00:00Z").body("name", contains("Banana"));
    }

    @Test
    void offsetDateTimeWithoutOffsetIsInvalid() {
        get("deliveredAt=gt=2026-10-02T12:00:00", null).statusCode(400);
    }

    // Sorting with nulls: quarkus-rsql has no syntax for "nulls first/last", so the
    // database default applies. PostgreSQL sorts nulls last in ascending order and
    // first in descending order, while H2 and MySQL do the opposite.

    @Test
    void nullsLastInAscendingOrder() {
        get(null, "harvested").statusCode(200)
                .body("name", contains("Banana", "Apple", "Green Apple", "Orange", "Strawberry", "Lime"));
    }

    @Test
    void nullsFirstInDescendingOrder() {
        get(null, "-harvested").statusCode(200)
                .body("name", contains("Lime", "Strawberry", "Orange", "Green Apple", "Apple", "Banana"));
    }

    @Test
    void tieBreakBetweenNulls() {
        get(null, "deliveredAt,name").statusCode(200)
                .body("name", contains("Banana", "Apple", "Green Apple", "Strawberry", "Lime", "Orange"));
        get(null, "-deliveredAt,name").statusCode(200)
                .body("name", contains("Lime", "Orange", "Strawberry", "Green Apple", "Apple", "Banana"));
    }

    @Test
    void nullAssociationInSort() {
        // Lime has no farm: the left join keeps it, and its null farm name sorts last
        get(null, "farm.name,name").statusCode(200)
                .body("name", contains("Banana", "Orange", "Green Apple", "Apple", "Strawberry", "Lime"));
    }

    @Test
    void sortByInstantAndUuid() {
        get(null, "-createdAt").statusCode(200)
                .body("name", contains("Strawberry", "Lime", "Orange", "Green Apple", "Apple", "Banana"));
        get(null, "code").statusCode(200)
                .body("name", contains("Apple", "Green Apple", "Banana", "Orange", "Lime", "Strawberry"));
    }

}
