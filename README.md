# quarkus-rsql-demo

Projeto demo para testar o [quarkus-rsql](https://github.com/matheuslealpa/quarkus-rsql) com Hibernate ORM Panache e H2 em memória.

## Rodar

```shell
mvn quarkus:dev     # http://localhost:8080
mvn test            # testes de exemplo em src/test/java/demo/RsqlDemoTest.java
```

Para testar a versão local da extensão: `mvn install` no `quarkus-rsql` e depois
`mvn quarkus:dev -Dquarkus-rsql.version=1.2.0-SNAPSHOT`.

## Modelo

- `Fruit`: `name`, `color` (enum), `weight`, `price`, `harvested` (data), `tags` (coleção), `farm` (`@ManyToOne`)
- `Farm`: `name`, `address.city`, `address.country` (embeddable), `organic`, `fruits` (`@OneToMany`), `ownerToken` (`@RsqlIgnore`)

## Exemplos

```shell
curl -G localhost:8080/fruits --data-urlencode 'filter=color==RED;weight=gt=100'
curl -G localhost:8080/fruits --data-urlencode 'filter=(color==RED,color==YELLOW);price<3'
curl -G localhost:8080/fruits --data-urlencode 'filter=name==*Apple*'
curl -G localhost:8080/fruits --data-urlencode 'filter=color=in=(YELLOW,ORANGE)'
curl -G localhost:8080/fruits --data-urlencode 'filter=harvested=ge=2026-10-10'
curl -G localhost:8080/fruits --data-urlencode 'filter=farm.address.country==BR'
curl -G localhost:8080/fruits --data-urlencode 'filter=tags==fresh;tags==promo'
curl -G localhost:8080/fruits --data-urlencode 'sort=-price' --data-urlencode 'size=3'
curl -G localhost:8080/fruits/count --data-urlencode 'filter=color==GREEN'
curl -G localhost:8080/farms --data-urlencode 'filter=fruits.name==Banana'

# erros -> 400 Bad Request
curl -G localhost:8080/fruits --data-urlencode 'filter=foo==bar'
curl -G localhost:8080/farms --data-urlencode 'filter=ownerToken=gt=m'      # @RsqlIgnore
curl -G localhost:8080/fruits/public --data-urlencode 'filter=price<3'      # seletor não permitido
```

`/fruits/public` só aceita `name`, `color` e `farm.name` no filtro e na ordenação.
O SQL gerado aparece no log (`quarkus.hibernate-orm.log.sql=true`).
