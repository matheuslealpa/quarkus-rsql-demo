package demo;

import java.util.List;

import io.github.matheuslealpa.rsql.RsqlFilter;
import io.github.matheuslealpa.rsql.RsqlQuery;
import io.github.matheuslealpa.rsql.RsqlSort;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/farms")
public class FarmResource {

    /** GET /farms?filter=fruits.name==Apple;address.city==Lisboa */
    @GET
    public List<Farm> list(@QueryParam("filter") @DefaultValue("") RsqlFilter filter,
            @QueryParam("sort") @DefaultValue("id") RsqlSort sort) {
        RsqlQuery query = filter.toQuery(Farm.class, sort);
        return Farm.find(query.query(), query.parameters()).list();
    }

}
