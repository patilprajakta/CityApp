package com.example.synerzip.explorecity.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Prajakta Patil on 16/2/17.
 * Copyright © 2017 Synerzip. All rights reserved
 */

@Getter
@Setter
public class GooglePredictions {

    private String description;

    private String id;

    private List<GoogleMatchedSubstrings> matched_substrings;

    private String place_id;

    private String reference;

    private GoogleStructuredFormatting structured_formatting;

    private List<GoogleTerms> terms;

    private List<String> types;
}
