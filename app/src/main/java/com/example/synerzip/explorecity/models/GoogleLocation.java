package com.example.synerzip.explorecity.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Prajakta Patil on 10/2/17.
 * Copyright © 2017 Synerzip. All rights reserved
 */
@Getter
@Setter
public class GoogleLocation {

    @SerializedName("lat")
    @Expose
    private Double lat;

    @SerializedName("lng")
    @Expose
    private Double lng;
}
