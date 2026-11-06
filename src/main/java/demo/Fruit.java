package demo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(indexes = {
        @Index(columnList = "farm_id"),
        @Index(columnList = "color"),
        @Index(columnList = "price"),
        @Index(columnList = "harvested"),
        @Index(columnList = "code", unique = true),
        @Index(columnList = "createdAt"),
        @Index(columnList = "deliveredAt") })
public class Fruit extends PanacheEntity {

    public String name;

    @Enumerated(EnumType.STRING)
    public Color color;

    public int weight;

    public BigDecimal price;

    public LocalDate harvested;

    /** Public identifier. */
    public UUID code;

    /** When the fruit was registered, always in UTC. */
    public Instant createdAt;

    /** When the fruit was delivered, in the offset of the recipient, or null if it hasn't been delivered yet. */
    public OffsetDateTime deliveredAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "Fruit_tags", joinColumns = @JoinColumn(name = "Fruit_id"),
            indexes = @Index(columnList = "Fruit_id, tags"))
    public Set<String> tags;

    @ManyToOne
    public Farm farm;

}
