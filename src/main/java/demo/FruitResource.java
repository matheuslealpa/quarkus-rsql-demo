package demo;

import java.util.List;
import java.util.Set;

import io.github.matheuslealpa.rsql.Rsql;
import io.github.matheuslealpa.rsql.RsqlFilter;
import io.github.matheuslealpa.rsql.RsqlQuery;
import io.github.matheuslealpa.rsql.RsqlSort;
import io.quarkus.panache.common.Page;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/fruits")
public class FruitResource {

    /** GET /fruits?filter=color==RED;weight=gt=100&sort=-price&page=0&size=20 */
    @GET
    public List<Fruit> list(@QueryParam("filter") @DefaultValue("") RsqlFilter filter,
            @QueryParam("sort") @DefaultValue("id") RsqlSort sort,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        RsqlQuery query = filter.toQuery(Fruit.class, sort);
        return Fruit.<Fruit> find(query.query(), query.parameters()).page(Page.of(page, size)).list();
    }

    /** GET /fruits/count?filter=... */
    @GET
    @Path("/count")
    public long count(@QueryParam("filter") @DefaultValue("") RsqlFilter filter) {
        RsqlQuery query = filter.toQuery(Fruit.class);
        return Fruit.find(query.query(), query.parameters()).count();
    }

    /** Public API: accepts only some selectors, in the filter and in the sort. */
    @GET
    @Path("/public")
    public List<Fruit> publicList(@QueryParam("filter") String filter, @QueryParam("sort") String sort) {
        RsqlQuery query = Rsql.toQuery(filter, sort, Fruit.class, Set.of("name", "color", "farm.name"));
        return Fruit.find(query.query(), query.parameters()).list();
    }

}
