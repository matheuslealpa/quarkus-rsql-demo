package demo;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    public String city;

    public String country;

}
