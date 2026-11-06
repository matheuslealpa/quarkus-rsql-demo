# quarkus-rsql-demo

Demo project for [quarkus-rsql](https://github.com/matheuslealpa/quarkus-rsql), with Hibernate ORM Panache and PostgreSQL 17.

## Running

```shell
docker compose up -d   # PostgreSQL 17 on localhost:5432 (demo/demo)
mvn quarkus:dev        # http://localhost:8080, with import.sql + bulk.sql (200,000 fruits)
mvn test               # starts its own PostgreSQL 17 with Dev Services, only needs Docker
```

To test the local version of the extension, run `mvn install` in `quarkus-rsql`, then add
`-Dquarkus-rsql.version=1.2.0-SNAPSHOT` to the commands above.

## Model

- `Fruit`: `name`, `color` (enum), `weight`, `price`, `harvested` (date), `code` (UUID), `createdAt` (Instant), `deliveredAt` (OffsetDateTime), `tags` (collection), `farm` (`@ManyToOne`)
- `Farm`: `name`, `address.city`, `address.country` (embeddable), `organic`, `fruits` (`@OneToMany`), `ownerToken` (`@RsqlIgnore`)

## Tests

| Class | What it covers |
|---|---|
| `RsqlDemoTest` | operators, `;`/`,`, wildcards, nested paths, collections, sorting, paging, 400 errors, `@RsqlIgnore`, accepted selectors |
| `TypesAndSortingTest` | `UUID`, `Instant`, `OffsetDateTime` (compared by instant across offsets) and sorting with nulls |
| `LimitsTest` | the four default limits, at their exact value and one above |
| `ConfiguredLimitsTest` | limits changed through configuration |
| `PerformanceTest` | 200,000 fruits: median response time of 16 queries, counts without duplicates through collections, and the cost of translating RSQL to HQL |

`PerformanceTest` prints a report with the median time of each query, and fails if any takes longer than 500 ms.

Notes on PostgreSQL:

- There's no syntax for `nulls first/last`, so the database default applies: PostgreSQL sorts nulls last in ascending order and first in descending order.
- `OffsetDateTime` is stored as `timestamptz`: comparisons use the instant (`10:00-03:00` matches `13:00Z`), but the original offset isn't kept and is read back as UTC.
- The wildcard (`*`) becomes a `like`, which is case-sensitive.

## Examples

```shell
curl -G localhost:8080/fruits --data-urlencode 'filter=color==RED;weight=gt=100'
curl -G localhost:8080/fruits --data-urlencode 'filter=(color==RED,color==YELLOW);price<3'
curl -G localhost:8080/fruits --data-urlencode 'filter=name==*Apple*'
curl -G localhost:8080/fruits --data-urlencode 'filter=color=in=(YELLOW,ORANGE)'
curl -G localhost:8080/fruits --data-urlencode 'filter=harvested=ge=2026-10-10'
curl -G localhost:8080/fruits --data-urlencode 'filter=farm.address.country==BR'
curl -G localhost:8080/fruits --data-urlencode 'filter=tags==fresh;tags==promo'
curl -G localhost:8080/fruits --data-urlencode 'filter=code==00000000-0000-0000-0000-000000000003'
curl -G localhost:8080/fruits --data-urlencode 'filter=createdAt=ge=2026-10-15T00:00:00Z'
curl -G localhost:8080/fruits --data-urlencode 'filter=deliveredAt==2026-10-02T15:00:00+02:00'
curl -G localhost:8080/fruits --data-urlencode 'sort=-harvested'          # nulls first
curl -G localhost:8080/fruits --data-urlencode 'sort=-price' --data-urlencode 'size=3'
curl -G localhost:8080/fruits/count --data-urlencode 'filter=color==GREEN'
curl -G localhost:8080/farms --data-urlencode 'filter=fruits.name==Banana'

# errors -> 400 Bad Request
curl -G localhost:8080/fruits --data-urlencode 'filter=foo==bar'
curl -G localhost:8080/farms --data-urlencode 'filter=ownerToken=gt=m'      # @RsqlIgnore
curl -G localhost:8080/fruits/public --data-urlencode 'filter=price<3'      # selector not accepted
```

`/fruits/public` accepts only `name`, `color` and `farm.name`, in the filter and in the sort.
In dev mode, the generated SQL is logged (`quarkus.hibernate-orm.log.sql=true`).
