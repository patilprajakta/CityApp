package com.example.synerzip.explorecity;

/**
 * Created by synerzip on 16/1/17.
 */

public class FoursquareVenue {

    private String name;
    private String city;
    private String checkins;
    private String category;

    public FoursquareVenue() {
        this.name = "";
        this.city = "";
        this.checkins="";
        this.setCategory("");
    }
    public
    String getCheckins() {
        return checkins;
    }

    public
    void setCheckins(String checkins) {
        this.checkins = checkins;
    }
    public String getCity() {
        if (city.length() > 0) {
            return city;
        }
        return city;
    }

    public void setCity(String city) {
        if (city != null) {
            this.city = city.replaceAll("\\(", "").replaceAll("\\)", "");
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}