package com.example.synerzip.explorecity.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by synerzip on 13/2/17.
 */
@Getter
@Setter
public class FoursquareVenue {
    String id;
    String name;
    double rating;
    FoursquareLocation location;
}