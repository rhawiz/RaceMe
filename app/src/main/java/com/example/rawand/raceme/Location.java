package com.example.rawand.raceme;

import java.io.Serializable;

public class Location implements Serializable {//Sets & gets location from display with weather

    private String country;
    private String city;

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

}
