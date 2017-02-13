package com.example.synerzip.explorecity.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by synerzip on 10/2/17.
 */
@Getter
@Setter
public class GoogleGeometry {

    @SerializedName("location")
    @Expose
    private GoogleLocation location;

    /**
     * @return The location
     */
    public GoogleLocation getLocation() {
        return location;
    }

    /**
     * @param location The location
     */
    public void setLocation(GoogleLocation location) {
        this.location = location;
    }
}