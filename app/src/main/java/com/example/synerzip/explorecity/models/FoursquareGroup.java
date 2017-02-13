package com.example.synerzip.explorecity.models;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by synerzip on 13/2/17.
 */
@Getter
@Setter
public class FoursquareGroup {
    List<FoursquareResults> results = new ArrayList<FoursquareResults>();
}
