package demo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.matheuslealpa.rsql.RsqlIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity
public class Farm extends PanacheEntity {

    public String name;

    @Embedded
    public Address address;

    public boolean organic;

    /** Can never be filtered: a filter such as ownerToken=gt=m would leak its value. */
    @RsqlIgnore
    @JsonIgnore
    public String ownerToken;

    @OneToMany(mappedBy = "farm")
    @JsonIgnore
    public List<Fruit> fruits;

}
