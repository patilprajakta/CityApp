package com.example.synerzip.explorecity.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Prajakta Patil on 10/2/17.
 * Copyright © 2017 Synerzip. All rights reserved
 */
@Getter
@Setter
public class GoogleResponse {

    @SerializedName("html_attributions")
    @Expose
    private List<Object> htmlAttributions = new ArrayList<Object>();
    @SerializedName("next_page_token")
    @Expose
    private String nextPageToken;
    @SerializedName("results")
    @Expose
    private List<GoogleResult> results = new ArrayList<GoogleResult>();
    @SerializedName("status")
    @Expose
    private String status;

}
