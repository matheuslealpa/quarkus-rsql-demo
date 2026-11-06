package demo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class RsqlDemoTest {

    private static io.restassured.response.ValidatableResponse fruits(String filter) {
        return given().queryParam("filter", filter).when().get("/fruits").then().statusCode(200);
    }

    @Test
    void semFiltroRetornaTudo() {
        given().when().get("/fruits").then().statusCode(200).body("$", hasSize(6));
    }

    @Test
    void igualdadeEComparacao() {
        fruits("color==RED;weight=gt=100").body("name", contains("Apple"));
        fruits("weight>=150").body("name", containsInAnyOrder("Apple", "Orange"));
    }

    @Test
    void ouEAgrupamento() {
        fruits("(color==RED,color==YELLOW);price<3").body("name", containsInAnyOrder("Apple", "Banana"));
    }

    @Test
    void curingaLike() {
        fruits("name==*Apple*").body("name", containsInAnyOrder("Apple", "Green Apple"));
        fruits("name==*berry").body("name", contains("Strawberry"));
        // o like diferencia maiúsculas de minúsculas
        fruits("name==*apple*").body("$", hasSize(0));
    }

    @Test
    void inOut() {
        fruits("color=in=(YELLOW,ORANGE)").body("name", containsInAnyOrder("Banana", "Orange"));
        fruits("color=out=(RED,GREEN)").body("name", containsInAnyOrder("Banana", "Orange"));
    }

    @Test
    void nulos() {
        fruits("harvested=null=").body("name", contains("Lime"));
        fruits("farm=null=").body("name", contains("Lime"));
    }

    @Test
    void datasEDecimais() {
        fruits("harvested=ge=2026-10-10;price=lt=4.00").body("name", contains("Orange"));
    }

    @Test
    void caminhoAninhadoEEmbeddable() {
        fruits("farm.name==Sunny").body("name", containsInAnyOrder("Apple", "Strawberry"));
        fruits("farm.address.country==BR").body("name", containsInAnyOrder("Banana", "Orange"));
        // left join: Lime (sem fazenda) também aparece no "ou"
        fruits("farm.name==Sunny,name==Lime").body("name", containsInAnyOrder("Apple", "Strawberry", "Lime"));
    }

    @Test
    void colecoes() {
        fruits("tags==fresh;tags==promo").body("name", contains("Apple"));
        fruits("tags!=promo").body("name", containsInAnyOrder("Green Apple", "Banana", "Lime", "Strawberry"));
        fruits("tags=null=").body("name", containsInAnyOrder("Banana", "Lime"));
        given().queryParam("filter", "fruits.name==Banana").when().get("/farms")
                .then().statusCode(200).body("name", contains("Fazenda Boa Vista"));
    }

    @Test
    void ordenacaoEPaginacao() {
        given().queryParam("sort", "-price").queryParam("size", 3).when().get("/fruits")
                .then().statusCode(200).body("name", contains("Strawberry", "Orange", "Green Apple"));
        given().queryParam("sort", "farm.name,name").queryParam("filter", "farm=notnull=").when().get("/fruits")
                .then().statusCode(200).body("name", contains("Banana", "Orange", "Green Apple", "Apple", "Strawberry"));
    }

    @Test
    void contagem() {
        given().queryParam("filter", "color==GREEN").when().get("/fruits/count")
                .then().statusCode(200).body(equalTo("2"));
    }

    @Test
    void erroViraBadRequest() {
        given().queryParam("filter", "foo==bar").when().get("/fruits")
                .then().statusCode(400).body(containsString("Unknown property"));
        given().queryParam("filter", "weight==pesado").when().get("/fruits").then().statusCode(400);
        given().queryParam("filter", "name==").when().get("/fruits").then().statusCode(400);
    }

    @Test
    void atributoIgnoradoNaoPodeSerFiltrado() {
        given().queryParam("filter", "ownerToken=gt=m").when().get("/farms")
                .then().statusCode(400).body(containsString("Unknown property"));
        given().queryParam("filter", "farm.ownerToken==secret-aaa").when().get("/fruits").then().statusCode(400);
    }

    @Test
    void seletoresPermitidos() {
        given().queryParam("filter", "farm.name==Sunny").queryParam("sort", "name").when().get("/fruits/public")
                .then().statusCode(200).body("name", contains("Apple", "Strawberry"));
        given().queryParam("filter", "price<3").when().get("/fruits/public").then().statusCode(400);
        given().queryParam("sort", "weight").when().get("/fruits/public").then().statusCode(400);
    }

    @Test
    void limites() {
        String muitos = "color=in=(" + "RED,".repeat(300) + "RED)";
        given().queryParam("filter", muitos).when().get("/fruits").then().statusCode(400);
    }

}
