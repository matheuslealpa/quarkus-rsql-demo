package demo;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.github.matheuslealpa.rsql.Rsql;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Loads bulk.sql (1,000 farms, 200,000 fruits and about 270,000 tags) and times typical filters.
 * The time limit is deliberately loose: it catches gross regressions, such as a join that
 * duplicates rows or an N+1 query, rather than comparing machines.
 */
@QuarkusTest
@TestProfile(PerformanceTest.BulkData.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PerformanceTest {

    public static class BulkData implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("quarkus.hibernate-orm.sql-load-script", "import.sql,bulk.sql");
        }
    }

    private static final long MAX_MILLIS = 500;
    private static final int WARMUP = 3;
    private static final int RUNS = 15;

    @Inject
    EntityManager em;

    @Test
    @Order(1)
    @Transactional
    void bulkDataIsLoaded() {
        assertThat(((Number) em.createNativeQuery("select count(*) from Fruit").getSingleResult()).longValue())
                .isEqualTo(200_006);
    }

    @Test
    @Order(2)
    void filterResponseTimes() {
        Map<String, Map<String, Object>> cases = new LinkedHashMap<>();
        cases.put("equality", Map.of("filter", "color==RED"));
        cases.put("prefix (like 'x%')", Map.of("filter", "name==Mango*"));
        cases.put("contains (like '%x%')", Map.of("filter", "name==*999*"));
        cases.put("range + sort", Map.of("filter", "price=ge=5;price=lt=6;weight>200", "sort", "-price,id"));
        cases.put("in with 50 values", Map.of("filter", inIds(50)));
        cases.put("association", Map.of("filter", "farm.address.city==Recife;farm.organic==true"));
        cases.put("or across a join", Map.of("filter", "farm.name=='Farm 7',name=='Kiwi 15'"));
        cases.put("collection (exists)", Map.of("filter", "tags==promo;tags==bulk"));
        cases.put("negated collection", Map.of("filter", "tags!=fresh;color==GREEN"));
        cases.put("Instant", Map.of("filter", "createdAt=ge=2025-06-01T00:00:00Z;createdAt=lt=2025-06-02T00:00:00Z"));
        cases.put("OffsetDateTime", Map.of("filter", "deliveredAt=gt=2025-08-01T00:00:00-03:00", "sort", "deliveredAt"));
        cases.put("UUID", Map.of("filter", "code==00000000-0000-0000-0000-000000000003"));
        cases.put("sort with nulls", Map.of("sort", "-harvested,id"));
        cases.put("deep page", Map.of("sort", "id", "page", 5_000, "size", 20));
        cases.put("64 comparisons", Map.of("filter", comparisons(64)));

        Map<String, Long> medians = new LinkedHashMap<>();
        cases.forEach((name, params) -> medians.put(name, median(() -> request(params).get("/fruits"))));
        medians.put("count with collection", median(() -> given().queryParam("filter", "tags==promo").get("/fruits/count")));

        StringBuilder report = new StringBuilder("\n=== Performance (median of " + RUNS + " requests, 200,000 fruits) ===\n");
        medians.forEach((name, millis) -> report.append(String.format("%-24s %5d ms%n", name, millis)));
        System.out.println(report);

        assertThat(medians).allSatisfy((name, millis) -> assertThat(millis).as(name).isLessThan(MAX_MILLIS));
    }

    @Test
    @Order(3)
    @Transactional
    void collectionFiltersNeitherDuplicateNorLoseRows() {
        long expected = ((Number) em.createNativeQuery(
                "select count(*) from Fruit f where exists (select 1 from Fruit_tags t where t.Fruit_id = f.id and t.tags = 'promo')"
                        + " and exists (select 1 from Fruit_tags t where t.Fruit_id = f.id and t.tags = 'bulk')")
                .getSingleResult()).longValue();
        long counted = Long.parseLong(given().queryParam("filter", "tags==promo;tags==bulk").get("/fruits/count").asString());
        assertThat(counted).isEqualTo(expected).isPositive();

        // paging through the result of a collection filter returns each id only once
        Set<Long> ids = new HashSet<>();
        int total = 0;
        for (int page = 0; page < 10; page++) {
            List<Long> pageIds = given().queryParam("filter", "tags=in=(promo,fresh,bulk)").queryParam("sort", "id")
                    .queryParam("page", page).queryParam("size", 100)
                    .get("/fruits").then().statusCode(200).extract().jsonPath().getList("id", Long.class);
            ids.addAll(pageIds);
            total += pageIds.size();
        }
        assertThat(ids).hasSize(total).hasSize(1000);
    }

    @Test
    @Order(4)
    void translationCost() {
        String filter = "(color==RED,color==GREEN);price=ge=1.5;farm.address.city=in=(Lisboa,Porto);"
                + "tags==promo;name==*an*;createdAt=gt=2025-01-01T00:00:00Z;deliveredAt=null=";
        for (int i = 0; i < 2_000; i++) {
            Rsql.toQuery(filter, "-price,farm.name", Fruit.class);
        }
        int n = 20_000;
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            Rsql.toQuery(filter, "-price,farm.name", Fruit.class);
        }
        double micros = (System.nanoTime() - start) / 1_000.0 / n;
        System.out.printf("%n=== RSQL to HQL translation: %.1f µs per filter (7 comparisons + sort) ===%n", micros);
        assertThat(micros).isLessThan(500);
    }

    private static RequestSpecification request(Map<String, Object> params) {
        RequestSpecification request = given();
        params.forEach(request::queryParam);
        return request;
    }

    private static long median(Supplier<Response> call) {
        for (int i = 0; i < WARMUP; i++) {
            call.get().then().statusCode(200);
        }
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            call.get();
            times.add((System.nanoTime() - start) / 1_000_000);
        }
        times.sort(null);
        return times.get(times.size() / 2);
    }

    private static String inIds(int n) {
        StringBuilder s = new StringBuilder("id=in=(");
        for (int i = 0; i < n; i++) {
            s.append(i == 0 ? "" : ",").append(1000 + i * 3_000);
        }
        return s.append(')').toString();
    }

    private static String comparisons(int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            s.append(i == 0 ? "" : ",").append("weight==").append(i);
        }
        return s.toString();
    }

}
