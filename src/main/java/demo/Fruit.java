package demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class Fruit extends PanacheEntity {

    public String name;

    @Enumerated(EnumType.STRING)
    public Color color;

    public int weight;

    public BigDecimal price;

    public LocalDate harvested;

    @ElementCollection(fetch = FetchType.EAGER)
    public Set<String> tags;

    @ManyToOne
    public Farm farm;

}
